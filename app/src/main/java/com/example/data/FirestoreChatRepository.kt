package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CloudHandleProfile(
    val uid: String,
    val handle: String,
    val displayName: String,
    val avatarEmoji: String
)

data class CloudConversationRoom(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val createdBy: String,
    val participantIds: List<String>,
    val participantNames: Map<String, String>,
    val lastMessage: String,
    val lastSenderId: String?,
    val updatedAtMs: Long
)

data class CloudCallSession(
    val id: String,
    val callerId: String,
    val callerName: String,
    val calleeId: String,
    val calleeName: String,
    val participantIds: List<String>,
    val isVideo: Boolean,
    val status: String, // RINGING, ANSWERED, ENDED, REJECTED
    val offerSdp: String,
    val answerSdp: String?
)

data class CloudIceCandidate(
    val senderId: String,
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val sdp: String
)

data class CloudFileTransferSession(
    val id: String,
    val requesterUid: String,
    val ownerUid: String,
    val participantIds: List<String>,
    val conversationId: String,
    val messageId: String,
    val fileId: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val status: String, // REQUESTED, ANSWERED, COMPLETED, FAILED
    val offerSdp: String,
    val answerSdp: String?
)

data class CloudParentalControlConfig(
    val uid: String,
    val parentEmail: String,
    val isVerified: Boolean,
    val restrictionsEnabled: Boolean,
    val blockStrangerDms: Boolean,
    val forceProfanityFilter: Boolean,
    val dailyLimitMinutes: Int,
    val codeHash: String,
    val codeSalt: String,
    val expiresAtMs: Long,
    val updatedAtMs: Long = System.currentTimeMillis()
)

data class CloudLiveLocation(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val participantIds: List<String>,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double,
    val label: String,
    val isActive: Boolean,
    val expiresAtMs: Long,
    val updatedAtMs: Long
)

data class CloudFeedbackReport(
    val id: String,
    val uid: String,
    val category: String,      // FEEDBACK, SUGGESTION, BUG, PERFORMANCE
    val severity: String,      // LOW, MEDIUM, HIGH, CRITICAL
    val rating: Int,           // 1..5
    val moduleTag: String,
    val title: String,
    val description: String,
    val status: String,        // OPEN, IN_REVIEW, RESOLVED
    val appVersion: String,
    val osVersion: String,
    val clientTimestampMs: Long
)

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun ensureUserProfile(
        preferredHandle: String? = null,
        displayNameOverride: String? = null,
        avatarEmoji: String = "⚡"
    ): Result<CloudHandleProfile> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Önce Google ile giriş yapmalısınız."))
        val uid = user.uid
        val displayName = (displayNameOverride?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "GEN Kullanıcısı").trim().take(80)

        return try {
            val userDocRef = firestore.collection(COLLECTION_USERS).document(uid)
            val existingUserSnap = userDocRef.get().await()

            val targetHandle = if (existingUserSnap.exists()) {
                val requested = preferredHandle?.let { sanitizeHandleOrNull(it) }
                requested ?: existingUserSnap.getString("handle")?.let { sanitizeHandleOrNull(it) }
                ?: generateDefaultHandle(user)
            } else {
                preferredHandle?.let { sanitizeHandleOrNull(it) } ?: generateDefaultHandle(user)
            }

            val handleDocRef = firestore.collection(COLLECTION_HANDLE_INDEX).document(targetHandle)
            val existingHandleSnap = handleDocRef.get().await()
            if (existingHandleSnap.exists()) {
                val ownerUid = existingHandleSnap.getString("uid")
                if (ownerUid != null && ownerUid != uid) {
                    return Result.failure(IllegalStateException("@$targetHandle kullanıcı adı başka bir cihaz tarafından kullanılıyor."))
                }
            }

            if (!existingUserSnap.exists()) {
                val userPayload = mapOf(
                    "uid" to uid,
                    "handle" to targetHandle,
                    "displayName" to displayName,
                    "email" to (user.email ?: "").take(254),
                    "avatarEmoji" to avatarEmoji.take(16),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(userPayload).await()
            } else {
                userDocRef.update(
                    mapOf(
                        "handle" to targetHandle,
                        "displayName" to displayName,
                        "email" to (user.email ?: "").take(254),
                        "avatarEmoji" to avatarEmoji.take(16),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (!existingHandleSnap.exists()) {
                val handlePayload = mapOf(
                    "uid" to uid,
                    "handle" to targetHandle,
                    "displayName" to displayName,
                    "avatarEmoji" to avatarEmoji.take(16),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                handleDocRef.set(handlePayload).await()
            } else {
                handleDocRef.update(
                    mapOf(
                        "displayName" to displayName,
                        "avatarEmoji" to avatarEmoji.take(16),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(
                CloudHandleProfile(
                    uid = uid,
                    handle = targetHandle,
                    displayName = displayName,
                    avatarEmoji = avatarEmoji
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "$COLLECTION_USERS/$uid", auth)
            Result.failure(e)
        }
    }

    suspend fun lookupHandle(rawHandle: String): Result<CloudHandleProfile?> {
        if (auth.currentUser == null) {
            return Result.failure(IllegalStateException("Kişi aramak için önce Google ile giriş yapın."))
        }
        val clean = rawHandle.trim().removePrefix("@").lowercase(Locale.US)
        if (!clean.matches(HANDLE_REGEX)) {
            return Result.failure(
                IllegalArgumentException("Kullanıcı adı en az 3 karakter olmalı ve yalnızca küçük harf, rakam veya alt çizgi (_) içermelidir.")
            )
        }
        return try {
            val doc = firestore.collection(COLLECTION_HANDLE_INDEX).document(clean).get().await()
            if (!doc.exists()) {
                Result.success(null)
            } else {
                Result.success(
                    CloudHandleProfile(
                        uid = doc.getString("uid") ?: return Result.success(null),
                        handle = doc.getString("handle") ?: clean,
                        displayName = doc.getString("displayName") ?: "@$clean",
                        avatarEmoji = doc.getString("avatarEmoji") ?: "👤"
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "$COLLECTION_HANDLE_INDEX/$clean", auth)
            Result.failure(e)
        }
    }

    suspend fun openOrCreateDirectConversation(
        counterpart: CloudHandleProfile,
        myDisplayName: String
    ): Result<CloudConversationRoom> {
        val myUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Önce Google ile giriş yapın."))
        val sortedUids = listOf(myUid, counterpart.uid).distinct().sorted()
        if (sortedUids.isEmpty()) {
            return Result.failure(IllegalArgumentException("Geçersiz kullanıcı kimliği."))
        }
        val convId = if (sortedUids.size == 1) {
            "dm_${sortedUids[0]}_self"
        } else {
            "dm_${sortedUids[0]}_${sortedUids[1]}"
        }

        return try {
            val docRef = firestore.collection(COLLECTION_CONVERSATIONS).document(convId)
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                val room = mapConversationSnapshot(existingSnap, myUid)
                    ?: return Result.failure(IllegalStateException("Sohbet verisi okunamadı."))
                Result.success(room)
            } else {
                val namesMap = linkedMapOf(
                    myUid to myDisplayName.ifBlank { "Kullanıcı" }.take(80)
                ).apply {
                    put(counterpart.uid, counterpart.displayName.ifBlank { "@${counterpart.handle}" }.take(80))
                }
                val roomTitle = counterpart.displayName.ifBlank { "@${counterpart.handle}" }.take(120)
                val payload = mapOf(
                    "id" to convId,
                    "title" to roomTitle,
                    "isGroup" to false,
                    "createdBy" to myUid,
                    "participantIds" to sortedUids,
                    "participantNames" to namesMap,
                    "lastMessage" to "Bulut sohbeti açıldı",
                    "lastSenderId" to myUid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(
                    CloudConversationRoom(
                        id = convId,
                        title = roomTitle,
                        isGroup = false,
                        createdBy = myUid,
                        participantIds = sortedUids,
                        participantNames = namesMap,
                        lastMessage = "Bulut sohbeti açıldı",
                        lastSenderId = myUid,
                        updatedAtMs = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "$COLLECTION_CONVERSATIONS/$convId", auth)
            Result.failure(e)
        }
    }

    suspend fun createGroupConversation(
        title: String,
        members: List<CloudHandleProfile>,
        myDisplayName: String
    ): Result<CloudConversationRoom> {
        val myUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Grup oluşturmak için Google ile giriş yapın."))
        val groupId = "grp_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val allParticipants = (listOf(myUid) + members.map { it.uid }).distinct().take(50)
        val namesMap = linkedMapOf(
            myUid to myDisplayName.ifBlank { "Kurucu" }.take(80)
        ).apply {
            members.forEach { member ->
                put(member.uid, member.displayName.ifBlank { "@${member.handle}" }.take(80))
            }
        }
        val cleanTitle = title.trim().ifBlank { "Yeni Grup" }.take(120)

        return try {
            val payload = mapOf(
                "id" to groupId,
                "title" to cleanTitle,
                "isGroup" to true,
                "createdBy" to myUid,
                "participantIds" to allParticipants,
                "participantNames" to namesMap,
                "lastMessage" to "Grup sohbeti oluşturuldu",
                "lastSenderId" to myUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_CONVERSATIONS).document(groupId).set(payload).await()
            Result.success(
                CloudConversationRoom(
                    id = groupId,
                    title = cleanTitle,
                    isGroup = true,
                    createdBy = myUid,
                    participantIds = allParticipants,
                    participantNames = namesMap,
                    lastMessage = "Grup sohbeti oluşturuldu",
                    lastSenderId = myUid,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "$COLLECTION_CONVERSATIONS/$groupId", auth)
            Result.failure(e)
        }
    }

    fun observeConversations(): Flow<List<CloudConversationRoom>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(COLLECTION_CONVERSATIONS)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, COLLECTION_CONVERSATIONS, auth)
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents.orEmpty()
                    .mapNotNull { doc -> mapConversationSnapshot(doc, currentUid) }
                    .sortedByDescending { it.updatedAtMs }
                trySend(rooms)
            }

        awaitClose { registration.remove() }
    }

    suspend fun publishMyE2eePublicKey(context: Context): Result<String> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("E2EE anahtar yayını için oturum gerekli."))
        return try {
            val pubKeyB64 = com.example.util.E2eeCryptoManager.getOrCreateMyPublicKeyBase64(context)
            firestore.collection(COLLECTION_E2EE_KEYS).document(currentUid)
                .set(
                    mapOf(
                        "uid" to currentUid,
                        "publicKeyB64" to pubKeyB64,
                        "algorithm" to "ECDH-P256",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(pubKeyB64)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPeerE2eePublicKey(context: Context, peerUid: String): String? {
        if (peerUid.isBlank()) return null
        com.example.util.E2eeCryptoManager.getCachedPeerPublicKey(context, peerUid)?.let { return it }
        return try {
            val snap = firestore.collection(COLLECTION_E2EE_KEYS).document(peerUid).get().await()
            val pub = snap.getString("publicKeyB64")?.takeIf { it.isNotBlank() }
            if (pub != null) {
                com.example.util.E2eeCryptoManager.cachePeerPublicKey(context, peerUid, pub)
            }
            pub
        } catch (_: Exception) {
            null
        }
    }

    fun observeMessages(conversationId: String, e2eeContext: Context? = null): Flow<List<DirectMessage>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES"
        val registration = firestore.collection(COLLECTION_CONVERSATIONS)
            .document(conversationId)
            .collection(COLLECTION_MESSAGES)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path, auth)
                    close(error)
                    return@addSnapshotListener
                }
                val msgs = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: "Kullanıcı"
                    val rawText = doc.getString("text") ?: ""
                    val participantIds = (doc.get("participantIds") as? List<*>)
                        ?.mapNotNull { it as? String }
                        ?: emptyList()
                    val peerUid = participantIds.firstOrNull { it != currentUid } ?: ""
                    val wasE2ee = com.example.util.E2eeCryptoManager.isE2eeCiphertext(rawText)
                    val decryptedText = if (wasE2ee && e2eeContext != null && peerUid.isNotBlank()) {
                        val peerPub = com.example.util.E2eeCryptoManager.getCachedPeerPublicKey(e2eeContext, peerUid)
                        if (peerPub != null) {
                            com.example.util.E2eeCryptoManager.decrypt(e2eeContext, conversationId, rawText, peerPub)
                                ?: "🔒 Şifreli mesaj çözülemedi (farklı cihaz anahtarı)"
                        } else {
                            "🔒 Şifreli mesaj (E2EE anahtar senkronizasyonu bekleniyor...)"
                        }
                    } else {
                        rawText
                    }
                    val clientTs = doc.getTimestamp("createdAt")?.toDate()?.time
                        ?: doc.getLong("clientTimestampMs")
                        ?: System.currentTimeMillis()
                    val rawTime = doc.getString("time")
                    val resolvedTime = if (!rawTime.isNullOrBlank() && rawTime != "Şimdi") {
                        rawTime
                    } else {
                        formatTimeShort(clientTs)
                    }
                    val readByList = (doc.get("readBy") as? List<*>)
                        ?.mapNotNull { it as? String }
                        ?: emptyList()
                    DirectMessage(
                        id = doc.getString("id") ?: doc.id,
                        conversationId = conversationId,
                        senderName = if (senderId == currentUid) "Ben" else senderName,
                        isMe = senderId == currentUid,
                        text = decryptedText,
                        time = resolvedTime,
                        timestamp = clientTs,
                        senderId = senderId.takeIf { it.isNotBlank() },
                        isEncrypted = wasE2ee,
                        deliveredAtMs = doc.getLong("deliveredAtMs") ?: doc.getTimestamp("createdAt")?.toDate()?.time,
                        readBy = readByList,
                        readAtMs = doc.getLong("readAtMs"),
                        isVoiceNote = doc.getBoolean("isVoiceNote") ?: false,
                        voiceDurationSeconds = (doc.getLong("voiceDurationSeconds") ?: 0L).toInt(),
                        attachmentType = doc.getString("attachmentType")?.takeIf { it.isNotBlank() },
                        attachmentName = doc.getString("attachmentName")?.takeIf { it.isNotBlank() },
                        attachmentUrl = doc.getString("attachmentUrl")?.takeIf { it.isNotBlank() },
                        attachmentSizeBytes = doc.getLong("attachmentSizeBytes") ?: 0L,
                        attachmentMimeType = doc.getString("attachmentMimeType")?.takeIf { it.isNotBlank() },
                        transferMode = doc.getString("transferMode")?.takeIf { it.isNotBlank() },
                        chunkCount = (doc.getLong("chunkCount") ?: 0L).toInt(),
                        isEdited = doc.getBoolean("isEdited") ?: false,
                        isDeletedForEveryone = doc.getBoolean("isDeletedForEveryone") ?: false
                    )
                }.sortedBy { it.timestamp }
                trySend(msgs)
            }

        awaitClose { registration.remove() }
    }

    suspend fun fetchMessagesOnce(conversationId: String, e2eeContext: Context? = null): List<DirectMessage> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snap = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_MESSAGES)
                .whereArrayContains("participantIds", currentUid)
                .get()
                .await()
            snap.documents.mapNotNull { doc ->
                val senderId = doc.getString("senderId") ?: ""
                val senderName = doc.getString("senderName") ?: "Kullanıcı"
                val rawText = doc.getString("text") ?: ""
                val participantIds = (doc.get("participantIds") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()
                val peerUid = participantIds.firstOrNull { it != currentUid } ?: ""
                val wasE2ee = com.example.util.E2eeCryptoManager.isE2eeCiphertext(rawText)
                val decryptedText = if (wasE2ee && e2eeContext != null && peerUid.isNotBlank()) {
                    val peerPub = fetchPeerE2eePublicKey(e2eeContext, peerUid)
                    if (peerPub != null) {
                        com.example.util.E2eeCryptoManager.decrypt(e2eeContext, conversationId, rawText, peerPub)
                            ?: "🔒 Şifreli mesaj (E2EE)"
                    } else {
                        "🔒 Şifreli mesaj (E2EE)"
                    }
                } else {
                    rawText
                }
                val clientTs = doc.getTimestamp("createdAt")?.toDate()?.time
                    ?: doc.getLong("clientTimestampMs")
                    ?: System.currentTimeMillis()
                val rawTime = doc.getString("time")
                val resolvedTime = if (!rawTime.isNullOrBlank() && rawTime != "Şimdi") rawTime else formatTimeShort(clientTs)
                val readByList = (doc.get("readBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                DirectMessage(
                    id = doc.getString("id") ?: doc.id,
                    conversationId = conversationId,
                    senderName = if (senderId == currentUid) "Ben" else senderName,
                    isMe = senderId == currentUid,
                    text = decryptedText,
                    time = resolvedTime,
                    timestamp = clientTs,
                    senderId = senderId.takeIf { it.isNotBlank() },
                    isEncrypted = wasE2ee,
                    deliveredAtMs = doc.getLong("deliveredAtMs"),
                    readBy = readByList,
                    readAtMs = doc.getLong("readAtMs"),
                    attachmentType = doc.getString("attachmentType"),
                    attachmentName = doc.getString("attachmentName"),
                    attachmentUrl = doc.getString("attachmentUrl"),
                    attachmentSizeBytes = doc.getLong("attachmentSizeBytes") ?: 0L,
                    attachmentMimeType = doc.getString("attachmentMimeType"),
                    transferMode = doc.getString("transferMode"),
                    chunkCount = (doc.getLong("chunkCount") ?: 0L).toInt(),
                    isDeletedForEveryone = doc.getBoolean("isDeletedForEveryone") ?: false
                )
            }.sortedBy { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        message: DirectMessage,
        participantIds: List<String>,
        senderDisplayName: String,
        e2eeContext: Context? = null
    ): Result<Unit> {
        if (conversationId == "conv_self_notes" || conversationId == "conv_gemini_ai") {
            return Result.failure(IllegalArgumentException("Yerel özel sohbetler buluta gönderilemez."))
        }
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Mesaj göndermek için Google ile giriş yapın."))
        val normalizedParticipants = if (participantIds.contains(currentUid)) {
            participantIds.distinct().take(50)
        } else {
            (listOf(currentUid) + participantIds).distinct().take(50)
        }

        val messagePath = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES/${message.id}"
        val convRef = firestore.collection(COLLECTION_CONVERSATIONS).document(conversationId)

        return try {
            val convSnap = convRef.get().await()
            val serverParticipants = (convSnap.get("participantIds") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.takeIf { it.isNotEmpty() }
                ?: normalizedParticipants

            val peerUid = serverParticipants.firstOrNull { it != currentUid }
            val canAttemptE2ee = e2eeContext != null &&
                !conversationId.startsWith("grp_") &&
                !peerUid.isNullOrBlank() &&
                message.attachmentType == null &&
                !message.isDeletedForEveryone &&
                !com.example.util.E2eeCryptoManager.isE2eeCiphertext(message.text)

            val encryptedText = if (canAttemptE2ee) {
                val peerPub = fetchPeerE2eePublicKey(e2eeContext!!, peerUid!!)
                if (!peerPub.isNullOrBlank()) {
                    com.example.util.E2eeCryptoManager.encrypt(
                        context = e2eeContext,
                        conversationId = conversationId,
                        plaintext = message.text,
                        peerPublicKeyB64 = peerPub
                    )
                } else null
            } else null

            val payloadText = (encryptedText ?: message.text).take(4000)
            val summaryLastMessage = if (encryptedText != null) {
                "🔒 Şifreli mesaj (E2EE)"
            } else {
                message.text.take(1000)
            }

            if (!convSnap.exists()) {
                val namesMap = normalizedParticipants.associateWith { uid ->
                    if (uid == currentUid) senderDisplayName.ifBlank { "Kullanıcı" }.take(80) else "Üye"
                }
                val convPayload = mapOf(
                    "id" to conversationId,
                    "title" to conversationId.take(60),
                    "isGroup" to false,
                    "createdBy" to currentUid,
                    "participantIds" to normalizedParticipants,
                    "participantNames" to namesMap,
                    "lastMessage" to summaryLastMessage,
                    "lastSenderId" to currentUid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(convPayload).await()
            }

            val tsMs = if (message.timestamp > 0) message.timestamp else System.currentTimeMillis()
            val formattedTime = message.time.takeIf { it.isNotBlank() && it != "Şimdi" } ?: formatTimeShort(tsMs)

            val msgPayload = mutableMapOf<String, Any>(
                "id" to message.id,
                "conversationId" to conversationId,
                "senderId" to currentUid,
                "senderName" to senderDisplayName.ifBlank { "Kullanıcı" }.take(80),
                "participantIds" to serverParticipants,
                "text" to payloadText,
                "time" to formattedTime.take(32),
                "clientTimestampMs" to tsMs,
                "deliveredAtMs" to tsMs,
                "isVoiceNote" to message.isVoiceNote,
                "voiceDurationSeconds" to message.voiceDurationSeconds.coerceIn(0, 3600),
                "isDeletedForEveryone" to message.isDeletedForEveryone,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            message.attachmentType?.takeIf { it.isNotBlank() }?.let {
                msgPayload["attachmentType"] = it.take(32)
            }
            message.attachmentName?.takeIf { it.isNotBlank() }?.let {
                msgPayload["attachmentName"] = it.take(255)
            }
            message.attachmentUrl?.takeIf { it.isNotBlank() }?.let {
                msgPayload["attachmentUrl"] = it.take(2048)
            }
            if (message.attachmentSizeBytes > 0L) {
                msgPayload["attachmentSizeBytes"] = message.attachmentSizeBytes
            }
            message.attachmentMimeType?.takeIf { it.isNotBlank() }?.let {
                msgPayload["attachmentMimeType"] = it.take(128)
            }
            message.transferMode?.takeIf { it.isNotBlank() }?.let {
                msgPayload["transferMode"] = it.take(32)
            }
            if (message.chunkCount > 0) {
                msgPayload["chunkCount"] = message.chunkCount.coerceIn(0, 3000)
            }

            convRef.collection(COLLECTION_MESSAGES).document(message.id).set(msgPayload).await()

            convRef.update(
                mapOf(
                    "lastMessage" to summaryLastMessage,
                    "lastSenderId" to currentUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, messagePath, auth)
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(
        conversationId: String,
        messageIds: List<String>
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        if (messageIds.isEmpty()) return Result.success(Unit)
        val nowMs = System.currentTimeMillis()
        return try {
            val msgsRef = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_MESSAGES)
            messageIds.distinct().forEach { msgId ->
                msgsRef.document(msgId).update(
                    mapOf(
                        "readBy" to FieldValue.arrayUnion(currentUid),
                        "readAtMs" to nowMs,
                        "deliveredAtMs" to nowMs,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES", auth)
            Result.failure(e)
        }
    }

    suspend fun editMessageText(
        conversationId: String,
        messageId: String,
        newText: String
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES/$messageId"
        return try {
            firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_MESSAGES)
                .document(messageId)
                .update(
                    mapOf(
                        "text" to newText.take(4000),
                        "isEdited" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteMessageForEveryone(conversationId: String, messageId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES/$messageId"
        return try {
            val convRef = firestore.collection(COLLECTION_CONVERSATIONS).document(conversationId)
            convRef.collection(COLLECTION_MESSAGES)
                .document(messageId)
                .update(
                    mapOf(
                        "text" to "🚫 Bu mesaj herkes için silindi.",
                        "isDeletedForEveryone" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            convRef.update(
                mapOf(
                    "lastMessage" to "🚫 Bu mesaj herkes için silindi.",
                    "lastSenderId" to currentUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    // --- Unlimited File Chunked Relay Helpers ---
    suspend fun uploadMessageFileChunk(
        conversationId: String,
        messageId: String,
        participantIds: List<String>,
        chunkIndex: Int,
        totalChunks: Int,
        dataBase64: String
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val chunkPath = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES/$messageId/$COLLECTION_CHUNKS/chunk_$chunkIndex"
        return try {
            val convRef = firestore.collection(COLLECTION_CONVERSATIONS).document(conversationId)
            val convSnap = convRef.get().await()
            val serverParticipants = (convSnap.get("participantIds") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.takeIf { it.isNotEmpty() }
                ?: participantIds.distinct().take(50)

            val payload = mapOf(
                "index" to chunkIndex,
                "totalChunks" to totalChunks,
                "senderId" to currentUid,
                "participantIds" to serverParticipants,
                "dataBase64" to dataBase64,
                "createdAt" to FieldValue.serverTimestamp()
            )
            convRef
                .collection(COLLECTION_MESSAGES)
                .document(messageId)
                .collection(COLLECTION_CHUNKS)
                .document("chunk_$chunkIndex")
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, chunkPath, auth)
            Result.failure(e)
        }
    }

    suspend fun downloadMessageFileChunks(
        conversationId: String,
        messageId: String
    ): Result<List<Pair<Int, String>>> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_MESSAGES/$messageId/$COLLECTION_CHUNKS"
        return try {
            val snap = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_MESSAGES)
                .document(messageId)
                .collection(COLLECTION_CHUNKS)
                .whereArrayContains("participantIds", currentUid)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc ->
                val idx = (doc.getLong("index") ?: return@mapNotNull null).toInt()
                val b64 = doc.getString("dataBase64") ?: return@mapNotNull null
                idx to b64
            }.sortedBy { it.first }
            Result.success(list)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            Result.failure(e)
        }
    }

    // --- Real-Time WebRTC Call Signaling (/calls/{callId}) ---
    suspend fun createOutgoingCall(
        callId: String,
        calleeUid: String,
        calleeName: String,
        callerName: String,
        isVideo: Boolean,
        offerSdp: String
    ): Result<CloudCallSession> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Arama yapmak için Google ile giriş yapın."))
        val participants = listOf(currentUid, calleeUid).distinct()
        if (participants.size != 2) {
            return Result.failure(IllegalArgumentException("Kendinizi arayamazsınız. Farklı bir kullanıcı hesabı seçin."))
        }
        return try {
            val payload = mapOf(
                "id" to callId,
                "callerId" to currentUid,
                "callerName" to callerName.ifBlank { "Kullanıcı" }.take(80),
                "calleeId" to calleeUid,
                "calleeName" to calleeName.ifBlank { "Kullanıcı" }.take(80),
                "participantIds" to participants,
                "isVideo" to isVideo,
                "status" to "RINGING",
                "offerSdp" to offerSdp.take(30000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_CALLS).document(callId).set(payload).await()
            Result.success(
                CloudCallSession(
                    id = callId,
                    callerId = currentUid,
                    callerName = callerName,
                    calleeId = calleeUid,
                    calleeName = calleeName,
                    participantIds = participants,
                    isVideo = isVideo,
                    status = "RINGING",
                    offerSdp = offerSdp,
                    answerSdp = null
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "$COLLECTION_CALLS/$callId", auth)
            Result.failure(e)
        }
    }

    suspend fun answerCall(callId: String, answerSdp: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_CALLS).document(callId).update(
                mapOf(
                    "status" to "ANSWERED",
                    "answerSdp" to answerSdp.take(30000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "$COLLECTION_CALLS/$callId", auth)
            Result.failure(e)
        }
    }

    suspend fun updateCallStatus(callId: String, status: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (status == "ENDED" || status == "REJECTED") {
                updates["endedAtMs"] = System.currentTimeMillis()
            }
            firestore.collection(COLLECTION_CALLS).document(callId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "$COLLECTION_CALLS/$callId", auth)
            Result.failure(e)
        }
    }

    suspend fun sendIceCandidate(
        callId: String,
        isCaller: Boolean,
        participantIds: List<String>,
        sdpMid: String,
        sdpMLineIndex: Int,
        sdp: String
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val subCol = if (isCaller) COLLECTION_CALLER_CANDIDATES else COLLECTION_CALLEE_CANDIDATES
        val candId = "ice_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        return try {
            val payload = mapOf(
                "senderId" to currentUid,
                "participantIds" to participantIds.distinct().take(2),
                "sdpMid" to sdpMid.take(64),
                "sdpMLineIndex" to sdpMLineIndex.coerceIn(0, 32),
                "sdp" to sdp.take(2048),
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_CALLS)
                .document(callId)
                .collection(subCol)
                .document(candId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "$COLLECTION_CALLS/$callId/$subCol/$candId", auth)
            Result.failure(e)
        }
    }

    fun observeIncomingRingingCalls(): Flow<CloudCallSession?> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = firestore.collection(COLLECTION_CALLS)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, COLLECTION_CALLS, auth)
                    trySend(null)
                    return@addSnapshotListener
                }
                val incoming = snap?.documents.orEmpty()
                    .mapNotNull { doc -> mapCallDoc(doc) }
                    .firstOrNull { it.calleeId == currentUid && it.status == "RINGING" }
                trySend(incoming)
            }
        awaitClose { reg.remove() }
    }

    fun observeCall(callId: String): Flow<CloudCallSession?> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = firestore.collection(COLLECTION_CALLS).document(callId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, "$COLLECTION_CALLS/$callId", auth)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(if (doc != null && doc.exists()) mapCallDoc(doc) else null)
            }
        awaitClose { reg.remove() }
    }

    fun observeIceCandidates(callId: String, readCallerCandidates: Boolean): Flow<CloudIceCandidate> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            close()
            return@callbackFlow
        }
        val subCol = if (readCallerCandidates) COLLECTION_CALLER_CANDIDATES else COLLECTION_CALLEE_CANDIDATES
        val reg = firestore.collection(COLLECTION_CALLS)
            .document(callId)
            .collection(subCol)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "$COLLECTION_CALLS/$callId/$subCol", auth)
                    return@addSnapshotListener
                }
                snap?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val sdp = doc.getString("sdp") ?: return@forEach
                        trySend(
                            CloudIceCandidate(
                                senderId = doc.getString("senderId") ?: "",
                                sdpMid = doc.getString("sdpMid") ?: "0",
                                sdpMLineIndex = (doc.getLong("sdpMLineIndex") ?: 0L).toInt(),
                                sdp = sdp
                            )
                        )
                    }
                }
            }
        awaitClose { reg.remove() }
    }

    // --- Real-Time WebRTC DataChannel File Transfer Signaling (/file_transfers/{transferId}) ---
    suspend fun createFileTransferRequest(
        transferId: String,
        ownerUid: String,
        conversationId: String,
        messageId: String,
        fileId: String,
        fileName: String,
        fileSizeBytes: Long,
        offerSdp: String
    ): Result<CloudFileTransferSession> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("P2P dosya transferi için Google ile giriş yapın."))
        val participants = listOf(currentUid, ownerUid).distinct()
        if (participants.size != 2) {
            return Result.failure(IllegalArgumentException("Dosya sahibi farklı bir kullanıcı olmalıdır."))
        }
        val path = "$COLLECTION_FILE_TRANSFERS/$transferId"
        return try {
            val safeSize = fileSizeBytes.coerceAtLeast(1L)
            val payload = mapOf(
                "id" to transferId,
                "requesterUid" to currentUid,
                "ownerUid" to ownerUid,
                "participantIds" to participants,
                "conversationId" to conversationId,
                "messageId" to messageId,
                "fileId" to fileId,
                "fileName" to fileName.ifBlank { "dosya" }.take(255),
                "fileSizeBytes" to safeSize,
                "status" to "REQUESTED",
                "offerSdp" to offerSdp.take(30000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_FILE_TRANSFERS).document(transferId).set(payload).await()
            Result.success(
                CloudFileTransferSession(
                    id = transferId,
                    requesterUid = currentUid,
                    ownerUid = ownerUid,
                    participantIds = participants,
                    conversationId = conversationId,
                    messageId = messageId,
                    fileId = fileId,
                    fileName = fileName,
                    fileSizeBytes = safeSize,
                    status = "REQUESTED",
                    offerSdp = offerSdp,
                    answerSdp = null
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun answerFileTransferRequest(transferId: String, answerSdp: String): Result<Unit> {
        val path = "$COLLECTION_FILE_TRANSFERS/$transferId"
        return try {
            firestore.collection(COLLECTION_FILE_TRANSFERS).document(transferId).update(
                mapOf(
                    "status" to "ANSWERED",
                    "answerSdp" to answerSdp.take(30000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateFileTransferStatus(transferId: String, status: String): Result<Unit> {
        val path = "$COLLECTION_FILE_TRANSFERS/$transferId"
        return try {
            firestore.collection(COLLECTION_FILE_TRANSFERS).document(transferId).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun sendFileTransferIceCandidate(
        transferId: String,
        isRequester: Boolean,
        participantIds: List<String>,
        sdpMid: String,
        sdpMLineIndex: Int,
        sdp: String
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val subCol = if (isRequester) COLLECTION_REQUESTER_CANDIDATES else COLLECTION_OWNER_CANDIDATES
        val candId = "fice_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "$COLLECTION_FILE_TRANSFERS/$transferId/$subCol/$candId"
        return try {
            val payload = mapOf(
                "senderId" to currentUid,
                "participantIds" to participantIds.distinct().take(2),
                "sdpMid" to sdpMid.take(64),
                "sdpMLineIndex" to sdpMLineIndex.coerceIn(0, 32),
                "sdp" to sdp.take(2048),
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_FILE_TRANSFERS)
                .document(transferId)
                .collection(subCol)
                .document(candId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    fun observeIncomingFileTransferRequests(): Flow<CloudFileTransferSession> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            close()
            return@callbackFlow
        }
        val emitted = mutableSetOf<String>()
        val reg = firestore.collection(COLLECTION_FILE_TRANSFERS)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, COLLECTION_FILE_TRANSFERS, auth)
                    return@addSnapshotListener
                }
                snap?.documents.orEmpty().forEach { doc ->
                    val session = mapFileTransferDoc(doc) ?: return@forEach
                    if (session.ownerUid == currentUid &&
                        session.status == "REQUESTED" &&
                        emitted.add(session.id)
                    ) {
                        trySend(session)
                    }
                }
            }
        awaitClose { reg.remove() }
    }

    fun observeFileTransferSession(transferId: String): Flow<CloudFileTransferSession?> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = firestore.collection(COLLECTION_FILE_TRANSFERS).document(transferId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, "$COLLECTION_FILE_TRANSFERS/$transferId", auth)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(if (doc != null && doc.exists()) mapFileTransferDoc(doc) else null)
            }
        awaitClose { reg.remove() }
    }

    fun observeFileTransferIceCandidates(
        transferId: String,
        readRequesterCandidates: Boolean
    ): Flow<CloudIceCandidate> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            close()
            return@callbackFlow
        }
        val subCol = if (readRequesterCandidates) COLLECTION_REQUESTER_CANDIDATES else COLLECTION_OWNER_CANDIDATES
        val reg = firestore.collection(COLLECTION_FILE_TRANSFERS)
            .document(transferId)
            .collection(subCol)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "$COLLECTION_FILE_TRANSFERS/$transferId/$subCol", auth)
                    return@addSnapshotListener
                }
                snap?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val sdp = doc.getString("sdp") ?: return@forEach
                        trySend(
                            CloudIceCandidate(
                                senderId = doc.getString("senderId") ?: "",
                                sdpMid = doc.getString("sdpMid") ?: "0",
                                sdpMLineIndex = (doc.getLong("sdpMLineIndex") ?: 0L).toInt(),
                                sdp = sdp
                            )
                        )
                    }
                }
            }
        awaitClose { reg.remove() }
    }

    private fun mapFileTransferDoc(doc: com.google.firebase.firestore.DocumentSnapshot): CloudFileTransferSession? {
        val requesterUid = doc.getString("requesterUid") ?: return null
        val ownerUid = doc.getString("ownerUid") ?: return null
        val pIds = (doc.get("participantIds") as? List<*>)?.mapNotNull { it as? String } ?: return null
        return CloudFileTransferSession(
            id = doc.getString("id") ?: doc.id,
            requesterUid = requesterUid,
            ownerUid = ownerUid,
            participantIds = pIds,
            conversationId = doc.getString("conversationId") ?: return null,
            messageId = doc.getString("messageId") ?: return null,
            fileId = doc.getString("fileId") ?: return null,
            fileName = doc.getString("fileName") ?: "dosya",
            fileSizeBytes = doc.getLong("fileSizeBytes") ?: 0L,
            status = doc.getString("status") ?: "REQUESTED",
            offerSdp = doc.getString("offerSdp") ?: return null,
            answerSdp = doc.getString("answerSdp")
        )
    }

    private fun mapCallDoc(doc: com.google.firebase.firestore.DocumentSnapshot): CloudCallSession? {
        val callerId = doc.getString("callerId") ?: return null
        val calleeId = doc.getString("calleeId") ?: return null
        val pIds = (doc.get("participantIds") as? List<*>)?.mapNotNull { it as? String } ?: return null
        return CloudCallSession(
            id = doc.getString("id") ?: doc.id,
            callerId = callerId,
            callerName = doc.getString("callerName") ?: "Arayan",
            calleeId = calleeId,
            calleeName = doc.getString("calleeName") ?: "Alıcı",
            participantIds = pIds,
            isVideo = doc.getBoolean("isVideo") ?: false,
            status = doc.getString("status") ?: "ENDED",
            offerSdp = doc.getString("offerSdp") ?: "",
            answerSdp = doc.getString("answerSdp")
        )
    }

    private fun mapConversationSnapshot(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        currentUid: String
    ): CloudConversationRoom? {
        val participantIds = (doc.get("participantIds") as? List<*>)
            ?.mapNotNull { it as? String }
            ?: return null
        if (currentUid !in participantIds) return null
        val participantNamesRaw = doc.get("participantNames") as? Map<*, *>
        val participantNames = participantNamesRaw
            ?.entries
            ?.mapNotNull { (k, v) ->
                val key = k as? String ?: return@mapNotNull null
                val value = v as? String ?: return@mapNotNull null
                key to value
            }
            ?.toMap()
            ?: emptyMap()
        val isGroup = doc.getBoolean("isGroup") ?: false
        val rawTitle = doc.getString("title") ?: "Sohbet"
        val displayTitle = if (!isGroup) {
            val otherUid = participantIds.firstOrNull { it != currentUid }
            otherUid?.let { participantNames[it] } ?: rawTitle
        } else {
            rawTitle
        }
        val updatedAtMs = doc.getTimestamp("updatedAt")?.toDate()?.time
            ?: doc.getTimestamp("createdAt")?.toDate()?.time
            ?: System.currentTimeMillis()
        return CloudConversationRoom(
            id = doc.getString("id") ?: doc.id,
            title = displayTitle,
            isGroup = isGroup,
            createdBy = doc.getString("createdBy") ?: "",
            participantIds = participantIds,
            participantNames = participantNames,
            lastMessage = doc.getString("lastMessage") ?: "",
            lastSenderId = doc.getString("lastSenderId"),
            updatedAtMs = updatedAtMs
        )
    }

    private fun generateDefaultHandle(user: FirebaseUser): String {
        val baseRaw = user.email?.substringBefore("@")
            ?: user.displayName
            ?: "user"
        val cleanBase = baseRaw.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(20)
            .ifBlank { "gen" }
        val suffix = user.uid.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "").take(4).ifBlank { "0001" }
        val combined = "${cleanBase}_$suffix".take(32)
        return if (combined.length >= 3) combined else "${combined}gen".take(32)
    }

    private fun sanitizeHandleOrNull(raw: String): String? {
        val clean = raw.trim().removePrefix("@").lowercase(Locale.US)
        return if (clean.matches(HANDLE_REGEX)) clean else null
    }

    // --- User Blocking, Abuse Reporting & KVKK Article 11 Compliance ---

    suspend fun blockUser(
        blockedUid: String,
        displayName: String,
        handle: String = ""
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Engelleme işlemi için önce giriş yapın."))
        val targetId = blockedUid.trim()
        if (targetId.isBlank() || targetId == currentUid) {
            return Result.failure(IllegalArgumentException("Geçersiz engelleme hedefi."))
        }
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_BLOCKED/$targetId"
        return try {
            val payload = mutableMapOf<String, Any>(
                "blockedUid" to targetId,
                "displayName" to displayName.ifBlank { "Kullanıcı" }.take(80),
                "createdAt" to FieldValue.serverTimestamp()
            )
            handle.trim().removePrefix("@").take(32).takeIf { it.isNotBlank() }?.let {
                payload["handle"] = it
            }
            firestore.collection(COLLECTION_USERS)
                .document(currentUid)
                .collection(COLLECTION_BLOCKED)
                .document(targetId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun unblockUser(blockedUid: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("İşlem için önce giriş yapın."))
        val targetId = blockedUid.trim()
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_BLOCKED/$targetId"
        return try {
            firestore.collection(COLLECTION_USERS)
                .document(currentUid)
                .collection(COLLECTION_BLOCKED)
                .document(targetId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun fetchBlockedUids(): Result<Set<String>> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.success(emptySet())
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_BLOCKED"
        return try {
            val snap = firestore.collection(COLLECTION_USERS)
                .document(currentUid)
                .collection(COLLECTION_BLOCKED)
                .get()
                .await()
            Result.success(snap.documents.mapNotNull { it.getString("blockedUid") ?: it.id }.toSet())
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            Result.failure(e)
        }
    }

    fun observeBlockedUids(): Flow<Set<String>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_BLOCKED"
        val reg = firestore.collection(COLLECTION_USERS)
            .document(currentUid)
            .collection(COLLECTION_BLOCKED)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path, auth)
                    return@addSnapshotListener
                }
                val ids = snap?.documents.orEmpty().mapNotNull { it.getString("blockedUid") ?: it.id }.toSet()
                trySend(ids)
            }
        awaitClose { reg.remove() }
    }

    suspend fun reportUser(
        targetUid: String,
        targetName: String,
        conversationId: String?,
        reason: String,
        details: String = ""
    ): Result<String> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Şikayet göndermek için önce giriş yapın."))
        val cleanTarget = targetUid.trim().take(128)
        if (cleanTarget.isBlank() || cleanTarget == currentUid) {
            return Result.failure(IllegalArgumentException("Geçersiz şikayet hedefi."))
        }
        val reportId = "rep_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "$COLLECTION_USER_REPORTS/$reportId"
        return try {
            val payload = mutableMapOf<String, Any>(
                "id" to reportId,
                "reporterUid" to currentUid,
                "targetUid" to cleanTarget,
                "reason" to reason.trim().ifBlank { "Uygunsuz davranış" }.take(120),
                "createdAt" to FieldValue.serverTimestamp()
            )
            targetName.trim().take(80).takeIf { it.isNotBlank() }?.let { payload["targetName"] = it }
            conversationId?.trim()?.take(128)?.takeIf { it.isNotBlank() }?.let { payload["conversationId"] = it }
            details.trim().take(1000).takeIf { it.isNotBlank() }?.let { payload["details"] = it }

            firestore.collection(COLLECTION_USER_REPORTS).document(reportId).set(payload).await()
            Result.success(reportId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun submitKvkkRequest(
        requestType: String,
        details: String,
        contactEmail: String = ""
    ): Result<String> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Bulut başvurusu için önce Google ile giriş yapın."))
        val normalizedType = requestType.trim().uppercase(Locale.US)
        if (normalizedType !in setOf("ACCESS", "CORRECTION", "DELETION", "OBJECTION")) {
            return Result.failure(IllegalArgumentException("Geçersiz KVKK başvuru türü."))
        }
        val cleanDetails = details.trim().take(2000)
        if (cleanDetails.isBlank()) {
            return Result.failure(IllegalArgumentException("Lütfen talep açıklamasını doldurun."))
        }
        val reqId = "kvkk_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val path = "$COLLECTION_KVKK_REQUESTS/$reqId"
        return try {
            val payload = mutableMapOf<String, Any>(
                "id" to reqId,
                "uid" to currentUid,
                "requestType" to normalizedType,
                "details" to cleanDetails,
                "status" to "SUBMITTED",
                "createdAt" to FieldValue.serverTimestamp()
            )
            contactEmail.trim().take(254).takeIf { it.isNotBlank() }?.let { payload["contactEmail"] = it }
            firestore.collection(COLLECTION_KVKK_REQUESTS).document(reqId).set(payload).await()
            Result.success(reqId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateCloudDisplayName(newDisplayName: String): Result<CloudHandleProfile> {
        val clean = newDisplayName.trim().take(80)
        if (clean.isBlank()) {
            return Result.failure(IllegalArgumentException("Görünen ad boş olamaz."))
        }
        return ensureUserProfile(displayNameOverride = clean)
    }

    suspend fun exportUserCloudDataJson(): Result<String> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Bulut verilerini dışa aktarmak için giriş yapın."))
        val uid = user.uid
        return try {
            val root = JSONObject()
            root.put("exportedAtIso", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
            root.put("uid", uid)

            val profileDoc = firestore.collection(COLLECTION_USERS).document(uid).get().await()
            val profileJson = JSONObject()
            if (profileDoc.exists()) {
                profileJson.put("handle", profileDoc.getString("handle") ?: "")
                profileJson.put("displayName", profileDoc.getString("displayName") ?: "")
                profileJson.put("email", profileDoc.getString("email") ?: (user.email ?: ""))
                profileJson.put("avatarEmoji", profileDoc.getString("avatarEmoji") ?: "")
            }
            root.put("cloudProfile", profileJson)

            val blockedDocs = firestore.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_BLOCKED).get().await()
            val blockedArr = JSONArray()
            blockedDocs.documents.forEach { d ->
                blockedArr.put(JSONObject().apply {
                    put("blockedUid", d.getString("blockedUid") ?: d.id)
                    put("displayName", d.getString("displayName") ?: "")
                    put("handle", d.getString("handle") ?: "")
                })
            }
            root.put("blockedUsers", blockedArr)

            val convDocs = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereArrayContains("participantIds", uid).get().await()
            val convArr = JSONArray()
            convDocs.documents.forEach { d ->
                convArr.put(JSONObject().apply {
                    put("id", d.id)
                    put("title", d.getString("title") ?: "")
                    put("isGroup", d.getBoolean("isGroup") ?: false)
                    put("lastMessage", d.getString("lastMessage") ?: "")
                })
            }
            root.put("conversations", convArr)

            val kvkkDocs = firestore.collection(COLLECTION_KVKK_REQUESTS)
                .whereEqualTo("uid", uid).get().await()
            val kvkkArr = JSONArray()
            kvkkDocs.documents.forEach { d ->
                kvkkArr.put(JSONObject().apply {
                    put("id", d.id)
                    put("requestType", d.getString("requestType") ?: "")
                    put("status", d.getString("status") ?: "")
                    put("details", d.getString("details") ?: "")
                })
            }
            root.put("kvkkRequests", kvkkArr)

            val parentalSnap = firestore.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_PARENTAL_CONTROLS).document(DOC_PARENTAL_CONFIG).get().await()
            if (parentalSnap.exists()) {
                root.put("parentalControlsCloud", JSONObject().apply {
                    put("parentEmail", parentalSnap.getString("parentEmail") ?: "")
                    put("isVerified", parentalSnap.getBoolean("isVerified") ?: false)
                    put("restrictionsEnabled", parentalSnap.getBoolean("restrictionsEnabled") ?: false)
                    put("blockStrangerDms", parentalSnap.getBoolean("blockStrangerDms") ?: true)
                    put("forceProfanityFilter", parentalSnap.getBoolean("forceProfanityFilter") ?: true)
                    put("dailyLimitMinutes", parentalSnap.getLong("dailyLimitMinutes") ?: 60L)
                })
            }

            val feedbackDocs = firestore.collection(COLLECTION_FEEDBACK_REPORTS)
                .whereEqualTo("uid", uid).get().await()
            val feedbackArr = JSONArray()
            feedbackDocs.documents.forEach { d ->
                feedbackArr.put(JSONObject().apply {
                    put("id", d.id)
                    put("category", d.getString("category") ?: "FEEDBACK")
                    put("severity", d.getString("severity") ?: "LOW")
                    put("rating", (d.getLong("rating") ?: 5L).toInt())
                    put("moduleTag", d.getString("moduleTag") ?: "")
                    put("title", d.getString("title") ?: "")
                    put("description", d.getString("description") ?: "")
                    put("status", d.getString("status") ?: "OPEN")
                    put("appVersion", d.getString("appVersion") ?: "")
                    put("osVersion", d.getString("osVersion") ?: "")
                    put("clientTimestampMs", d.getLong("clientTimestampMs") ?: 0L)
                })
            }
            root.put("feedbackReports", feedbackArr)

            Result.success(root.toString(2))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "$COLLECTION_USERS/$uid", auth)
            Result.failure(e)
        }
    }

    // --- Firebase Parental Control Config Sync (/users/{uid}/parental_controls/config) ---

    suspend fun saveParentalControlConfig(
        parentEmail: String,
        isVerified: Boolean,
        restrictionsEnabled: Boolean,
        blockStrangerDms: Boolean,
        forceProfanityFilter: Boolean,
        dailyLimitMinutes: Int,
        codeHash: String,
        codeSalt: String,
        expiresAtMs: Long
    ): Result<CloudParentalControlConfig> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Firebase Ebeveyn Denetimi için Google oturumu gerekir."))
        val cleanEmail = parentEmail.trim().take(254)
        if (cleanEmail.length < 3) {
            return Result.failure(IllegalArgumentException("Geçersiz ebeveyn e-posta adresi."))
        }
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_PARENTAL_CONTROLS/$DOC_PARENTAL_CONFIG"
        val docRef = firestore.collection(COLLECTION_USERS)
            .document(currentUid)
            .collection(COLLECTION_PARENTAL_CONTROLS)
            .document(DOC_PARENTAL_CONFIG)
        return try {
            val snap = docRef.get().await()
            val clampedLimit = dailyLimitMinutes.coerceIn(10, 480)
            if (!snap.exists()) {
                val payload = mapOf(
                    "uid" to currentUid,
                    "parentEmail" to cleanEmail,
                    "isVerified" to isVerified,
                    "restrictionsEnabled" to restrictionsEnabled,
                    "blockStrangerDms" to blockStrangerDms,
                    "forceProfanityFilter" to forceProfanityFilter,
                    "dailyLimitMinutes" to clampedLimit,
                    "codeHash" to codeHash.take(256),
                    "codeSalt" to codeSalt.take(128),
                    "expiresAtMs" to expiresAtMs.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "parentEmail" to cleanEmail,
                    "isVerified" to isVerified,
                    "restrictionsEnabled" to restrictionsEnabled,
                    "blockStrangerDms" to blockStrangerDms,
                    "forceProfanityFilter" to forceProfanityFilter,
                    "dailyLimitMinutes" to clampedLimit,
                    "codeHash" to codeHash.take(256),
                    "codeSalt" to codeSalt.take(128),
                    "expiresAtMs" to expiresAtMs.coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(
                CloudParentalControlConfig(
                    uid = currentUid,
                    parentEmail = cleanEmail,
                    isVerified = isVerified,
                    restrictionsEnabled = restrictionsEnabled,
                    blockStrangerDms = blockStrangerDms,
                    forceProfanityFilter = forceProfanityFilter,
                    dailyLimitMinutes = clampedLimit,
                    codeHash = codeHash,
                    codeSalt = codeSalt,
                    expiresAtMs = expiresAtMs
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun fetchParentalControlConfig(): Result<CloudParentalControlConfig?> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Google oturumu açık değil."))
        val path = "$COLLECTION_USERS/$currentUid/$COLLECTION_PARENTAL_CONTROLS/$DOC_PARENTAL_CONFIG"
        return try {
            val snap = firestore.collection(COLLECTION_USERS)
                .document(currentUid)
                .collection(COLLECTION_PARENTAL_CONTROLS)
                .document(DOC_PARENTAL_CONFIG)
                .get()
                .await()
            if (!snap.exists()) return Result.success(null)
            Result.success(
                CloudParentalControlConfig(
                    uid = snap.getString("uid") ?: currentUid,
                    parentEmail = snap.getString("parentEmail") ?: "",
                    isVerified = snap.getBoolean("isVerified") ?: false,
                    restrictionsEnabled = snap.getBoolean("restrictionsEnabled") ?: false,
                    blockStrangerDms = snap.getBoolean("blockStrangerDms") ?: true,
                    forceProfanityFilter = snap.getBoolean("forceProfanityFilter") ?: true,
                    dailyLimitMinutes = (snap.getLong("dailyLimitMinutes") ?: 60L).toInt(),
                    codeHash = snap.getString("codeHash") ?: "",
                    codeSalt = snap.getString("codeSalt") ?: "",
                    expiresAtMs = snap.getLong("expiresAtMs") ?: 0L,
                    updatedAtMs = snap.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path, auth)
            Result.failure(e)
        }
    }

    // --- Real-Time Live Location Sharing (/conversations/{cid}/live_locations/{locId}) ---

    suspend fun upsertLiveLocation(session: CloudLiveLocation): Result<Unit> {
        if (session.conversationId == "conv_self_notes" || session.conversationId == "conv_gemini_ai") {
            return Result.success(Unit)
        }
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Canlı konum paylaşımı için Google ile giriş yapın."))
        val normalizedParticipants = if (session.participantIds.contains(currentUid)) {
            session.participantIds.distinct().take(50)
        } else {
            (listOf(currentUid) + session.participantIds).distinct().take(50)
        }
        val path = "$COLLECTION_CONVERSATIONS/${session.conversationId}/$COLLECTION_LIVE_LOCATIONS/${session.id}"
        val convRef = firestore.collection(COLLECTION_CONVERSATIONS).document(session.conversationId)
        val locRef = convRef.collection(COLLECTION_LIVE_LOCATIONS).document(session.id)
        return try {
            val convSnap = convRef.get().await()
            if (!convSnap.exists()) {
                val namesMap = normalizedParticipants.associateWith { uid ->
                    if (uid == currentUid) session.senderName.ifBlank { "Kullanıcı" }.take(80) else "Üye"
                }
                convRef.set(
                    mapOf(
                        "id" to session.conversationId,
                        "title" to session.conversationId.take(60),
                        "isGroup" to false,
                        "createdBy" to currentUid,
                        "participantIds" to normalizedParticipants,
                        "participantNames" to namesMap,
                        "lastMessage" to "📍 ${session.label.take(80)}",
                        "lastSenderId" to currentUid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            val serverParticipants = (convSnap.get("participantIds") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.takeIf { it.isNotEmpty() }
                ?: normalizedParticipants

            val existing = locRef.get().await()
            val lat = session.latitude.coerceIn(-90.0, 90.0)
            val lng = session.longitude.coerceIn(-180.0, 180.0)
            val acc = session.accuracyMeters.coerceIn(0.0, 100000.0)
            val cleanLabel = session.label.ifBlank { "Canlı Konum" }.take(120)

            if (!existing.exists()) {
                locRef.set(
                    mapOf(
                        "id" to session.id,
                        "conversationId" to session.conversationId,
                        "senderId" to currentUid,
                        "senderName" to session.senderName.ifBlank { "Kullanıcı" }.take(80),
                        "participantIds" to serverParticipants,
                        "latitude" to lat,
                        "longitude" to lng,
                        "accuracyMeters" to acc,
                        "label" to cleanLabel,
                        "isActive" to session.isActive,
                        "expiresAtMs" to session.expiresAtMs.coerceAtLeast(0L),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                locRef.update(
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lng,
                        "accuracyMeters" to acc,
                        "label" to cleanLabel,
                        "isActive" to session.isActive,
                        "expiresAtMs" to session.expiresAtMs.coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun stopLiveLocation(conversationId: String, locationId: String): Result<Unit> {
        if (conversationId.isBlank() || locationId.isBlank()) return Result.success(Unit)
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Oturum gerekli."))
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_LIVE_LOCATIONS/$locationId"
        return try {
            val locRef = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(COLLECTION_LIVE_LOCATIONS)
                .document(locationId)
            val snap = locRef.get().await()
            if (snap.exists() && snap.getString("senderId") == currentUid) {
                locRef.update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    fun observeLiveLocations(conversationId: String): Flow<Map<String, CloudLiveLocation>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null || conversationId == "conv_self_notes" || conversationId == "conv_gemini_ai") {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        val path = "$COLLECTION_CONVERSATIONS/$conversationId/$COLLECTION_LIVE_LOCATIONS"
        val reg = firestore.collection(COLLECTION_CONVERSATIONS)
            .document(conversationId)
            .collection(COLLECTION_LIVE_LOCATIONS)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path, auth)
                    return@addSnapshotListener
                }
                val map = mutableMapOf<String, CloudLiveLocation>()
                snapshot?.documents.orEmpty().forEach { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val lat = doc.getDouble("latitude") ?: return@forEach
                    val lng = doc.getDouble("longitude") ?: return@forEach
                    val acc = doc.getDouble("accuracyMeters") ?: 0.0
                    val exp = doc.getLong("expiresAtMs") ?: 0L
                    val activeFlag = doc.getBoolean("isActive") ?: false
                    val isExpired = exp > 0L && System.currentTimeMillis() > exp
                    map[id] = CloudLiveLocation(
                        id = id,
                        conversationId = conversationId,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "Kullanıcı",
                        participantIds = (doc.get("participantIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        latitude = lat,
                        longitude = lng,
                        accuracyMeters = acc,
                        label = doc.getString("label") ?: "Canlı Konum",
                        isActive = activeFlag && !isExpired,
                        expiresAtMs = exp,
                        updatedAtMs = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
                trySend(map)
            }
        awaitClose { reg.remove() }
    }

    suspend fun deleteCloudAccountAndOwnedData(allowPartialAuthFallback: Boolean = false): Result<Unit> {
        val user = auth.currentUser
            ?: return if (allowPartialAuthFallback) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Bulut hesabı silmek için giriş yapılmış olmalıdır."))
            }
        val uid = user.uid
        return try {
            val userRef = firestore.collection(COLLECTION_USERS).document(uid)
            val userSnap = runCatching { userRef.get().await() }.getOrNull()
            val handle = userSnap?.getString("handle")

            // Delete user's messages inside participant conversations and delete conversations created by user
            runCatching {
                val convDocs = firestore.collection(COLLECTION_CONVERSATIONS)
                    .whereArrayContains("participantIds", uid)
                    .get()
                    .await()
                convDocs.documents.forEach { convDoc ->
                    runCatching {
                        val ownMsgs = convDoc.reference.collection(COLLECTION_MESSAGES)
                            .whereEqualTo("senderId", uid)
                            .get()
                            .await()
                        ownMsgs.documents.forEach { msgDoc ->
                            runCatching { msgDoc.reference.delete().await() }
                        }
                    }
                    if (convDoc.getString("createdBy") == uid) {
                        runCatching { convDoc.reference.delete().await() }
                    }
                }
            }

            // Delete calls initiated by user
            runCatching {
                firestore.collection(COLLECTION_CALLS)
                    .whereEqualTo("callerId", uid)
                    .get()
                    .await()
                    .documents.forEach { runCatching { it.reference.delete().await() } }
            }

            // Delete live locations shared by user
            runCatching {
                firestore.collection(COLLECTION_LIVE_LOCATIONS)
                    .whereEqualTo("senderId", uid)
                    .get()
                    .await()
                    .documents.forEach { runCatching { it.reference.delete().await() } }
            }

            runCatching {
                val blockedDocs = userRef.collection(COLLECTION_BLOCKED).get().await()
                blockedDocs.documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                val parentalDocs = userRef.collection(COLLECTION_PARENTAL_CONTROLS).get().await()
                parentalDocs.documents.forEach { it.reference.delete().await() }
            }

            if (!handle.isNullOrBlank()) {
                runCatching {
                    val handleRef = firestore.collection(COLLECTION_HANDLE_INDEX).document(handle)
                    val hSnap = handleRef.get().await()
                    if (hSnap.exists() && hSnap.getString("uid") == uid) {
                        handleRef.delete().await()
                    }
                }
            }

            runCatching {
                firestore.collection(COLLECTION_DEVICE_LINKS)
                    .whereEqualTo("ownerUid", uid).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                firestore.collection(COLLECTION_PARENT_LINKS)
                    .whereEqualTo("childUid", uid).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                firestore.collection(COLLECTION_PARENT_LINKS)
                    .whereEqualTo("parentUid", uid).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                firestore.collection(COLLECTION_KVKK_REQUESTS)
                    .whereEqualTo("uid", uid).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                firestore.collection(COLLECTION_FEEDBACK_REPORTS)
                    .whereEqualTo("uid", uid).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }

            runCatching {
                firestore.collection(COLLECTION_E2EE_KEYS).document(uid).delete().await()
            }

            if (userSnap?.exists() == true) {
                runCatching { userRef.delete().await() }
            }

            if (allowPartialAuthFallback) {
                runCatching { user.delete().await() }
                Result.success(Unit)
            } else {
                user.delete().await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            if (allowPartialAuthFallback) Result.success(Unit) else Result.failure(e)
        }
    }

    // --- Analyzable Feedback, Suggestions & Bug Reports (/feedback_reports/{reportId}) ---

    suspend fun submitFeedbackReport(report: CloudFeedbackReport): Result<CloudFeedbackReport> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Bulut eşitlemesi için Google ile giriş yapın."))
        val safeCategory = report.category.trim().uppercase(Locale.US).let {
            if (it in setOf("FEEDBACK", "SUGGESTION", "BUG", "PERFORMANCE")) it else "FEEDBACK"
        }
        val safeSeverity = report.severity.trim().uppercase(Locale.US).let {
            if (it in setOf("LOW", "MEDIUM", "HIGH", "CRITICAL")) it else "LOW"
        }
        val safeRating = report.rating.coerceIn(1, 5)
        val safeModule = report.moduleTag.trim().ifBlank { "Genel" }.take(40)
        val safeTitle = report.title.trim().take(120)
        val safeDesc = report.description.trim().take(2000)
        if (safeTitle.isBlank() || safeDesc.isBlank()) {
            return Result.failure(IllegalArgumentException("Başlık ve açıklama alanları boş bırakılamaz."))
        }
        val safeId = report.id.trim().takeIf { it.matches(Regex("^[a-zA-Z0-9_-]{4,64}$")) }
            ?: "fb_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val safeAppVer = report.appVersion.trim().ifBlank { "1.0" }.take(32)
        val safeOsVer = report.osVersion.trim().ifBlank { "Android" }.take(48)
        val safeTs = report.clientTimestampMs.coerceAtLeast(0L)
        val path = "$COLLECTION_FEEDBACK_REPORTS/$safeId"
        return try {
            val payload = mapOf(
                "id" to safeId,
                "uid" to currentUid,
                "category" to safeCategory,
                "severity" to safeSeverity,
                "rating" to safeRating,
                "moduleTag" to safeModule,
                "title" to safeTitle,
                "description" to safeDesc,
                "status" to "OPEN",
                "appVersion" to safeAppVer,
                "osVersion" to safeOsVer,
                "clientTimestampMs" to safeTs,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_FEEDBACK_REPORTS).document(safeId).set(payload).await()
            Result.success(
                report.copy(
                    id = safeId,
                    uid = currentUid,
                    category = safeCategory,
                    severity = safeSeverity,
                    rating = safeRating,
                    moduleTag = safeModule,
                    title = safeTitle,
                    description = safeDesc,
                    status = "OPEN",
                    appVersion = safeAppVer,
                    osVersion = safeOsVer,
                    clientTimestampMs = safeTs
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun fetchMyFeedbackReports(): Result<List<CloudFeedbackReport>> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.success(emptyList())
        return try {
            val snap = firestore.collection(COLLECTION_FEEDBACK_REPORTS)
                .whereEqualTo("uid", currentUid)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc -> mapFeedbackDoc(doc) }
                .sortedByDescending { it.clientTimestampMs }
            Result.success(list)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, COLLECTION_FEEDBACK_REPORTS, auth)
            Result.failure(e)
        }
    }

    suspend fun updateFeedbackStatus(
        reportId: String,
        newStatus: String,
        newRating: Int? = null,
        newDescription: String? = null
    ): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Güncelleme için Google ile giriş yapın."))
        val safeStatus = newStatus.trim().uppercase(Locale.US)
        if (safeStatus !in setOf("OPEN", "IN_REVIEW", "RESOLVED")) {
            return Result.failure(IllegalArgumentException("Geçersiz durum değeri."))
        }
        val path = "$COLLECTION_FEEDBACK_REPORTS/$reportId"
        return try {
            val ref = firestore.collection(COLLECTION_FEEDBACK_REPORTS).document(reportId)
            val snap = ref.get().await()
            if (!snap.exists() || snap.getString("uid") != currentUid) {
                return Result.failure(IllegalStateException("Rapor bulunamadı veya yetkiniz yok."))
            }
            val updates = mutableMapOf<String, Any>(
                "status" to safeStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            newRating?.let { updates["rating"] = it.coerceIn(1, 5) }
            newDescription?.trim()?.takeIf { it.isNotBlank() }?.let { updates["description"] = it.take(2000) }
            ref.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteFeedbackReport(reportId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Silme için Google ile giriş yapın."))
        val path = "$COLLECTION_FEEDBACK_REPORTS/$reportId"
        return try {
            val ref = firestore.collection(COLLECTION_FEEDBACK_REPORTS).document(reportId)
            val snap = ref.get().await()
            if (snap.exists() && snap.getString("uid") == currentUid) {
                ref.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path, auth)
            Result.failure(e)
        }
    }

    private fun mapFeedbackDoc(doc: com.google.firebase.firestore.DocumentSnapshot): CloudFeedbackReport? {
        val id = doc.getString("id") ?: doc.id
        val uid = doc.getString("uid") ?: return null
        val title = doc.getString("title") ?: return null
        val description = doc.getString("description") ?: ""
        return CloudFeedbackReport(
            id = id,
            uid = uid,
            category = doc.getString("category") ?: "FEEDBACK",
            severity = doc.getString("severity") ?: "LOW",
            rating = (doc.getLong("rating") ?: 5L).toInt().coerceIn(1, 5),
            moduleTag = doc.getString("moduleTag") ?: "Genel",
            title = title,
            description = description,
            status = doc.getString("status") ?: "OPEN",
            appVersion = doc.getString("appVersion") ?: "1.0",
            osVersion = doc.getString("osVersion") ?: "Android",
            clientTimestampMs = doc.getLong("clientTimestampMs")
                ?: doc.getTimestamp("createdAt")?.toDate()?.time
                ?: System.currentTimeMillis()
        )
    }

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_BLOCKED = "blocked"
        const val COLLECTION_PARENTAL_CONTROLS = "parental_controls"
        const val DOC_PARENTAL_CONFIG = "config"
        const val COLLECTION_HANDLE_INDEX = "handle_index"
        const val COLLECTION_CONVERSATIONS = "conversations"
        const val COLLECTION_MESSAGES = "messages"
        const val COLLECTION_CHUNKS = "chunks"
        const val COLLECTION_LIVE_LOCATIONS = "live_locations"
        const val COLLECTION_CALLS = "calls"
        const val COLLECTION_CALLER_CANDIDATES = "callerCandidates"
        const val COLLECTION_CALLEE_CANDIDATES = "calleeCandidates"
        const val COLLECTION_FILE_TRANSFERS = "file_transfers"
        const val COLLECTION_REQUESTER_CANDIDATES = "requesterCandidates"
        const val COLLECTION_OWNER_CANDIDATES = "ownerCandidates"
        const val COLLECTION_DEVICE_LINKS = "device_links"
        const val COLLECTION_PARENT_LINKS = "parent_links"
        const val COLLECTION_USER_REPORTS = "user_reports"
        const val COLLECTION_KVKK_REQUESTS = "kvkk_requests"
        const val COLLECTION_FEEDBACK_REPORTS = "feedback_reports"
        const val COLLECTION_E2EE_KEYS = "e2ee_keys"
        private val HANDLE_REGEX = Regex("^[a-z0-9_]{3,32}$")

        fun create(context: Context): FirestoreChatRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            return FirestoreChatRepository(db, FirebaseAuth.getInstance())
        }

        fun formatTimeShort(epochMs: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
        }
    }
}

fun CloudConversationRoom.toConversationItem(): ConversationItem {
    return ConversationItem(
        id = id,
        name = title,
        initials = if (isGroup) "👥" else title.take(2).uppercase(),
        avatarEmoji = if (isGroup) "👥" else "🔐",
        isGroup = isGroup,
        isOnline = true,
        isVerified = true,
        lastMessage = lastMessage,
        lastTime = formatConversationListTime(updatedAtMs),
        participantIds = participantIds,
        participantNames = participantNames
    )
}
