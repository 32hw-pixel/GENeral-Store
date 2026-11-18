package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.CloudFileTransferSession
import com.example.data.FirestoreChatRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Genuine Cross-Network Peer-to-Peer WebRTC DataChannel File Streaming Engine.
 * Streams 1.5 GB - 4 GB+ files directly between authenticated peers over DTLS-encrypted SCTP
 * using public STUN servers + Firestore signaling (/file_transfers), with zero RAM buffering
 * and automatic DataChannel backpressure control.
 */
object WebRtcFileTransferEngine {
    private const val TAG = "WebRtcFileXfer"
    private const val DATA_CHUNK_BYTES = 64 * 1024
    private const val MAX_BUFFERED_AMOUNT_BYTES = 2L * 1024L * 1024L
    private const val HANDSHAKE_TIMEOUT_MS = 25_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var factory: PeerConnectionFactory? = null
    private var hostListenerJob: Job? = null

    @Synchronized
    private fun ensureFactory(context: Context): PeerConnectionFactory {
        factory?.let { return it }
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        val created = PeerConnectionFactory.builder().createPeerConnectionFactory()
        factory = created
        return created
    }

    private fun buildIceServers(): List<PeerConnection.IceServer> = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun.cloudflare.com:3478").createIceServer()
    )

    fun startHostResponder(context: Context) {
        val appCtx = context.applicationContext
        if (hostListenerJob?.isActive == true) return
        hostListenerJob = scope.launch {
            UnlimitedFileTransferManager.restorePersistedFiles(appCtx)
            val repo = FirestoreChatRepository.create(appCtx)
            try {
                repo.observeIncomingFileTransferRequests().collect { request ->
                    launch {
                        serveIncomingRequest(appCtx, repo, request)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Host responder listener stopped: ${e.message}")
            }
        }
    }

    fun stopHostResponder() {
        hostListenerJob?.cancel()
        hostListenerJob = null
    }

    private suspend fun serveIncomingRequest(
        context: Context,
        repo: FirestoreChatRepository,
        session: CloudFileTransferSession
    ) {
        val localFile = UnlimitedFileTransferManager.resolveRegisteredFile(context, session.fileId)
        if (localFile == null || !localFile.exists() || !localFile.canRead()) {
            repo.updateFileTransferStatus(session.id, "FAILED")
            return
        }

        val pcf = ensureFactory(context)
        var pc: PeerConnection? = null
        var iceObserverJob: Job? = null
        var transferJob: Job? = null
        val completed = CompletableDeferred<Unit>()
        val closedFlag = AtomicBoolean(false)

        try {
            val rtcConfig = PeerConnection.RTCConfiguration(buildIceServers()).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }

            pc = pcf.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    if (newState == PeerConnection.IceConnectionState.FAILED ||
                        newState == PeerConnection.IceConnectionState.CLOSED
                    ) {
                        closedFlag.set(true)
                        if (!completed.isCompleted) {
                            completed.completeExceptionally(IllegalStateException("P2P bağlantısı koptu ($newState)"))
                        }
                    }
                }
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
                override fun onIceCandidate(candidate: IceCandidate) {
                    scope.launch {
                        repo.sendFileTransferIceCandidate(
                            transferId = session.id,
                            isRequester = false,
                            participantIds = session.participantIds,
                            sdpMid = candidate.sdpMid ?: "data",
                            sdpMLineIndex = candidate.sdpMLineIndex,
                            sdp = candidate.sdp
                        )
                    }
                }
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
                override fun onRenegotiationNeeded() {}
                override fun onDataChannel(dc: DataChannel?) {
                    if (dc == null) return
                    dc.registerObserver(object : DataChannel.Observer {
                        override fun onBufferedAmountChange(previousAmount: Long) {}
                        override fun onMessage(buffer: DataChannel.Buffer?) {}
                        override fun onStateChange() {
                            if (dc.state() == DataChannel.State.OPEN && transferJob == null) {
                                transferJob = scope.launch {
                                    streamFileOverDataChannel(localFile, dc, closedFlag)
                                        .onSuccess {
                                            repo.updateFileTransferStatus(session.id, "COMPLETED")
                                            completed.complete(Unit)
                                        }
                                        .onFailure { err ->
                                            repo.updateFileTransferStatus(session.id, "FAILED")
                                            completed.completeExceptionally(err)
                                        }
                                }
                            } else if (dc.state() == DataChannel.State.CLOSED) {
                                closedFlag.set(true)
                            }
                        }
                    })
                }
            }) ?: throw IllegalStateException("PeerConnection oluşturulamadı.")

            val remoteSet = CompletableDeferred<Unit>()
            pc.setRemoteDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    remoteSet.complete(Unit)
                }
                override fun onSetFailure(error: String?) {
                    remoteSet.completeExceptionally(IllegalStateException(error ?: "Remote SDP hatası"))
                }
            }, SessionDescription(SessionDescription.Type.OFFER, session.offerSdp))
            remoteSet.await()

            val answerDesc = CompletableDeferred<SessionDescription>()
            pc.createAnswer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription) {
                    answerDesc.complete(desc)
                }
                override fun onCreateFailure(error: String?) {
                    answerDesc.completeExceptionally(IllegalStateException(error ?: "Answer SDP hatası"))
                }
            }, MediaConstraints())
            val answer = answerDesc.await()

            val localSet = CompletableDeferred<Unit>()
            pc.setLocalDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    localSet.complete(Unit)
                }
                override fun onSetFailure(error: String?) {
                    localSet.completeExceptionally(IllegalStateException(error ?: "Local SDP hatası"))
                }
            }, answer)
            localSet.await()

            repo.answerFileTransferRequest(session.id, answer.description).getOrThrow()

            iceObserverJob = scope.launch {
                repo.observeFileTransferIceCandidates(session.id, readRequesterCandidates = true).collect { cand ->
                    pc.addIceCandidate(IceCandidate(cand.sdpMid, cand.sdpMLineIndex, cand.sdp))
                }
            }

            // Wait until file completes or stalls cleanly
            withTimeoutOrNull(60 * 60 * 1000L) {
                completed.await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed serving file ${session.fileName}: ${e.message}")
            repo.updateFileTransferStatus(session.id, "FAILED")
        } finally {
            closedFlag.set(true)
            transferJob?.cancel()
            iceObserverJob?.cancel()
            try {
                pc?.close()
                pc?.dispose()
            } catch (_: Exception) {}
        }
    }

    private suspend fun streamFileOverDataChannel(
        file: File,
        dc: DataChannel,
        closedFlag: AtomicBoolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileSize = file.length()
            val metaBytes = "META:$fileSize".toByteArray(StandardCharsets.UTF_8)
            dc.send(DataChannel.Buffer(ByteBuffer.wrap(metaBytes), false))

            FileInputStream(file).use { fis ->
                val buf = ByteArray(DATA_CHUNK_BYTES)
                while (!closedFlag.get() && dc.state() == DataChannel.State.OPEN) {
                    while (!closedFlag.get() &&
                        dc.state() == DataChannel.State.OPEN &&
                        dc.bufferedAmount() > MAX_BUFFERED_AMOUNT_BYTES
                    ) {
                        delay(15)
                    }
                    if (closedFlag.get() || dc.state() != DataChannel.State.OPEN) {
                        return@withContext Result.failure(IllegalStateException("Alıcı veri kanalını kapattı."))
                    }
                    val read = fis.read(buf)
                    if (read == -1) break
                    val payload = if (read == buf.size) buf.copyOf() else buf.copyOf(read)
                    val ok = dc.send(DataChannel.Buffer(ByteBuffer.wrap(payload), true))
                    if (!ok) {
                        delay(20)
                        if (!dc.send(DataChannel.Buffer(ByteBuffer.wrap(payload), true))) {
                            return@withContext Result.failure(IllegalStateException("Veri kanalı tamponu dolu."))
                        }
                    }
                }
            }

            while (!closedFlag.get() &&
                dc.state() == DataChannel.State.OPEN &&
                dc.bufferedAmount() > 0L
            ) {
                delay(20)
            }
            if (dc.state() == DataChannel.State.OPEN) {
                val eofBytes = "EOF".toByteArray(StandardCharsets.UTF_8)
                dc.send(DataChannel.Buffer(ByteBuffer.wrap(eofBytes), false))
                delay(200)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            try {
                if (dc.state() == DataChannel.State.OPEN) {
                    val errBytes = "ERR:${e.localizedMessage}".toByteArray(StandardCharsets.UTF_8)
                    dc.send(DataChannel.Buffer(ByteBuffer.wrap(errBytes), false))
                }
            } catch (_: Exception) {}
            Result.failure(e)
        }
    }

    suspend fun downloadFileOverP2P(
        context: Context,
        ownerUid: String,
        conversationId: String,
        messageId: String,
        fileId: String,
        fileName: String,
        expectedSizeBytes: Long,
        destFile: File,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val repo = FirestoreChatRepository.create(context)
        val currentUid = repo.currentUid()
            ?: return@withContext Result.failure(IllegalStateException("P2P indirme için Google ile giriş yapın."))
        if (ownerUid == currentUid || ownerUid.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Dosyanın göndericisi bu cihazda veya kimliği eksik."))
        }

        val transferId = "xfer_${UUID.randomUUID().toString().replace("-", "").take(18)}"
        val participants = listOf(currentUid, ownerUid).distinct()
        val tempFile = File(destFile.parentFile, "${destFile.name}.part")
        if (tempFile.exists()) tempFile.delete()

        val pcf = ensureFactory(context)
        var pc: PeerConnection? = null
        var dc: DataChannel? = null
        var sessionJob: Job? = null
        var iceJob: Job? = null
        val transferDone = CompletableDeferred<File>()
        var lastActivityMs = System.currentTimeMillis()

        try {
            val fos = BufferedOutputStream(FileOutputStream(tempFile), DATA_CHUNK_BYTES)
            var totalBytesExpected = expectedSizeBytes
            var bytesReceived = 0L
            var streamClosed = false

            fun closeFosSafely() {
                if (!streamClosed) {
                    streamClosed = true
                    try {
                        fos.flush()
                        fos.close()
                    } catch (_: Exception) {}
                }
            }

            val rtcConfig = PeerConnection.RTCConfiguration(buildIceServers()).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }

            pc = pcf.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    if (newState == PeerConnection.IceConnectionState.FAILED) {
                        closeFosSafely()
                        if (!transferDone.isCompleted) {
                            transferDone.completeExceptionally(
                                IllegalStateException("Gönderici cihazla P2P NAT bağlantısı kurulamadı.")
                            )
                        }
                    }
                }
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
                override fun onIceCandidate(candidate: IceCandidate) {
                    scope.launch {
                        repo.sendFileTransferIceCandidate(
                            transferId = transferId,
                            isRequester = true,
                            participantIds = participants,
                            sdpMid = candidate.sdpMid ?: "data",
                            sdpMLineIndex = candidate.sdpMLineIndex,
                            sdp = candidate.sdp
                        )
                    }
                }
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
                override fun onDataChannel(dcParam: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
            }) ?: throw IllegalStateException("WebRTC PeerConnection başlatılamadı.")

            val init = DataChannel.Init().apply {
                ordered = true
            }
            dc = pc.createDataChannel("file-xfer", init)
            dc.registerObserver(object : DataChannel.Observer {
                override fun onBufferedAmountChange(previousAmount: Long) {}
                override fun onStateChange() {}
                override fun onMessage(buffer: DataChannel.Buffer) {
                    lastActivityMs = System.currentTimeMillis()
                    val payload = ByteArray(buffer.data.remaining())
                    buffer.data.get(payload)
                    if (!buffer.binary) {
                        val ctrl = String(payload, StandardCharsets.UTF_8)
                        when {
                            ctrl.startsWith("META:") -> {
                                val len = ctrl.removePrefix("META:").toLongOrNull()
                                if (len != null && len > 0L) {
                                    totalBytesExpected = len
                                }
                            }
                            ctrl == "EOF" -> {
                                closeFosSafely()
                                if (tempFile.exists() && tempFile.length() > 0L) {
                                    if (destFile.exists()) destFile.delete()
                                    val moved = tempFile.renameTo(destFile)
                                    if (!moved) {
                                        tempFile.copyTo(destFile, overwrite = true)
                                        tempFile.delete()
                                    }
                                    onProgress(1f)
                                    if (!transferDone.isCompleted) {
                                        transferDone.complete(destFile)
                                    }
                                } else if (!transferDone.isCompleted) {
                                    transferDone.completeExceptionally(IllegalStateException("Aktarılan dosya boş."))
                                }
                            }
                            ctrl.startsWith("ERR:") -> {
                                closeFosSafely()
                                if (!transferDone.isCompleted) {
                                    transferDone.completeExceptionally(
                                        IllegalStateException(ctrl.removePrefix("ERR:"))
                                    )
                                }
                            }
                        }
                    } else {
                        try {
                            fos.write(payload)
                            bytesReceived += payload.size
                            if (totalBytesExpected > 0L) {
                                val pct = (bytesReceived.toFloat() / totalBytesExpected.toFloat()).coerceIn(0.02f, 0.99f)
                                onProgress(pct)
                            }
                        } catch (ioe: Exception) {
                            closeFosSafely()
                            if (!transferDone.isCompleted) {
                                transferDone.completeExceptionally(ioe)
                            }
                        }
                    }
                }
            })

            val offerDesc = CompletableDeferred<SessionDescription>()
            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription) {
                    offerDesc.complete(desc)
                }
                override fun onCreateFailure(error: String?) {
                    offerDesc.completeExceptionally(IllegalStateException(error ?: "Offer SDP hatası"))
                }
            }, MediaConstraints())
            val offer = offerDesc.await()

            val localSet = CompletableDeferred<Unit>()
            pc.setLocalDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    localSet.complete(Unit)
                }
                override fun onSetFailure(error: String?) {
                    localSet.completeExceptionally(IllegalStateException(error ?: "Local SDP hatası"))
                }
            }, offer)
            localSet.await()

            repo.createFileTransferRequest(
                transferId = transferId,
                ownerUid = ownerUid,
                conversationId = conversationId,
                messageId = messageId,
                fileId = fileId,
                fileName = fileName,
                fileSizeBytes = expectedSizeBytes.coerceAtLeast(1L),
                offerSdp = offer.description
            ).getOrThrow()

            sessionJob = scope.launch {
                repo.observeFileTransferSession(transferId).collect { updated ->
                    when (updated?.status) {
                        "ANSWERED" -> {
                            val ans = updated.answerSdp
                            if (!ans.isNullOrBlank() && pc.remoteDescription == null) {
                                pc.setRemoteDescription(
                                    SimpleSdpObserver(),
                                    SessionDescription(SessionDescription.Type.ANSWER, ans)
                                )
                            }
                        }
                        "FAILED" -> {
                            closeFosSafely()
                            if (!transferDone.isCompleted) {
                                transferDone.completeExceptionally(
                                    IllegalStateException("Gönderici cihazda dosya bulunamadı veya aktarım reddedildi.")
                                )
                            }
                        }
                    }
                }
            }

            iceJob = scope.launch {
                repo.observeFileTransferIceCandidates(transferId, readRequesterCandidates = false).collect { cand ->
                    pc.addIceCandidate(IceCandidate(cand.sdpMid, cand.sdpMLineIndex, cand.sdp))
                }
            }

            // Watchdog for offline sender or stalled stream
            val watchdogJob = scope.launch {
                val startTime = System.currentTimeMillis()
                while (!transferDone.isCompleted) {
                    delay(1000)
                    val now = System.currentTimeMillis()
                    if (bytesReceived == 0L && now - startTime > HANDSHAKE_TIMEOUT_MS) {
                        closeFosSafely()
                        if (!transferDone.isCompleted) {
                            transferDone.completeExceptionally(
                                IllegalStateException("Gönderici cihaz çevrimdışı veya dosyayı paylaşamıyor.")
                            )
                        }
                        break
                    }
                    if (bytesReceived > 0L && now - lastActivityMs > 45_000L) {
                        closeFosSafely()
                        if (!transferDone.isCompleted) {
                            transferDone.completeExceptionally(
                                IllegalStateException("P2P aktarım zaman aşımına uğradı.")
                            )
                        }
                        break
                    }
                }
            }

            try {
                val file = transferDone.await()
                Result.success(file)
            } finally {
                watchdogJob.cancel()
                closeFosSafely()
            }
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            Result.failure(e)
        } finally {
            sessionJob?.cancel()
            iceJob?.cancel()
            if (tempFile.exists()) tempFile.delete()
            try {
                dc?.close()
                pc?.close()
                pc?.dispose()
            } catch (_: Exception) {}
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {
            Log.e(TAG, "SDP create failure: $error")
        }
        override fun onSetFailure(error: String?) {
            Log.e(TAG, "SDP set failure: $error")
        }
    }
}
