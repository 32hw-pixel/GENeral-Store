package com.example.data

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

object SecurityUtils {

    private val secureRandom = SecureRandom()

    fun generateSalt(length: Int = 16): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated == expectedHash
    }

    fun generateResetCode(): String {
        val code = Random.nextInt(100000, 999999)
        return code.toString()
    }

    fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(Regex(pattern))
    }

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.WEAK

        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 4 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    private const val LOCK_PREFS = "general_chat_lock_prefs"
    private const val KEY_LOCK_HASH = "lock_hash"
    private const val KEY_LOCK_SALT = "lock_salt"
    private const val KEY_IS_PIN_MODE = "is_pin_mode"
    private const val KEY_LOCK_ON_BACKGROUND = "lock_on_background"
    private const val KEY_LOCK_ON_PANIC = "lock_on_panic"
    private const val KEY_FAILED_ATTEMPTS = "lock_failed_attempts"
    private const val KEY_LOCKOUT_STAGE = "lock_lockout_stage"
    private const val KEY_LOCKOUT_UNTIL_MS = "lockout_until_ms"

    const val MAX_ATTEMPTS_PER_ROUND = 5

    data class LockAttemptStatus(
        val isLockedOut: Boolean,
        val remainingSeconds: Int,
        val failedAttemptsInRound: Int,
        val remainingAttempts: Int,
        val lockoutMinutes: Int,
        val lockoutStage: Int
    )

    fun saveAppLockPasscode(
        context: android.content.Context,
        passcode: String,
        isPinMode: Boolean = passcode.trim().let { it.length == 4 && it.all { ch -> ch.isDigit() } }
    ) {
        val clean = passcode.trim()
        require(clean.length >= 4) { "Uygulama kilidi şifresi en az 4 karakter olmalıdır." }
        if (isPinMode) {
            require(clean.length == 4 && clean.all { it.isDigit() }) {
                "Ekran kilidi PIN kodu tam 4 rakamdan oluşmalıdır."
            }
        }
        val salt = generateSalt()
        val hash = hashPassword(clean, salt)
        context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCK_SALT, salt)
            .putString(KEY_LOCK_HASH, hash)
            .putBoolean(KEY_IS_PIN_MODE, isPinMode)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putInt(KEY_LOCKOUT_STAGE, 0)
            .putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
            .apply()
    }

    fun hasAppLockPasscode(context: android.content.Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return !prefs.getString(KEY_LOCK_HASH, null).isNullOrBlank() &&
            !prefs.getString(KEY_LOCK_SALT, null).isNullOrBlank()
    }

    fun isAppLockPinMode(context: android.content.Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PIN_MODE, true)
    }

    fun isLockOnBackgroundEnabled(context: android.content.Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOCK_ON_BACKGROUND, true)
    }

    fun setLockOnBackgroundEnabled(context: android.content.Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOCK_ON_BACKGROUND, enabled)
            .apply()
    }

    fun isLockOnPanicEnabled(context: android.content.Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOCK_ON_PANIC, true)
    }

    fun setLockOnPanicEnabled(context: android.content.Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOCK_ON_PANIC, enabled)
            .apply()
    }

    /**
     * Lockout escalation:
     * - Stage 1 (first 5 wrong attempts): 1 minute
     * - Stage 2 (next 5 wrong attempts): 1 + 5 = 6 minutes
     * - Stage 3 (next 5 wrong attempts): 6 + 5 = 11 minutes (+5 min each round)
     */
    fun getLockoutDurationMinutesForStage(stage: Int): Int {
        if (stage <= 0) return 0
        return 1 + (stage - 1) * 5
    }

    fun getFailedLockAttempts(context: android.content.Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0).coerceIn(0, MAX_ATTEMPTS_PER_ROUND)
    }

    fun getLockoutStage(context: android.content.Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LOCKOUT_STAGE, 0).coerceAtLeast(0)
    }

    fun getLockoutRemainingSeconds(
        context: android.content.Context,
        nowMs: Long = System.currentTimeMillis()
    ): Int {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        val untilMs = prefs.getLong(KEY_LOCKOUT_UNTIL_MS, 0L)
        if (untilMs <= nowMs) return 0
        return ((untilMs - nowMs + 999L) / 1000L).toInt().coerceAtLeast(1)
    }

    fun recordFailedLockAttempt(
        context: android.content.Context,
        nowMs: Long = System.currentTimeMillis()
    ): LockAttemptStatus {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        val currentRemainingSec = getLockoutRemainingSeconds(context, nowMs)
        val currentStage = prefs.getInt(KEY_LOCKOUT_STAGE, 0).coerceAtLeast(0)
        if (currentRemainingSec > 0) {
            return LockAttemptStatus(
                isLockedOut = true,
                remainingSeconds = currentRemainingSec,
                failedAttemptsInRound = MAX_ATTEMPTS_PER_ROUND,
                remainingAttempts = 0,
                lockoutMinutes = getLockoutDurationMinutesForStage(currentStage),
                lockoutStage = currentStage
            )
        }

        val nextAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0).coerceAtLeast(0) + 1
        return if (nextAttempts >= MAX_ATTEMPTS_PER_ROUND) {
            val nextStage = currentStage + 1
            val durationMin = getLockoutDurationMinutesForStage(nextStage)
            val untilMs = nowMs + durationMin * 60_000L
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putInt(KEY_LOCKOUT_STAGE, nextStage)
                .putLong(KEY_LOCKOUT_UNTIL_MS, untilMs)
                .apply()
            LockAttemptStatus(
                isLockedOut = true,
                remainingSeconds = durationMin * 60,
                failedAttemptsInRound = MAX_ATTEMPTS_PER_ROUND,
                remainingAttempts = 0,
                lockoutMinutes = durationMin,
                lockoutStage = nextStage
            )
        } else {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, nextAttempts)
                .apply()
            LockAttemptStatus(
                isLockedOut = false,
                remainingSeconds = 0,
                failedAttemptsInRound = nextAttempts,
                remainingAttempts = (MAX_ATTEMPTS_PER_ROUND - nextAttempts).coerceAtLeast(0),
                lockoutMinutes = 0,
                lockoutStage = currentStage
            )
        }
    }

    fun resetLockAttemptState(context: android.content.Context) {
        context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putInt(KEY_LOCKOUT_STAGE, 0)
            .putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
            .apply()
    }

    fun verifyAppLockPasscode(context: android.content.Context, passcode: String): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
        val salt = prefs.getString(KEY_LOCK_SALT, null) ?: return false
        val expectedHash = prefs.getString(KEY_LOCK_HASH, null) ?: return false
        return verifyPassword(passcode.trim(), salt, expectedHash)
    }

    @Volatile
    var suppressNextBackgroundLock: Boolean = false

    fun suppressNextBackgroundLockOnce() {
        suppressNextBackgroundLock = true
    }

    fun consumeSuppressNextBackgroundLock(): Boolean {
        val wasSuppressed = suppressNextBackgroundLock
        suppressNextBackgroundLock = false
        return wasSuppressed
    }

    fun clearAppLockPasscode(context: android.content.Context) {
        context.applicationContext.getSharedPreferences(LOCK_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

enum class PasswordStrength(val label: String) {
    WEAK("Zayıf"),
    MEDIUM("Orta"),
    STRONG("Güçlü")
}
