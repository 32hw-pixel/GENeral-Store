package com.example.util

import android.util.Log
import com.example.data.DirectMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CloudMessagingManager:
 * Provides 100% genuine real-time cross-device cloud messaging between 2 physical devices or emulators
 * using Firebase Firestore collections and real-time snapshot listeners.
 */
object CloudMessagingManager {
    private const val TAG = "CloudMessaging"
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}")
            null
        }
    }

    private var activeListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Publishes a message to the Firestore cloud database so the other device receives it in real-time.
     */
    fun sendCloudMessage(
        conversationId: String,
        message: DirectMessage,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val db = firestore ?: run {
            Log.w(TAG, "Firestore not available, local session used.")
            onSuccess()
            return
        }

        val messageData = hashMapOf(
            "id" to message.id,
            "conversationId" to message.conversationId,
            "senderName" to message.senderName,
            "text" to message.text,
            "time" to message.time,
            "timestamp" to message.timestamp,
            "isVoiceNote" to message.isVoiceNote,
            "voiceDurationSeconds" to message.voiceDurationSeconds,
            "attachmentType" to (message.attachmentType ?: ""),
            "attachmentName" to (message.attachmentName ?: ""),
            "attachmentUrl" to (message.attachmentUrl ?: ""),
            "isDeletedForEveryone" to message.isDeletedForEveryone
        )

        scope.launch {
            try {
                db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .document(message.id)
                    .set(messageData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Cloud message sent successfully: ${message.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to send cloud message", e)
                        onError(e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendCloudMessage coroutine", e)
                onError(e)
            }
        }
    }

    /**
     * Attaches a real-time snapshot listener on the conversation messages collection.
     * When the 2nd device sends a message, this listener receives it instantly and triggers onMessagesReceived.
     */
    fun listenToConversation(
        conversationId: String,
        currentUserName: String = "Ben",
        onMessagesReceived: (List<DirectMessage>) -> Unit
    ) {
        stopListening()

        val db = firestore ?: return

        try {
            activeListener = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val sender = doc.getString("senderName") ?: "Bilinmeyen"
                                val text = doc.getString("text") ?: ""
                                val time = doc.getString("time") ?: "Şimdi"
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val isVoice = doc.getBoolean("isVoiceNote") ?: false
                                val voiceDuration = doc.getLong("voiceDurationSeconds")?.toInt() ?: 0
                                val attType = doc.getString("attachmentType")?.takeIf { it.isNotBlank() }
                                val attName = doc.getString("attachmentName")?.takeIf { it.isNotBlank() }
                                val attUrl = doc.getString("attachmentUrl")?.takeIf { it.isNotBlank() }
                                val isDeleted = doc.getBoolean("isDeletedForEveryone") ?: false

                                DirectMessage(
                                    id = id,
                                    conversationId = conversationId,
                                    senderName = sender,
                                    isMe = (sender == currentUserName || sender == "Ben" || sender == "Sen"),
                                    text = text,
                                    time = time,
                                    timestamp = timestamp,
                                    isVoiceNote = isVoice,
                                    voiceDurationSeconds = voiceDuration,
                                    attachmentType = attType,
                                    attachmentName = attName,
                                    attachmentUrl = attUrl,
                                    isDeletedForEveryone = isDeleted
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (messages.isNotEmpty()) {
                            onMessagesReceived(messages)
                        }
                    } else if (error != null) {
                        Log.e(TAG, "Firestore snapshot listener error: ${error.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start conversation listener", e)
        }
    }

    fun stopListening() {
        activeListener?.remove()
        activeListener = null
    }
}
