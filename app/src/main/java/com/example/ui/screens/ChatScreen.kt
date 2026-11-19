package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import coil.compose.AsyncImage
import com.example.ui.components.ChatWallpaperBackground
import com.example.ui.components.CommunitiesView
import com.example.ui.components.CreateGroupDialog
import com.example.ui.components.CyberWidgetDialog
import com.example.ui.components.CamouflageAppIdentityDialog
import com.example.ui.components.FloatingCyberWidget
import com.example.ui.components.FriendContact
import com.example.ui.components.GeminiAiHistoryDialog
import com.example.ui.components.OfflineP2PTunnelDialog
import com.example.ui.components.ServiceStatusDashboardCard
import com.example.ui.components.ThemePickerDialog
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalAppThemeMode
import com.example.util.ChatContextCache
import com.example.util.GenAiSpeechManager
import com.example.util.LocalMeshServer
import com.example.util.SelfChatStorage
import com.example.util.UnlimitedFileTransferManager
import com.example.util.WebRtcCallEngine
import com.example.util.WebRtcFileTransferEngine
import com.example.util.rememberSpeechToTextLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.AiMode
import com.example.data.CallHistoryItem
import com.example.data.CallType
import com.example.data.ChatMessageEntity
import com.example.data.CloudCallSession
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.data.FirestoreChatRepository
import com.example.data.MaskType
import com.example.data.UserEntity
import com.example.data.defaultCallHistory
import com.example.data.defaultConversations
import com.example.data.formatClockTime
import com.example.data.formatConversationListTime
import com.example.data.toConversationItem
import com.example.ui.AuthViewModel
import com.example.ui.ChatViewModel
import com.example.ui.components.GeneralChatLogo
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.CyberYellowContainer
import com.example.ui.theme.CyberYellowLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.InstantNotificationManager
import com.example.util.SoundEffectsManager
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import kotlin.math.roundToInt

private data class ActiveWebRtcCallState(
    val contactName: String,
    val calleeUid: String,
    val isVideo: Boolean,
    val isIncoming: Boolean = false,
    val incomingSession: CloudCallSession? = null,
    val startedAtMs: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    currentUser: UserEntity?,
    isPaperMode: Boolean,
    onTogglePaperMode: () -> Unit,
    onTriggerPanic: () -> Unit,
    onTriggerGlobalPanic: () -> Unit = {},
    onSelectMask: (MaskType) -> Unit,
    onOpenChannel: () -> Unit,
    onOpenAppLock: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenShowcase: () -> Unit = {},
    onOpenMiniGames: () -> Unit = {},
    onReplaySplash: () -> Unit = {},
    onWipeAllData: () -> Unit = {},
    onDeleteCloudAccount: (((Result<Unit>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 5 Navigation Tabs: 0: Sohbetler, 1: Topluluklar, 2: Aramalar, 3: GEN AI, 4: Ayarlar
    val selectedTab by authViewModel.selectedMainTab.collectAsState()
    val strings = com.example.util.LocalAppStrings.current

    // Cross-Device Internet Cloud State
    val context = LocalContext.current
    val cloudUser by authViewModel.firebaseUser.collectAsState()
    val cloudProfile by authViewModel.cloudProfile.collectAsState()
    val cloudAuthLoading by authViewModel.cloudAuthLoading.collectAsState()
    val selectedLanguage by authViewModel.selectedLanguage.collectAsState()
    val fontScaleFactor by authViewModel.fontScaleFactor.collectAsState()
    val zoomEnabled by authViewModel.zoomEnabled.collectAsState()
    val zoomScaleFactor by authViewModel.zoomScaleFactor.collectAsState()
    val appThemeMode by authViewModel.appThemeMode.collectAsState()
    val hasAppLock by authViewModel.hasAppLock.collectAsState()
    val isPinLockMode by authViewModel.isPinLockMode.collectAsState()
    val lockOnBackground by authViewModel.lockOnBackground.collectAsState()
    val lockOnPanic by authViewModel.lockOnPanic.collectAsState()
    val isAiGenerating by chatViewModel.isGenerating.collectAsState()

    LaunchedEffect(selectedTab) {
        val label = when (selectedTab) {
            0 -> strings.tabChats
            1 -> strings.tabCommunities
            2 -> strings.tabCalls
            3 -> strings.tabGenAi
            else -> strings.tabSettings
        }
        com.example.util.ParentalControlManager.logNavigationEvent("Sekme: $label", "Giriş")
    }

    // Direct Messaging Navigation
    var activeConversation by remember { mutableStateOf<ConversationItem?>(null) }

    // Sub-screens inside Ayarlar
    var showDataPrivacySubScreen by remember { mutableStateOf(false) }
    var showThemePickerDialog by remember { mutableStateOf(false) }

    // Live WebRTC Call & Incoming Call State
    var activeWebRtcCall by remember { mutableStateOf<ActiveWebRtcCallState?>(null) }
    var pendingCallRequest by remember { mutableStateOf<ActiveWebRtcCallState?>(null) }
    var incomingCallPrompt by remember { mutableStateOf<CloudCallSession?>(null) }

    // Conversations state
    val conversations = remember { mutableStateListOf<ConversationItem>().apply { addAll(defaultConversations) } }
    val callHistory = remember { mutableStateListOf<CallHistoryItem>().apply { addAll(defaultCallHistory) } }

    val currentCallerName = cloudProfile?.displayName
        ?: cloudUser?.displayName
        ?: currentUser?.fullName
        ?: "Ben"

    val coroutineScope = rememberCoroutineScope()

    fun startVerifiedCallSession(req: ActiveWebRtcCallState) {
        activeWebRtcCall = req.copy(startedAtMs = System.currentTimeMillis())
        if (req.incomingSession != null) {
            WebRtcCallEngine.acceptIncomingCall(context, req.incomingSession)
        } else {
            WebRtcCallEngine.startOutgoingCall(
                context = context,
                calleeUid = req.calleeUid,
                calleeName = req.contactName,
                callerName = currentCallerName,
                isVideo = req.isVideo
            )
        }
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val req = pendingCallRequest
        pendingCallRequest = null
        if (req != null) {
            val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!audioGranted) {
                android.widget.Toast.makeText(
                    context,
                    "Mikrofon izni verilmedi; ses karşıya gitmeyebilir.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            val cameraGranted = results[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            startVerifiedCallSession(
                req.copy(isVideo = req.isVideo && cameraGranted)
            )
        }
    }

    fun initiateWebRtcCall(request: ActiveWebRtcCallState) {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (request.isVideo && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.CAMERA)
            }
        }
        if (needed.isEmpty()) {
            startVerifiedCallSession(request)
        } else {
            pendingCallRequest = request
            callPermissionLauncher.launch(needed.toTypedArray())
        }
    }

    // Keep local-only conversations ("GEN AI" and "Siz") synced with on-device storage
    LaunchedEffect(activeConversation?.id, cloudUser?.uid) {
        val selfMsgs = SelfChatStorage.loadMessages(context)
        val latestSelf = selfMsgs.lastOrNull()
        val selfIdx = conversations.indexOfFirst { it.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID }
        if (selfIdx != -1 && latestSelf != null) {
            conversations[selfIdx] = conversations[selfIdx].copy(
                lastMessage = latestSelf.text.ifBlank { "[${latestSelf.attachmentType ?: "Not"}]" },
                lastTime = if (latestSelf.timestamp > 0L) formatConversationListTime(latestSelf.timestamp) else latestSelf.time,
                messages = selfMsgs
            )
        }
    }

    // Ensure profile exists whenever Firebase user changes & listen to real-time conversations
    LaunchedEffect(cloudUser?.uid) {
        val uid = cloudUser?.uid
        if (uid != null) {
            authViewModel.syncCloudProfile(context)
            val repo = FirestoreChatRepository.create(context)
            repo.observeConversations()
                .collect { cloudRooms ->
                    val cloudItems = cloudRooms.map { it.toConversationItem() }
                    val localOnly = conversations.filter { local ->
                        local.id == "conv_gemini_ai" ||
                            local.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID ||
                            cloudItems.none { it.id == local.id }
                    }
                    conversations.clear()
                    val geminiConv = localOnly.firstOrNull { it.id == "conv_gemini_ai" }
                    if (geminiConv != null) conversations.add(geminiConv)
                    val selfConv = localOnly.firstOrNull { it.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID }
                    if (selfConv != null) conversations.add(selfConv)
                    conversations.addAll(cloudItems)
                    conversations.addAll(
                        localOnly.filter {
                            it.id != "conv_gemini_ai" && it.id != SelfChatStorage.SELF_NOTES_CONVERSATION_ID
                        }
                    )
                    activeConversation?.let { active ->
                        cloudItems.firstOrNull { it.id == active.id }?.let { updated ->
                            activeConversation = updated.copy(messages = active.messages)
                        }
                    }
                }
        } else {
            conversations.removeAll {
                it.id != "conv_gemini_ai" && it.id != SelfChatStorage.SELF_NOTES_CONVERSATION_ID
            }
        }
    }

    // Observe incoming WebRTC calls & P2P 1.5GB+ file transfer requests in real time across devices
    LaunchedEffect(cloudUser?.uid) {
        val uid = cloudUser?.uid
        if (uid != null) {
            UnlimitedFileTransferManager.restorePersistedFiles(context)
            WebRtcFileTransferEngine.startHostResponder(context)
            val repo = FirestoreChatRepository.create(context)
            repo.observeIncomingRingingCalls().collect { incoming ->
                incomingCallPrompt = if (incoming != null && incoming.id != activeWebRtcCall?.incomingSession?.id) {
                    incoming
                } else {
                    null
                }
            }
        } else {
            WebRtcFileTransferEngine.stopHostResponder()
            incomingCallPrompt = null
        }
    }

    // Sync unlocked conversation metadata & latest messages into ChatContextCache (only changed rooms)
    val lastFetchedMsgByRoom = remember { mutableMapOf<String, String>() }
    val convSignature = conversations.joinToString("|") { "${it.id}:${it.lastMessage}:${it.advancedPrivacyEnabled}:${it.messages.size}" }
    LaunchedEffect(convSignature, cloudUser?.uid) {
        ChatContextCache.updateConversations(conversations.toList())
        if (cloudUser?.uid != null) {
            val repo = FirestoreChatRepository.create(context)
            conversations.filter {
                it.id != "conv_gemini_ai" &&
                    it.id != SelfChatStorage.SELF_NOTES_CONVERSATION_ID &&
                    !it.advancedPrivacyEnabled
            }.forEach { room ->
                val marker = "${room.lastMessage}|${room.messages.size}"
                if (lastFetchedMsgByRoom[room.id] != marker) {
                    val fetched = repo.fetchMessagesOnce(room.id)
                    if (fetched.isNotEmpty()) {
                        ChatContextCache.updateRoomMessages(room.id, fetched)
                    }
                    lastFetchedMsgByRoom[room.id] = marker
                }
            }
        }
    }

    // New Chat dialog
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newChatNameInput by remember { mutableStateOf("") }
    var isVerifyingFriend by remember { mutableStateOf(false) }
    var verificationStepText by remember { mutableStateOf("") }
    var verifiedFriendProfile by remember { mutableStateOf<com.example.data.CloudHandleProfile?>(null) }
    var friendVerificationError by remember { mutableStateOf<String?>(null) }
    var customHandleInput by remember { mutableStateOf("") }

    // Cloud & Offline LAN server dialogs
    var showCloudConfigDialog by remember { mutableStateOf(false) }
    var showP2PTunnelDialog by remember { mutableStateOf(false) }

    // Global Incoming Call Banner/Dialog (works on any tab or sub-screen)
    val ringingSession = incomingCallPrompt
    if (ringingSession != null && activeWebRtcCall == null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (ringingSession.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = NeonGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ringingSession.isVideo) "Gelen Görüntülü Arama" else "Gelen Sesli Arama",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = ringingSession.callerName.ifBlank { "GENERAL CHAT Kullanıcısı" },
                        color = CyberYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "WebRTC DTLS-SRTP Şifreli Arama (${if (ringingSession.isVideo) "Kamera + Mikrofon" else "Ses"})",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        incomingCallPrompt = null
                        initiateWebRtcCall(
                            ActiveWebRtcCallState(
                                contactName = ringingSession.callerName.ifBlank { "Arayan" },
                                calleeUid = ringingSession.callerId,
                                isVideo = ringingSession.isVideo,
                                isIncoming = true,
                                incomingSession = ringingSession
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Kabul Et", color = CyberBlack, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        incomingCallPrompt = null
                        WebRtcCallEngine.rejectCall(context, ringingSession.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Reddet", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CyberDarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Live WebRTC Active Call Modal (works from both DirectMessageScreen and AramalarTabContent)
    if (activeWebRtcCall != null) {
        val currentCall = activeWebRtcCall!!
        ActiveCallDialog(
            contactName = currentCall.contactName,
            isVideo = currentCall.isVideo,
            startedAtMs = currentCall.startedAtMs,
            onEndCall = { durationText ->
                WebRtcCallEngine.hangUp(context)
                val initials = currentCall.contactName.trim().split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
                    .ifBlank { "?" }
                val callType = when {
                    currentCall.isVideo && currentCall.isIncoming -> CallType.VIDEO_INCOMING
                    currentCall.isVideo -> CallType.VIDEO_OUTGOING
                    currentCall.isIncoming -> CallType.VOICE_INCOMING
                    else -> CallType.VOICE_OUTGOING
                }
                callHistory.add(
                    0,
                    CallHistoryItem(
                        id = "call_${System.currentTimeMillis()}",
                        contactName = currentCall.contactName,
                        initials = initials,
                        type = callType,
                        timeString = formatClockTime(),
                        duration = durationText
                    )
                )
                activeWebRtcCall = null
            }
        )
    }

    // If inside a direct conversation, show DirectMessageScreen
    if (activeConversation != null) {
        BackHandler { activeConversation = null }
        DirectMessageScreen(
            conversation = activeConversation!!,
            currentUserUid = cloudUser?.uid,
            currentUserDisplayName = currentCallerName,
            onBack = { activeConversation = null },
            onStartCall = { name, isVideo ->
                val peerUid = activeConversation?.participantIds
                    ?.firstOrNull { it != cloudUser?.uid }
                    .orEmpty()
                initiateWebRtcCall(
                    ActiveWebRtcCallState(
                        contactName = name,
                        calleeUid = peerUid,
                        isVideo = isVideo
                    )
                )
            },
            onTriggerGlobalPanic = onTriggerGlobalPanic
        )
        return
    }

    // If inside Gizlilik, KVKK ve Uyumluluk sub-screen
    if (showDataPrivacySubScreen) {
        BackHandler { showDataPrivacySubScreen = false }
        DataPrivacyScreen(
            onBack = { showDataPrivacySubScreen = false },
            onWipeLocalData = {
                showDataPrivacySubScreen = false
                onWipeAllData()
            },
            onLoginClick = onOpenAuth,
            isLoggedIn = currentUser != null || cloudUser != null,
            isCloudSignedIn = cloudUser != null,
            currentDisplayName = currentCallerName.takeIf { it != "Ben" }.orEmpty(),
            currentEmail = cloudUser?.email ?: currentUser?.email ?: "",
            onUpdateDisplayName = { newName, cb ->
                authViewModel.updateDisplayName(context, newName, cb)
            },
            onDeleteCloudAccount = onDeleteCloudAccount
        )
        return
    }

    ChatWallpaperBackground(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            val barBg = if (appThemeMode == AppThemeMode.CYBER_DARK) CyberDarkSurface else MaterialTheme.colorScheme.surface
            val barFg = if (appThemeMode == AppThemeMode.CYBER_DARK) Color.White else MaterialTheme.colorScheme.onSurface
            val accentTint = if (appThemeMode == AppThemeMode.CYBER_DARK) CyberYellow else MaterialTheme.colorScheme.primary
            val mutedBarFg = if (appThemeMode == AppThemeMode.CYBER_DARK) TextSecondary else MaterialTheme.colorScheme.onSurfaceVariant

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.semantics { heading() }
                            ) {
                                Text(
                                    text = "GEN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = accentTint
                                )
                                Text(
                                    text = "eral Chat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = barFg
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onTriggerPanic) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Panik Maskesi",
                                    tint = accentTint
                                )
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = onTriggerGlobalPanic,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .height(36.dp)
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = "Acil Global Panik Kilidi ve Veri Gizleme"
                                        }
                                        .testTag("panic_btn_chats")
                                ) {
                                    Text("🚨 PANİK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            IconButton(onClick = { showThemePickerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Tema Seçimi",
                                    tint = accentTint
                                )
                            }

                            if (selectedTab == 0) {
                                IconButton(onClick = { showNewChatDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Yeni Sohbet",
                                        tint = barFg
                                    )
                                }
                            }
                            IconButton(onClick = { authViewModel.selectedMainTab.value = 4 }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ayarlar",
                                    tint = if (selectedTab == 4) accentTint else barFg
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = barBg.copy(alpha = 0.94f),
                            titleContentColor = barFg
                        )
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = barBg.copy(alpha = 0.95f),
                            tonalElevation = 8.dp
                        ) {
                            val navItems = listOf(
                                Triple(0, strings.tabChats, Icons.Default.Chat),
                                Triple(1, strings.tabCommunities, Icons.Default.Groups),
                                Triple(2, strings.tabCalls, Icons.Default.Call),
                                Triple(3, strings.tabGenAi, Icons.Default.SmartToy),
                                Triple(4, strings.tabSettings, Icons.Default.Settings)
                            )
                            navItems.forEach { (index, label, icon) ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { authViewModel.selectedMainTab.value = index },
                                    modifier = Modifier.semantics {
                                        stateDescription = if (isSelected) "Seçili" else "Seçili değil"
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) accentTint else mutedBarFg
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            color = if (isSelected) accentTint else mutedBarFg,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = if (appThemeMode == AppThemeMode.CYBER_DARK) CyberYellowContainer else MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                },
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = barBg.copy(alpha = 0.95f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                        ) {
                            val navItems = listOf(
                                Triple(0, strings.tabChats, Icons.Default.Chat),
                                Triple(1, strings.tabCommunities, Icons.Default.Groups),
                                Triple(2, strings.tabCalls, Icons.Default.Call),
                                Triple(3, strings.tabGenAi, Icons.Default.SmartToy),
                                Triple(4, strings.tabSettings, Icons.Default.Settings)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            navItems.forEach { (index, label, icon) ->
                                val isSelected = selectedTab == index
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { authViewModel.selectedMainTab.value = index },
                                    modifier = Modifier.semantics {
                                        stateDescription = if (isSelected) "Seçili" else "Seçili değil"
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) accentTint else mutedBarFg
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            color = if (isSelected) accentTint else mutedBarFg,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        indicatorColor = if (appThemeMode == AppThemeMode.CYBER_DARK) CyberYellowContainer else MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 920.dp)
                    ) {
                        when (selectedTab) {
                            0 -> SohbetlerTabContent(
                                conversations = conversations,
                                isCloudConnected = cloudUser != null,
                                isAccountSignedIn = currentUser != null || cloudUser != null,
                                cloudHandle = cloudProfile?.handle,
                                cloudAuthLoading = cloudAuthLoading,
                                onGoogleConnect = { authViewModel.signInWithGoogle(context, navigateToChatOnSuccess = false) },
                                onOpenNewChat = { showNewChatDialog = true },
                                onOpenSelfChat = {
                                    val selfRoom = conversations.firstOrNull {
                                        it.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID
                                    } ?: defaultConversations.first {
                                        it.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID
                                    }
                                    activeConversation = selfRoom
                                },
                                onConversationClick = { activeConversation = it },
                                onGoToAi = { authViewModel.selectedMainTab.value = 3 },
                                onOpenAuth = onOpenAuth,
                                onOpenAbout = onOpenAbout,
                                onOpenChannel = onOpenChannel,
                                onOpenCloudSetup = { showCloudConfigDialog = true },
                                onOpenOfflineRadar = { showP2PTunnelDialog = true }
                            )
                            1 -> CommunitiesView(
                                onOpenCommunityChat = { activeConversation = it },
                                isPaper = isPaperMode
                            )
                            2 -> AramalarTabContent(
                                callHistory = callHistory,
                                recentContacts = conversations.filter {
                                    it.id != "conv_gemini_ai" && it.id != SelfChatStorage.SELF_NOTES_CONVERSATION_ID
                                },
                                onStartCall = { target, isVideo ->
                                    val cleanTarget = target.trim().removePrefix("@")
                                    val matchedPeerUid = conversations.firstOrNull { conv ->
                                        conv.id != "conv_gemini_ai" &&
                                            conv.id != SelfChatStorage.SELF_NOTES_CONVERSATION_ID && (
                                            conv.name.equals(target, ignoreCase = true) ||
                                                conv.username?.removePrefix("@")?.equals(cleanTarget, ignoreCase = true) == true
                                            )
                                    }?.participantIds?.firstOrNull { it != cloudUser?.uid }

                                    if (!matchedPeerUid.isNullOrBlank() || cloudUser == null) {
                                        initiateWebRtcCall(
                                            ActiveWebRtcCallState(
                                                contactName = target,
                                                calleeUid = matchedPeerUid.orEmpty(),
                                                isVideo = isVideo
                                            )
                                        )
                                    } else {
                                        coroutineScope.launch {
                                            val resolved = FirestoreChatRepository.create(context)
                                                .lookupHandle(cleanTarget)
                                                .getOrNull()
                                            initiateWebRtcCall(
                                                ActiveWebRtcCallState(
                                                    contactName = resolved?.displayName ?: target,
                                                    calleeUid = resolved?.uid.orEmpty(),
                                                    isVideo = isVideo
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                            3 -> GenAiTabContent(
                                chatViewModel = chatViewModel
                            )
                            4 -> AyarlarTabContent(
                                currentUser = currentUser,
                                cloudUserEmail = cloudUser?.email,
                                cloudHandle = cloudProfile?.handle,
                                selectedLanguage = selectedLanguage,
                                isPaperMode = isPaperMode,
                                appThemeMode = appThemeMode,
                                fontScaleFactor = fontScaleFactor,
                                zoomEnabled = zoomEnabled,
                                zoomScaleFactor = zoomScaleFactor,
                                hasAppLock = hasAppLock,
                                isPinLockMode = isPinLockMode,
                                lockOnBackground = lockOnBackground,
                                lockOnPanic = lockOnPanic,
                                isCallActive = activeWebRtcCall != null,
                                isAiGenerating = isAiGenerating,
                                onLanguageSelect = { option ->
                                    authViewModel.selectedLanguage.value = option
                                    com.example.util.GenAiSpeechManager.setLanguageCode(option.code)
                                },
                                onFontScaleChange = { newScale ->
                                    authViewModel.setFontScale(context, newScale)
                                },
                                onZoomEnabledChange = { enabled ->
                                    authViewModel.setZoomEnabled(context, enabled)
                                },
                                onZoomScaleChange = { newZoom ->
                                    authViewModel.setZoomScale(context, newZoom)
                                },
                                onLockOnBackgroundChange = { enabled ->
                                    authViewModel.setLockOnBackground(context, enabled)
                                },
                                onLockOnPanicChange = { enabled ->
                                    authViewModel.setLockOnPanic(context, enabled)
                                },
                                onLockNow = {
                                    if (!authViewModel.lockScreenIfConfigured(context)) {
                                        onOpenAppLock()
                                    }
                                },
                                onOpenThemePicker = { showThemePickerDialog = true },
                                onTogglePaperMode = onTogglePaperMode,
                                onOpenAuth = onOpenAuth,
                                onOpenFreeServer = { showCloudConfigDialog = true },
                                onOpenDataPrivacy = { showDataPrivacySubScreen = true },
                                onOpenMasks = { onSelectMask(MaskType.WEATHER) },
                                onOpenAppLock = onOpenAppLock,
                                onOpenAbout = onOpenAbout,
                                onOpenShowcase = onOpenShowcase,
                                onOpenMiniGames = onOpenMiniGames,
                                onReplaySplash = onReplaySplash,
                                onCloseAllSessions = {
                                    chatViewModel.cancelGeneration()
                                    GenAiSpeechManager.stop()
                                    com.example.util.BackgroundMusicManager.pause()
                                    if (activeWebRtcCall != null) {
                                        WebRtcCallEngine.hangUp(context)
                                        activeWebRtcCall = null
                                    }
                                    incomingCallPrompt = null
                                    LocalMeshServer.stop()
                                    LocalMeshServer.stopPeerDiscovery()
                                    val unlinkedCount = com.example.util.LinkedDevicesManager.devices.value.size
                                    com.example.util.LinkedDevicesManager.clearAllDevices()
                                    com.example.util.CloudMessagingManager.stopListening()
                                    com.example.util.LiveLocationManager.stopAllActiveSessions(context)
                                    showNewChatDialog = false
                                    showCreateGroupDialog = false
                                    showCloudConfigDialog = false
                                    showP2PTunnelDialog = false
                                    showThemePickerDialog = false
                                    showDataPrivacySubScreen = false
                                    activeConversation = null
                                    android.widget.Toast.makeText(
                                        context,
                                        "Tüm açık oturumlar, bağlı cihazlar ($unlinkedCount), canlı konum, yerel ağ ve çağrılar güvenle kapatıldı.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    }

                    // Roadmap Section 9: Multitasking Floating Widget
                    FloatingCyberWidget(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }
    }

    val directoryResults by authViewModel.directorySearchResults.collectAsState()
    var directorySearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(showNewChatDialog) {
        if (showNewChatDialog) {
            authViewModel.searchDirectory(directorySearchQuery)
        }
    }

    // Theme Picker Dialog
    if (showThemePickerDialog) {
        ThemePickerDialog(
            authViewModel = authViewModel,
            onDismiss = { showThemePickerDialog = false }
        )
    }

    // Küresel Frekanstan Kişi Bul (Real Cross-Device Cloud Lookup via Firebase Firestore)
    if (showNewChatDialog) {
        val dialogClipboard = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = {
                showNewChatDialog = false
                directorySearchQuery = ""
                newChatNameInput = ""
                isVerifyingFriend = false
                verifiedFriendProfile = null
                friendVerificationError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CyberYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İnternetten Kişi Bul & Ekle",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cloudUser == null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            border = BorderStroke(1.dp, CyberYellow),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Farklı Ağ / Cihazlarla İnternet Sohbeti",
                                    color = CyberYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tıpkı WhatsApp gibi farklı internet ağlarından iki telefonun mesajlaşabilmesi için önce Google hesabınla buluta bağlanmalısın.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        authViewModel.signInWithGoogle(context, navigateToChatOnSuccess = false)
                                    },
                                    enabled = !cloudAuthLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("dialog_google_signin_button")
                                ) {
                                    Text(
                                        text = if (cloudAuthLoading) "Bağlanıyor..." else "Google ile Bağlan",
                                        color = CyberBlack,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    } else {
                        val myHandle = cloudProfile?.handle
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E16)),
                            border = BorderStroke(1.dp, NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "BENİM BULUT KİMLİĞİM (KARŞI CİHAZA BUNU VER)",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (myHandle != null) "@$myHandle" else "Hazırlanıyor...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    if (myHandle != null) {
                                        OutlinedButton(
                                            onClick = {
                                                dialogClipboard.setText(AnnotatedString("@$myHandle"))
                                            },
                                            border = BorderStroke(1.dp, NeonGreen),
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Kopyala", color = NeonGreen, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                showNewChatDialog = false
                                showCreateGroupDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132B1F)),
                            border = BorderStroke(1.dp, NeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("👥 Yeni Bulut Grup Sohbeti Oluştur", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "ARKADAŞININ @KULLANICI_ADINI GİR (BULUTTAN ARA)",
                            color = CyberYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newChatNameInput,
                                onValueChange = {
                                    newChatNameInput = it
                                    friendVerificationError = null
                                },
                                placeholder = { Text("Örn: @ahmet_84a1", color = TextMuted, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AlternateEmail,
                                        contentDescription = null,
                                        tint = CyberYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                enabled = !isVerifyingFriend,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friend_id_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberYellow,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = CyberBlack,
                                    unfocusedContainerColor = CyberBlack
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    val cleanId = newChatNameInput.trim().removePrefix("@").lowercase()
                                    if (cleanId.length < 3) {
                                        friendVerificationError = "Kullanıcı adı en az 3 karakter olmalıdır."
                                        return@Button
                                    }
                                    SoundPoolManager.playMechanicalClick()
                                    coroutineScope.launch {
                                        isVerifyingFriend = true
                                        verifiedFriendProfile = null
                                        friendVerificationError = null
                                        verificationStepText = "Firebase Cloud Firestore'da '@$cleanId' aranıyor..."
                                        val repo = com.example.data.FirestoreChatRepository.create(context)
                                        val res = repo.lookupHandle(cleanId)
                                        isVerifyingFriend = false
                                        res.onSuccess { found ->
                                            if (found != null) {
                                                verifiedFriendProfile = found
                                                verificationStepText = "Bulundu: ${found.displayName} (@${found.handle})"
                                                SoundEffectsManager.playConfirmSound()
                                            } else {
                                                friendVerificationError = "@$cleanId henüz GENeral Chat bulutuna Google ile bağlanmamış. Karşı cihazın Google ile giriş yapıp kendi @kimlik adını verdiğinden emin ol."
                                            }
                                        }.onFailure { err ->
                                            friendVerificationError = err.localizedMessage ?: "Arama sırasında hata oluştu."
                                        }
                                    }
                                },
                                enabled = !isVerifyingFriend && newChatNameInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberYellow,
                                    disabledContainerColor = CyberYellow.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("verify_friend_button")
                            ) {
                                Text("Bul", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (isVerifyingFriend) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberBlack),
                                border = BorderStroke(1.dp, CyberYellow),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = CyberYellow,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = verificationStepText, color = NeonCyan, fontSize = 11.sp)
                                }
                            }
                        }

                        friendVerificationError?.let { err ->
                            Text(text = err, color = NeonRed, fontSize = 11.sp)
                        }

                        verifiedFriendProfile?.let { candidate ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E16)),
                                border = BorderStroke(1.5.dp, NeonGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verified_friend_result_card")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(CyberBlack)
                                                .border(1.5.dp, NeonGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = candidate.avatarEmoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = candidate.displayName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Doğrulandı",
                                                    tint = NeonGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = "@${candidate.handle}",
                                                color = NeonGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            coroutineScope.launch {
                                                val repo = com.example.data.FirestoreChatRepository.create(context)
                                                val myName = cloudProfile?.displayName ?: cloudUser?.displayName ?: "Kullanıcı"
                                                val openRes = repo.openOrCreateDirectConversation(candidate, myName)
                                                openRes.onSuccess { room ->
                                                    val conv = room.toConversationItem()
                                                    if (conversations.none { it.id == conv.id }) {
                                                        conversations.add(0, conv)
                                                    }
                                                    activeConversation = conv
                                                    showNewChatDialog = false
                                                    newChatNameInput = ""
                                                    verifiedFriendProfile = null
                                                }.onFailure { err ->
                                                    friendVerificationError = err.localizedMessage ?: "Sohbet odası açılamadı."
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("start_chat_with_friend_button")
                                    ) {
                                        Text("Mesaj At", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = CyberCardBorder)

                        Text(
                            text = "KENDİ @KULLANICI_ADINI ÖZELLEŞTİR",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customHandleInput,
                                onValueChange = { customHandleInput = it },
                                placeholder = { Text("Örn: ali_2025", color = TextMuted, fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberYellow,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = CyberBlack,
                                    unfocusedContainerColor = CyberBlack
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = {
                                    if (customHandleInput.trim().length >= 3) {
                                        authViewModel.syncCloudProfile(context, preferredHandle = customHandleInput.trim())
                                        customHandleInput = ""
                                    }
                                },
                                border = BorderStroke(1.dp, CyberYellow),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Kaydet", color = CyberYellow, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewChatDialog = false
                        directorySearchQuery = ""
                        newChatNameInput = ""
                        isVerifyingFriend = false
                        verifiedFriendProfile = null
                        friendVerificationError = null
                    }
                ) {
                    Text("Kapat", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showCreateGroupDialog) {
        val realContacts = remember(conversations.toList()) {
            conversations
                .filter { it.id != "conv_gemini_ai" && !it.isGroup }
                .map {
                    FriendContact(
                        id = it.id,
                        name = it.name,
                        username = it.username ?: "@${it.name.lowercase().replace(" ", "_")}",
                        avatarEmoji = it.avatarEmoji ?: "👤"
                    )
                }
        }
        CreateGroupDialog(
            availableContacts = realContacts,
            onGroupCreated = { newGroup ->
                if (cloudUser != null) {
                    coroutineScope.launch {
                        val repo = com.example.data.FirestoreChatRepository.create(context)
                        val myName = cloudProfile?.displayName ?: cloudUser?.displayName ?: "Kurucu"
                        val res = repo.createGroupConversation(
                            title = newGroup.name,
                            members = emptyList(),
                            myDisplayName = myName
                        )
                        res.onSuccess { room ->
                            val cloudGroup = room.toConversationItem()
                            if (conversations.none { it.id == cloudGroup.id }) {
                                conversations.add(0, cloudGroup)
                            }
                            activeConversation = cloudGroup
                        }.onFailure {
                            conversations.add(0, newGroup)
                            activeConversation = newGroup
                        }
                    }
                } else {
                    conversations.add(0, newGroup)
                    activeConversation = newGroup
                }
            },
            onDismiss = { showCreateGroupDialog = false },
            isPaper = isPaperMode
        )
    }

    // Dual-Mode Server Dialog: Global Internet Firestore + Local Wi-Fi HTTP Server
    if (showCloudConfigDialog) {
        FreeServerAndCloudDialog(
            cloudUserEmail = cloudUser?.email,
            cloudHandle = cloudProfile?.handle,
            cloudAuthLoading = cloudAuthLoading,
            onGoogleSignIn = { authViewModel.signInWithGoogle(context, navigateToChatOnSuccess = false) },
            onGoogleSignOut = { authViewModel.signOutGoogle(context) },
            onDismiss = { showCloudConfigDialog = false }
        )
    }

    if (showP2PTunnelDialog) {
        OfflineP2PTunnelDialog(
            onSendMessage = { /* Delivered over live NSD / HTTP LAN server inside dialog */ },
            onDismiss = { showP2PTunnelDialog = false }
        )
    }
}

/**
 * Tab 0: Sohbetler Screen (Exact match to Frame 00:05 & 00:06 + Messaging List)
 */
@Composable
internal fun SohbetlerTabContent(
    conversations: List<ConversationItem>,
    isCloudConnected: Boolean,
    isAccountSignedIn: Boolean = isCloudConnected,
    cloudHandle: String?,
    cloudAuthLoading: Boolean,
    onGoogleConnect: () -> Unit,
    onOpenNewChat: () -> Unit,
    onOpenSelfChat: () -> Unit,
    onConversationClick: (ConversationItem) -> Unit,
    onGoToAi: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenAbout: () -> Unit = {},
    onOpenChannel: () -> Unit,
    onOpenCloudSetup: () -> Unit,
    onOpenOfflineRadar: () -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    val strings = com.example.util.LocalAppStrings.current
    val clipboardManager = LocalClipboardManager.current
    var chatSearchQuery by remember { mutableStateOf("") }
    val displayedConversations = remember(conversations.toList(), chatSearchQuery) {
        if (chatSearchQuery.isBlank()) conversations
        else {
            val q = chatSearchQuery.trim().lowercase(java.util.Locale.getDefault())
            conversations.filter { conv ->
                conv.name.lowercase(java.util.Locale.getDefault()).contains(q) ||
                (conv.username?.lowercase(java.util.Locale.getDefault())?.contains(q) == true) ||
                conv.messages.any { m ->
                    m.text.lowercase(java.util.Locale.getDefault()).contains(q) ||
                    m.time.contains(q)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Real-Time Service Status Dashboard Indicator + Offline-First LocalStorage Notification
            ServiceStatusDashboardCard(
                isCloudSignedIn = isCloudConnected,
                onOpenFreeServer = onOpenCloudSetup,
                onOpenOfflineRadar = onOpenOfflineRadar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

        // Cross-Device Internet Messaging Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isCloudConnected) Color(0xFF0F1E16) else (if (isPaper) PaperSurface else CyberCardBg)
            ),
            border = BorderStroke(1.dp, if (isCloudConnected) NeonGreen else CyberYellow),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isCloudConnected) "🟢 ÇEVRİMİÇİ MESAJLAŞMA AKTİF" else "🟡 BULUT MESAJLAŞMA & CİHAZ İÇİ NOTLAR",
                    color = if (isCloudConnected) NeonGreen else CyberYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (isPaper) PaperBg else Color(0xFF0A1D13),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_e2ee_security_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Uçtan uca şifreleme güvenlik etiketi",
                            tint = if (isPaper) PaperText else NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Uçtan Uca Şifreli (E2EE) · 1'e 1 mesajlar sunucuda şifreli tutulur ve okunamaz",
                            color = if (isPaper) PaperText else NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (isCloudConnected) {
                    Text(
                        text = if (cloudHandle != null) {
                            "Kimliğin: @$cloudHandle · Farklı ağlardaki kişiler bu kullanıcı adıyla seni ekleyip arayabilir."
                        } else {
                            "Hesap kimliğin hazırlanıyor..."
                        },
                        color = if (isPaper) PaperText else Color.White,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (cloudHandle != null) {
                            OutlinedButton(
                                onClick = { clipboardManager.setText(AnnotatedString("@$cloudHandle")) },
                                border = BorderStroke(1.dp, NeonGreen),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("@$cloudHandle Kopyala", color = NeonGreen, fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = onOpenNewChat,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("+ Kişi Bul", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Farklı ağlardaki cihazlarla gerçek zamanlı sohbet ve ebeveyn bulut eşitlemesi için Google ile giriş yapın veya tamamen cihaz içi 'Siz' defterini kullanın.",
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onGoogleConnect,
                        enabled = !cloudAuthLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("home_google_signin_banner_btn")
                    ) {
                        Text(
                            text = if (cloudAuthLoading) "Bağlanıyor..." else "Google ile Bağlan (Ücretsiz)",
                            color = CyberBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onOpenSelfChat,
                        border = BorderStroke(1.dp, CyberYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("home_self_notes_button")
                    ) {
                        Text(
                            text = "📝 Siz (Sadece Cihaz İçi Özel Notlar)",
                            color = if (isPaper) PaperText else CyberYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CyberYellow.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_general_4k_emblem),
                contentDescription = "GENeral Chat 4K Amblem",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            GeneralChatLogo(size = 44.dp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = strings.homeEcosystemTag,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else CyberYellow,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "GENeral Chat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isPaper) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = strings.homeSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperTextMuted else TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                SoundPoolManager.playMechanicalClick()
                onGoToAi()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPaper) PaperText else CyberYellow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("home_gen_ai_button")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isPaper) PaperBg else CyberBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.homeGenAiBtn,
                color = if (isPaper) PaperBg else CyberBlack,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isAccountSignedIn) {
            Button(
                onClick = {
                    SoundPoolManager.playMechanicalClick()
                    onOpenAbout()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("home_about_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isPaper) PaperText else CyberYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.homeAboutBtn,
                    color = if (isPaper) PaperText else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        } else {
            Button(
                onClick = {
                    SoundPoolManager.playMechanicalClick()
                    onOpenAuth()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("home_auth_button")
            ) {
                Text(
                    text = strings.homeAuthBtn,
                    color = if (isPaper) PaperText else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                SoundPoolManager.playMechanicalClick()
                onOpenChannel()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("home_announcements_button")
        ) {
            Text(
                text = strings.homeAnnouncementsBtn,
                color = if (isPaper) PaperText else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "01",
                        color = if (isPaper) PaperText else CyberYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.homeCardTryTitle,
                        color = if (isPaper) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.homeCardTrySub,
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "02",
                        color = if (isPaper) PaperText else CyberYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.homeCardTalkTitle,
                        color = if (isPaper) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.homeCardTalkSub,
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sürüm 7.3.3 · Orijinal Sarı-Siyah Tasarım",
            style = MaterialTheme.typography.labelSmall,
            color = if (isPaper) PaperTextMuted else TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = chatSearchQuery,
            onValueChange = { chatSearchQuery = it },
            placeholder = { Text(strings.searchChatsPlaceholder, fontSize = 12.sp, color = if (isPaper) PaperTextMuted else TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = if (isPaper) PaperText else CyberYellow,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (chatSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { chatSearchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Temizle",
                            tint = if (isPaper) PaperText else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isPaper) PaperBorder else CyberYellow,
                unfocusedBorderColor = if (isPaper) PaperBorder else CyberCardBorder,
                focusedTextColor = if (isPaper) PaperText else Color.White,
                unfocusedTextColor = if (isPaper) PaperText else Color.White,
                focusedContainerColor = if (isPaper) PaperSurface else CyberBlack,
                unfocusedContainerColor = if (isPaper) PaperSurface else CyberBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_chats_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (chatSearchQuery.isBlank()) strings.incomingMessagesTitle else "Arama Sonuçları",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPaper) PaperText else Color.White
            )
            Text(
                text = "${displayedConversations.size} ${strings.chatCountSuffix}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPaper) PaperText else CyberYellow
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        }

        if (displayedConversations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noMatchingChats,
                        color = if (isPaper) PaperTextMuted else TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(displayedConversations, key = { it.id }, contentType = { "conversation_row" }) { conv ->
                ConversationRowItem(
                    conversation = conv,
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        onConversationClick(conv)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConversationRowItem(
    conversation: ConversationItem,
    onClick: () -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPaper) PaperBorder else Color(0xFF262000))
                    .border(1.5.dp, if (isPaper) PaperTextMuted else CyberYellow.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!conversation.logoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = conversation.logoUri,
                        contentDescription = "${conversation.name} logosu",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = conversation.initials,
                        color = if (isPaper) PaperText else CyberYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = conversation.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaper) PaperText else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!conversation.isGroup && conversation.id != "conv_gemini_ai") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = if (conversation.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID) {
                                    "Cihaz içi kasa"
                                } else {
                                    "E2EE Şifreli Sohbet"
                                },
                                tint = if (isPaper) PaperText else NeonGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = conversation.lastTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaper) PaperTextMuted else TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isPaper) PaperText else CyberYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = if (isPaper) PaperBg else CyberBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Aramalar Screen (Voice & Video Calls Hub)
 */
@Composable
private fun AramalarTabContent(
    callHistory: List<CallHistoryItem>,
    recentContacts: List<ConversationItem> = emptyList(),
    onStartCall: (String, Boolean) -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    var showNewCallDialog by remember { mutableStateOf(false) }
    var callTargetInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "GÜVENLİ ARAMALAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaper) PaperText else CyberYellow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Sesli & Görüntülü Aramalar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaper) PaperText else Color.White
                )
            }

            Button(
                onClick = {
                    SoundPoolManager.playMechanicalClick()
                    showNewCallDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else CyberYellow),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("calls_new_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = if (isPaper) PaperBg else CyberBlack,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Yeni Arama",
                    color = if (isPaper) PaperBg else CyberBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // WebRTC E2EE Shield Pill
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WebRTC DTLS-SRTP şifreli sesli ve görüntülü arama aktif. İstediğiniz kişiyle HD ses & kamera görüşmesi başlatın.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaper) PaperTextMuted else TextSecondary
                )
            }
        }

        if (recentContacts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "HIZLI SESLİ & GÖRÜNTÜLÜ ARAMA",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPaper) PaperTextMuted else CyberYellow,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentContacts, key = { "quick_call_${it.id}" }) { contact ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = contact.name,
                                color = if (isPaper) PaperText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    onStartCall(contact.name, false)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("quick_voice_call_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "${contact.name} ile sesli görüşme başlat",
                                    tint = if (isPaper) PaperText else CyberYellow
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    onStartCall(contact.name, true)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("quick_video_call_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "${contact.name} ile görüntülü görüşme başlat",
                                    tint = if (isPaper) PaperText else NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (callHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz arama geçmişi yok. 'Yeni Arama' veya hızlı kişi kartları üzerinden sesli/görüntülü görüşme başlatabilirsiniz.",
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(callHistory, key = { it.id }, contentType = { "call_row" }) { call ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isPaper) PaperBorder else Color(0xFF262000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = call.initials,
                                        color = if (isPaper) PaperText else CyberYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = call.contactName,
                                        color = if (isPaper) PaperText else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isMissed = call.type == CallType.VOICE_MISSED
                                        Icon(
                                            imageVector = if (call.type == CallType.VIDEO_OUTGOING || call.type == CallType.VIDEO_INCOMING) Icons.Default.Videocam else Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = if (isMissed) NeonRed else (if (isPaper) PaperText else CyberYellow),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${call.timeString} · ${call.duration}",
                                            color = if (isMissed) NeonRed else (if (isPaper) PaperTextMuted else TextSecondary),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        onStartCall(call.contactName, false)
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "${call.contactName} sesli ara",
                                        tint = if (isPaper) PaperText else CyberYellow
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        onStartCall(call.contactName, true)
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "${call.contactName} görüntülü ara",
                                        tint = if (isPaper) PaperText else NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewCallDialog) {
        AlertDialog(
            onDismissRequest = { showNewCallDialog = false },
            title = {
                Text("Yeni Sesli / Görüntülü Arama", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Aramak istediğiniz kişinin adını veya @kullanici_id bilgisini girin:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (recentContacts.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(recentContacts, key = { "dlg_call_${it.id}" }) { contact ->
                                FilterChip(
                                    selected = callTargetInput.equals(contact.name, ignoreCase = true),
                                    onClick = { callTargetInput = contact.username ?: contact.name },
                                    label = { Text(contact.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = callTargetInput,
                        onValueChange = { callTargetInput = it },
                        placeholder = { Text("@kullanici_adi veya ad", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_call_target_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberYellow,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        )
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val target = callTargetInput.trim()
                            if (target.isNotEmpty()) {
                                showNewCallDialog = false
                                callTargetInput = ""
                                onStartCall(target, false)
                            }
                        },
                        enabled = callTargetInput.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier.testTag("start_voice_call_dialog_button")
                    ) {
                        Text("Sesli Ara", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val target = callTargetInput.trim()
                            if (target.isNotEmpty()) {
                                showNewCallDialog = false
                                callTargetInput = ""
                                onStartCall(target, true)
                            }
                        },
                        enabled = callTargetInput.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("start_video_call_dialog_button")
                    ) {
                        Text("Görüntülü", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCallDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Tab 2: GEN AI Screen (Exact 1:1 match to Frame 00:08 in the video)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GenAiTabContent(
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    val strings = com.example.util.LocalAppStrings.current
    val messages by chatViewModel.messages.collectAsState()
    val sessions by chatViewModel.sessions.collectAsState()
    val activeSessionId by chatViewModel.activeSessionId.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    val inputText by chatViewModel.inputText.collectAsState()
    val selectedMode by chatViewModel.selectedMode.collectAsState()
    val isGenerating by chatViewModel.isGenerating.collectAsState()
    val thinkingStep by chatViewModel.thinkingStep.collectAsState()
    val streamingText by chatViewModel.streamingText.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()
    val errorBanner by chatViewModel.errorBanner.collectAsState()
    val lastFailedPrompt by chatViewModel.lastFailedPrompt.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val isTtsSpeaking by GenAiSpeechManager.isSpeaking.collectAsState()
    val speakingMsgId by GenAiSpeechManager.speakingMessageId.collectAsState()
    val autoReadEnabled by GenAiSpeechManager.autoReadEnabled.collectAsState()
    var lastAutoReadId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(messages.size, autoReadEnabled) {
        val newest = messages.lastOrNull()
        if (autoReadEnabled && newest != null && newest.role == "model" && newest.id != lastAutoReadId) {
            lastAutoReadId = newest.id
            GenAiSpeechManager.speak(context, newest.id.toString(), newest.content, forceStart = true)
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val payload = chatViewModel.buildExportPayload(formatJson = true)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(payload.toByteArray(Charsets.UTF_8))
                }
                android.widget.Toast.makeText(context, "JSON sohbet dosyası kaydedildi ✓", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Dışa aktarma hatası: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                val payload = chatViewModel.buildExportPayload(formatJson = false)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(payload.toByteArray(Charsets.UTF_8))
                }
                android.widget.Toast.makeText(context, "Metin (.txt) sohbet dosyası kaydedildi ✓", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Dışa aktarma hatası: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            GenAiSpeechManager.stop()
        }
    }

    val speechState = rememberSpeechToTextLauncher { spokenText ->
        val current = chatViewModel.inputText.value
        chatViewModel.inputText.value = if (current.isBlank()) spokenText else "$current $spokenText"
        SoundPoolManager.playMechanicalClick()
    }

    val aiModes: List<Pair<AiMode, String>> = listOf(
        Pair(AiMode.GENERAL, "Akıllı otomatik"),
        Pair(AiMode.LEARN, "Konuyu öğren"),
        Pair(AiMode.MATH, "Matematik"),
        Pair(AiMode.CODE, "Kodlama"),
        Pair(AiMode.TRANSLATE, "Çeviri & Sözlük"),
        Pair(AiMode.IDEAS, "İçerik fikirleri"),
        Pair(AiMode.GUIDE, "Rehber hazırla")
    )

    LaunchedEffect(messages.size, streamingText, thinkingStep, errorMessage) {
        if (messages.isNotEmpty() || streamingText != null || thinkingStep != null || errorMessage != null) {
            val extraItems = (if (streamingText != null || thinkingStep != null) 1 else 0) +
                (if (errorMessage != null) 1 else 0)
            val target = (messages.size + extraItems) - 1
            if (target >= 0) listState.animateScrollToItem(target)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "GEN AI PRO MOTORU",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaper) PaperText else CyberYellow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "GEN AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isPaper) PaperText else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.aiStatusSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status Card: GEN AI + Auto Read + Clear & Export buttons
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (isPaper) PaperText else CyberYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "GEN AI",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaper) PaperText else Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Kodlama · Web Arama · Çok Dilli Sözlük",
                                        color = if (isPaper) PaperTextMuted else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        showHistoryDialog = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isPaper) PaperText else CyberYellow
                                    ),
                                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Geçmiş (${sessions.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        chatViewModel.startNewChat()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPaper) PaperBg else Color(0xFF262000)
                                    ),
                                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        "Yeni",
                                        color = if (isPaper) PaperText else CyberYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Auto-Read Sesli Oku Switch Chip
                        FilterChip(
                            selected = autoReadEnabled,
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                val next = !autoReadEnabled
                                GenAiSpeechManager.setAutoReadEnabled(context, next)
                                if (!next) GenAiSpeechManager.stop()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (autoReadEnabled) strings.aiAutoReadOn else strings.aiAutoReadOff,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isPaper) PaperText else CyberYellow,
                                selectedLabelColor = if (isPaper) PaperBg else CyberBlack,
                                selectedLeadingIconColor = if (isPaper) PaperBg else CyberBlack,
                                containerColor = if (isPaper) PaperBg else CyberBlack,
                                labelColor = if (isPaper) PaperText else TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("ai_auto_read_toggle")
                        )

                        // Action Row: Sohbeti Temizle & Sohbeti Dışa Aktar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    GenAiSpeechManager.stop()
                                    chatViewModel.clearCurrentSession()
                                    android.widget.Toast.makeText(context, "Sohbet geçmişi temizlendi.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.55f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("ai_clear_chat_button")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.aiClearChat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        showExportMenu = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isPaper) PaperText else NeonCyan
                                    ),
                                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else NeonCyan.copy(alpha = 0.55f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("ai_export_chat_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.aiExportChat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                DropdownMenu(
                                    expanded = showExportMenu,
                                    onDismissRequest = { showExportMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("JSON Olarak İndir (.json)") },
                                        onClick = {
                                            showExportMenu = false
                                            com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                            exportJsonLauncher.launch("gen_ai_sohbet_${System.currentTimeMillis()}.json")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Düz Metin İndir (.txt)") },
                                        onClick = {
                                            showExportMenu = false
                                            com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                            exportTxtLauncher.launch("gen_ai_sohbet_${System.currentTimeMillis()}.txt")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Room SQLite session status card
                val currentSession = sessions.firstOrNull { it.id == activeSessionId }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414)
                    ),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SoundPoolManager.playMechanicalClick()
                            showHistoryDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Oturum: ${currentSession?.title ?: "Yeni Konuşma"} (💾 Room SQLite)",
                                fontSize = 11.sp,
                                color = if (isPaper) PaperText else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Değiştir ▾",
                            fontSize = 10.sp,
                            color = CyberYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Yellow Highlight Card (GEN AI Pro capabilities)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPaper) PaperSurface else Color(0xFF1E1A04)
                    ),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isPaper) PaperText else CyberYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GEN AI Çekirdeği · Kodlama, Bellek & Sesli Okuma",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isPaper) PaperText else CyberYellow
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "GEN AI Pro motoru; çalışan tam kod üretimi, çok turlu konuşma belleği, son yazdığınız kişileri hatırlama, mesaj arama yardımı, Türkçe sesli yanıt dinleme ve JSON/TXT dışa aktarma özelliklerini destekler.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPaper) PaperText else Color(0xFFE8E8E8),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yanıt tarzı section
                Text(
                    text = "Yanıt tarzı",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaper) PaperText else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Varsayılan mod; sorunun türüne göre en uygun yaklaşımı seçer. Aynı konuşmada takip soruları sorulabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(aiModes) { pair ->
                        val mode = pair.first
                        val label = pair.second
                        val isSelected = selectedMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                chatViewModel.setMode(mode)
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isPaper) PaperText else CyberYellow,
                                selectedLabelColor = if (isPaper) PaperBg else CyberBlack,
                                containerColor = if (isPaper) PaperSurface else CyberCardBg,
                                labelColor = if (isPaper) PaperText else Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) (if (isPaper) PaperText else CyberYellow) else (if (isPaper) PaperBorder else CyberCardBorder)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Greeting Bubble
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isPaper) PaperText else CyberYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = if (isPaper) PaperBg else CyberBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GEN AI", fontWeight = FontWeight.Bold, color = if (isPaper) PaperText else CyberYellow, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Merhaba! Ben GEN AI. Kod yazmadan hata ayıklamaya, matematikten çeviriye ve sohbet özetlemeye kadar her konuda yanındayım. Yanıtlarımın üzerine dokunarak kopyalayabilir veya Dinle butonuyla sesli dinleyebilirsin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPaper) PaperText else Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // GEN AI Conversation messages with HH:mm timestamp, Tap/Long-Press Copy, and Voice Readout (TTS)
            items(messages, key = { it.id }, contentType = { it.role }) { msg ->
                val isUser = msg.role == "user"
                val timeLabel = formatClockTime(msg.timestamp)
                val msgKey = msg.id.toString()
                val isThisSpeaking = isTtsSpeaking && speakingMsgId == msgKey
                val copyBubbleText = {
                    SoundPoolManager.playMechanicalClick()
                    clipboardManager.setText(AnnotatedString(msg.content))
                    android.widget.Toast.makeText(context, "Mesaj panoya kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 315.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isUser) {
                                    if (isPaper) PaperText else CyberYellow
                                } else {
                                    if (isPaper) PaperSurface else CyberCardBg
                                }
                            )
                            .border(
                                1.dp,
                                if (isUser) {
                                    if (isPaper) PaperText else CyberYellow
                                } else {
                                    if (isPaper) PaperBorder else CyberCardBorder
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .combinedClickable(
                                onClick = copyBubbleText,
                                onLongClick = copyBubbleText
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.content,
                                color = if (isUser) {
                                    if (isPaper) PaperBg else CyberBlack
                                } else {
                                    if (isPaper) PaperText else Color.White
                                },
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUser) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isThisSpeaking) NeonRed.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.14f))
                                                .clickable {
                                                    SoundPoolManager.playMechanicalClick()
                                                    GenAiSpeechManager.speak(context, msgKey, msg.content)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isThisSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                contentDescription = if (isThisSpeaking) "Sesli okumayı durdur" else "Mesajı sesli dinle",
                                                tint = if (isThisSpeaking) NeonRed else (if (isPaper) PaperText else NeonCyan),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                             Text(
                                                 text = if (isThisSpeaking) strings.aiStopReading else strings.aiReadAloud,
                                                 fontSize = 10.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = if (isThisSpeaking) NeonRed else (if (isPaper) PaperText else CyberYellow)
                                             )
                                         }
                                     }

                                     Row(
                                         modifier = Modifier
                                             .clip(RoundedCornerShape(6.dp))
                                             .background(
                                                 if (isUser) Color.Black.copy(alpha = 0.12f)
                                                 else Color.White.copy(alpha = 0.08f)
                                             )
                                             .clickable { copyBubbleText() }
                                             .padding(horizontal = 6.dp, vertical = 3.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Icon(
                                             imageVector = Icons.Default.ContentCopy,
                                             contentDescription = strings.aiCopy,
                                             tint = if (isUser) {
                                                 if (isPaper) PaperBg else CyberBlack
                                             } else {
                                                 if (isPaper) PaperTextMuted else TextSecondary
                                             },
                                             modifier = Modifier.size(11.dp)
                                         )
                                         Spacer(modifier = Modifier.width(3.dp))
                                         Text(
                                             text = strings.aiCopy,
                                             fontSize = 10.sp,
                                             fontWeight = FontWeight.SemiBold,
                                             color = if (isUser) {
                                                 if (isPaper) PaperBg else CyberBlack
                                             } else {
                                                 if (isPaper) PaperTextMuted else TextSecondary
                                             }
                                         )
                                     }
                                 }

                                 Text(
                                     text = timeLabel,
                                     color = if (isUser) {
                                         if (isPaper) PaperBg.copy(alpha = 0.75f) else CyberBlack.copy(alpha = 0.72f)
                                     } else {
                                         if (isPaper) PaperTextMuted else TextMuted
                                     },
                                     fontSize = 10.sp
                                 )
                             }
                         }
                     }
                 }
             }

            // Explicit Thinking Phase Card
            if (thinkingStep != null && streamingText == null) {
                item(key = "gen_ai_thinking_card") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else Color(0xFF1A1604)),
                        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = if (isPaper) PaperText else CyberYellow,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.aiThinkingTitle,
                                    color = if (isPaper) PaperText else CyberYellow,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = thinkingStep.orEmpty(),
                                    color = if (isPaper) PaperTextMuted else Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Explicit Visible Error Card (never silently fails; provides Retry & Offline options)
            if (errorBanner != null || errorMessage != null) {
                val activeErr = errorBanner
                item(key = "gen_ai_error_card") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0A0A)),
                        border = BorderStroke(1.2.dp, NeonRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("ai_error_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = CyberYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = activeErr?.title ?: "AI Bağlantı / Sunucu Hatası",
                                    color = CyberYellow,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = activeErr?.userMessage ?: errorMessage.orEmpty(),
                                color = Color.White,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if ((activeErr?.isRetryable ?: true) && !lastFailedPrompt.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            val ctxBlock = ChatContextCache.buildPromptContext(context, lastFailedPrompt.orEmpty())
                                            chatViewModel.retryLastMessage(chatContextSummary = ctxBlock)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("ai_retry_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = CyberBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Yeniden Dene", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                if (!lastFailedPrompt.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            val ctxBlock = ChatContextCache.buildPromptContext(context, lastFailedPrompt.orEmpty())
                                            chatViewModel.useOfflineFallback(chatContextSummary = ctxBlock)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("ai_offline_fallback_button")
                                    ) {
                                        Text("Çevrimdışı Yanıtla", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { chatViewModel.dismissError() },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("ai_dismiss_error_button")
                                ) {
                                    Text("Kapat", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

             // Streaming response
             if (streamingText != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPaper) PaperSurface else CyberCardBg)
                                .border(1.dp, if (isPaper) PaperBorder else CyberYellow, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = streamingText.orEmpty(),
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // High-Contrast Visual Recording Indicator Banner
        if (speechState.isListening) {
            val pulseTransition = rememberInfiniteTransition(label = "recPulse")
            val dotAlpha by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(550),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "recDotAlpha"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB00018))
                    .border(2.dp, CyberYellow)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = "Canlı ses kaydı aktif. Konuşuyorsunuz."
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(CyberYellow.copy(alpha = dotAlpha))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "● REC · SES KAYDEDİLİYOR — KONUŞUN",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (speechState.recognizedText.isNotBlank()) {
                            Text(
                                text = speechState.recognizedText,
                                color = CyberYellow,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { speechState.stopListening() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    border = BorderStroke(1.5.dp, CyberYellow),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Durdur", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isPaper) PaperSurface else CyberDarkSurface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    chatViewModel.inputText.value = it
                    SoundPoolManager.playKeyboardClick()
                },
                placeholder = { Text("GEN AI'a soru sor veya konuş...", color = if (isPaper) PaperTextMuted else TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (speechState.isListening) Color(0xFFD90429) else (if (isPaper) PaperBorder else CyberYellow),
                    unfocusedBorderColor = if (speechState.isListening) CyberYellow else (if (isPaper) PaperBorder else CyberCardBorder),
                    focusedTextColor = if (isPaper) PaperText else Color.White,
                    unfocusedTextColor = if (isPaper) PaperText else Color.White,
                    focusedContainerColor = if (isPaper) PaperBg else CyberBlack,
                    unfocusedContainerColor = if (isPaper) PaperBg else CyberBlack
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    SoundPoolManager.playNotification()
                    val ctxBlock = ChatContextCache.buildPromptContext(context, inputText)
                    chatViewModel.sendMessage(chatContextSummary = ctxBlock)
                })
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Speech-to-Text Microphone Button (minimum 48dp accessible touch target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (speechState.isListening) Color(0xFFB00018) else (if (isPaper) PaperBorder else CyberCardBg))
                    .border(
                        width = if (speechState.isListening) 2.dp else 1.dp,
                        color = if (speechState.isListening) CyberYellow else (if (isPaper) PaperBorder else CyberCardBorder),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        if (speechState.isListening) {
                            speechState.stopListening()
                        } else {
                            speechState.startListening()
                        }
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ai_mic_button")
                ) {
                    Icon(
                        imageVector = if (speechState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (speechState.isListening) "Ses kaydını durdur" else "Mikrofon ile konuş",
                        tint = if (speechState.isListening) Color.White else (if (isPaper) PaperText else CyberYellow),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isPaper) PaperText else CyberYellow),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isGenerating) {
                            SoundPoolManager.playMechanicalClick()
                            chatViewModel.cancelGeneration()
                        } else {
                            SoundPoolManager.playNotification()
                            val ctxBlock = ChatContextCache.buildPromptContext(context, inputText)
                            chatViewModel.sendMessage(chatContextSummary = ctxBlock)
                        }
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isGenerating) "Yanıtı durdur" else "Gönder",
                        tint = if (isPaper) PaperBg else CyberBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showHistoryDialog) {
            GeminiAiHistoryDialog(
                sessions = sessions,
                activeSessionId = activeSessionId,
                onSelectSession = { sid ->
                    chatViewModel.loadSession(sid)
                },
                onStartNewChat = {
                    chatViewModel.startNewChat()
                },
                onDeleteSession = { sid ->
                    chatViewModel.deleteSession(sid)
                },
                onRenameSession = { sid, newTitle ->
                    chatViewModel.renameSession(sid, newTitle)
                },
                onClearAll = {
                    chatViewModel.clearAllSessions()
                },
                onDismiss = {
                    showHistoryDialog = false
                },
                isPaper = isPaper
            )
        }
    }
}

/**
 * Tab 3: Ayarlar Screen (Exact 1:1 match to Frame 00:10 - 00:12)
 */
@Composable
private fun AyarlarTabContent(
    currentUser: UserEntity?,
    cloudUserEmail: String?,
    cloudHandle: String?,
    selectedLanguage: com.example.data.LanguageOption,
    isPaperMode: Boolean,
    appThemeMode: AppThemeMode,
    fontScaleFactor: Float,
    zoomEnabled: Boolean,
    zoomScaleFactor: Float,
    hasAppLock: Boolean,
    isPinLockMode: Boolean,
    lockOnBackground: Boolean,
    lockOnPanic: Boolean,
    isCallActive: Boolean,
    isAiGenerating: Boolean,
    onLanguageSelect: (com.example.data.LanguageOption) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onZoomEnabledChange: (Boolean) -> Unit,
    onZoomScaleChange: (Float) -> Unit,
    onLockOnBackgroundChange: (Boolean) -> Unit,
    onLockOnPanicChange: (Boolean) -> Unit,
    onLockNow: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onTogglePaperMode: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenFreeServer: () -> Unit,
    onOpenDataPrivacy: () -> Unit,
    onOpenMasks: () -> Unit,
    onOpenAppLock: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenShowcase: () -> Unit,
    onOpenMiniGames: () -> Unit,
    onReplaySplash: () -> Unit,
    onCloseAllSessions: () -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.util.LocalAppStrings.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val serverRunning by LocalMeshServer.isRunning.collectAsState()
    val serverUrl by LocalMeshServer.serverUrl.collectAsState()
    val linkedDevices by com.example.util.LinkedDevicesManager.devices.collectAsState()
    val activePairCode by com.example.util.LinkedDevicesManager.activePairingCode.collectAsState()
    val parentalState by com.example.util.ParentalControlManager.state.collectAsState()
    val observedChild by com.example.util.ParentalControlManager.observedChildState.collectAsState()
    val profanityEnabled by com.example.util.ProfanityFilter.isFilterEnabled.collectAsState()
    val isSpeaking by GenAiSpeechManager.isSpeaking.collectAsState()
    val isMusicPlaying by com.example.util.BackgroundMusicManager.isPlaying.collectAsState()
    val liveLocations by com.example.util.LiveLocationManager.activeLocalSessions.collectAsState()
    val openSessionCount = (if (serverRunning) 1 else 0) +
        linkedDevices.size +
        (if (isSpeaking) 1 else 0) +
        (if (isMusicPlaying) 1 else 0) +
        (if (isCallActive) 1 else 0) +
        (if (isAiGenerating) 1 else 0) +
        liveLocations.size

    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showCyberWidgetDialog by remember { mutableStateOf(false) }
    var showCamouflageDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLinkedDevicesDialog by remember { mutableStateOf(false) }
    var showParentalDialog by remember { mutableStateOf(false) }
    var showProfanityChallengeDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.util.FeedbackAnalyticsManager.init(context)
    }
    val feedbackSummary by com.example.util.FeedbackAnalyticsManager.summary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "GENERAL CHAT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else CyberYellow,
            letterSpacing = 1.sp
        )
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isPaperMode) PaperText else Color.White
        )
        Text(
            text = strings.settingsSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = if (isPaperMode) PaperTextMuted else TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Hesap ve sunucu
        Text(
            text = strings.settingsSecAccount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 1: Hesap
        SettingActionCard(
            title = "Hesap",
            subtitle = when {
                cloudUserEmail != null -> "Google Bulut: $cloudUserEmail (${cloudHandle?.let { "@$it" } ?: "hazır"})"
                currentUser != null -> currentUser.email
                else -> "Hesap bağlı değil"
            },
            buttonLabel = if (currentUser != null || cloudUserEmail != null) "Profil" else "Giriş",
            onAction = onOpenAuth
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 2: 100% Free Built-In Server & Global Cloud
        SettingActionCard(
            title = "İnternet Bulutu & Yerel Sunucu",
            subtitle = when {
                cloudUserEmail != null -> "İnternet Bulutu AKTİF (@${cloudHandle ?: "kimlik"}) · Yerel: ${if (serverRunning) serverUrl else "Kapalı"}"
                serverRunning -> "Yerel Sunucu AKTİF · $serverUrl (İnternet bulutu için Google'a bağlan)"
                else -> "Farklı ağlardan sohbet için Google Bulutu'nu veya aynı Wi-Fi'da Yerel Sunucu'yu aç"
            },
            buttonLabel = if (cloudUserEmail != null || serverRunning) "Yönet (Açık)" else "Sunucuyu Aç",
            buttonColor = CyberYellow,
            onAction = onOpenFreeServer
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 3: Bulut bağlantısı
        SettingActionCard(
            title = "Küresel Çapraz Cihaz Eşitlemesi",
            subtitle = if (cloudUserEmail != null) "Firebase Cloud Firestore aktif ($cloudUserEmail)" else "Bağlı değil · Farklı telefonlarla mesajlaşmak için Google ile bağlan",
            buttonLabel = if (cloudUserEmail != null) "Bağlı ✓" else "Bağlan",
            onAction = onOpenFreeServer
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Bağlı Cihazlar (Web / iOS / Android Aktarım)
        SettingActionCard(
            title = strings.settingsLinkedDevicesTitle,
            subtitle = "Eşleştirme Kodu: $activePairCode · Aktif Bağlı Cihaz: ${linkedDevices.size}",
            buttonLabel = "Cihazlar",
            buttonColor = CyberYellow,
            onAction = { showLinkedDevicesDialog = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Görünüm ve dil
        Text(
            text = strings.settingsSecAppearance,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = strings.settingsLanguageTitle,
            subtitle = "${selectedLanguage.flag} ${selectedLanguage.countryName} · ${selectedLanguage.languageName} [${selectedLanguage.code.uppercase()}]",
            buttonLabel = "Değiştir",
            buttonColor = CyberYellow,
            onAction = { showLanguageDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Renk teması & Ekran Frekansı
        SettingActionCard(
            title = "Ekran & Renk Frekansı",
            subtitle = "Aktif: ${appThemeMode.title} — ${appThemeMode.description}",
            buttonLabel = "Değiştir",
            buttonColor = CyberYellow,
            onAction = onOpenThemePicker
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Sohbet arka planı
        SettingActionCard(
            title = "Sohbet Duvar Kağıdı",
            subtitle = "Siber Izgara, Sunucu Odası, Neon Mor, Karbon ve Kişisel Fotoğraf.",
            buttonLabel = "Değiştir",
            buttonColor = CyberYellow,
            onAction = {
                com.example.util.SoundPoolManager.playMenuNav()
                showWallpaperDialog = true
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Ses ve Müzik Yönetim Merkezi
        SettingActionCard(
            title = "Ses ve Müzik Yönetim Merkezi",
            subtitle = "Stabil Siber Beat, Sakin Mod, Cihaz Müziği ve Tok Tuş Sesleri.",
            buttonLabel = "Yönet",
            buttonColor = CyberYellow,
            onAction = {
                com.example.util.SoundPoolManager.playMenuNav()
                showMusicDialog = true
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Kağıt Ekran (Paper Mode)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaperMode) PaperBorder else CyberCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Kağıt Ekran (Paper Mode)",
                        color = if (isPaperMode) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Mat mürekkep efektiyle göz yormayan kitap okuma rahatlığı.",
                        color = if (isPaperMode) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isPaperMode,
                    onCheckedChange = {
                        SoundPoolManager.playMechanicalClick()
                        onTogglePaperMode()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isPaperMode) PaperBg else CyberBlack,
                        checkedTrackColor = if (isPaperMode) PaperText else CyberYellow
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Yazı Boyutu Ayarı (In-app Font Size Customization)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaperMode) PaperBorder else CyberCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = if (isPaperMode) PaperText else CyberYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yazı Boyutu",
                            color = if (isPaperMode) PaperText else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "%${(fontScaleFactor * 100f).roundToInt()}",
                        color = if (isPaperMode) PaperText else CyberYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tüm uygulama metinlerinin ve sohbet balonlarının ölçeğini anlık ayarlar.",
                    color = if (isPaperMode) PaperTextMuted else TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = fontScaleFactor.coerceIn(0.85f, 1.45f),
                    onValueChange = { onFontScaleChange(it) },
                    valueRange = 0.85f..1.45f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isPaperMode) PaperText else CyberYellow,
                        activeTrackColor = if (isPaperMode) PaperText else CyberYellow,
                        inactiveTrackColor = if (isPaperMode) PaperBorder else CyberCardBorder
                    ),
                    modifier = Modifier.testTag("settings_font_scale_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        0.85f to "Küçük",
                        1.0f to "Normal",
                        1.2f to "Büyük",
                        1.4f to "Çok Büyük"
                    )
                    presets.forEach { (presetScale, label) ->
                        val isSelected = kotlin.math.abs(fontScaleFactor - presetScale) < 0.04f
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                onFontScaleChange(presetScale)
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isPaperMode) PaperText else CyberYellow,
                                selectedLabelColor = if (isPaperMode) PaperBg else CyberBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Ekran Yakınlaştırma (Zoom)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (zoomEnabled) CyberYellow else (if (isPaperMode) PaperBorder else CyberCardBorder)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = if (isPaperMode) PaperText else CyberYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ekran Yakınlaştırma (Zoom)",
                                color = if (isPaperMode) PaperText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (zoomEnabled) "Aktif ölçek: %${(zoomScaleFactor * 100f).roundToInt()} · Kapatılınca normale döner" else "Tüm ekranı istediğin kadar büyüt veya küçült",
                                color = if (isPaperMode) PaperTextMuted else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = zoomEnabled,
                        onCheckedChange = { enabled ->
                            SoundPoolManager.playMechanicalClick()
                            onZoomEnabledChange(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isPaperMode) PaperBg else CyberBlack,
                            checkedTrackColor = if (isPaperMode) PaperText else CyberYellow
                        ),
                        modifier = Modifier.testTag("settings_zoom_switch")
                    )
                }

                if (zoomEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = zoomScaleFactor.coerceIn(1.05f, 1.40f),
                        onValueChange = { onZoomScaleChange(it) },
                        valueRange = 1.05f..1.40f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isPaperMode) PaperText else CyberYellow,
                            activeTrackColor = if (isPaperMode) PaperText else CyberYellow,
                            inactiveTrackColor = if (isPaperMode) PaperBorder else CyberCardBorder
                        ),
                        modifier = Modifier.testTag("settings_zoom_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Güvenlik, ebeveyn ve oturum kontrolü
        Text(
            text = strings.settingsSecSafety,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Ebeveyn Denetimi & Kısıtlama (Firebase Destekli)
        SettingActionCard(
            title = strings.settingsParentalTitle,
            subtitle = when {
                parentalState.restrictionsEnabled || parentalState.isMinorMode ->
                    "Kısıtlamalar AKTİF (${parentalState.parentEmail ?: "Firebase Doğrulamalı"}) · ${parentalState.usedMinutesToday}/${parentalState.dailyLimitMinutes} dk · Kod: ${parentalState.childPairingCode}"
                parentalState.isParentEmailVerified ->
                    "Ebeveyn Onaylı (${parentalState.parentEmail}) · Kısıtlamalar açılabilir · Kod: ${parentalState.childPairingCode}"
                else ->
                    "Ebeveyn e-postası ekle, Firebase üzerinden kod gönder ve kısıtlamaları yönet · Kod: ${parentalState.childPairingCode}"
            },
            buttonLabel = "Yönet",
            buttonColor = CyberYellow,
            onAction = {
                com.example.util.ParentalControlManager.refreshFromFirebase(context)
                com.example.util.ParentalControlManager.refreshObservedChildFromLocalIfMatching()
                showParentalDialog = true
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Küfür & Hakaret Filtresi (Doğrulamalı Kapatma)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (profanityEnabled) CyberCardBorder else NeonRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.settingsProfanityTitle,
                        color = if (isPaperMode) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (profanityEnabled) {
                            "Açık (Varsayılan) · Kapatmak için 18+ yaş ve güvenlik kodu gerekir."
                        } else {
                            "Kapalı (18+ Doğrulandı) · Tek dokunuşla yeniden açılabilir."
                        },
                        color = if (isPaperMode) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = profanityEnabled,
                    onCheckedChange = { wantsOn ->
                        SoundPoolManager.playMechanicalClick()
                        if (wantsOn) {
                            com.example.util.ProfanityFilter.enableFilter(context)
                        } else {
                            showProfanityChallengeDialog = true
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isPaperMode) PaperBg else CyberBlack,
                        checkedTrackColor = if (isPaperMode) PaperText else CyberYellow
                    ),
                    modifier = Modifier.testTag("settings_profanity_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-screen button: Gizlilik, KVKK ve Uyumluluk
        SettingActionCard(
            title = "Gizlilik, KVKK ve Uyumluluk",
            subtitle = "6698 sayılı KVKK aydınlatma metni, Madde 11 hakları, gelişmiş gizlilik ve hesap silme.",
            buttonLabel = "Aç",
            buttonColor = CyberYellow,
            onAction = onOpenDataPrivacy
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "10 Gizlilik Maskesi (Kamuflaj)",
            subtitle = "Hava Durumu, Not Defteri, Hesap Makinesi maskeleri.",
            buttonLabel = "Aç",
            onAction = onOpenMasks
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: 4 Haneli Ekran Kilidi PIN (Panik & Arka Plan Koruması)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (hasAppLock) NeonGreen.copy(alpha = 0.7f) else CyberYellow.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_pin_lock_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Ekran Kilidi PIN",
                            tint = if (hasAppLock) NeonGreen else CyberYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ekran Kilidi PIN (4 Haneli)",
                                color = if (isPaperMode) PaperText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when {
                                    hasAppLock && isPinLockMode -> "Aktif (4 Haneli Sayısal PIN · Tuzlu SHA-256)"
                                    hasAppLock -> "Aktif (Gelişmiş Şifre 4-20)"
                                    else -> "Kapalı — Panik veya arka plan kilidi için PIN belirleyin"
                                },
                                color = if (hasAppLock) NeonGreen else (if (isPaperMode) PaperTextMuted else TextSecondary),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Arka plana alınınca otomatik kilitle",
                        color = if (isPaperMode) PaperText else Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Switch(
                        checked = lockOnBackground,
                        onCheckedChange = {
                            SoundPoolManager.playMechanicalClick()
                            onLockOnBackgroundChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isPaperMode) PaperBg else CyberBlack,
                            checkedTrackColor = if (isPaperMode) PaperText else CyberYellow
                        ),
                        modifier = Modifier.testTag("settings_lock_on_background_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Panik butonunda PIN kilidini aç",
                        color = if (isPaperMode) PaperText else Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Switch(
                        checked = lockOnPanic,
                        onCheckedChange = {
                            SoundPoolManager.playMechanicalClick()
                            onLockOnPanicChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isPaperMode) PaperBg else CyberBlack,
                            checkedTrackColor = if (isPaperMode) PaperText else CyberYellow
                        ),
                        modifier = Modifier.testTag("settings_lock_on_panic_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            onOpenAppLock()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaperMode) PaperText else CyberYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("settings_open_pin_setup_button")
                    ) {
                        Text(
                            text = if (hasAppLock) "PIN Değiştir" else "4 Haneli PIN Ayarla",
                            color = if (isPaperMode) PaperBg else CyberBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    if (hasAppLock) {
                        OutlinedButton(
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                onLockNow()
                            },
                            border = BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_lock_now_button")
                        ) {
                            Text(
                                text = "Şimdi Kilitle",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Siber Mini Oyunlar (3 Oyun)",
            subtitle = "Cyber Runner (Koşu), Cyber Bird (Kuş Uçurma), Cyber XOX (Tic-Tac-Toe).",
            buttonLabel = "Oyna",
            buttonColor = CyberYellow,
            onAction = onOpenMiniGames
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Açılış Ekranı & Slogan",
            subtitle = "'BAĞLANTIDA KAL.' glitch açılış ekranı ve animasyonları tekrar izle.",
            buttonLabel = "Aç",
            buttonColor = CyberYellow,
            onAction = onReplaySplash
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "GENeral Chat Nasıl Kullanılır?",
            subtitle = "Animasyonlu donanım ve sistem tanıtım vitrini ve master kullanım kılavuzu.",
            buttonLabel = "İzle & Oku",
            buttonColor = CyberYellow,
            onAction = onOpenShowcase
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Masaüstü Widget & Hızlı Panik",
            subtitle = "Android ana ekran widget'ı ve acil durum panik butonu entegrasyonu.",
            buttonLabel = "Ayarla",
            buttonColor = CyberYellow,
            onAction = { showCyberWidgetDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Ekran Maskesi & Başlık Profili",
            subtitle = "Uygulama içi kamuflaj görünümü ve hızlı profil seçimi (Sistem ikonu ve GENeral Chat adı korunur).",
            buttonLabel = "Özelleştir",
            buttonColor = NeonCyan,
            onAction = { showCamouflageDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Uygulamayı Paylaş
        SettingActionCard(
            title = strings.settingsShareAppTitle,
            subtitle = "GENeral Chat bağlantısını WhatsApp, SMS veya sosyal medya ile arkadaşlarına gönder.",
            buttonLabel = "Paylaş",
            buttonColor = CyberYellow,
            onAction = {
                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                val shareText = "GENeral Chat — Uçtan uca şifreli mesajlaşma, WebRTC sesli/görüntülü arama, 4 haneli PIN/İMHA koruması ve GEN AI.\nResmî kanal & GitHub sürümleri: https://youtube.com/@GENeralYTB"
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "GENeral Chat")
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "GENeral Chat'i Paylaş"))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Çok fazla açık işlem / oturum varsa hepsini güvenle kapat
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (openSessionCount >= 2) Color(0xFF28100A) else if (isPaperMode) PaperSurface else CyberCardBg
            ),
            border = BorderStroke(1.dp, if (openSessionCount >= 2) NeonRed else CyberCardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_sessions_safety_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Güvenli Oturum & İşlem Kontrolü",
                    color = if (isPaperMode) PaperText else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (openSessionCount >= 2) {
                        "⚠️ $openSessionCount açık işlem/oturum algılandı (Bağlı cihazlar: ${linkedDevices.size}, LAN Sunucu: ${if (serverRunning) "Açık" else "Kapalı"}). Güvenli kullanım için hepsini tek dokunuşla kapatabilirsiniz."
                    } else {
                        "Aktif açık oturum/işlem sayısı: $openSessionCount · Yerel sunucu, bağlı web cihazları, canlı konum, sesli okuma ve aramaları güvenle sonlandırır."
                    },
                    color = if (openSessionCount >= 2) CyberYellow else (if (isPaperMode) PaperTextMuted else TextSecondary),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Button(
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        onCloseAllSessions()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A0B0B)),
                    border = BorderStroke(1.dp, NeonRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("settings_close_all_sessions_button")
                ) {
                    Text(
                        text = strings.settingsCloseSessionsTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Geri Bildirim, Öneri & Hata Bildirimi",
            subtitle = "Toplam ${feedbackSummary.totalCount} analiz kaydı · Görüş paylaş, öneri sun, hata bildir ve JSON/CSV analiz raporu çıkar.",
            buttonLabel = "Aç",
            buttonColor = CyberYellow,
            buttonTestTag = "settings_feedback_button",
            onAction = { showFeedbackDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingActionCard(
            title = "Hakkında & Destek",
            subtitle = "Sistem ayrıntıları, GEN AI destek ve acil durum protokolleri.",
            buttonLabel = "Görüntüle",
            onAction = onOpenAbout
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.settingsLanguageTitle, color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.data.defaultLanguages.forEach { option ->
                        val isCur = option.code == selectedLanguage.code && option.countryName == selectedLanguage.countryName
                        Card(
                            onClick = {
                                onLanguageSelect(option)
                                showLanguageDialog = false
                                android.widget.Toast.makeText(
                                    context,
                                    "${option.flag} ${option.countryName} · ${option.languageName} seçildi",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = if (isCur) Color(0xFF262000) else CyberBlack),
                            border = BorderStroke(1.dp, if (isCur) CyberYellow else CyberCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(option.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(option.countryName, color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${option.languageName} [${option.code.uppercase()}]", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                                if (isCur) Text("AKTİF ✓", color = CyberYellow, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Kapat", color = CyberYellow)
                }
            },
            containerColor = CyberCardBg
        )
    }

    if (showLinkedDevicesDialog) {
        var devName by remember { mutableStateOf("") }
        var devPlatform by remember { mutableStateOf("Web (Chrome/Safari/Firefox)") }
        AlertDialog(
            onDismissRequest = { showLinkedDevicesDialog = false },
            title = { Text(strings.settingsLinkedDevicesTitle, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1604)),
                        border = BorderStroke(1.dp, CyberYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AKTARIM & EŞLEŞTİRME KODU", color = CyberYellow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(activePairCode, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(activePairCode))
                                        android.widget.Toast.makeText(context, "Kod kopyalandı: $activePairCode", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, CyberYellow)
                                ) { Text("Kodu Kopyala", color = CyberYellow, fontSize = 11.sp) }
                                OutlinedButton(
                                    onClick = { com.example.util.LinkedDevicesManager.regeneratePairingCode() },
                                    border = BorderStroke(1.dp, NeonCyan)
                                ) { Text("Yenile", color = NeonCyan, fontSize = 11.sp) }
                            }
                            Text(
                                text = "Web Companion Portalı: ${if (serverRunning) "$serverUrl/companion" else "Yerel Sunucu Kapalı"}",
                                color = if (serverRunning) NeonGreen else TextSecondary,
                                fontSize = 11.sp
                            )
                            if (!serverRunning) {
                                Button(
                                    onClick = { LocalMeshServer.start(context = context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Web Companion Portalını Başlat", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = devName,
                        onValueChange = { devName = it },
                        placeholder = { Text("Yeni cihaz adı (ör. iPhone / MacBook Safari)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            com.example.util.LinkedDevicesManager.linkDevice(
                                deviceName = devName.ifBlank { "Web / Mobil İstemci" },
                                platform = devPlatform
                            )
                            devName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Bu Cihazı Eşle ve Aktar", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    if (linkedDevices.isNotEmpty()) {
                        Text("Bağlı Cihazlar (${linkedDevices.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        linkedDevices.forEach { dev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberBlack),
                                border = BorderStroke(1.dp, CyberCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(dev.deviceName, color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${dev.platform} · Kod: ${dev.pairingCode} · ${dev.linkedAt}", color = TextSecondary, fontSize = 10.sp)
                                    }
                                    TextButton(onClick = { com.example.util.LinkedDevicesManager.unlinkDevice(dev.id) }) {
                                        Text("Kes", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkedDevicesDialog = false }) { Text("Tamam", color = CyberYellow) }
            },
            containerColor = CyberCardBg
        )
    }

    if (showParentalDialog) {
        var ageEdit by remember { mutableStateOf((parentalState.ageYears ?: 18).toString()) }
        var parentCodeInput by remember { mutableStateOf(parentalState.supervisedChildCode.orEmpty()) }
        var limitMinutesEdit by remember { mutableStateOf(parentalState.dailyLimitMinutes.toString()) }
        var parentEmailInput by remember(parentalState.parentEmail, parentalState.verificationPendingEmail) {
            mutableStateOf(parentalState.verificationPendingEmail ?: parentalState.parentEmail.orEmpty())
        }
        var emailOtpInput by remember { mutableStateOf("") }
        var emailVerifyMessage by remember { mutableStateOf<String?>(null) }
        var lastGeneratedEmailOtp by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showParentalDialog = false },
            title = { Text(strings.settingsParentalTitle, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Kullanıcı Yaş Profili & Koruma", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageEdit,
                            onValueChange = { ageEdit = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Yaş", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val parsed = ageEdit.toIntOrNull()
                                if (parsed != null && parsed in 5..120) {
                                    com.example.util.ParentalControlManager.setUserAge(parsed, context)
                                    android.widget.Toast.makeText(
                                        context,
                                        if (parsed < 18) "18 yaş altı Ebeveyn Denetimi etkinleştirildi." else "Yetişkin (18+) profili güncellendi.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                        ) { Text("Kaydet", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    }
                    Text(
                        text = "Bu Cihaz Eşleştirme Kodu: ${parentalState.childPairingCode}\nDurum: ${if (parentalState.isMinorMode) "18 ALTI KORUMALI (${parentalState.usedMinutesToday}/${parentalState.dailyLimitMinutes} dk)" else "18+ Tam Erişim"}",
                        color = if (parentalState.isMinorMode) NeonCyan else NeonGreen,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = CyberCardBorder)
                    Text("2. Firebase Destekli Ebeveyn E-posta Doğrulaması & Kısıtlama", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (!parentalState.lastFirebaseStatus.isNullOrBlank()) {
                        Text(
                            text = "Bulut Durumu: ${parentalState.lastFirebaseStatus}",
                            color = if (parentalState.isSyncedWithFirebase) NeonGreen else NeonCyan,
                            fontSize = 10.sp
                        )
                    }
                    if (parentalState.isParentEmailVerified && !parentalState.parentEmail.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2316)),
                            border = BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Doğrulanmış Ebeveyn E-postası: ${parentalState.parentEmail} ✓",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ebeveyn Kısıtlamalarını Aktif Et", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Switch(
                                        checked = parentalState.restrictionsEnabled,
                                        onCheckedChange = { on ->
                                            com.example.util.ParentalControlManager.setRestrictionsEnabled(
                                                enabled = on,
                                                blockStrangers = parentalState.blockStrangerDmsForMinor,
                                                context = context
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = CyberBlack, checkedTrackColor = NeonGreen),
                                        modifier = Modifier.testTag("parental_restrictions_switch")
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tanımadıklarından DM Engelle", color = Color.White, fontSize = 11.sp)
                                    Switch(
                                        checked = parentalState.blockStrangerDmsForMinor,
                                        onCheckedChange = { block ->
                                            com.example.util.ParentalControlManager.setRestrictionsEnabled(
                                                enabled = parentalState.restrictionsEnabled,
                                                blockStrangers = block,
                                                context = context
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = CyberBlack, checkedTrackColor = CyberYellow)
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        com.example.util.ParentalControlManager.clearParentEmailVerification(context)
                                        emailVerifyMessage = "Ebeveyn doğrulaması sıfırlandı."
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Doğrulamayı Sıfırla", color = NeonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = parentEmailInput,
                        onValueChange = { parentEmailInput = it },
                        label = { Text("Ebeveyn E-posta Adresi", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("parental_email_input")
                    )
                    Button(
                        onClick = {
                            val reqRes = com.example.util.ParentalControlManager.requestParentEmailVerification(
                                rawEmail = parentEmailInput,
                                context = context
                            )
                            reqRes.onSuccess { code ->
                                lastGeneratedEmailOtp = code
                                emailVerifyMessage = "6 haneli doğrulama kodu oluşturuldu ve Firebase hesabınıza eşitlendi (10 dk geçerli). Göndermek için e-posta uygulamanız açılıyor."
                                val emailIntent = com.example.util.ParentalControlManager.buildVerificationEmailIntent(
                                    parentEmail = parentEmailInput.trim(),
                                    code = code
                                )
                                try {
                                    context.startActivity(
                                        android.content.Intent.createChooser(emailIntent, "Ebeveyn Doğrulama E-postası Gönder")
                                    )
                                } catch (_: Exception) {
                                    emailVerifyMessage = "E-posta istemcisi bulunamadı. Firebase & oturum doğrulama kodu: $code"
                                }
                            }.onFailure { err ->
                                emailVerifyMessage = err.localizedMessage
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier.fillMaxWidth().testTag("parental_send_code_button")
                    ) {
                        Text("Doğrulama Kodu Gönder (Firebase)", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = emailOtpInput,
                            onValueChange = { emailOtpInput = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text("6 Haneli Kod", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("parental_otp_input")
                        )
                        Button(
                            onClick = {
                                val verRes = com.example.util.ParentalControlManager.verifyParentEmailCode(
                                    enteredCode = emailOtpInput,
                                    context = context
                                )
                                verRes.onSuccess {
                                    emailOtpInput = ""
                                    lastGeneratedEmailOtp = null
                                    emailVerifyMessage = "Ebeveyn e-postası Firebase üzerinden doğrulandı ve kısıtlamalar etkinleştirildi!"
                                }.onFailure { err ->
                                    emailVerifyMessage = err.localizedMessage
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.testTag("parental_verify_code_button")
                        ) { Text("Onayla ve Kısıtla", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 11.sp) }
                    }

                    if (lastGeneratedEmailOtp != null) {
                        Text(
                            text = "Çevrimdışı yedek referans (oturum): ${lastGeneratedEmailOtp}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    if (emailVerifyMessage != null) {
                        Text(emailVerifyMessage!!, color = NeonCyan, fontSize = 11.sp)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141B29)),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("parental_email_help_article")
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Yardım Makalesi", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = "kod gelmedi mi?\nEğer gönderdiğimiz kod hala gelmemişse kullandığınız e-mail uygulamanızın sayfasını yenilemeyi deneyin",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    HorizontalDivider(color = CyberCardBorder)
                    Text("3. Ebeveyn İzleme Paneli (Canlı Aktivite)", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = parentCodeInput,
                        onValueChange = { parentCodeInput = it.uppercase() },
                        placeholder = { Text("Çocuğun kodunu gir (ör. ${parentalState.childPairingCode})", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { com.example.util.ParentalControlManager.linkParentToChildCode(parentCodeInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.weight(1f)
                        ) { Text("Çocuğu İzle", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 11.sp) }

                        OutlinedTextField(
                            value = limitMinutesEdit,
                            onValueChange = { limitMinutesEdit = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Dk Limit", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                        Button(
                            onClick = {
                                val lim = limitMinutesEdit.toIntOrNull()?.coerceIn(10, 480) ?: 60
                                com.example.util.ParentalControlManager.updateSupervisedChildLimit(lim)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                        ) { Text("Uygula", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }

                    val targetReport = observedChild ?: parentalState
                    Text("Mesajlaşılan Kişiler (${targetReport.contactedPeers.size}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    if (targetReport.contactedPeers.isEmpty()) {
                        Text("Henüz dış kişi mesajı yok.", color = TextMuted, fontSize = 11.sp)
                    } else {
                        targetReport.contactedPeers.take(5).forEach { c ->
                            Text("• [${c.time}] ${c.peerName} ${if (c.peerHandle.isNotBlank()) "(@${c.peerHandle})" else ""}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Text("Canlı Ekran Giriş/Çıkış Kayıtları (${targetReport.screenEvents.size}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    targetReport.screenEvents.take(6).forEach { ev ->
                        Text("• [${ev.time}] ${ev.screenName} — ${ev.action}", color = NeonCyan, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParentalDialog = false }) { Text("Kapat", color = CyberYellow) }
            },
            containerColor = CyberCardBg
        )
    }

    if (showProfanityChallengeDialog) {
        val expectedChallenge = remember { com.example.util.ProfanityFilter.generateChallengeCode() }
        var adultChecked by remember { mutableStateOf(false) }
        var enteredChallenge by remember { mutableStateOf("") }
        var challengeError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showProfanityChallengeDialog = false },
            title = { Text("Küfür Filtresini Kapatma Doğrulaması", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Küfür filtresini devre dışı bırakmak için 18 yaş ve üzeri olmanız ve aşağıdaki güvenlik doğrulama kodunu onaylamanız gerekir.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "DOĞRULAMA KODU: $expectedChallenge",
                        color = CyberYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = adultChecked,
                            onCheckedChange = { adultChecked = it }
                        )
                        Text("18 yaşından büyüğüm ve filtreyi kendi isteğimle kapatıyorum.", color = Color.White, fontSize = 11.sp)
                    }
                    OutlinedTextField(
                        value = enteredChallenge,
                        onValueChange = { enteredChallenge = it.filter { c -> c.isDigit() }.take(4) },
                        placeholder = { Text("4 haneli kodu girin", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (challengeError != null) {
                        Text(challengeError!!, color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = com.example.util.ProfanityFilter.requestDisableWithVerification(
                            context = context,
                            adultConfirmed = adultChecked,
                            expectedCode = expectedChallenge,
                            enteredCode = enteredChallenge
                        )
                        res.onSuccess {
                            showProfanityChallengeDialog = false
                            android.widget.Toast.makeText(context, "Küfür filtresi doğrulama ile kapatıldı.", android.widget.Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            challengeError = err.localizedMessage
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Doğrula ve Kapat", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfanityChallengeDialog = false }) {
                    Text("Filtreyi Açık Tut", color = NeonGreen)
                }
            },
            containerColor = CyberCardBg
        )
    }

    if (showCyberWidgetDialog) {
        CyberWidgetDialog(onDismiss = { showCyberWidgetDialog = false })
    }

    if (showCamouflageDialog) {
        CamouflageAppIdentityDialog(onDismiss = { showCamouflageDialog = false })
    }

    if (showWallpaperDialog) {
        com.example.ui.components.WallpaperSettingsDialog(
            onDismiss = { showWallpaperDialog = false }
        )
    }

    if (showMusicDialog) {
        com.example.ui.components.MusicSettingsDialog(
            onDismiss = { showMusicDialog = false }
        )
    }

    if (showFeedbackDialog) {
        com.example.ui.components.FeedbackModuleDialog(
            isCloudSignedIn = cloudUserEmail != null,
            onDismiss = { showFeedbackDialog = false }
        )
    }
}

@Composable
private fun SettingActionCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    buttonColor: Color = Color.White,
    buttonTestTag: String? = null,
    onAction: () -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isPaper) PaperText else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    SoundPoolManager.playMenuNav()
                    onAction()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaper) PaperBg else Color(0xFF262000)
                ),
                border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberYellow.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = if (buttonTestTag != null) Modifier.testTag(buttonTestTag) else Modifier
            ) {
                Text(
                    text = buttonLabel,
                    color = if (isPaper) PaperText else CyberYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Real Live WebRTC Audio & Video Call Modal with STUN ICE + SurfaceViewRenderer
 */
@Composable
private fun ActiveCallDialog(
    contactName: String,
    isVideo: Boolean,
    startedAtMs: Long,
    onEndCall: (String) -> Unit
) {
    val context = LocalContext.current
    val connectionStatus by WebRtcCallEngine.connectionStatus.collectAsState()
    val isConnected by WebRtcCallEngine.isConnected.collectAsState()
    val isCallEndedByRemote by WebRtcCallEngine.isCallEndedByRemote.collectAsState()
    val localVideoTrack by WebRtcCallEngine.localVideoTrack.collectAsState()
    val remoteVideoTrack by WebRtcCallEngine.remoteVideoTrack.collectAsState()

    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt().coerceAtLeast(0)
        }
    }

    val formattedDuration = String.format("%02d:%02d", callSeconds / 60, callSeconds % 60)

    LaunchedEffect(isCallEndedByRemote) {
        if (isCallEndedByRemote) {
            delay(900)
            onEndCall(formattedDuration)
        }
    }

    AlertDialog(
        onDismissRequest = { onEndCall(formattedDuration) },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.5.dp, if (isConnected) NeonGreen else CyberYellow, RoundedCornerShape(16.dp))
                    ) {
                        if (remoteVideoTrack != null) {
                            WebRtcVideoRendererView(
                                videoTrack = remoteVideoTrack,
                                mirror = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = CyberYellow,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Karşı kamera görüntüsü bekleniyor...",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (localVideoTrack != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(width = 86.dp, height = 116.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.5.dp, CyberYellow, RoundedCornerShape(10.dp))
                            ) {
                                WebRtcVideoRendererView(
                                    videoTrack = localVideoTrack,
                                    mirror = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CyberYellowContainer)
                            .border(2.dp, if (isConnected) NeonGreen else CyberYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = if (isConnected) NeonGreen else CyberYellow,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = connectionStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isConnected) NeonGreen else CyberYellow,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedDuration,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            WebRtcCallEngine.setMicMuted(isMuted)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) NeonRed else CyberCardBg)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mikrofon",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            WebRtcCallEngine.setSpeakerphone(context, isSpeakerOn)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) CyberYellow else CyberCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Hoparlör",
                            tint = if (isSpeakerOn) CyberBlack else Color.White
                        )
                    }

                    if (isVideo) {
                        IconButton(
                            onClick = { WebRtcCallEngine.switchCamera() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyberCardBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Kamera Değiştir",
                                tint = CyberYellow
                            )
                        }
                    }

                    IconButton(
                        onClick = { onEndCall(formattedDuration) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Aramayı Sonlandır",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = CyberDarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun WebRtcVideoRendererView(
    videoTrack: VideoTrack?,
    mirror: Boolean,
    modifier: Modifier = Modifier
) {
    val eglContext = remember { WebRtcCallEngine.getEglContext() }
    var rendererRef by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(videoTrack, rendererRef) {
        val renderer = rendererRef
        if (renderer != null && videoTrack != null) {
            try {
                videoTrack.addSink(renderer)
            } catch (_: Exception) {}
        }
        onDispose {
            if (renderer != null && videoTrack != null) {
                try {
                    videoTrack.removeSink(renderer)
                } catch (_: Exception) {}
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                if (eglContext != null) {
                    init(eglContext, null)
                }
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setMirror(mirror)
                setEnableHardwareScaler(true)
                rendererRef = this
            }
        },
        update = { view ->
            view.setMirror(mirror)
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                rendererRef?.release()
            } catch (_: Exception) {}
            rendererRef = null
        }
    }
}

@Composable
private fun FreeServerAndCloudDialog(
    cloudUserEmail: String?,
    cloudHandle: String?,
    cloudAuthLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onGoogleSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val isRunning by LocalMeshServer.isRunning.collectAsState()
    val serverUrl by LocalMeshServer.serverUrl.collectAsState()
    val requestCount by LocalMeshServer.requestCount.collectAsState()
    val serverMessages by LocalMeshServer.messages.collectAsState()
    val statusMessage by LocalMeshServer.statusMessage.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var hostMessageInput by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = CyberYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("İnternet Bulutu & Yerel Sunucu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Farklı ağlar arası bulut + Wi-Fi içi yerel sunucu", color = TextSecondary, fontSize = 11.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mode 1: Cross-Device Over-The-Internet Firebase Firestore
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (cloudUserEmail != null) Color(0xFF0F1E16) else CyberDarkSurface),
                    border = BorderStroke(1.dp, if (cloudUserEmail != null) NeonGreen else CyberYellow),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (cloudUserEmail != null) "1. İNTERNET BULUT SUNUCUSU: BAĞLI 🟢" else "1. İNTERNET BULUT SUNUCUSU: BAĞLI DEĞİL",
                            color = if (cloudUserEmail != null) NeonGreen else CyberYellow,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (cloudUserEmail != null) {
                                "Farklı şehir/Wi-Fi/mobil ağlardaki iki cihaz Firebase Cloud Firestore üzerinden WhatsApp gibi gerçek zamanlı mesajlaşabilir.\nHesap: $cloudUserEmail\nKimlik: @${cloudHandle ?: "hazır"}"
                            } else {
                                "Farklı ağlardaki iki cihazın internetten gerçek zamanlı konuşabilmesi için Google ile bağlan (Firebase Firestore Spark ücretsiz katman)."
                            },
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (cloudUserEmail == null) {
                            Button(
                                onClick = onGoogleSignIn,
                                enabled = !cloudAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                            ) {
                                Text(
                                    text = if (cloudAuthLoading) "Bağlanıyor..." else "Google ile Bağlan",
                                    color = CyberBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = onGoogleSignOut,
                                border = BorderStroke(1.dp, NeonRed),
                                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                            ) {
                                Text("Bulut Oturumunu Kapat", color = NeonRed, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Mode 2: LocalMeshServer (Same Wi-Fi/Hotspot 0 TRY HTTP server)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = BorderStroke(1.dp, if (isRunning) NeonCyan else CyberCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isRunning) "2. DAHİLİ YEREL SUNUCU: AKTİF (PORT 8765)" else "2. DAHİLİ YEREL SUNUCU (AYNI WI-FI): KAPALI",
                            color = if (isRunning) NeonCyan else TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusMessage,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isRunning) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "İstek: $requestCount · Mesaj: ${serverMessages.size}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(serverUrl))
                                    copiedNotice = true
                                },
                                border = BorderStroke(1.dp, CyberYellow),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (copiedNotice) "Kopyalandı ✓" else "Adresi Kopyala ($serverUrl)", color = CyberYellow, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        SoundPoolManager.playMechanicalClick()
                        if (isRunning) LocalMeshServer.stop() else LocalMeshServer.start(context = context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) NeonRed else CyberYellow
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("toggle_free_server_button")
                ) {
                    Text(
                        text = if (isRunning) "Yerel Sunucuyu Durdur" else "Yerel Wi-Fi Sunucusunu Başlat (Port 8765)",
                        color = if (isRunning) Color.White else CyberBlack,
                        fontWeight = FontWeight.Black
                    )
                }

                if (isRunning) {
                    Text(
                        text = "Canlı Yerel Sunucu Odası (Aynı Wi-Fi / Hotspot'taki cihazlar $serverUrl adresinden bağlanabilir):",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    if (serverMessages.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            border = BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                serverMessages.takeLast(6).forEach { m ->
                                    Text(
                                        text = "[${m.time}] ${m.sender}: ${m.text}",
                                        color = if (m.fromWebClient) NeonCyan else CyberYellow,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hostMessageInput,
                            onValueChange = { hostMessageInput = it },
                            placeholder = { Text("Sunucuya mesaj gönder...", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberYellow,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = CyberBlack,
                                unfocusedContainerColor = CyberBlack
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (hostMessageInput.isNotBlank()) {
                                    LocalMeshServer.postHostMessage("Ana Cihaz", hostMessageInput)
                                    hostMessageInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Gönder", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
            ) {
                Text("Tamam", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
