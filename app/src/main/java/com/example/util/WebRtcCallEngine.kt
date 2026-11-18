package com.example.util

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.data.CloudCallSession
import com.example.data.FirestoreChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.util.UUID

/**
 * Genuine Peer-to-Peer WebRTC Audio + Video Engine backed by:
 * - Free public STUN servers (Google + Cloudflare)
 * - Live Firestore Call & ICE Signaling (/calls/{callId})
 * - DTLS-SRTP Encrypted Media Streams
 */
object WebRtcCallEngine {
    private const val TAG = "WebRtcCallEngine"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var factory: PeerConnectionFactory? = null
    private var eglBase: EglBase? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackInternal: VideoTrack? = null

    private var activeCallJob: Job? = null
    private var iceJob: Job? = null
    private var activeCallId: String? = null
    private var isCallerSide: Boolean = true
    private var activeParticipants: List<String> = emptyList()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack = _remoteVideoTrack.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Hazır")
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _isCallEndedByRemote = MutableStateFlow(false)
    val isCallEndedByRemote = _isCallEndedByRemote.asStateFlow()

    fun getEglContext(): EglBase.Context? = eglBase?.eglBaseContext

    @Synchronized
    private fun ensureInitialized(context: Context) {
        if (eglBase == null) {
            eglBase = EglBase.create()
        }
        if (factory == null) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
            )
            val ctx = eglBase!!.eglBaseContext
            factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(ctx, true, true))
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(ctx))
                .createPeerConnectionFactory()
        }
    }

    private fun buildIceServers(): List<PeerConnection.IceServer> {
        return listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun.cloudflare.com:3478").createIceServer()
        )
    }

    fun startOutgoingCall(
        context: Context,
        calleeUid: String,
        calleeName: String,
        callerName: String,
        isVideo: Boolean,
        onError: (String) -> Unit = {}
    ): String {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(18)}"
        activeCallId = callId
        isCallerSide = true
        _isCallEndedByRemote.value = false
        _isConnected.value = false
        _connectionStatus.value = if (currentUid != null && calleeUid.isNotBlank() && calleeUid != currentUid) {
            "Çalıyor (WebRTC Bağlantısı Kuruluyor)..."
        } else {
            "Yerel Şifreli Arama Hazır (Google Bulut ile farklı cihazı ara)"
        }

        if (currentUid == null || calleeUid.isBlank() || calleeUid == currentUid) {
            // Setup local mic/camera preview even in local preview mode
            setupLocalMediaAndPeer(context, callId, isVideo, listOf(currentUid ?: "local"))
            return callId
        }

        val participants = listOf(currentUid, calleeUid)
        activeParticipants = participants
        val repo = FirestoreChatRepository.create(context)

        scope.launch {
            try {
                setupLocalMediaAndPeer(context, callId, isVideo, participants, repo)
                val pc = peerConnection ?: return@launch
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideo) "true" else "false"))
                }
                pc.createOffer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        pc.setLocalDescription(SimpleSdpObserver(), desc)
                        scope.launch {
                            repo.createOutgoingCall(
                                callId = callId,
                                calleeUid = calleeUid,
                                calleeName = calleeName,
                                callerName = callerName,
                                isVideo = isVideo,
                                offerSdp = desc.description
                            ).onFailure { err ->
                                _connectionStatus.value = "Sinyal Hatası: ${err.localizedMessage}"
                                onError(err.localizedMessage ?: "Arama sinyali gönderilemedi.")
                            }
                        }
                    }
                }, constraints)

                activeCallJob?.cancel()
                activeCallJob = scope.launch {
                    repo.observeCall(callId).collect { session ->
                        when (session?.status) {
                            "ANSWERED" -> {
                                val ans = session.answerSdp
                                if (!ans.isNullOrBlank() && pc.remoteDescription == null) {
                                    pc.setRemoteDescription(
                                        SimpleSdpObserver(),
                                        SessionDescription(SessionDescription.Type.ANSWER, ans)
                                    )
                                    _connectionStatus.value = "WebRTC DTLS-SRTP Bağlı"
                                }
                            }
                            "REJECTED", "ENDED" -> {
                                _connectionStatus.value = "Arama Sonlandı"
                                _isCallEndedByRemote.value = true
                            }
                        }
                    }
                }

                iceJob?.cancel()
                iceJob = scope.launch {
                    repo.observeIceCandidates(callId, readCallerCandidates = false).collect { cand ->
                        pc.addIceCandidate(IceCandidate(cand.sdpMid, cand.sdpMLineIndex, cand.sdp))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Outgoing call error", e)
                _connectionStatus.value = "Bağlantı Hatası: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "WebRTC başlatılamadı.")
            }
        }
        return callId
    }

    fun acceptIncomingCall(
        context: Context,
        session: CloudCallSession,
        onError: (String) -> Unit = {}
    ) {
        activeCallId = session.id
        isCallerSide = false
        activeParticipants = session.participantIds
        _isCallEndedByRemote.value = false
        _isConnected.value = false
        _connectionStatus.value = "Yanıtlanıyor..."

        val repo = FirestoreChatRepository.create(context)
        scope.launch {
            try {
                setupLocalMediaAndPeer(context, session.id, session.isVideo, session.participantIds, repo)
                val pc = peerConnection ?: return@launch
                pc.setRemoteDescription(
                    SimpleSdpObserver(),
                    SessionDescription(SessionDescription.Type.OFFER, session.offerSdp)
                )
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (session.isVideo) "true" else "false"))
                }
                pc.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        pc.setLocalDescription(SimpleSdpObserver(), desc)
                        scope.launch {
                            repo.answerCall(session.id, desc.description)
                                .onSuccess { _connectionStatus.value = "WebRTC DTLS-SRTP Bağlı" }
                                .onFailure { onError(it.localizedMessage ?: "Arama yanıtlanamadı.") }
                        }
                    }
                }, constraints)

                activeCallJob?.cancel()
                activeCallJob = scope.launch {
                    repo.observeCall(session.id).collect { updated ->
                        if (updated?.status == "ENDED" || updated?.status == "REJECTED") {
                            _connectionStatus.value = "Arama Sonlandı"
                            _isCallEndedByRemote.value = true
                        }
                    }
                }

                iceJob?.cancel()
                iceJob = scope.launch {
                    repo.observeIceCandidates(session.id, readCallerCandidates = true).collect { cand ->
                        pc.addIceCandidate(IceCandidate(cand.sdpMid, cand.sdpMLineIndex, cand.sdp))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Accept call error", e)
                onError(e.localizedMessage ?: "Arama kabul edilemedi.")
            }
        }
    }

    fun rejectCall(context: Context, callId: String) {
        scope.launch {
            try {
                FirestoreChatRepository.create(context).updateCallStatus(callId, "REJECTED")
            } catch (_: Exception) {}
        }
    }

    private fun setupLocalMediaAndPeer(
        context: Context,
        callId: String,
        isVideo: Boolean,
        participants: List<String>,
        repo: FirestoreChatRepository? = null
    ) {
        ensureInitialized(context)
        val pcf = factory ?: return

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = true
        } catch (_: Exception) {}

        val rtcConfig = PeerConnection.RTCConfiguration(buildIceServers()).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val pc = pcf.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                when (newState) {
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED -> {
                        _isConnected.value = true
                        _connectionStatus.value = "Canlı WebRTC DTLS-SRTP Bağlantısı Aktif"
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED,
                    PeerConnection.IceConnectionState.FAILED -> {
                        _connectionStatus.value = "Bağlantı zayıf / yeniden bağlanıyor..."
                    }
                    else -> {}
                }
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate) {
                if (repo != null && participants.size == 2) {
                    scope.launch {
                        repo.sendIceCandidate(
                            callId = callId,
                            isCaller = isCallerSide,
                            participantIds = participants,
                            sdpMid = candidate.sdpMid ?: "0",
                            sdpMLineIndex = candidate.sdpMLineIndex,
                            sdp = candidate.sdp
                        )
                    }
                }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(dc: org.webrtc.DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) {
                    track.setEnabled(true)
                    _remoteVideoTrack.value = track
                }
            }
        })
        peerConnection = pc

        try {
            val audioSource = pcf.createAudioSource(MediaConstraints())
            val audioTrack = pcf.createAudioTrack("ARDAMSa0", audioSource)
            audioTrack.setEnabled(true)
            localAudioTrack = audioTrack
            pc?.addTrack(audioTrack, listOf("ARDAMS"))
        } catch (e: Exception) {
            Log.w(TAG, "Audio capture unavailable on emulator/device: ${e.message}")
        }

        if (isVideo) {
            try {
                val capturer = createCameraCapturer(context)
                if (capturer != null) {
                    videoCapturer = capturer
                    val sth = SurfaceTextureHelper.create("CaptureThread", eglBase!!.eglBaseContext)
                    surfaceTextureHelper = sth
                    val videoSource = pcf.createVideoSource(capturer.isScreencast)
                    capturer.initialize(sth, context.applicationContext, videoSource.capturerObserver)
                    capturer.startCapture(640, 480, 30)
                    val vTrack = pcf.createVideoTrack("ARDAMSv0", videoSource)
                    vTrack.setEnabled(true)
                    localVideoTrackInternal = vTrack
                    _localVideoTrack.value = vTrack
                    pc?.addTrack(vTrack, listOf("ARDAMS"))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Camera capturer unavailable: ${e.message}")
            }
        }
    }

    private fun createCameraCapturer(context: Context): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        // Prefer front camera, fallback to any camera
        val front = deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
        if (front != null) {
            enumerator.createCapturer(front, null)?.let { return it }
        }
        val back = deviceNames.firstOrNull { enumerator.isBackFacing(it) }
        if (back != null) {
            enumerator.createCapturer(back, null)?.let { return it }
        }
        return null
    }

    fun setMicMuted(muted: Boolean) {
        try {
            localAudioTrack?.setEnabled(!muted)
        } catch (_: Exception) {}
    }

    fun setVideoEnabled(enabled: Boolean) {
        try {
            localVideoTrackInternal?.setEnabled(enabled)
        } catch (_: Exception) {}
    }

    fun switchCamera() {
        try {
            (videoCapturer as? CameraVideoCapturer)?.switchCamera(null)
        } catch (_: Exception) {}
    }

    fun setSpeakerphone(context: Context, enabled: Boolean) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.isSpeakerphoneOn = enabled
        } catch (_: Exception) {}
    }

    fun hangUp(context: Context) {
        val callId = activeCallId
        activeCallId = null
        activeCallJob?.cancel()
        iceJob?.cancel()
        activeCallJob = null
        iceJob = null

        if (callId != null && FirebaseAuth.getInstance().currentUser != null && activeParticipants.size == 2) {
            scope.launch {
                try {
                    FirestoreChatRepository.create(context).updateCallStatus(callId, "ENDED")
                } catch (_: Exception) {}
            }
        }

        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
        } catch (_: Exception) {}
        videoCapturer = null

        try {
            surfaceTextureHelper?.dispose()
        } catch (_: Exception) {}
        surfaceTextureHelper = null

        try {
            localVideoTrackInternal?.dispose()
            localAudioTrack?.dispose()
        } catch (_: Exception) {}
        localVideoTrackInternal = null
        localAudioTrack = null
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null

        try {
            peerConnection?.close()
            peerConnection?.dispose()
        } catch (_: Exception) {}
        peerConnection = null

        _isConnected.value = false
        _connectionStatus.value = "Hazır"
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
