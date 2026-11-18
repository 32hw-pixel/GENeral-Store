package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.DirectMessage
import com.example.data.FirestoreChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * CloudMessagingManager:
 * Thin bridge backed by FirestoreChatRepository targeting the provisioned Enterprise Firestore DB.
 * Only operates when an authenticated Google user exists; otherwise leaves local/AI sessions untouched.
 */
object CloudMessagingManager {
    private const val TAG = "CloudMessaging"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var activeListenJob: Job? = null

    fun sendCloudMessage(
        context: Context,
        conversationId: String,
        message: DirectMessage,
        participantIds: List<String>,
        senderDisplayName: String,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            onSuccess()
            return
        }
        val repo = try {
            FirestoreChatRepository.create(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirestoreChatRepository: ${e.message}")
            onError(e)
            return
        }

        scope.launch {
            val result = repo.sendMessage(
                conversationId = conversationId,
                message = message,
                participantIds = participantIds,
                senderDisplayName = senderDisplayName,
                e2eeContext = context
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it) }
        }
    }

    fun listenToConversation(
        context: Context,
        conversationId: String,
        onMessagesReceived: (List<DirectMessage>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        stopListening()
        if (FirebaseAuth.getInstance().currentUser == null) return

        val repo = try {
            FirestoreChatRepository.create(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirestoreChatRepository listener: ${e.message}")
            onError(e)
            return
        }

        activeListenJob = scope.launch {
            repo.observeMessages(conversationId, e2eeContext = context)
                .catch { e ->
                    Log.e(TAG, "Snapshot listener error on $conversationId", e)
                    onError(e)
                }
                .collect { msgs ->
                    onMessagesReceived(msgs)
                }
        }
    }

    fun stopListening() {
        activeListenJob?.cancel()
        activeListenJob = null
    }
}
