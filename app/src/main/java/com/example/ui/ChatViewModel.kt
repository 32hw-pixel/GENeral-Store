package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AiClassifiedError
import com.example.data.AiErrorClassifier
import com.example.data.AiMode
import com.example.data.AiServiceConfig
import com.example.data.AiServiceException
import com.example.data.AiUiState
import com.example.data.ChatMessageEntity
import com.example.data.ChatRepository
import com.example.data.ChatSessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val currentUserId: Long? = null
) : ViewModel() {

    val sessions: StateFlow<List<ChatSessionEntity>> = chatRepository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = _activeSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) {
                flowOf(emptyList())
            } else {
                chatRepository.getMessagesForSession(sessionId)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val inputText = MutableStateFlow("")
    val selectedMode = MutableStateFlow(AiMode.GENERAL)

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _thinkingStep = MutableStateFlow<String?>(null)
    val thinkingStep = _thinkingStep.asStateFlow()

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText = _streamingText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _errorBanner = MutableStateFlow<AiClassifiedError?>(null)
    val errorBanner = _errorBanner.asStateFlow()

    private val _lastFailedPrompt = MutableStateFlow<String?>(null)
    val lastFailedPrompt = _lastFailedPrompt.asStateFlow()

    private var activeJob: Job? = null

    init {
        // Automatically select the latest session if available, or create a starter one
        viewModelScope.launch {
            chatRepository.allSessions.collect { sessionList ->
                if (_activeSessionId.value == null && sessionList.isNotEmpty()) {
                    _activeSessionId.value = sessionList.first().id
                    selectedMode.value = AiMode.fromId(sessionList.first().mode)
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        val session = sessions.value.find { it.id == sessionId }
        if (session != null) {
            selectedMode.value = AiMode.fromId(session.mode)
        }
        _streamingText.value = null
        _thinkingStep.value = null
        _errorMessage.value = null
        _errorBanner.value = null
        _lastFailedPrompt.value = null
        _uiState.value = AiUiState.Idle
    }

    fun loadSession(sessionId: String) = selectSession(sessionId)

    fun startNewChat(mode: AiMode = selectedMode.value) {
        viewModelScope.launch {
            val newSession = chatRepository.createNewSession(currentUserId, mode)
            _activeSessionId.value = newSession.id
            selectedMode.value = mode
            _streamingText.value = null
            _thinkingStep.value = null
            _errorMessage.value = null
            _errorBanner.value = null
            _lastFailedPrompt.value = null
            _uiState.value = AiUiState.Idle
            inputText.value = ""
        }
    }

    fun setMode(mode: AiMode) {
        selectedMode.value = mode
    }

    fun sendMessage(explicitPrompt: String? = null, chatContextSummary: String = "") {
        val prompt = (explicitPrompt ?: inputText.value).trim()
        if (prompt.isBlank() || _isGenerating.value) return

        if (explicitPrompt == null) {
            inputText.value = ""
        }
        _errorMessage.value = null
        _errorBanner.value = null
        _lastFailedPrompt.value = null

        activeJob = viewModelScope.launch {
            val startedAtMs = System.currentTimeMillis()
            var sessionId = _activeSessionId.value
            if (sessionId == null) {
                val newSession = chatRepository.createNewSession(currentUserId, selectedMode.value)
                sessionId = newSession.id
                _activeSessionId.value = sessionId
            }

            _isGenerating.value = true
            _thinkingStep.value = "⚡ GEN AI hazırlanıyor..."
            _streamingText.value = null
            _uiState.value = AiUiState.Thinking("⚡ GEN AI hazırlanıyor...")

            val result = chatRepository.sendMessageStream(
                sessionId = sessionId,
                userMessageText = prompt,
                mode = selectedMode.value,
                chatContextSummary = chatContextSummary,
                onThinkingStep = { step ->
                    _thinkingStep.value = step
                    _uiState.value = AiUiState.Thinking(step)
                },
                onChunk = { updatedText ->
                    _thinkingStep.value = null
                    _streamingText.value = updatedText
                    _uiState.value = AiUiState.Streaming(updatedText)
                }
            )

            _isGenerating.value = false
            _thinkingStep.value = null
            _streamingText.value = null

            result.onSuccess { reply ->
                val elapsed = (System.currentTimeMillis() - startedAtMs).coerceAtLeast(1L)
                _lastFailedPrompt.value = null
                _uiState.value = AiUiState.Success(
                    text = reply,
                    modelUsed = AiServiceConfig.PRIMARY_MODEL,
                    latencyMs = elapsed
                )
            }.onFailure { error ->
                val classified = (error as? AiServiceException)?.classified ?: AiErrorClassifier.classify(error)
                _errorBanner.value = classified
                _errorMessage.value = classified.userMessage
                _lastFailedPrompt.value = prompt
                _uiState.value = AiUiState.Error(
                    error = classified,
                    failedPrompt = prompt,
                    canUseOfflineFallback = true
                )
            }
        }
    }

    fun retryLastMessage(chatContextSummary: String = "") {
        val retryPrompt = _lastFailedPrompt.value?.trim().orEmpty()
        if (retryPrompt.isBlank() || _isGenerating.value) return
        sendMessage(explicitPrompt = retryPrompt, chatContextSummary = chatContextSummary)
    }

    fun useOfflineFallback(chatContextSummary: String = "") {
        val prompt = _lastFailedPrompt.value?.trim().orEmpty()
        val sessionId = _activeSessionId.value ?: return
        if (prompt.isBlank() || _isGenerating.value) return

        activeJob = viewModelScope.launch {
            val startedAtMs = System.currentTimeMillis()
            _isGenerating.value = true
            _errorBanner.value = null
            _errorMessage.value = null
            val res = chatRepository.generateOfflineFallbackReply(
                sessionId = sessionId,
                prompt = prompt,
                mode = selectedMode.value,
                chatContextSummary = chatContextSummary
            )
            _isGenerating.value = false
            res.onSuccess { reply ->
                _lastFailedPrompt.value = null
                _uiState.value = AiUiState.Success(
                    text = reply,
                    modelUsed = "offline-engine",
                    latencyMs = (System.currentTimeMillis() - startedAtMs).coerceAtLeast(1L)
                )
            }.onFailure { err ->
                val classified = AiErrorClassifier.classify(err)
                _errorBanner.value = classified
                _errorMessage.value = classified.userMessage
                _uiState.value = AiUiState.Error(classified, failedPrompt = prompt)
            }
        }
    }

    fun cancelGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
        _thinkingStep.value = null
        _streamingText.value = null
        _uiState.value = AiUiState.Idle
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                _activeSessionId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            chatRepository.clearAllSessions()
            _activeSessionId.value = null
            _errorBanner.value = null
            _errorMessage.value = null
            _lastFailedPrompt.value = null
            _uiState.value = AiUiState.Idle
        }
    }

    fun clearCurrentSession() {
        activeJob?.cancel()
        _isGenerating.value = false
        _streamingText.value = null
        _errorMessage.value = null
        _errorBanner.value = null
        _lastFailedPrompt.value = null
        _uiState.value = AiUiState.Idle
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            chatRepository.clearSessionMessages(sid)
        }
    }

    fun buildExportPayload(formatJson: Boolean): String {
        val session = sessions.value.firstOrNull { it.id == _activeSessionId.value }
        val msgList = messages.value
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return if (formatJson) {
            val root = org.json.JSONObject()
            root.put("app", "GENeral Chat - GEN AI")
            root.put("sessionTitle", session?.title ?: "Yeni Sohbet")
            root.put("mode", selectedMode.value.title)
            root.put("exportedAt", sdf.format(java.util.Date()))
            val arr = org.json.JSONArray()
            msgList.forEach { msg ->
                val item = org.json.JSONObject()
                item.put("sender", if (msg.role == "user") "Siz" else "GEN AI")
                item.put("role", msg.role)
                item.put("time", sdf.format(java.util.Date(msg.timestamp)))
                item.put("content", msg.content)
                arr.put(item)
            }
            root.put("messages", arr)
            root.toString(2)
        } else {
            buildString {
                appendLine("=== GENeral Chat — GEN AI Sohbet Dışa Aktarımı ===")
                appendLine("Oturum: ${session?.title ?: "Yeni Sohbet"}")
                appendLine("Mod: ${selectedMode.value.title}")
                appendLine("Dışa Aktarma Tarihi: ${sdf.format(java.util.Date())}")
                appendLine("====================================================")
                appendLine()
                if (msgList.isEmpty()) {
                    appendLine("(Bu oturumda henüz mesaj yok.)")
                } else {
                    msgList.forEach { msg ->
                        val who = if (msg.role == "user") "Siz" else "GEN AI"
                        val timeStr = sdf.format(java.util.Date(msg.timestamp))
                        appendLine("[$timeStr] $who:")
                        appendLine(msg.content)
                        appendLine()
                    }
                }
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
        _errorBanner.value = null
        if (_uiState.value is AiUiState.Error) {
            _uiState.value = AiUiState.Idle
        }
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val currentUserId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
