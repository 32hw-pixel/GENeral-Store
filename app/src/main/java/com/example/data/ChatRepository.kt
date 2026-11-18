package com.example.data

import android.util.Log
import com.example.util.OfflineAiEngine
import com.example.util.WebResearchHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

private const val TAG = "ChatRepository"

private fun safeLogW(message: String) {
    try {
        Log.w(TAG, message)
    } catch (_: Throwable) {
    }
}

private fun safeLogE(message: String, error: Throwable) {
    try {
        Log.e(TAG, message, error)
    } catch (_: Throwable) {
    }
}

class ChatRepository(
    private val chatDao: ChatDao,
    private val aiCloudClient: AiCloudClient = FirebaseAiCloudClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(userId: Long? = null, mode: AiMode = AiMode.GENERAL): ChatSessionEntity =
        withContext(ioDispatcher) {
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

    suspend fun deleteSession(sessionId: String) = withContext(ioDispatcher) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) = withContext(ioDispatcher) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun clearAllSessions() = withContext(ioDispatcher) {
        chatDao.clearAllMessages()
        chatDao.clearAllSessions()
    }

    suspend fun clearSessionMessages(sessionId: String) = withContext(ioDispatcher) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.updateSessionTitle(sessionId, "Yeni Sohbet")
    }

    suspend fun sendMessageStream(
        sessionId: String,
        userMessageText: String,
        mode: AiMode,
        chatContextSummary: String = "",
        onThinkingStep: suspend (String) -> Unit = {},
        onChunk: suspend (String) -> Unit
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val trimmedPrompt = userMessageText.trim()
            if (trimmedPrompt.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Mesaj boş olamaz."))
            }

            // 1. Save user message to database (avoid duplicate user bubble when retrying a failed prompt)
            val existingMessages = chatDao.getMessagesForSessionList(sessionId)
            val lastMessage = existingMessages.lastOrNull()
            val isDuplicateRetry = lastMessage != null &&
                lastMessage.role == "user" &&
                lastMessage.content == trimmedPrompt
            if (!isDuplicateRetry) {
                val userMsg = ChatMessageEntity(
                    sessionId = sessionId,
                    role = "user",
                    content = trimmedPrompt,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(userMsg)
            }

            // 2. Fetch past conversation history
            val pastMessages = chatDao.getMessagesForSessionList(sessionId)
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

            // 3. Optional fast web research ONLY for factual web-lookup prompts (0 ms overhead otherwise)
            val webResearch = if (WebResearchHelper.shouldResearchOnline(trimmedPrompt)) {
                onThinkingStep("🌐 Güncel web kaynakları taranıyor...")
                try {
                    withTimeoutOrNull(AiServiceConfig.WEB_RESEARCH_BUDGET_MS) {
                        WebResearchHelper.searchLive(trimmedPrompt)
                    }
                } catch (_: TimeoutCancellationException) {
                    null
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }

            // 4. Windowed conversation history keeps TTFT consistently fast
            val historyTurns = pastMessages
                .dropLast(1)
                .takeLast(AiServiceConfig.MAX_HISTORY_TURNS)
                .map { msg ->
                    AiTurnHistory(
                        role = if (msg.role == "user") "user" else "model",
                        text = msg.content
                    )
                }

            val turnPrompt = FirebaseAiCloudClient.buildTurnPromptEnvelope(
                userPrompt = trimmedPrompt,
                chatContextSummary = chatContextSummary,
                webResearch = webResearch
            )

            onThinkingStep("⚡ GEN AI bulut akışını başlatıyor...")

            val modelCandidates = listOf(
                AiServiceConfig.PRIMARY_MODEL to AiServiceConfig.PRIMARY_TIMEOUT_MS,
                AiServiceConfig.FAST_MODEL to AiServiceConfig.FAST_TIMEOUT_MS
            )

            var finalReply = ""
            var lastClassifiedError: AiClassifiedError? = null
            var lastThrowable: Throwable? = null

            for ((index, candidate) in modelCandidates.withIndex()) {
                val (modelId, timeoutMs) = candidate
                try {
                    val reply = aiCloudClient.streamChat(
                        modelName = modelId,
                        mode = mode,
                        history = historyTurns,
                        prompt = turnPrompt,
                        timeoutMs = timeoutMs,
                        onChunk = onChunk
                    )
                    if (reply.isNotBlank()) {
                        finalReply = OfflineAiEngine.sanitizeIdentity(reply).trim()
                        break
                    }
                } catch (e: CancellationException) {
                    if (e !is TimeoutCancellationException) throw e
                    val classified = AiErrorClassifier.classify(e)
                    lastClassifiedError = classified
                    lastThrowable = e
                    safeLogW("Cloud AI timed out on $modelId (${classified.type})")
                    if (index < modelCandidates.lastIndex && AiErrorClassifier.isRetryableWithFallbackModel(classified.type)) {
                        onThinkingStep("🔄 Yedek hızlı modele geçiliyor...")
                    } else {
                        break
                    }
                } catch (cloudErr: Exception) {
                    val classified = AiErrorClassifier.classify(cloudErr)
                    lastClassifiedError = classified
                    lastThrowable = cloudErr
                    safeLogE("Cloud AI failed on $modelId [${classified.type}]: ${cloudErr.message}", cloudErr)
                    if (index < modelCandidates.lastIndex && AiErrorClassifier.isRetryableWithFallbackModel(classified.type)) {
                        onThinkingStep("🔄 Alternatif AI servisine bağlanılıyor...")
                    } else {
                        // Fail fast on offline, 403 App Check, or 400 content errors without stalling
                        break
                    }
                }
            }

            if (finalReply.isBlank()) {
                val errorToSurface = lastClassifiedError ?: AiClassifiedError(
                    type = AiErrorType.EMPTY_RESPONSE,
                    title = "Yanıt Alınamadı",
                    userMessage = "Yapay zekâ servisinden yanıt alınamadı. Bağlantınızı kontrol edip yeniden deneyin.",
                    isRetryable = true
                )
                return@withContext Result.failure(AiServiceException(errorToSurface, lastThrowable))
            }

            // 6. Save verified model reply to database
            val modelMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                content = finalReply,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(modelMsg)
            chatDao.touchSession(sessionId)

            Result.success(finalReply)
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            val classified = AiErrorClassifier.classify(e)
            Result.failure(AiServiceException(classified, e))
        } catch (e: Exception) {
            val classified = AiErrorClassifier.classify(e)
            safeLogE("Chat generation failed unexpectedly", e)
            Result.failure(AiServiceException(classified, e))
        }
    }

    /**
     * Explicit user-triggered offline fallback when cloud AI is unreachable (airplane mode / outage).
     */
    suspend fun generateOfflineFallbackReply(
        sessionId: String,
        prompt: String,
        mode: AiMode,
        chatContextSummary: String = ""
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val trimmed = prompt.trim()
            if (trimmed.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Mesaj boş olamaz."))
            }
            val offlineReply = OfflineAiEngine.generateResponse(
                prompt = trimmed,
                mode = mode,
                chatContextSummary = chatContextSummary,
                webResearch = null
            )
            val modelMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                content = offlineReply,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(modelMsg)
            chatDao.touchSession(sessionId)
            Result.success(offlineReply)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
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
