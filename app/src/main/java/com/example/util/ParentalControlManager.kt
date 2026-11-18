package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.FirestoreChatRepository
import com.example.data.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChildContactEvent(
    val peerName: String,
    val peerHandle: String,
    val time: String
)

data class ChildScreenEvent(
    val screenName: String,
    val action: String,
    val time: String
)

data class ParentalControlState(
    val ageYears: Int? = null,
    val isAgeSet: Boolean = false,
    val isMinorMode: Boolean = false,
    val restrictionsEnabled: Boolean = false,
    val dailyLimitMinutes: Int = 60,
    val usedMinutesToday: Int = 0,
    val childPairingCode: String = "",
    val linkedParentUid: String? = null,
    val supervisedChildCode: String? = null,
    val isScreenTimeLocked: Boolean = false,
    val blockStrangerDmsForMinor: Boolean = true,
    val parentEmail: String? = null,
    val isParentEmailVerified: Boolean = false,
    val isSyncedWithFirebase: Boolean = false,
    val lastFirebaseStatus: String? = null,
    val verificationPendingEmail: String? = null,
    val verificationCodeExpiresAtMs: Long = 0L,
    val verificationFailedAttempts: Int = 0,
    val verificationCooldownUntilMs: Long = 0L,
    val contactedPeers: List<ChildContactEvent> = emptyList(),
    val screenEvents: List<ChildScreenEvent> = emptyList()
)

/**
 * Age-Gated Parental Controls & Real-Time Parent-Child Activity Synchronization.
 * - Syncs parent email verification challenges and active restrictions to Firebase Firestore
 *   (/users/{uid}/parental_controls/config) alongside salted SHA-256 local verification.
 * - Restrictions enforce profanity filter lock, stranger DM protection, and daily usage limits.
 */
object ParentalControlManager {
    private const val PREFS = "general_chat_parental_prefs"
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var parentWatchListener: ListenerRegistration? = null

    private val _state = MutableStateFlow(ParentalControlState())
    val state: StateFlow<ParentalControlState> = _state.asStateFlow()

    private val _observedChildState = MutableStateFlow<ParentalControlState?>(null)
    val observedChildState: StateFlow<ParentalControlState?> = _observedChildState.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val hasAge = prefs.contains("age_years")
        val age = if (hasAge) prefs.getInt("age_years", 18) else null
        val todayKey = currentDayKey()
        val savedDay = prefs.getString("usage_day", todayKey)
        val usedMins = if (savedDay == todayKey) prefs.getInt("used_minutes", 0) else 0
        val limitMins = prefs.getInt("daily_limit_minutes", 60).coerceIn(10, 480)
        val pairCode = prefs.getString("child_pairing_code", null) ?: generatePairCode().also {
            prefs.edit().putString("child_pairing_code", it).apply()
        }
        val supervisedCode = prefs.getString("supervised_child_code", null)
        val isMinor = age != null && age < 18
        val restrictionsEnabled = prefs.getBoolean("restrictions_enabled", false)
        val contacts = parseContactsJson(prefs.getString("contacts_json", null))
        val screens = parseScreensJson(prefs.getString("screens_json", null))

        _state.value = ParentalControlState(
            ageYears = age,
            isAgeSet = hasAge,
            isMinorMode = isMinor || restrictionsEnabled,
            restrictionsEnabled = restrictionsEnabled,
            dailyLimitMinutes = limitMins,
            usedMinutesToday = usedMins,
            childPairingCode = pairCode,
            supervisedChildCode = supervisedCode,
            isScreenTimeLocked = (isMinor || restrictionsEnabled) && usedMins >= limitMins,
            blockStrangerDmsForMinor = prefs.getBoolean("block_strangers", true),
            parentEmail = prefs.getString("parent_email", null),
            isParentEmailVerified = prefs.getBoolean("parent_email_verified", false),
            isSyncedWithFirebase = prefs.getBoolean("firebase_synced", false),
            verificationPendingEmail = prefs.getString("verification_pending_email", null),
            verificationCodeExpiresAtMs = prefs.getLong("verification_expires_at", 0L),
            verificationFailedAttempts = prefs.getInt("verification_failed_attempts", 0),
            verificationCooldownUntilMs = prefs.getLong("verification_cooldown_until", 0L),
            contactedPeers = contacts,
            screenEvents = screens
        )
    }

    fun setUserAge(age: Int, context: Context? = appContext) {
        context?.let { init(it) }
        val cleanAge = age.coerceIn(4, 120)
        val isMinor = cleanAge < 18
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs?.edit()
            ?.putInt("age_years", cleanAge)
            ?.apply()
        val cur = _state.value
        val activeRestricted = isMinor || cur.restrictionsEnabled
        _state.value = cur.copy(
            ageYears = cleanAge,
            isAgeSet = true,
            isMinorMode = activeRestricted,
            isScreenTimeLocked = activeRestricted && cur.usedMinutesToday >= cur.dailyLimitMinutes
        )
        if (activeRestricted) {
            ProfanityFilter.forceEnableForMinor(appContext)
            syncChildSnapshotToCloud()
        }
    }

    fun isAdultVerified(): Boolean {
        val s = _state.value
        if (s.restrictionsEnabled) return false
        val age = s.ageYears ?: return true
        return age >= 18 && !s.isMinorMode
    }

    fun setDailyLimitMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(10, 480)
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putInt("daily_limit_minutes", clamped)?.apply()
        val cur = _state.value
        _state.value = cur.copy(
            dailyLimitMinutes = clamped,
            isScreenTimeLocked = cur.isMinorMode && cur.usedMinutesToday >= clamped
        )
        syncChildSnapshotToCloud()
    }

    fun recordUsageMinute() {
        val cur = _state.value
        if (!cur.isMinorMode) return
        val nextUsed = cur.usedMinutesToday + 1
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("usage_day", currentDayKey())
            ?.putInt("used_minutes", nextUsed)
            ?.apply()
        _state.value = cur.copy(
            usedMinutesToday = nextUsed,
            isScreenTimeLocked = nextUsed >= cur.dailyLimitMinutes
        )
        syncChildSnapshotToCloud()
    }

    fun unlockScreenTimeByParent(extraMinutes: Int = 30) {
        val cur = _state.value
        val nextLimit = (cur.usedMinutesToday + extraMinutes).coerceAtLeast(cur.dailyLimitMinutes + extraMinutes)
        setDailyLimitMinutes(nextLimit)
    }

    fun logContactedPerson(peerName: String, peerHandle: String = "") {
        val cur = _state.value
        val entry = ChildContactEvent(
            peerName = peerName.ifBlank { "Kişi" },
            peerHandle = peerHandle,
            time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
        val updated = (listOf(entry) + cur.contactedPeers).take(40)
        _state.value = cur.copy(contactedPeers = updated)
        persistContacts(updated)
        if (cur.isMinorMode) syncChildSnapshotToCloud()
    }

    fun logNavigationEvent(screenName: String, action: String = "Giriş") {
        val cur = _state.value
        val entry = ChildScreenEvent(
            screenName = screenName,
            action = action,
            time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
        val updated = (listOf(entry) + cur.screenEvents).take(50)
        _state.value = cur.copy(screenEvents = updated)
        persistScreens(updated)
        if (cur.isMinorMode) syncChildSnapshotToCloud()
    }

    fun linkParentToChildCode(code: String) {
        val normalized = code.trim().uppercase(Locale.US)
        if (normalized.isBlank()) return
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putString("supervised_child_code", normalized)?.apply()
        _state.value = _state.value.copy(supervisedChildCode = normalized)

        // If linking to self / local child state during offline or single-device test, reflect live state
        if (normalized == _state.value.childPairingCode) {
            _observedChildState.value = _state.value
        }
        startCloudParentObserver(normalized)
    }

    fun requestParentEmailVerification(
        rawEmail: String,
        context: Context? = appContext
    ): Result<String> {
        context?.let { init(it) }
        val cleanEmail = rawEmail.trim()
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Lütfen geçerli bir ebeveyn e-posta adresi girin."))
        }
        val code = (100000 + SecureRandom().nextInt(900000)).toString()
        val salt = SecurityUtils.generateSalt()
        val codeHash = SecurityUtils.hashPassword(code, salt)
        val expiresAt = System.currentTimeMillis() + 10 * 60 * 1000L

        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("verification_pending_email", cleanEmail)
            ?.putString("verification_code_salt", salt)
            ?.putString("verification_code_hash", codeHash)
            ?.putLong("verification_expires_at", expiresAt)
            ?.putInt("verification_failed_attempts", 0)
            ?.putLong("verification_cooldown_until", 0L)
            ?.apply()

        _state.value = _state.value.copy(
            verificationPendingEmail = cleanEmail,
            verificationCodeExpiresAtMs = expiresAt,
            verificationFailedAttempts = 0,
            verificationCooldownUntilMs = 0L
        )
        syncConfigToFirebase(
            parentEmail = cleanEmail,
            isVerified = _state.value.isParentEmailVerified,
            restrictionsEnabled = _state.value.restrictionsEnabled,
            codeHash = codeHash,
            codeSalt = salt,
            expiresAtMs = expiresAt
        )
        return Result.success(code)
    }

    fun verifyParentEmailCode(
        enteredCode: String,
        context: Context? = appContext
    ): Result<Boolean> {
        context?.let { init(it) }
        val cleanCode = enteredCode.trim()
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?: return Result.failure(IllegalStateException("Ebeveyn doğrulama depolarına erişilemedi."))

        val now = System.currentTimeMillis()
        val cooldownUntil = prefs.getLong("verification_cooldown_until", 0L)
        if (cooldownUntil > now) {
            val waitSec = ((cooldownUntil - now) / 1000L).coerceAtLeast(1L)
            return Result.failure(IllegalStateException("Çok fazla hatalı kod denemesi. Lütfen $waitSec saniye sonra tekrar deneyin."))
        }

        val pendingEmail = prefs.getString("verification_pending_email", null)
        val salt = prefs.getString("verification_code_salt", null)
        val expectedHash = prefs.getString("verification_code_hash", null)
        val expiresAt = prefs.getLong("verification_expires_at", 0L)

        if (pendingEmail.isNullOrBlank() || salt.isNullOrBlank() || expectedHash.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Önce ebeveyn e-posta adresinize doğrulama kodu isteyin."))
        }

        if (now > expiresAt) {
            return Result.failure(IllegalStateException("Doğrulama kodunun süresi doldu (10 dk). Lütfen yeni kod gönderin."))
        }

        if (cleanCode.length != 6 || !SecurityUtils.verifyPassword(cleanCode, salt, expectedHash)) {
            val nextFail = prefs.getInt("verification_failed_attempts", 0) + 1
            val nextCooldown = if (nextFail >= 5) now + 30_000L else 0L
            val storedFail = if (nextFail >= 5) 0 else nextFail
            prefs.edit()
                .putInt("verification_failed_attempts", storedFail)
                .putLong("verification_cooldown_until", nextCooldown)
                .apply()
            _state.value = _state.value.copy(
                verificationFailedAttempts = storedFail,
                verificationCooldownUntilMs = nextCooldown
            )
            return Result.failure(
                IllegalArgumentException(
                    if (nextCooldown > 0L) "5 hatalı deneme nedeniyle kod doğrulama 30 saniye kilitlendi."
                    else "Kod eşleşmedi. Kalan deneme: ${5 - storedFail}"
                )
            )
        }

        prefs.edit()
            .putString("parent_email", pendingEmail)
            .putBoolean("parent_email_verified", true)
            .putBoolean("restrictions_enabled", true)
            .remove("verification_pending_email")
            .remove("verification_code_salt")
            .remove("verification_code_hash")
            .remove("verification_expires_at")
            .putInt("verification_failed_attempts", 0)
            .putLong("verification_cooldown_until", 0L)
            .apply()

        ProfanityFilter.forceEnableForMinor(appContext)

        _state.value = _state.value.copy(
            parentEmail = pendingEmail,
            isParentEmailVerified = true,
            isMinorMode = true,
            restrictionsEnabled = true,
            verificationPendingEmail = null,
            verificationCodeExpiresAtMs = 0L,
            verificationFailedAttempts = 0,
            verificationCooldownUntilMs = 0L
        )
        syncConfigToFirebase(
            parentEmail = pendingEmail,
            isVerified = true,
            restrictionsEnabled = true,
            codeHash = "",
            codeSalt = "",
            expiresAtMs = 0L
        )
        return Result.success(true)
    }

    fun setRestrictionsEnabled(enabled: Boolean, blockStrangers: Boolean = _state.value.blockStrangerDmsForMinor, context: Context? = appContext) {
        context?.let { init(it) }
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs?.edit()
            ?.putBoolean("restrictions_enabled", enabled)
            ?.putBoolean("block_strangers", blockStrangers)
            ?.apply()
        val cur = _state.value
        val activeRestricted = enabled || ((cur.ageYears ?: 18) < 18)
        if (activeRestricted) {
            ProfanityFilter.forceEnableForMinor(appContext)
        }
        _state.value = cur.copy(
            restrictionsEnabled = enabled,
            blockStrangerDmsForMinor = blockStrangers,
            isMinorMode = activeRestricted,
            isScreenTimeLocked = activeRestricted && cur.usedMinutesToday >= cur.dailyLimitMinutes
        )
        val emailToSync = cur.parentEmail ?: cur.verificationPendingEmail
        if (!emailToSync.isNullOrBlank()) {
            syncConfigToFirebase(
                parentEmail = emailToSync,
                isVerified = cur.isParentEmailVerified,
                restrictionsEnabled = enabled,
                codeHash = "",
                codeSalt = "",
                expiresAtMs = 0L
            )
        }
    }

    fun refreshFromFirebase(context: Context? = appContext) {
        context?.let { init(it) }
        val ctx = appContext ?: return
        val hasSignedInUser = try { FirebaseAuth.getInstance().currentUser != null } catch (_: Exception) { false }
        if (!hasSignedInUser) return
        syncScope.launch {
            val res = FirestoreChatRepository.create(ctx).fetchParentalControlConfig()
            res.onSuccess { cloud ->
                if (cloud != null) {
                    val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("parent_email", cloud.parentEmail)
                        .putBoolean("parent_email_verified", cloud.isVerified)
                        .putBoolean("restrictions_enabled", cloud.restrictionsEnabled)
                        .putBoolean("block_strangers", cloud.blockStrangerDms)
                        .putInt("daily_limit_minutes", cloud.dailyLimitMinutes)
                        .putBoolean("firebase_synced", true)
                        .apply()
                    if (cloud.restrictionsEnabled && cloud.forceProfanityFilter) {
                        ProfanityFilter.forceEnableForMinor(ctx)
                    }
                    val cur = _state.value
                    val activeRestricted = cloud.restrictionsEnabled || ((cur.ageYears ?: 18) < 18)
                    _state.value = cur.copy(
                        parentEmail = cloud.parentEmail,
                        isParentEmailVerified = cloud.isVerified,
                        restrictionsEnabled = cloud.restrictionsEnabled,
                        blockStrangerDmsForMinor = cloud.blockStrangerDms,
                        dailyLimitMinutes = cloud.dailyLimitMinutes,
                        isMinorMode = activeRestricted,
                        isSyncedWithFirebase = true,
                        lastFirebaseStatus = "Firebase bulut ebeveyn kaydı eşitlendi ✓"
                    )
                }
            }
        }
    }

    private fun syncConfigToFirebase(
        parentEmail: String,
        isVerified: Boolean,
        restrictionsEnabled: Boolean,
        codeHash: String,
        codeSalt: String,
        expiresAtMs: Long
    ) {
        val ctx = appContext ?: return
        val hasSignedInUser = try { FirebaseAuth.getInstance().currentUser != null } catch (_: Exception) { false }
        if (!hasSignedInUser) {
            _state.value = _state.value.copy(
                isSyncedWithFirebase = false,
                lastFirebaseStatus = "Yerel doğrulama aktif (bulut eşitleme için Google oturumu önerilir)"
            )
            return
        }
        val cur = _state.value
        syncScope.launch {
            val res = FirestoreChatRepository.create(ctx).saveParentalControlConfig(
                parentEmail = parentEmail,
                isVerified = isVerified,
                restrictionsEnabled = restrictionsEnabled,
                blockStrangerDms = cur.blockStrangerDmsForMinor,
                forceProfanityFilter = true,
                dailyLimitMinutes = cur.dailyLimitMinutes,
                codeHash = codeHash,
                codeSalt = codeSalt,
                expiresAtMs = expiresAtMs
            )
            res.onSuccess {
                ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean("firebase_synced", true).apply()
                _state.value = _state.value.copy(
                    isSyncedWithFirebase = true,
                    lastFirebaseStatus = "Firebase'e kaydedildi ✓"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isSyncedWithFirebase = false,
                    lastFirebaseStatus = "Firebase eşitleme uyarısı: ${err.localizedMessage ?: "Bilinmeyen hata"}"
                )
            }
        }
    }

    fun clearParentEmailVerification(context: Context? = appContext) {
        context?.let { init(it) }
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.remove("parent_email")
            ?.putBoolean("parent_email_verified", false)
            ?.putBoolean("restrictions_enabled", false)
            ?.putBoolean("firebase_synced", false)
            ?.remove("verification_pending_email")
            ?.remove("verification_code_salt")
            ?.remove("verification_code_hash")
            ?.remove("verification_expires_at")
            ?.apply()
        val cur = _state.value
        val minorByAge = (cur.ageYears ?: 18) < 18
        _state.value = cur.copy(
            parentEmail = null,
            isParentEmailVerified = false,
            restrictionsEnabled = false,
            isMinorMode = minorByAge,
            isSyncedWithFirebase = false,
            lastFirebaseStatus = null,
            verificationPendingEmail = null,
            verificationCodeExpiresAtMs = 0L
        )
    }

    fun buildVerificationEmailIntent(parentEmail: String, code: String): Intent {
        val subject = Uri.encode("GENeral Chat Ebeveyn Denetimi Doğrulama Kodu")
        val body = Uri.encode(
            "Merhaba,\n\n" +
                "GENeral Chat Ebeveyn Denetimi doğrulama kodunuz: $code\n" +
                "Bu kod 10 dakika boyunca geçerlidir.\n\n" +
                "Çocuk Cihaz Eşleştirme Kodu: ${_state.value.childPairingCode}\n" +
                "Not: GENeral Chat üzerinde bağımsız SMTP sunucusu bulunmadığı için e-posta cihazınızdaki e-posta istemcisi aracılığıyla hazırlanır."
        )
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${Uri.encode(parentEmail)}?subject=$subject&body=$body")
        }
    }

    fun refreshObservedChildFromLocalIfMatching() {
        val supervised = _state.value.supervisedChildCode
        if (!supervised.isNullOrBlank() && supervised == _state.value.childPairingCode) {
            _observedChildState.value = _state.value
        }
    }

    fun updateSupervisedChildLimit(minutes: Int) {
        val supervised = _state.value.supervisedChildCode ?: return
        if (supervised == _state.value.childPairingCode) {
            setDailyLimitMinutes(minutes)
            _observedChildState.value = _state.value
        }
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            getNamedFirestore()?.collection("parent_links")?.document(supervised)
                ?.set(
                    mapOf(
                        "parentUid" to uid,
                        "dailyLimitMinutes" to minutes.coerceIn(10, 480),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
        } catch (_: Exception) {
        }
    }

    private fun startCloudParentObserver(linkCode: String) {
        parentWatchListener?.remove()
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = getNamedFirestore() ?: return
            val docRef = db.collection("parent_links").document(linkCode)
            docRef.set(
                mapOf(
                    "linkCode" to linkCode,
                    "parentUid" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            parentWatchListener = docRef.addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val age = snap.getLong("childAge")?.toInt()
                    val limit = snap.getLong("dailyLimitMinutes")?.toInt() ?: 60
                    val used = snap.getLong("usedMinutesToday")?.toInt() ?: 0
                    val contacts = parseContactsJson(snap.getString("contactsJson"))
                    val screens = parseScreensJson(snap.getString("screensJson"))
                    _observedChildState.value = ParentalControlState(
                        ageYears = age,
                        isMinorMode = (age ?: 12) < 18,
                        dailyLimitMinutes = limit,
                        usedMinutesToday = used,
                        childPairingCode = linkCode,
                        contactedPeers = contacts,
                        screenEvents = screens
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun syncChildSnapshotToCloud() {
        val cur = _state.value
        if (cur.supervisedChildCode == cur.childPairingCode) {
            _observedChildState.value = cur
        }
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = getNamedFirestore() ?: return
            val payload = mapOf(
                "linkCode" to cur.childPairingCode,
                "childUid" to uid,
                "childAge" to (cur.ageYears ?: 13),
                "dailyLimitMinutes" to cur.dailyLimitMinutes,
                "usedMinutesToday" to cur.usedMinutesToday,
                "contactsJson" to serializeContacts(cur.contactedPeers),
                "screensJson" to serializeScreens(cur.screenEvents),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("parent_links").document(cur.childPairingCode)
                .set(payload, SetOptions.merge())
        } catch (_: Exception) {
        }
    }

    private fun getNamedFirestore(): FirebaseFirestore? {
        val ctx = appContext ?: return null
        return try {
            val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
            val dbId = if (resId != 0) ctx.getString(resId) else ""
            if (dbId.isBlank()) return null
            val instance = FirebaseFirestore.getInstance(dbId)
            instance.apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder().build()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun generatePairCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val suffix = (1..5).map { chars.random() }.joinToString("")
        return "EBN-$suffix"
    }

    private fun currentDayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun persistContacts(list: List<ChildContactEvent>) {
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putString("contacts_json", serializeContacts(list))?.apply()
    }

    private fun persistScreens(list: List<ChildScreenEvent>) {
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putString("screens_json", serializeScreens(list))?.apply()
    }

    private fun serializeContacts(list: List<ChildContactEvent>): String {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("n", c.peerName)
                put("h", c.peerHandle)
                put("t", c.time)
            })
        }
        return arr.toString()
    }

    private fun serializeScreens(list: List<ChildScreenEvent>): String {
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(JSONObject().apply {
                put("s", s.screenName)
                put("a", s.action)
                put("t", s.time)
            })
        }
        return arr.toString()
    }

    private fun parseContactsJson(raw: String?): List<ChildContactEvent> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ChildContactEvent(
                    peerName = obj.optString("n"),
                    peerHandle = obj.optString("h"),
                    time = obj.optString("t")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseScreensJson(raw: String?): List<ChildScreenEvent> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ChildScreenEvent(
                    screenName = obj.optString("s"),
                    action = obj.optString("a"),
                    time = obj.optString("t")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
