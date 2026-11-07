package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "ChatRepository"

class ChatRepository(private val chatDao: ChatDao) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(userId: Long? = null, mode: AiMode = AiMode.GENERAL): ChatSessionEntity =
        withContext(Dispatchers.IO) {
            val newSession = ChatSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Yeni Sohbet",
                mode = mode.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            chatDao.insertSession(newSession)
            newSession
        }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) = withContext(Dispatchers.IO) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun clearAllSessions() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
        chatDao.clearAllSessions()
    }

    suspend fun sendMessageStream(
        sessionId: String,
        userMessageText: String,
        mode: AiMode,
        onChunk: suspend (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmedPrompt = userMessageText.trim()
            if (trimmedPrompt.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Mesaj boş olamaz."))
            }

            // 1. Save user message to database
            val userMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "user",
                content = trimmedPrompt,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMsg)

            // 2. Fetch past conversation history
            val pastMessages = chatDao.getMessagesForSessionList(sessionId)
            // If this is the first message or only userMsg in session, update title
            if (pastMessages.size <= 2) {
                val titleCandidate = if (trimmedPrompt.length > 28) {
                    trimmedPrompt.take(28) + "..."
                } else {
                    trimmedPrompt
                }
                chatDao.updateSessionTitle(sessionId, titleCandidate)
            } else {
                chatDao.touchSession(sessionId)
            }

            // 3. Prepare multi-turn history for Gemini (excluding the just added userMsg)
            val historyContentList = mutableListOf<Content>()
            pastMessages.dropLast(1).forEach { msg ->
                val role = if (msg.role == "user") "user" else "model"
                historyContentList.add(
                    content(role) {
                        text(msg.content)
                    }
                )
            }

            // 4. Initialize generative model lazily with systemInstruction for the active mode
            val generativeModel = Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                systemInstruction = content { text(mode.systemPrompt) }
            )

            val chatSession = generativeModel.startChat(history = historyContentList)
            val accumulatedResponse = StringBuilder()

            // 5. Stream response with 60s timeout
            withTimeout(60_000L) {
                chatSession.sendMessageStream(trimmedPrompt).collect { chunk ->
                    val textPart = chunk.text.orEmpty()
                    if (textPart.isNotEmpty()) {
                        accumulatedResponse.append(textPart)
                        onChunk(accumulatedResponse.toString())
                    }
                }
            }

            val finalReply = accumulatedResponse.toString()
            if (finalReply.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Yapay zekadan boş yanıt alındı."))
            }

            // 6. Save model reply to database
            val modelMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                content = finalReply,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(modelMsg)
            chatDao.touchSession(sessionId)

            Result.success(finalReply)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chat generation timed out", e)
            Result.failure(Exception("Yapay zeka yanıtı zaman aşımına uğradı (60 sn). Lütfen tekrar deneyin."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Chat generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Emergency Panic Mode System Override:
     * Instantly purges all chat sessions and messages from persistent storage.
     */
    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        try {
            chatDao.clearAllMessages()
            chatDao.clearAllSessions()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to purge chat history in panic override", e)
        }
    }
}
