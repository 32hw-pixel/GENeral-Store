package com.example.data

import android.util.Log
import com.example.util.OfflineAiEngine
import com.example.util.WebResearchResult
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

enum class AiErrorType {
    TIMEOUT,
    OFFLINE_OR_NETWORK,
    SERVER_UNAVAILABLE,
    QUOTA_EXCEEDED,
    APP_CHECK_OR_AUTH,
    CONTENT_BLOCKED_OR_INVALID,
    EMPTY_RESPONSE,
    UNKNOWN
}

data class AiClassifiedError(
    val type: AiErrorType,
    val title: String,
    val userMessage: String,
    val isRetryable: Boolean,
    val technicalDetail: String? = null
)

class AiServiceException(
    val classified: AiClassifiedError,
    cause: Throwable? = null
) : Exception(classified.userMessage, cause)

sealed interface AiUiState {
    data object Idle : AiUiState
    data class Thinking(val step: String) : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Success(val text: String, val modelUsed: String, val latencyMs: Long) : AiUiState
    data class Error(
        val error: AiClassifiedError,
        val failedPrompt: String? = null,
        val canUseOfflineFallback: Boolean = true
    ) : AiUiState
}

data class AiTurnHistory(
    val role: String,
    val text: String
)

interface AiCloudClient {
    suspend fun streamChat(
        modelName: String,
        mode: AiMode,
        history: List<AiTurnHistory>,
        prompt: String,
        timeoutMs: Long,
        onChunk: suspend (String) -> Unit
    ): String
}

object AiServiceConfig {
    const val PRIMARY_MODEL = "gemini-3.8-flash"
    const val FAST_MODEL = "gemini-3.5-flash-lite"
    const val PRIMARY_TIMEOUT_MS = 45_000L
    const val FAST_TIMEOUT_MS = 30_000L
    const val WEB_RESEARCH_BUDGET_MS = 1_800L
    const val MAX_HISTORY_TURNS = 12
    const val MAX_HISTORY_TURN_CHARS = 1_200
    const val STREAM_THROTTLE_MS = 36L
    const val STREAM_MIN_CHAR_DELTA = 16
}

object FirebaseAiCloudClient : AiCloudClient {
    private const val TAG = "FirebaseAiCloudClient"

    // Warm lazy default model references (evaluated on first inference, never at construction time)
    private val defaultPrimaryModel: GenerativeModel by lazy {
        createGenerativeModel(AiServiceConfig.PRIMARY_MODEL, AiMode.GENERAL)
    }

    private val defaultFastModel: GenerativeModel by lazy {
        createGenerativeModel(AiServiceConfig.FAST_MODEL, AiMode.GENERAL)
    }

    // Lazily cached GenerativeModel instances per (modelName, modeId) to avoid per-turn SDK recreation overhead
    private val modelCache = ConcurrentHashMap<String, GenerativeModel>()

    private fun getOrCreateModel(modelName: String, mode: AiMode): GenerativeModel {
        if (mode == AiMode.GENERAL) {
            if (modelName == AiServiceConfig.PRIMARY_MODEL) return defaultPrimaryModel
            if (modelName == AiServiceConfig.FAST_MODEL) return defaultFastModel
        }
        val cacheKey = "$modelName:${mode.id}"
        return modelCache.getOrPut(cacheKey) {
            createGenerativeModel(modelName, mode)
        }
    }

    private fun createGenerativeModel(modelName: String, mode: AiMode): GenerativeModel {
        val systemText = buildString {
            appendLine(mode.systemPrompt)
            appendLine("KİMLİK KURALI: Adın yalnızca 'GEN AI'. Hiçbir koşulda kendine Gemini veya başka bir marka adı deme.")
        }
        return Firebase.ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = if (mode == AiMode.CODE || mode == AiMode.MATH) 0.35f else 0.7f
                topP = 0.9f
            },
            systemInstruction = content { text(systemText) }
        )
    }

    override suspend fun streamChat(
        modelName: String,
        mode: AiMode,
        history: List<AiTurnHistory>,
        prompt: String,
        timeoutMs: Long,
        onChunk: suspend (String) -> Unit
    ): String {
        val model = getOrCreateModel(modelName, mode)
        val windowedHistory: List<Content> = history
            .takeLast(AiServiceConfig.MAX_HISTORY_TURNS)
            .mapNotNull { turn ->
                val clean = turn.text.trim()
                if (clean.isEmpty()) return@mapNotNull null
                val mappedRole = if (turn.role == "user") "user" else "model"
                content(mappedRole) {
                    text(clean.take(AiServiceConfig.MAX_HISTORY_TURN_CHARS))
                }
            }

        val chatSession = model.startChat(history = windowedHistory)
        val accumulated = StringBuilder()
        var lastEmitAtMs = 0L
        var lastEmitLength = 0

        withTimeout(timeoutMs) {
            chatSession.sendMessageStream(prompt).collect { chunk ->
                val textPart = chunk.text.orEmpty()
                if (textPart.isNotEmpty()) {
                    accumulated.append(textPart)
                    val now = System.currentTimeMillis()
                    val currentLen = accumulated.length
                    val isFirstToken = lastEmitLength == 0
                    val enoughDelta = (currentLen - lastEmitLength) >= AiServiceConfig.STREAM_MIN_CHAR_DELTA
                    val enoughTime = (now - lastEmitAtMs) >= AiServiceConfig.STREAM_THROTTLE_MS
                    if (isFirstToken || enoughDelta || enoughTime) {
                        val sanitized = OfflineAiEngine.sanitizeIdentity(accumulated.toString())
                        onChunk(sanitized)
                        lastEmitAtMs = now
                        lastEmitLength = currentLen
                    }
                }
            }
        }

        val finalReply = OfflineAiEngine.sanitizeIdentity(accumulated.toString()).trim()
        if (finalReply.isBlank()) {
            Log.w(TAG, "Empty response returned from $modelName")
            throw IllegalStateException("EMPTY_AI_RESPONSE")
        }
        onChunk(finalReply)
        return finalReply
    }

    fun buildTurnPromptEnvelope(
        userPrompt: String,
        chatContextSummary: String = "",
        webResearch: WebResearchResult? = null
    ): String {
        val hasContext = chatContextSummary.isNotBlank()
        val hasResearch = webResearch != null && webResearch.succeeded && webResearch.summary.isNotBlank()
        if (!hasContext && !hasResearch) return userPrompt

        return buildString {
            if (hasContext) {
                appendLine("[SOHBET BAĞLAMI]")
                appendLine(chatContextSummary.trim().take(1_400))
                appendLine()
            }
            if (hasResearch && webResearch != null) {
                appendLine("[GÜNCEL İNTERNET ARAŞTIRMASI — ${webResearch.query}]")
                appendLine(webResearch.summary.trim().take(1_200))
                if (webResearch.sourceUrls.isNotEmpty()) {
                    appendLine("Kaynaklar: ${webResearch.sourceUrls.joinToString(", ")}")
                }
                appendLine()
            }
            appendLine("[KULLANICI SORUSU]")
            append(userPrompt)
        }
    }
}

object AiErrorClassifier {
    private const val TAG = "GEN_AI_Error"

    private fun logError(message: String, error: Throwable) {
        try {
            Log.e(TAG, message, error)
        } catch (_: Throwable) {
            // Safe no-op in plain JVM tests without Android Log mock
        }
    }

    fun isRetryableWithFallbackModel(type: AiErrorType): Boolean {
        return when (type) {
            AiErrorType.TIMEOUT,
            AiErrorType.SERVER_UNAVAILABLE,
            AiErrorType.QUOTA_EXCEEDED,
            AiErrorType.EMPTY_RESPONSE,
            AiErrorType.UNKNOWN -> true
            AiErrorType.OFFLINE_OR_NETWORK,
            AiErrorType.APP_CHECK_OR_AUTH,
            AiErrorType.CONTENT_BLOCKED_OR_INVALID -> false
        }
    }

    fun classify(error: Throwable): AiClassifiedError {
        if (error is CancellationException && error !is TimeoutCancellationException) {
            throw error
        }

        if (error is AiServiceException) {
            return error.classified
        }

        val causes = generateSequence(error) { it.cause }.toList()
        val combinedText = causes.joinToString(" | ") {
            "${it::class.simpleName.orEmpty()}: ${it.message.orEmpty()}"
        }.lowercase()

        val result = when {
            causes.any { it is TimeoutCancellationException || it is SocketTimeoutException } ||
                combinedText.contains("requesttimeout") ||
                combinedText.contains("timed out") ||
                combinedText.contains("timeout") -> {
                AiClassifiedError(
                    type = AiErrorType.TIMEOUT,
                    title = "Yanıt Zaman Aşımı",
                    userMessage = "Yapay zekâ sunucusu yanıt süresini aştı. İnternet bağlantınızı kontrol edip tekrar deneyin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }

            causes.any {
                it is UnknownHostException ||
                    it is ConnectException ||
                    it is NoRouteToHostException ||
                    it is IOException
            } ||
                combinedText.contains("unable to resolve host") ||
                combinedText.contains("failed to connect") ||
                combinedText.contains("network") ||
                combinedText.contains("socket") ||
                combinedText.contains("unreachable") ||
                combinedText.contains("no address associated") -> {
                AiClassifiedError(
                    type = AiErrorType.OFFLINE_OR_NETWORK,
                    title = "Bağlantı Kurulamadı",
                    userMessage = "İnternet bağlantısı kurulamadı veya bulut sunucusuna erişilemiyor. Ağınızı kontrol edip tekrar deneyin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }

            combinedText.contains("quotaexceeded") ||
                combinedText.contains("resource_exhausted") ||
                combinedText.contains("429") ||
                combinedText.contains("quota") ||
                combinedText.contains("rate limit") -> {
                AiClassifiedError(
                    type = AiErrorType.QUOTA_EXCEEDED,
                    title = "Servis Kotası Yoğun",
                    userMessage = "Yapay zekâ servis kotası geçici olarak doldu. Kısa bir süre bekleyip yeniden deneyin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }

            combinedText.contains("app check") ||
                combinedText.contains("appcheck") ||
                combinedText.contains("attestation") ||
                combinedText.contains("403") ||
                combinedText.contains("permission_denied") ||
                combinedText.contains("invalidapikey") ||
                combinedText.contains("servicedisabled") ||
                combinedText.contains("unauthorized") ||
                combinedText.contains("401") -> {
                AiClassifiedError(
                    type = AiErrorType.APP_CHECK_OR_AUTH,
                    title = "Servis Doğrulama Hatası",
                    userMessage = "Uygulama güvenlik doğrulaması (App Check) reddedildi. Lütfen güncel sürümle tekrar deneyin.",
                    isRetryable = false,
                    technicalDetail = error.message
                )
            }

            combinedText.contains("promptblocked") ||
                combinedText.contains("responsestopped") ||
                combinedText.contains("safety") ||
                combinedText.contains("invalid_argument") ||
                combinedText.contains("bad request") ||
                combinedText.contains("400") -> {
                AiClassifiedError(
                    type = AiErrorType.CONTENT_BLOCKED_OR_INVALID,
                    title = "İstek İşlenemedi",
                    userMessage = "Sorunuz güvenlik politikası veya geçersiz biçim nedeniyle sunucu tarafından reddedildi. Farklı bir ifadeyle tekrar deneyin.",
                    isRetryable = false,
                    technicalDetail = error.message
                )
            }

            combinedText.contains("serverexception") ||
                combinedText.contains("500") ||
                combinedText.contains("502") ||
                combinedText.contains("503") ||
                combinedText.contains("504") ||
                combinedText.contains("unavailable") ||
                combinedText.contains("overloaded") ||
                combinedText.contains("internal") -> {
                AiClassifiedError(
                    type = AiErrorType.SERVER_UNAVAILABLE,
                    title = "Sunucu Geçici Olarak Kullanılamıyor",
                    userMessage = "Yapay zekâ sunucusu şu anda yoğun veya geçici olarak hizmet dışı. Birkaç saniye sonra tekrar deneyin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }

            combinedText.contains("empty_ai_response") -> {
                AiClassifiedError(
                    type = AiErrorType.EMPTY_RESPONSE,
                    title = "Boş Yanıt Alındı",
                    userMessage = "Yapay zekâ servisinden boş yanıt döndü. Lütfen sorunuzu yeniden gönderin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }

            else -> {
                AiClassifiedError(
                    type = AiErrorType.UNKNOWN,
                    title = "Bulut Servis Hatası",
                    userMessage = "Yapay zekâ servisiyle iletişim sırasında beklenmeyen bir sorun oluştu. Bağlantınızı kontrol edip tekrar deneyin.",
                    isRetryable = true,
                    technicalDetail = error.message
                )
            }
        }

        logError("Classified AI error [${result.type.name}]: ${error.message}", error)
        return result
    }
}
