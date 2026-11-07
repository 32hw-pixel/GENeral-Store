package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AiMode
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

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText = _streamingText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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
        _errorMessage.value = null
    }

    fun loadSession(sessionId: String) = selectSession(sessionId)

    fun startNewChat(mode: AiMode = selectedMode.value) {
        viewModelScope.launch {
            val newSession = chatRepository.createNewSession(currentUserId, mode)
            _activeSessionId.value = newSession.id
            selectedMode.value = mode
            _streamingText.value = null
            _errorMessage.value = null
            inputText.value = ""
        }
    }

    fun setMode(mode: AiMode) {
        selectedMode.value = mode
        val curSessionId = _activeSessionId.value
        if (curSessionId != null && messages.value.isEmpty()) {
            // If the chat hasn't started yet, switch the mode on this session
            // We can also create a new one if preferred
        }
    }

    fun sendMessage(explicitPrompt: String? = null) {
        val prompt = (explicitPrompt ?: inputText.value).trim()
        if (prompt.isBlank() || _isGenerating.value) return

        inputText.value = ""
        _errorMessage.value = null

        activeJob = viewModelScope.launch {
            var sessionId = _activeSessionId.value
            if (sessionId == null) {
                val newSession = chatRepository.createNewSession(currentUserId, selectedMode.value)
                sessionId = newSession.id
                _activeSessionId.value = sessionId
            }

            _isGenerating.value = true
            _streamingText.value = ""

            val result = chatRepository.sendMessageStream(
                sessionId = sessionId,
                userMessageText = prompt,
                mode = selectedMode.value
            ) { updatedText ->
                _streamingText.value = updatedText
            }

            _isGenerating.value = false
            _streamingText.value = null

            result.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Yapay zeka yanıt oluştururken bir sorunla karşılaştı."
            }
        }
    }

    fun cancelGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
        _streamingText.value = null
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
        }
    }

    fun dismissError() {
        _errorMessage.value = null
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
