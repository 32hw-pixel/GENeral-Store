package com.example.data

import com.example.ui.ChatViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class AiResponseAndErrorHandlingTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeChatDao : ChatDao {
        private val sessionsFlow = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
        private val messagesFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
        private var nextId = 1L

        override fun getAllSessions(): Flow<List<ChatSessionEntity>> =
            sessionsFlow.map { list -> list.sortedByDescending { it.updatedAt } }

        override suspend fun getSessionById(id: String): ChatSessionEntity? =
            sessionsFlow.value.firstOrNull { it.id == id }

        override suspend fun insertSession(session: ChatSessionEntity) {
            val updated = sessionsFlow.value.filterNot { it.id == session.id } + session
            sessionsFlow.value = updated
        }

        override suspend fun updateSessionTitle(id: String, title: String, updatedAt: Long) {
            sessionsFlow.value = sessionsFlow.value.map {
                if (it.id == id) it.copy(title = title, updatedAt = updatedAt) else it
            }
        }

        override suspend fun touchSession(id: String, updatedAt: Long) {
            sessionsFlow.value = sessionsFlow.value.map {
                if (it.id == id) it.copy(updatedAt = updatedAt) else it
            }
        }

        override suspend fun deleteSession(id: String) {
            sessionsFlow.value = sessionsFlow.value.filterNot { it.id == id }
        }

        override fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
            messagesFlow.map { list ->
                list.filter { it.sessionId == sessionId }.sortedBy { it.timestamp }
            }

        override suspend fun getMessagesForSessionList(sessionId: String): List<ChatMessageEntity> =
            messagesFlow.value.filter { it.sessionId == sessionId }.sortedBy { it.timestamp }

        override suspend fun insertMessage(message: ChatMessageEntity): Long {
            val id = if (message.id == 0L) nextId++ else message.id
            val entity = message.copy(id = id)
            messagesFlow.value = messagesFlow.value.filterNot { it.id == id } + entity
            return id
        }

        override suspend fun deleteMessagesForSession(sessionId: String) {
            messagesFlow.value = messagesFlow.value.filterNot { it.sessionId == sessionId }
        }

        override suspend fun clearAllMessages() {
            messagesFlow.value = emptyList()
        }

        override suspend fun clearAllSessions() {
            sessionsFlow.value = emptyList()
        }
    }

    @Test
    fun `error classifier maps network, timeout, quota, server, and app check errors cleanly and rethrows CancellationException`() {
        val offline = AiErrorClassifier.classify(UnknownHostException("Unable to resolve host"))
        assertEquals(AiErrorType.OFFLINE_OR_NETWORK, offline.type)
        assertTrue(offline.isRetryable)
        assertFalse(AiErrorClassifier.isRetryableWithFallbackModel(offline.type))
        assertTrue(offline.userMessage.contains("İnternet bağlantısı", ignoreCase = true))

        val timeout = AiErrorClassifier.classify(SocketTimeoutException("Read timed out"))
        assertEquals(AiErrorType.TIMEOUT, timeout.type)
        assertTrue(timeout.isRetryable)
        assertTrue(AiErrorClassifier.isRetryableWithFallbackModel(timeout.type))

        val quota = AiErrorClassifier.classify(RuntimeException("QuotaExceededException HTTP 429 RESOURCE_EXHAUSTED"))
        assertEquals(AiErrorType.QUOTA_EXCEEDED, quota.type)
        assertTrue(quota.isRetryable)

        val serverDown = AiErrorClassifier.classify(RuntimeException("ServerException 503 UNAVAILABLE"))
        assertEquals(AiErrorType.SERVER_UNAVAILABLE, serverDown.type)
        assertTrue(serverDown.isRetryable)

        val appCheck = AiErrorClassifier.classify(RuntimeException("HTTP 403 App Check attestation rejected"))
        assertEquals(AiErrorType.APP_CHECK_OR_AUTH, appCheck.type)
        assertFalse(appCheck.isRetryable)
        assertFalse(AiErrorClassifier.isRetryableWithFallbackModel(appCheck.type))

        var rethrewCancellation = false
        try {
            AiErrorClassifier.classify(CancellationException("Coroutine cancelled"))
        } catch (_: CancellationException) {
            rethrewCancellation = true
        }
        assertTrue(rethrewCancellation)
    }

    @Test
    fun `chat repository windows long history, streams sanitized chunks, and fails fast when offline`() = runTest {
        val dao = FakeChatDao()
        val recordedModels = mutableListOf<String>()
        var capturedHistorySize = -1

        val streamingClient = object : AiCloudClient {
            override suspend fun streamChat(
                modelName: String,
                mode: AiMode,
                history: List<AiTurnHistory>,
                prompt: String,
                timeoutMs: Long,
                onChunk: suspend (String) -> Unit
            ): String {
                recordedModels.add(modelName)
                capturedHistorySize = history.size
                onChunk("Merhaba, ben Gemini ")
                val full = "Merhaba, ben Gemini değil GEN AI!"
                onChunk(full)
                return full
            }
        }

        val repo = ChatRepository(dao, streamingClient)
        val session = repo.createNewSession(mode = AiMode.CODE)
        repeat(20) { idx ->
            dao.insertMessage(
                ChatMessageEntity(
                    sessionId = session.id,
                    role = if (idx % 2 == 0) "user" else "model",
                    content = "Geçmiş mesaj #$idx",
                    timestamp = 1000L + idx
                )
            )
        }

        val streamedChunks = mutableListOf<String>()
        val ok = repo.sendMessageStream(
            sessionId = session.id,
            userMessageText = "Kotlin örneği yaz",
            mode = AiMode.CODE,
            onChunk = { streamedChunks.add(it) }
        )

        assertTrue(ok.isSuccess)
        assertEquals(listOf(AiServiceConfig.PRIMARY_MODEL), recordedModels)
        assertEquals(AiServiceConfig.MAX_HISTORY_TURNS, capturedHistorySize)
        assertFalse(ok.getOrThrow().contains("Gemini", ignoreCase = true))
        assertTrue(ok.getOrThrow().contains("GEN AI"))

        // Offline error must fail fast (single call, no secondary model stall) and return AiServiceException
        recordedModels.clear()
        val offlineClient = object : AiCloudClient {
            override suspend fun streamChat(
                modelName: String,
                mode: AiMode,
                history: List<AiTurnHistory>,
                prompt: String,
                timeoutMs: Long,
                onChunk: suspend (String) -> Unit
            ): String {
                recordedModels.add(modelName)
                throw UnknownHostException("No network")
            }
        }
        val offlineRepo = ChatRepository(dao, offlineClient)
        val failRes = offlineRepo.sendMessageStream(
            sessionId = session.id,
            userMessageText = "Şimdi dene",
            mode = AiMode.GENERAL,
            onChunk = {}
        )
        assertTrue(failRes.isFailure)
        assertEquals(1, recordedModels.size) // Fail-fast without stalling on fallback model
        val ex = failRes.exceptionOrNull() as? AiServiceException
        assertNotNull(ex)
        assertEquals(AiErrorType.OFFLINE_OR_NETWORK, ex!!.classified.type)
    }

    @Test
    fun `chat repository transparently fails over to fast model on primary timeout`() = runTest {
        val dao = FakeChatDao()
        val triedModels = mutableListOf<String>()
        val client = object : AiCloudClient {
            override suspend fun streamChat(
                modelName: String,
                mode: AiMode,
                history: List<AiTurnHistory>,
                prompt: String,
                timeoutMs: Long,
                onChunk: suspend (String) -> Unit
            ): String {
                triedModels.add(modelName)
                if (modelName == AiServiceConfig.PRIMARY_MODEL) {
                    throw SocketTimeoutException("Primary timed out")
                }
                val reply = "Yedek hızlı model yanıtı"
                onChunk(reply)
                return reply
            }
        }

        val repo = ChatRepository(dao, client)
        val session = repo.createNewSession()
        val res = repo.sendMessageStream(
            sessionId = session.id,
            userMessageText = "Hızlı test",
            mode = AiMode.GENERAL,
            onChunk = {}
        )

        assertTrue(res.isSuccess)
        assertEquals("Yedek hızlı model yanıtı", res.getOrThrow())
        assertEquals(listOf(AiServiceConfig.PRIMARY_MODEL, AiServiceConfig.FAST_MODEL), triedModels)
    }

    @Test
    fun `chat viewmodel exposes Error state on connectivity failure and recovers cleanly on retry without duplicate user message`() = runTest {
        val dao = FakeChatDao()
        var shouldFail = true
        val toggleClient = object : AiCloudClient {
            override suspend fun streamChat(
                modelName: String,
                mode: AiMode,
                history: List<AiTurnHistory>,
                prompt: String,
                timeoutMs: Long,
                onChunk: suspend (String) -> Unit
            ): String {
                if (shouldFail) {
                    throw UnknownHostException("Offline")
                }
                val answer = "Bağlantı geri geldi, yanıt hazır!"
                onChunk(answer)
                return answer
            }
        }

        val repo = ChatRepository(dao, toggleClient, ioDispatcher = testDispatcher)
        val vm = ChatViewModel(repo)
        vm.startNewChat()
        advanceUntilIdle()

        vm.sendMessage(explicitPrompt = "Merhaba GEN AI")
        advanceUntilIdle()

        val errorState = vm.uiState.value as? AiUiState.Error
        assertNotNull(errorState)
        assertEquals(AiErrorType.OFFLINE_OR_NETWORK, errorState!!.error.type)
        assertEquals("Merhaba GEN AI", vm.lastFailedPrompt.value)
        assertNotNull(vm.errorBanner.value)

        // Recover connectivity and retry
        shouldFail = false
        vm.retryLastMessage()
        advanceUntilIdle()

        assertTrue(vm.uiState.value is AiUiState.Success)
        assertNull(vm.errorBanner.value)
        assertNull(vm.lastFailedPrompt.value)

        val sid = vm.activeSessionId.value!!
        val savedMsgs = dao.getMessagesForSessionList(sid)
        // Must have exactly 1 user prompt (not duplicated on retry) and 1 AI reply
        assertEquals(2, savedMsgs.size)
        assertEquals("user", savedMsgs[0].role)
        assertEquals("Merhaba GEN AI", savedMsgs[0].content)
        assertEquals("model", savedMsgs[1].role)
        assertEquals("Bağlantı geri geldi, yanıt hazır!", savedMsgs[1].content)
    }
}
