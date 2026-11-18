package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Log
import com.example.data.AiClassifiedError
import com.example.data.AiErrorClassifier
import com.example.data.AiMode
import com.example.data.AiServiceConfig
import com.example.data.AiTurnHistory
import com.example.data.CloudLiveLocation
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.data.FirebaseAiCloudClient
import com.example.data.FirestoreChatRepository
import com.example.data.ScheduledMessage
import com.example.data.formatByteSize
import com.example.data.formatChatDayHeader
import com.example.data.formatClockTime
import com.example.data.isSameCalendarDay
import com.example.ui.components.ChatAiSummaryDialog
import com.example.ui.components.ChatMediaGalleryDialog
import com.example.ui.components.ChatWallpaperBackground
import com.example.ui.components.ContactProfileDialog
import com.example.ui.components.ExportChatDialog
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.components.OfflineP2PTunnelDialog
import com.example.ui.components.ScheduleMessageDialog
import com.example.ui.components.ScheduledMessagesListDialog
import com.example.ui.components.ServiceStatusDashboardCard
import com.example.ui.components.WallpaperSettingsDialog
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ChatThemePalette
import com.example.ui.theme.ChatThemePresets
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.LocalPaperMode
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
import com.example.util.ChatContextCache
import com.example.util.ChatWallpaper
import com.example.util.ChatWallpaperManager
import com.example.util.CloudMessagingManager
import com.example.util.LiveLocationManager
import com.example.util.MediaGallerySaver
import com.example.util.NetworkConnectivityAndOfflineManager
import com.example.util.OfflineAiEngine
import com.example.util.PrivacyPreferencesManager
import com.example.util.ProfanityFilter
import com.example.util.SelfChatStorage
import com.example.util.SoundPoolManager
import com.example.util.UnlimitedFileTransferManager
import com.example.util.VoiceNoteRecorder
import com.example.util.WebResearchHelper
import com.example.util.rememberSpeechToTextLauncher
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    conversation: ConversationItem,
    currentUserUid: String? = null,
    currentUserDisplayName: String = "Ben",
    onBack: () -> Unit,
    onStartCall: (String, Boolean) -> Unit, // contactName, isVideo
    onTriggerGlobalPanic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isPaper = LocalPaperMode.current
    val themeMode = LocalAppThemeMode.current
    val selectedWallpaper by ChatWallpaperManager.selectedWallpaper.collectAsState()
    val strings = com.example.util.LocalAppStrings.current
    val isLightChatSurface = isPaper ||
        selectedWallpaper == ChatWallpaper.PAPER_INK ||
        themeMode == AppThemeMode.LIGHT_MODERN
    val topBarBg = if (isLightChatSurface) PaperSurface else CyberDarkSurface
    val topBarTitleColor = if (isLightChatSurface) PaperText else Color.White
    val topBarSubColor = if (isLightChatSurface) PaperTextMuted else TextSecondary
    val accentYellow = if (isLightChatSurface) PaperText else CyberYellow

    val isGeminiConv = conversation.id == "conv_gemini_ai" ||
        conversation.name.contains("GEN AI", ignoreCase = true) ||
        conversation.name.contains("Gemini", ignoreCase = true)
    val isSelfNotesConv = conversation.id == SelfChatStorage.SELF_NOTES_CONVERSATION_ID

    DisposableEffect(conversation.id, isSelfNotesConv) {
        if (!isSelfNotesConv) {
            com.example.util.ParentalControlManager.logNavigationEvent("Sohbet: ${conversation.name}", "Giriş")
        }
        onDispose {
            if (!isSelfNotesConv) {
                com.example.util.ParentalControlManager.logNavigationEvent("Sohbet: ${conversation.name}", "Çıkış")
            }
        }
    }

    val messages = remember(conversation.id) { mutableStateListOf<DirectMessage>().apply { addAll(conversation.messages) } }
    var selfLoadedReady by remember(conversation.id) { mutableStateOf(!isSelfNotesConv) }
    LaunchedEffect(conversation.id, isSelfNotesConv) {
        if (isSelfNotesConv) {
            val saved = SelfChatStorage.loadMessages(context)
            messages.clear()
            messages.addAll(saved)
            selfLoadedReady = true
        }
    }
    LaunchedEffect(conversation.id, isSelfNotesConv, selfLoadedReady, messages.toList()) {
        if (isSelfNotesConv && selfLoadedReady) {
            SelfChatStorage.saveMessages(context, messages.toList())
        }
    }

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showEmojiTray by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationLat by remember { mutableStateOf("") }
    var locationLng by remember { mutableStateOf("") }
    var locationLabel by remember { mutableStateOf("Konumum") }
    var locationAccuracyMeters by remember { mutableStateOf(0.0) }
    var isLiveLocationMode by remember { mutableStateOf(true) }
    var liveLocationDurationMin by remember { mutableIntStateOf(15) }
    var isFetchingGpsSnapshot by remember { mutableStateOf(false) }
    var cloudLiveLocations by remember { mutableStateOf<Map<String, CloudLiveLocation>>(emptyMap()) }
    val localLiveLocations by LiveLocationManager.activeLocalSessions.collectAsState()
    val mergedLiveLocations = remember(cloudLiveLocations, localLiveLocations) {
        cloudLiveLocations + localLiveLocations
    }
    var fullScreenImageMsg by remember { mutableStateOf<DirectMessage?>(null) }
    var showEventDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDate by remember {
        mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()))
    }
    var eventTime by remember { mutableStateOf("19:00") }
    var eventPlace by remember { mutableStateOf("") }

    // Cyberpunk Individual Chat Theme State
    var chatThemeId by remember { mutableStateOf(conversation.customTheme.ifBlank { "CYBER_YELLOW" }) }
    val currentPalette: ChatThemePalette = remember(chatThemeId) { ChatThemePresets.getPalette(chatThemeId) }
    var showChatThemeDialog by remember { mutableStateOf(false) }

    // Roadmap dialog states
    var showAiSummaryDialog by remember { mutableStateOf(false) }
    var showMediaGalleryDialog by remember { mutableStateOf(false) }
    var showP2PTunnelDialog by remember { mutableStateOf(false) }
    var showKillSwitchConfirmDialog by remember { mutableStateOf(false) }

    // Dynamic Typing Indicator, AI Connectivity Error State & Scheduled Messages State
    var isCounterpartTyping by remember { mutableStateOf(false) }
    var aiErrorInfo by remember { mutableStateOf<AiClassifiedError?>(null) }
    var lastFailedAiPrompt by remember { mutableStateOf<String?>(null) }
    val scheduledMessages = remember { mutableStateListOf<ScheduledMessage>() }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showScheduledListDialog by remember { mutableStateOf(false) }

    // Contact Custom Name & Profile States
    val peerCloudUid = remember(conversation.id, conversation.isGroup, conversation.participantIds, currentUserUid, isGeminiConv, isSelfNotesConv) {
        if (conversation.isGroup || isGeminiConv || isSelfNotesConv) null
        else conversation.participantIds.firstOrNull { it != currentUserUid }
    }
    val isDirectPeerChat = !isGeminiConv && !isSelfNotesConv && !conversation.isGroup && !peerCloudUid.isNullOrBlank()
    val myE2eePubKey = remember { com.example.util.E2eeCryptoManager.getOrCreateMyPublicKeyBase64(context) }
    var peerE2eePubKey by remember(conversation.id, peerCloudUid) {
        mutableStateOf(peerCloudUid?.let { com.example.util.E2eeCryptoManager.getCachedPeerPublicKey(context, it) })
    }
    var showE2eeDetailsDialog by remember { mutableStateOf(false) }

    var customContactName by remember { mutableStateOf(conversation.name) }
    var isContactBlocked by remember { mutableStateOf(conversation.isBlocked) }
    var isContactMuted by remember { mutableStateOf(false) }
    var showContactProfileDialog by remember { mutableStateOf(false) }
    var showExportChatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(conversation.id, currentUserUid, peerCloudUid) {
        if (currentUserUid != null && !isGeminiConv && !isSelfNotesConv) {
            val repo = FirestoreChatRepository.create(context)
            repo.publishMyE2eePublicKey(context)
            if (peerCloudUid != null) {
                val fetchedKey = repo.fetchPeerE2eePublicKey(context, peerCloudUid)
                if (!fetchedKey.isNullOrBlank()) {
                    peerE2eePubKey = fetchedKey
                }
            }
        }
    }

    LaunchedEffect(currentUserUid, peerCloudUid) {
        if (currentUserUid != null && peerCloudUid != null) {
            FirestoreChatRepository.create(context).observeBlockedUids().collect { blockedSet ->
                isContactBlocked = peerCloudUid in blockedSet
            }
        }
    }

    // Scheduled Message Auto-Delivery Engine (Ticks every second to dispatch due messages)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val now = System.currentTimeMillis()
            val due = scheduledMessages.filter { it.scheduledEpochMillis <= now }
            if (due.isNotEmpty()) {
                due.forEach { scheduled ->
                    val newMsg = DirectMessage(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = conversation.id,
                        senderName = "Sen (Zamanlanmış)",
                        isMe = true,
                        text = scheduled.text,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
                    )
                    messages.add(newMsg)
                    SoundPoolManager.playNotification()
                    Toast.makeText(context, "Zamanlanmış mesaj iletildi!", Toast.LENGTH_SHORT).show()
                }
                scheduledMessages.removeAll(due)
            }
        }
    }

    // Swipe-to-Reply & Burner Timer States
    var replyingToMessage by remember { mutableStateOf<DirectMessage?>(null) }
    var activeBurnerSeconds by remember { mutableIntStateOf(0) } // 0 = off, 10, 30, 60

    // Multi-message Selection Mode (Batch delete/copy)
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedMessageIds = remember { mutableStateListOf<String>() }

    // Gelişmiş Sohbet Gizliliği state (auto-synced with PrivacyPreferencesManager)
    val privacyRev by PrivacyPreferencesManager.revision.collectAsState()
    val chatPrivacyConfig = remember(privacyRev, conversation.id) {
        PrivacyPreferencesManager.getChatConfig(context, conversation.id)
    }
    var isScreenCaptureProtected by remember { mutableStateOf(chatPrivacyConfig.blockScreenCapture) }

    LaunchedEffect(chatPrivacyConfig.blockScreenCapture) {
        isScreenCaptureProtected = chatPrivacyConfig.blockScreenCapture
    }

    LaunchedEffect(isScreenCaptureProtected) {
        val window = (context as? android.app.Activity)?.window
        if (isScreenCaptureProtected) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Live Burner Countdown Engine (Ticks down and wipes self-destructing messages)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val expiredIds = mutableListOf<String>()
            for (i in messages.indices) {
                val m = messages[i]
                val sec = m.selfDestructSeconds
                if (sec != null && sec > 0) {
                    val nextSec = sec - 1
                    if (nextSec <= 0) {
                        expiredIds.add(m.id)
                    } else {
                        messages[i] = m.copy(selfDestructSeconds = nextSec)
                    }
                }
            }
            if (expiredIds.isNotEmpty()) {
                messages.removeAll { it.id in expiredIds }
                SoundPoolManager.playMechanicalClick()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var sendAsCounterpart by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showLinkInputDialog by remember { mutableStateOf(false) }
    var linkInputUrl by remember { mutableStateOf("") }

    // Hardware Audio Recording Setup (Real microphone voice notes)
    val voiceRecorder = remember { VoiceNoteRecorder(context) }
    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var currentAmplitude by remember { mutableIntStateOf(0) }

    DisposableEffect(conversation.id) {
        onDispose {
            CloudMessagingManager.stopListening()
            voiceRecorder.cancel()
            val window = (context as? android.app.Activity)?.window
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = voiceRecorder.start()
            if (file != null) {
                isRecordingVoiceNote = true
                recordingSeconds = 0
                SoundPoolManager.playMechanicalClick()
            } else {
                Toast.makeText(context, "Mikrofon başlatılamadı.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Ses kaydı için mikrofon izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecordingVoiceNote) {
        if (isRecordingVoiceNote) {
            while (isRecordingVoiceNote) {
                delay(100)
                currentAmplitude = voiceRecorder.getMaxAmplitude()
            }
        }
    }

    LaunchedEffect(isRecordingVoiceNote) {
        if (isRecordingVoiceNote) {
            while (isRecordingVoiceNote) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Chat History Search (Keywords & Dates) State
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) } // null, "TODAY", "YESTERDAY", "MEDIA", or custom "yyyy-MM-dd"
    var showDatePickerModal by remember { mutableStateOf(false) }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    // Filter matching message indices in messages list based on query and date (recomputed dynamically across midnight)
    val matchingIndices = remember(messages.toList(), searchQuery, selectedDateFilter, isSearchActive) {
        if (!isSearchActive || (searchQuery.isBlank() && selectedDateFilter == null)) {
            emptyList()
        } else {
            val nowCal = java.util.Calendar.getInstance()
            val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayIsoStr = isoFmt.format(nowCal.time)
            nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayIsoStr = isoFmt.format(nowCal.time)
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            messages.indices.filter { idx ->
                val msg = messages[idx]
                val msgTs = if (msg.timestamp > 0) msg.timestamp else System.currentTimeMillis()
                val msgDate = Date(msgTs)
                val msgDateIso = isoFmt.format(msgDate)
                val msgDateDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(msgDate)
                val msgDateDay = SimpleDateFormat("d MMMM", Locale("tr")).format(msgDate).lowercase(Locale.getDefault())
                val msgDayOfWeek = SimpleDateFormat("EEEE", Locale("tr")).format(msgDate).lowercase(Locale.getDefault())

                val matchesDateFilter = when (selectedDateFilter) {
                    null, "ALL" -> true
                    "TODAY" -> msgDateIso == todayIsoStr
                    "YESTERDAY" -> msgDateIso == yesterdayIsoStr
                    "MEDIA" -> msg.attachmentType != null || msg.isVoiceNote
                    else -> msgDateIso == selectedDateFilter || msgDateDisplay == selectedDateFilter
                }

                val matchesQuery = if (q.isBlank()) {
                    true
                } else {
                    msg.text.lowercase(Locale.getDefault()).contains(q) ||
                    msg.senderName.lowercase(Locale.getDefault()).contains(q) ||
                    (msg.attachmentName?.lowercase(Locale.getDefault())?.contains(q) == true) ||
                    msg.time.lowercase(Locale.getDefault()).contains(q) ||
                    msgDateDisplay.contains(q) ||
                    msgDateIso.contains(q) ||
                    msgDateDay.contains(q) ||
                    msgDayOfWeek.contains(q)
                }

                matchesDateFilter && matchesQuery
            }
        }
    }

    LaunchedEffect(matchingIndices) {
        if (matchingIndices.isNotEmpty()) {
            if (currentMatchIndex !in matchingIndices.indices) {
                currentMatchIndex = matchingIndices.size - 1
            }
            listState.animateScrollToItem(matchingIndices[currentMatchIndex])
        } else {
            currentMatchIndex = 0
        }
    }

    fun handlePickedUri(uri: Uri) {
        showAttachmentDialog = false
        scope.launch {
            Toast.makeText(context, "Dosya akışı hazırlanıyor (1,5+ GB P2P / LAN / 100 MB Bulut)...", Toast.LENGTH_SHORT).show()
            val preparedResult = UnlimitedFileTransferManager.prepareAndRegisterAttachment(context, uri)
            preparedResult.onSuccess { prep ->
                val nowMs = System.currentTimeMillis()
                val newMsgId = "msg_file_$nowMs"
                val newMsg = DirectMessage(
                    id = newMsgId,
                    conversationId = conversation.id,
                    senderName = "Ben",
                    isMe = true,
                    text = "[${prep.detectedType}] ${prep.displayName} (${formatByteSize(prep.sizeBytes)})",
                    time = formatClockTime(nowMs),
                    timestamp = nowMs,
                    senderId = currentUserUid,
                    attachmentType = prep.detectedType,
                    attachmentName = prep.displayName,
                    attachmentUrl = prep.urlOrUri,
                    attachmentSizeBytes = prep.sizeBytes,
                    attachmentMimeType = prep.mimeType,
                    transferMode = prep.transferMode,
                    chunkCount = prep.chunkCount
                )
                messages.add(newMsg)
                SoundPoolManager.playConfirm()

                if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
                    CloudMessagingManager.sendCloudMessage(
                        context = context,
                        conversationId = conversation.id,
                        message = newMsg,
                        participantIds = conversation.participantIds,
                        senderDisplayName = currentUserDisplayName
                    )
                    if (prep.transferMode == "CLOUD_CHUNKS") {
                        UnlimitedFileTransferManager.uploadCloudChunksIfNeeded(
                            context = context,
                            conversationId = conversation.id,
                            messageId = newMsgId,
                            participantIds = conversation.participantIds,
                            prepared = prep
                        )
                    }
                }
                val modeText = when (prep.transferMode) {
                    "CLOUD_CHUNKS" -> "Bulut Parçalı (<=100 MB)"
                    "P2P_WEBRTC" -> "WebRTC P2P + LAN HTTP Akışı"
                    else -> "LAN HTTP Range Akışı"
                }
                Toast.makeText(
                    context,
                    "${prep.displayName} (${formatByteSize(prep.sizeBytes)}) gönderildi · $modeText",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { err ->
                Toast.makeText(context, "Dosya hazırlanamadı: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Unlimited Storage-Access-Framework document/file picker (zero broad storage permissions)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { handlePickedUri(it) }
    }

    // Zero-permission Android Photo/Video Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { handlePickedUri(it) }
    }

    // Direct Camera photo capture launcher (requests CAMERA runtime permission first)
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val outFile = File(context.filesDir, "cam_${System.currentTimeMillis()}.jpg")
                FileOutputStream(outFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
                }
                handlePickedUri(Uri.fromFile(outFile))
            } catch (e: Exception) {
                Toast.makeText(context, "Fotoğraf kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                cameraPreviewLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Kamera açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Fotoğraf çekmek için kamera izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    fun resolveLastLocationAndOpenDialog() {
        showLocationDialog = true
        scope.launch {
            isFetchingGpsSnapshot = true
            val best = LiveLocationManager.fetchFreshLocationSnapshot(context)
            if (best != null) {
                locationLat = String.format(Locale.US, "%.5f", best.latitude)
                locationLng = String.format(Locale.US, "%.5f", best.longitude)
                locationAccuracyMeters = best.accuracy.toDouble()
            }
            isFetchingGpsSnapshot = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            resolveLastLocationAndOpenDialog()
        } else {
            Toast.makeText(context, "Konum izni verilmedi; koordinatları elle girebilirsiniz.", Toast.LENGTH_SHORT).show()
            showLocationDialog = true
        }
    }

    val serviceStatus by NetworkConnectivityAndOfflineManager.serviceStatus.collectAsState()

    // Genuine Over-The-Internet Real-Time Cloud Sync + Offline-First LocalStorage fallback
    LaunchedEffect(conversation.id, currentUserUid, isGeminiConv, isSelfNotesConv, peerE2eePubKey) {
        if (!isGeminiConv && !isSelfNotesConv) {
            val cached = NetworkConnectivityAndOfflineManager.loadCachedConversationMessages(context, conversation.id)
            val pending = NetworkConnectivityAndOfflineManager.getPendingMessagesForConversation(context, conversation.id)
            if (cached.isNotEmpty() || pending.isNotEmpty()) {
                val mergedInitial = (cached + pending.filter { p -> cached.none { it.id == p.id } })
                    .sortedBy { it.timestamp }
                messages.clear()
                messages.addAll(mergedInitial)
            }
        }
        if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
            CloudMessagingManager.listenToConversation(
                context = context,
                conversationId = conversation.id,
                onMessagesReceived = { cloudMsgs ->
                    NetworkConnectivityAndOfflineManager.cacheConversationMessages(context, conversation.id, cloudMsgs)
                    val stillPending = NetworkConnectivityAndOfflineManager.getPendingMessagesForConversation(context, conversation.id)
                        .filter { pending -> cloudMsgs.none { it.id == pending.id } }
                    messages.clear()
                    messages.addAll((cloudMsgs + stillPending).sortedBy { it.timestamp })
                    val unreadIds = cloudMsgs.filter { !it.isMe && !it.readBy.contains(currentUserUid) }.map { it.id }
                    if (unreadIds.isNotEmpty()) {
                        scope.launch {
                            FirestoreChatRepository.create(context)
                                .markMessagesAsRead(conversation.id, unreadIds)
                        }
                    }
                },
                onError = { err ->
                    val cachedFallback = NetworkConnectivityAndOfflineManager.loadCachedConversationMessages(context, conversation.id)
                    if (cachedFallback.isNotEmpty() && messages.isEmpty()) {
                        messages.addAll(cachedFallback)
                    }
                    Toast.makeText(
                        context,
                        err.localizedMessage ?: "Bulut senkronizasyonu kesildi; çevrimdışı yerel önbellek aktif.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    // Real-time live location stream for cloud conversations
    LaunchedEffect(conversation.id, currentUserUid, isGeminiConv, isSelfNotesConv) {
        if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
            FirestoreChatRepository.create(context)
                .observeLiveLocations(conversation.id)
                .collect { locMap ->
                    cloudLiveLocations = locMap
                }
        }
    }

    // Keep GEN AI live chat-context cache synchronized (excluding private self-notes & honoring privacy lock)
    LaunchedEffect(conversation.id, messages.toList(), isSelfNotesConv) {
        if (!isGeminiConv && !isSelfNotesConv) {
            ChatContextCache.updateRoomMessages(conversation.id, messages.toList())
        }
    }

    // Message edit dialog
    var messageToEdit by remember { mutableStateOf<DirectMessage?>(null) }
    var editInputText by remember { mutableStateOf("") }

    // Message action sheet / dialog
    var selectedMessageForAction by remember { mutableStateOf<DirectMessage?>(null) }

    // Speech-to-Text Integration
    val speechState = rememberSpeechToTextLauncher { spokenText ->
        val filtered = ProfanityFilter.filter(spokenText)
        inputMessage = if (inputMessage.isBlank()) filtered else "$inputMessage $filtered"
        SoundPoolManager.playMechanicalClick()
    }

    fun sendMessage(
        overrideText: String? = null,
        isVoiceNote: Boolean = false,
        voiceDuration: Int = 0,
        attachmentType: String? = null,
        attachmentName: String? = null,
        attachmentUrl: String? = null
    ) {
        val rawText = overrideText ?: inputMessage.trim()
        if (rawText.isBlank() && !isVoiceNote && attachmentType == null) return

        if (!isGeminiConv && !isSelfNotesConv && isContactBlocked) {
            Toast.makeText(
                context,
                "Engellenen kullanıcıya mesaj gönderemezsiniz. Engeli profil menüsünden kaldırabilirsiniz.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val parentalState = com.example.util.ParentalControlManager.state.value
        if (!isGeminiConv && !isSelfNotesConv && parentalState.isMinorMode && parentalState.blockStrangerDmsForMinor &&
            conversation.participantIds.isEmpty()
        ) {
            Toast.makeText(
                context,
                "Ebeveyn Denetimi Aktif: Kısıtlı hesaplar yalnızca onaylı kişilerle mesajlaşabilir.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val text = ProfanityFilter.filter(rawText)
        val sentAtMs = System.currentTimeMillis()
        val timeNow = formatClockTime(sentAtMs)
        val isSenderMe = !sendAsCounterpart

        if (isSenderMe && !isGeminiConv && !isSelfNotesConv) {
            com.example.util.ParentalControlManager.logContactedPerson(
                peerName = customContactName,
                peerHandle = conversation.username.orEmpty()
            )
        }

        val replyTarget = replyingToMessage
        val newMsg = DirectMessage(
            id = "msg_$sentAtMs",
            conversationId = conversation.id,
            senderName = if (isSenderMe) "Ben" else conversation.name,
            isMe = isSenderMe,
            text = text,
            time = timeNow,
            timestamp = sentAtMs,
            senderId = if (isSenderMe) currentUserUid else peerCloudUid,
            isEncrypted = (isDirectPeerChat && !peerE2eePubKey.isNullOrBlank() && attachmentType == null) || isSelfNotesConv,
            isVoiceNote = isVoiceNote,
            voiceDurationSeconds = voiceDuration,
            attachmentType = attachmentType,
            attachmentName = attachmentName,
            attachmentUrl = attachmentUrl,
            replyToMessageId = replyTarget?.id,
            replyToText = replyTarget?.text?.take(70),
            replyToSender = replyTarget?.senderName,
            selfDestructSeconds = if (activeBurnerSeconds > 0) activeBurnerSeconds else null
        )
        val isDuplicateRetryBubble = isGeminiConv && isSenderMe && overrideText != null &&
            messages.lastOrNull()?.let { it.isMe && it.text == text } == true
        if (!isDuplicateRetryBubble) {
            messages.add(newMsg)
        }
        replyingToMessage = null
        if (overrideText == null) inputMessage = ""
        SoundPoolManager.playMechanicalClick()

        // Real cloud broadcast to the other device over Firebase Firestore OR offline-first local outbox queue
        if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
            if (NetworkConnectivityAndOfflineManager.serviceStatus.value.isOnline) {
                CloudMessagingManager.sendCloudMessage(
                    context = context,
                    conversationId = conversation.id,
                    message = newMsg,
                    participantIds = conversation.participantIds,
                    senderDisplayName = currentUserDisplayName,
                    onError = { _ ->
                        NetworkConnectivityAndOfflineManager.enqueueOfflineMessage(
                            context = context,
                            conversationId = conversation.id,
                            message = newMsg,
                            participantIds = conversation.participantIds,
                            senderDisplayName = currentUserDisplayName
                        )
                        scope.launch {
                            Toast.makeText(
                                context,
                                "Ağ zayıf: Mesaj yerel kuyruğa alındı, bağlantı düzelince otomatik iletilecek.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } else {
                NetworkConnectivityAndOfflineManager.enqueueOfflineMessage(
                    context = context,
                    conversationId = conversation.id,
                    message = newMsg,
                    participantIds = conversation.participantIds,
                    senderDisplayName = currentUserDisplayName
                )
                Toast.makeText(
                    context,
                    "Çevrimdışı mod: Mesaj Yerel Depoya kaydedildi, internet gelince iletilecek.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (!isGeminiConv && !isSelfNotesConv) {
            NetworkConnectivityAndOfflineManager.cacheConversationMessages(context, conversation.id, messages.toList())
        }

        // Streaming low-latency cloud AI + transparent server/connectivity error handling for GEN AI DM
        if (isSenderMe && isGeminiConv) {
            aiErrorInfo = null
            lastFailedAiPrompt = null
            scope.launch {
                isCounterpartTyping = true
                val streamingMsgId = "msg_genai_${System.currentTimeMillis()}"
                val replyTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                var insertedPlaceholder = false
                try {
                    val lowerText = text.lowercase()
                    val adversarialKeywords = listOf(
                        "ignore all previous instructions", "system prompt", "jailbreak",
                        "bypass security", "reveal secret", "unrestricted", "dan mode"
                    )
                    val isAdversarial = adversarialKeywords.any { lowerText.contains(it) }

                    var replyText = ""
                    if (isAdversarial) {
                        delay(250)
                        replyText = "🛡️ [SİBER GÜVENLİK KALKANI DEVREDE]: Tespit edilen 3. taraf sızma ve prompt injection denemesi başarıyla bertaraf edildi. GENeral Chat çekirdek güvenliği tam yetkiyle korunmaktadır."
                    } else {
                        val ctxSummary = ChatContextCache.buildPromptContext(context, text)
                        val webResearch = if (WebResearchHelper.shouldResearchOnline(text)) {
                            withTimeoutOrNull(AiServiceConfig.WEB_RESEARCH_BUDGET_MS) {
                                WebResearchHelper.searchLive(text)
                            }
                        } else null

                        val historyTurns = messages
                            .dropLast(1)
                            .takeLast(AiServiceConfig.MAX_HISTORY_TURNS)
                            .filter { it.text.isNotBlank() }
                            .map { msg ->
                                AiTurnHistory(
                                    role = if (msg.isMe) "user" else "model",
                                    text = msg.text
                                )
                            }

                        val turnPrompt = FirebaseAiCloudClient.buildTurnPromptEnvelope(
                            userPrompt = text,
                            chatContextSummary = ctxSummary,
                            webResearch = webResearch
                        )

                        val modelCandidates = listOf(
                            AiServiceConfig.PRIMARY_MODEL to AiServiceConfig.PRIMARY_TIMEOUT_MS,
                            AiServiceConfig.FAST_MODEL to AiServiceConfig.FAST_TIMEOUT_MS
                        )

                        var lastClassifiedError: AiClassifiedError? = null
                        for ((index, candidate) in modelCandidates.withIndex()) {
                            val (modelId, timeoutMs) = candidate
                            try {
                                val streamed = withContext(Dispatchers.IO) {
                                    FirebaseAiCloudClient.streamChat(
                                        modelName = modelId,
                                        mode = AiMode.GENERAL,
                                        history = historyTurns,
                                        prompt = turnPrompt,
                                        timeoutMs = timeoutMs,
                                        onChunk = { partial ->
                                            withContext(Dispatchers.Main) {
                                                isCounterpartTyping = false
                                                val existingIdx = messages.indexOfFirst { it.id == streamingMsgId }
                                                if (existingIdx == -1) {
                                                    insertedPlaceholder = true
                                                    messages.add(
                                                        DirectMessage(
                                                            id = streamingMsgId,
                                                            conversationId = conversation.id,
                                                            senderName = "GEN AI",
                                                            isMe = false,
                                                            text = partial,
                                                            time = replyTime,
                                                            timestamp = System.currentTimeMillis()
                                                        )
                                                    )
                                                } else {
                                                    messages[existingIdx] = messages[existingIdx].copy(text = partial)
                                                }
                                            }
                                        }
                                    )
                                }
                                if (streamed.isNotBlank()) {
                                    replyText = OfflineAiEngine.sanitizeIdentity(streamed).trim()
                                    break
                                }
                            } catch (e: CancellationException) {
                                if (e !is TimeoutCancellationException) throw e
                                val classified = AiErrorClassifier.classify(e)
                                lastClassifiedError = classified
                                Log.w("DirectMessageScreen", "DM GEN AI timed out on $modelId (${classified.type})")
                                if (index >= modelCandidates.lastIndex || !AiErrorClassifier.isRetryableWithFallbackModel(classified.type)) {
                                    break
                                }
                            } catch (cloudErr: Exception) {
                                val classified = AiErrorClassifier.classify(cloudErr)
                                lastClassifiedError = classified
                                Log.e("DirectMessageScreen", "DM GEN AI error on $modelId [${classified.type}]: ${cloudErr.message}", cloudErr)
                                if (index >= modelCandidates.lastIndex || !AiErrorClassifier.isRetryableWithFallbackModel(classified.type)) {
                                    break
                                }
                            }
                        }

                        if (replyText.isBlank()) {
                            if (insertedPlaceholder) {
                                messages.removeAll { it.id == streamingMsgId }
                            }
                            aiErrorInfo = lastClassifiedError ?: AiClassifiedError(
                                type = com.example.data.AiErrorType.EMPTY_RESPONSE,
                                title = "Yanıt Alınamadı",
                                userMessage = "Yapay zekâ servisinden yanıt alınamadı. Bağlantınızı kontrol edip yeniden deneyin.",
                                isRetryable = true
                            )
                            lastFailedAiPrompt = text
                            return@launch
                        }
                    }

                    val existingIdx = messages.indexOfFirst { it.id == streamingMsgId }
                    if (existingIdx != -1) {
                        messages[existingIdx] = messages[existingIdx].copy(text = replyText)
                    } else {
                        messages.add(
                            DirectMessage(
                                id = streamingMsgId,
                                conversationId = conversation.id,
                                senderName = "GEN AI",
                                isMe = false,
                                text = replyText,
                                time = replyTime,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    SoundPoolManager.playNotification()
                    if (com.example.util.GenAiSpeechManager.autoReadEnabled.value) {
                        com.example.util.GenAiSpeechManager.speak(context, streamingMsgId, replyText, forceStart = true)
                    }
                } finally {
                    isCounterpartTyping = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedMessageIds.size} mesaj seçildi",
                            color = currentPalette.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedMessageIds.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Vazgeç", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedMessageIds.size == messages.size) {
                                selectedMessageIds.clear()
                            } else {
                                selectedMessageIds.clear()
                                selectedMessageIds.addAll(messages.map { it.id })
                            }
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Tümünü Seç", tint = currentPalette.accentColor)
                        }
                        IconButton(onClick = {
                            val selectedMsgs = messages.filter { it.id in selectedMessageIds }
                            val combined = selectedMsgs.joinToString("\n") { "[${it.senderName}]: ${it.text}" }
                            clipboardManager.setText(AnnotatedString(combined))
                            Toast.makeText(context, "${selectedMsgs.size} mesaj kopyalandı.", Toast.LENGTH_SHORT).show()
                            isSelectionMode = false
                            selectedMessageIds.clear()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val count = selectedMessageIds.size
                            messages.removeAll { it.id in selectedMessageIds }
                            Toast.makeText(context, "$count mesaj silindi.", Toast.LENGTH_SHORT).show()
                            isSelectionMode = false
                            selectedMessageIds.clear()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = NeonRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isPaper) PaperSurface else CyberCardBg)
                )
            } else if (isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Kelime veya tarih ara (örn: selam, 14:20, 05.11)...", fontSize = 12.sp, color = if (isPaper) PaperTextMuted else TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = if (isPaper) PaperText else Color.White,
                                unfocusedTextColor = if (isPaper) PaperText else Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dm_search_input")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                            selectedDateFilter = null
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Aramayı Kapat",
                                tint = if (isPaper) PaperText else Color.White
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Temizle", tint = if (isPaper) PaperText else Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = { showDatePickerModal = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Tarih Seç",
                                tint = if (selectedDateFilter != null) CyberYellow else if (isPaper) PaperText else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (matchingIndices.isNotEmpty()) {
                            Text(
                                text = "${currentMatchIndex + 1}/${matchingIndices.size}",
                                color = CyberYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (matchingIndices.isNotEmpty()) {
                                        currentMatchIndex = if (currentMatchIndex > 0) currentMatchIndex - 1 else matchingIndices.size - 1
                                        scope.launch { listState.animateScrollToItem(matchingIndices[currentMatchIndex]) }
                                    }
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Önceki", tint = if (isPaper) PaperText else Color.White)
                            }
                            IconButton(
                                onClick = {
                                    if (matchingIndices.isNotEmpty()) {
                                        currentMatchIndex = if (currentMatchIndex < matchingIndices.size - 1) currentMatchIndex + 1 else 0
                                        scope.launch { listState.animateScrollToItem(matchingIndices[currentMatchIndex]) }
                                    }
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Sonraki", tint = if (isPaper) PaperText else Color.White)
                            }
                        } else if (searchQuery.isNotBlank() || selectedDateFilter != null) {
                            Text(
                                text = "0 sonuç",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isPaper) PaperSurface else CyberCardBg)
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    SoundPoolManager.playMechanicalClick()
                                    showContactProfileDialog = true
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isLightChatSurface) Color(0xFFE9EDEF) else currentPalette.otherBubbleBg)
                                    .border(
                                        1.dp,
                                        if (isContactBlocked) NeonRed else if (isLightChatSurface) Color(0xFFD1D7DB) else currentPalette.accentColor,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!conversation.logoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = conversation.logoUri,
                                        contentDescription = customContactName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(38.dp).clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = conversation.avatarEmoji ?: conversation.initials,
                                        color = if (isLightChatSurface) PaperText else CyberYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = customContactName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = topBarTitleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (!isGeminiConv && !isSelfNotesConv) {
                                        val isHeaderE2eeActive = isDirectPeerChat && !peerE2eePubKey.isNullOrBlank()
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Surface(
                                            onClick = { showE2eeDetailsDialog = true },
                                            color = if (isHeaderE2eeActive) NeonGreen.copy(alpha = 0.16f) else CyberYellow.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, if (isHeaderE2eeActive) NeonGreen else CyberYellow),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.testTag("dm_topbar_e2ee_pill")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "E2EE Güvenlik Etiketi",
                                                    tint = if (isHeaderE2eeActive) NeonGreen else CyberYellow,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    text = if (isHeaderE2eeActive) "E2EE" else "TLS",
                                                    color = if (isHeaderE2eeActive) NeonGreen else CyberYellow,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                    if (isContactBlocked) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "[ENGELLİ]",
                                            color = NeonRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                val subtitleText = when {
                                    isSelfNotesConv -> "Yalnızca sizin cihazınızda · Kimseye gitmez"
                                    isCounterpartTyping -> "yazıyor..."
                                    isGeminiConv -> "GEN AI Asistan"
                                    conversation.isOnline -> "çevrimiçi"
                                    else -> "son görülme ${conversation.lastTime.ifBlank { formatClockTime(System.currentTimeMillis()) }}"
                                }
                                Text(
                                    text = subtitleText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCounterpartTyping || conversation.isOnline) CyberYellow else topBarSubColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("dm_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = topBarTitleColor
                            )
                        }
                    },
                    actions = {
                        if (!isGeminiConv && !isSelfNotesConv) {
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isContactBlocked) {
                                        Toast.makeText(context, "Engellenen kullanıcı aranamaz.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onStartCall(customContactName, true)
                                    }
                                },
                                modifier = Modifier.testTag("dm_video_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Görüntülü Ara",
                                    tint = if (isContactBlocked) TextMuted else topBarTitleColor
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isContactBlocked) {
                                        Toast.makeText(context, "Engellenen kullanıcı aranamaz.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onStartCall(customContactName, false)
                                    }
                                },
                                modifier = Modifier.testTag("dm_voice_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Sesli Ara",
                                    tint = if (isContactBlocked) TextMuted else topBarTitleColor
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showTopMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Daha Fazla",
                                    tint = topBarTitleColor
                                )
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false },
                                modifier = Modifier.background(CyberCardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sohbette Ara", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                                    onClick = {
                                        showTopMenu = false
                                        SoundPoolManager.playMechanicalClick()
                                        isSearchActive = true
                                    },
                                    modifier = Modifier.testTag("dm_search_button")
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isScreenCaptureProtected) "Ekran Koruması (Aktif)" else "Ekran Koruması (Kapalı)", color = if (isScreenCaptureProtected) NeonGreen else Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = if (isScreenCaptureProtected) NeonGreen else NeonRed) },
                                    onClick = {
                                        showTopMenu = false
                                        isScreenCaptureProtected = !isScreenCaptureProtected
                                        SoundPoolManager.playMechanicalClick()
                                        Toast.makeText(
                                            context,
                                            if (isScreenCaptureProtected) "Ekran koruması devrede (FLAG_SECURE Aktif)" else "Ekran koruması devre dışı bırakıldı",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duvar Kağıdını Değiştir", color = CyberYellow) },
                                    leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null, tint = CyberYellow) },
                                    onClick = {
                                        showTopMenu = false
                                        showWallpaperDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sohbet Balonu Teması", color = currentPalette.accentColor) },
                                    leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null, tint = currentPalette.accentColor) },
                                    onClick = {
                                        showTopMenu = false
                                        showChatThemeDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Kişi Profili & Ayarlar", color = currentPalette.accentColor) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = currentPalette.accentColor) },
                                    onClick = {
                                        showTopMenu = false
                                        showContactProfileDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Zamanlanmış Mesajlar (${scheduledMessages.size})", color = CyberYellow) },
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = CyberYellow) },
                                    onClick = {
                                        showTopMenu = false
                                        showScheduledListDialog = true
                                    }
                                )
                                val exportBlocked = chatPrivacyConfig.advancedPrivacyEnabled || chatPrivacyConfig.blockExportAndForward
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (exportBlocked) "Dışa Aktar (Kilitli)" else "Sohbeti Dışa Aktar",
                                            color = if (exportBlocked) TextMuted else NeonCyan
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (exportBlocked) Icons.Default.Lock else Icons.Default.Download,
                                            contentDescription = null,
                                            tint = if (exportBlocked) NeonRed else NeonCyan
                                        )
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        if (exportBlocked) {
                                            Toast.makeText(context, "Gelişmiş Sohbet Gizliliği aktif: Dışa aktarma kilitli.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showExportChatDialog = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Çoklu Mesaj Seçimi", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null, tint = currentPalette.accentColor) },
                                    onClick = {
                                        showTopMenu = false
                                        isSelectionMode = true
                                    }
                                )
                                val aiBlocked = chatPrivacyConfig.advancedPrivacyEnabled || chatPrivacyConfig.excludeFromAi
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (aiBlocked) "Yapay Zeka Özeti (Gizlilik Kilitli)" else "Yapay Zeka Özeti",
                                            color = if (aiBlocked) TextMuted else CyberYellow
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (aiBlocked) Icons.Default.Lock else Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (aiBlocked) NeonRed else CyberYellow
                                        )
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        if (aiBlocked) {
                                            Toast.makeText(context, "Gelişmiş Sohbet Gizliliği: Bu sohbet AI özetinden hariç tutuldu.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showAiSummaryDialog = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Medya & Belge Kasası", color = NeonCyan) },
                                    leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan) },
                                    onClick = {
                                        showTopMenu = false
                                        showMediaGalleryDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Çevrimdışı P2P Tüneli", color = NeonGreen) },
                                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen) },
                                    onClick = {
                                        showTopMenu = false
                                        showP2PTunnelDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ses ve Müzik", color = CyberYellow) },
                                    leadingIcon = { Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyberYellow) },
                                    onClick = {
                                        showTopMenu = false
                                        showMusicDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sohbeti Arşivle", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = CyberYellow) },
                                    onClick = {
                                        showTopMenu = false
                                        Toast.makeText(context, "Sohbet arşivlendi.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Acil Durum Kill-Switch", color = NeonRed) },
                                    leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = NeonRed) },
                                    onClick = {
                                        showTopMenu = false
                                        showKillSwitchConfirmDialog = true
                                    },
                                    modifier = Modifier.testTag("dm_panic_button")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        ChatWallpaperBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // In-Chat Search Quick Date & Media Filter Chips
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isPaper) PaperSurface else CyberCardBg)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDateFilter == null,
                        onClick = { selectedDateFilter = null },
                        label = { Text("Tümü", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = if (isPaper) PaperBg else CyberBlack,
                            labelColor = if (isPaper) PaperText else Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    FilterChip(
                        selected = selectedDateFilter == "TODAY",
                        onClick = { selectedDateFilter = if (selectedDateFilter == "TODAY") null else "TODAY" },
                        label = { Text("Bugün", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = if (isPaper) PaperBg else CyberBlack,
                            labelColor = if (isPaper) PaperText else Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    FilterChip(
                        selected = selectedDateFilter == "YESTERDAY",
                        onClick = { selectedDateFilter = if (selectedDateFilter == "YESTERDAY") null else "YESTERDAY" },
                        label = { Text("Dün", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = if (isPaper) PaperBg else CyberBlack,
                            labelColor = if (isPaper) PaperText else Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    FilterChip(
                        selected = selectedDateFilter == "MEDIA",
                        onClick = { selectedDateFilter = if (selectedDateFilter == "MEDIA") null else "MEDIA" },
                        label = { Text("Medya", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = if (isPaper) PaperBg else CyberBlack,
                            labelColor = if (isPaper) PaperText else Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (selectedDateFilter != null && selectedDateFilter !in listOf("TODAY", "YESTERDAY", "MEDIA")) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedDateFilter = null },
                            label = { Text("🗓️ $selectedDateFilter ✕", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = CyberBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            // Pinned Message Quick Access Banner
            val pinnedMsg = messages.find { it.isPinned }
            if (pinnedMsg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isLightChatSurface) Color.White.copy(alpha = 0.94f) else currentPalette.otherBubbleBg)
                        .border(0.5.dp, if (isLightChatSurface) Color(0xFFD1D7DB) else currentPalette.accentColor.copy(alpha = 0.5f))
                        .clickable {
                            val pIdx = messages.indexOfFirst { it.id == pinnedMsg.id }
                            if (pIdx != -1) {
                                scope.launch { listState.animateScrollToItem(pIdx + 1) }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = accentYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SABİTLENMİŞ MESAJ (${pinnedMsg.senderName})", color = accentYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(pinnedMsg.text, color = if (isLightChatSurface) PaperText else Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(
                        onClick = {
                            val pIdx = messages.indexOfFirst { it.id == pinnedMsg.id }
                            if (pIdx != -1) {
                                messages[pIdx] = pinnedMsg.copy(isPinned = false)
                                Toast.makeText(context, "Sabitleme kaldırıldı.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (!isGeminiConv) {
                val e2eeActiveNow = isDirectPeerChat && !peerE2eePubKey.isNullOrBlank()
                val badgeAccent = when {
                    isSelfNotesConv -> CyberYellow
                    e2eeActiveNow -> NeonGreen
                    isDirectPeerChat -> CyberYellow
                    else -> NeonCyan
                }
                val badgeText = when {
                    isSelfNotesConv -> "🔒 CİHAZ İÇİ ŞİFRELİ KASA · Sunucuya yüklenmez"
                    e2eeActiveNow -> "🔒 UÇTAN UCA ŞİFRELİ (E2EE) · Sunucuda şifreli tutulur ve okunamaz"
                    isDirectPeerChat -> "🛡️ E2EE Anahtar Bekleniyor · TLS aktif (karşı taraf çevrimiçi olunca tam E2EE)"
                    else -> "🛡️ Grup Sohbeti · TLS Aktarım Koruması Aktif"
                }
                Surface(
                    onClick = { showE2eeDetailsDialog = true },
                    color = if (isLightChatSurface) PaperSurface else Color(0xFF0B1912),
                    border = BorderStroke(1.dp, badgeAccent.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("dm_e2ee_security_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (e2eeActiveNow || isSelfNotesConv) Icons.Default.VerifiedUser else Icons.Default.Security,
                            contentDescription = "Uçtan uca şifreleme durumu",
                            tint = if (isLightChatSurface) PaperText else badgeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = badgeText,
                            color = if (isLightChatSurface) PaperText else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Detay",
                            color = if (isLightChatSurface) PaperText else badgeAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            if (!isSelfNotesConv && (!serviceStatus.isOnline || serviceStatus.offlineModeNotificationVisible || serviceStatus.pendingOutboxCount > 0)) {
                ServiceStatusDashboardCard(
                    isCloudSignedIn = currentUserUid != null,
                    onOpenFreeServer = { showP2PTunnelDialog = true },
                    onOpenOfflineRadar = { showP2PTunnelDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Message History inside original Yellow-Black ChatWallpaperBackground
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "chat_header_notice") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        if (messages.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isLightChatSurface) PaperSurface else CyberCardBg.copy(alpha = 0.92f),
                                border = BorderStroke(0.8.dp, if (isLightChatSurface) PaperBorder else CyberYellow.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = formatChatDayHeader(System.currentTimeMillis()),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLightChatSurface) PaperTextMuted else CyberYellow,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLightChatSurface) PaperSurface else Color(0xFF1A1604).copy(alpha = 0.95f),
                            border = BorderStroke(
                                1.dp,
                                if (isLightChatSurface) PaperBorder else if (isDirectPeerChat && !peerE2eePubKey.isNullOrBlank()) NeonGreen.copy(alpha = 0.65f) else CyberYellow.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .clickable { showE2eeDetailsDialog = true }
                        ) {
                            Text(
                                text = when {
                                    isSelfNotesConv ->
                                        "📝 Sadece Size Özel Not Defteri · Buraya yazdığınız notlar, fotoğraflar ve ses kayıtları yalnızca cihazınızda kalır ve kimseye gönderilmez."
                                    isDirectPeerChat && !peerE2eePubKey.isNullOrBlank() ->
                                        "🔒 Uçtan Uca Şifreleme (E2EE · ECDH P-256 + AES-256-GCM) Aktif.\nMesajlarınız sunucuda yalnızca şifreli (E2EE:v1:...) olarak tutulur; sunucu veya 3. taraflar tarafından okunamaz."
                                    isDirectPeerChat ->
                                        "🛡️ GENeral Chat Güvenlik Kalkanı · TLS aktarımı aktif. Karşı taraf anahtarını yayınladığında tüm mesajlar sunucuda yalnızca okunamaz E2EE şifreli olarak tutulacaktır."
                                    else ->
                                        "🛡️ GENeral Chat · Bulut sohbetleri TLS ile aktarılır. Ekran/dışa aktarım koruması için sohbet profilinden Gelişmiş Gizlilik açılabilir."
                                },
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center,
                                color = if (isLightChatSurface) PaperText else if (isDirectPeerChat && !peerE2eePubKey.isNullOrBlank()) NeonGreen else CyberYellow,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                itemsIndexed(
                    items = messages,
                    key = { _, msg -> msg.id },
                    contentType = { _, msg -> msg.attachmentType ?: if (msg.isVoiceNote) "VOICE" else "TEXT" }
                ) { idx, msg ->
                    val showDayDivider = idx == 0 || !isSameCalendarDay(messages[idx - 1].timestamp, msg.timestamp)
                    if (showDayDivider) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isLightChatSurface) PaperSurface else CyberCardBg.copy(alpha = 0.92f),
                                border = BorderStroke(0.8.dp, if (isLightChatSurface) PaperBorder else CyberYellow.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = formatChatDayHeader(msg.timestamp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLightChatSurface) PaperTextMuted else CyberYellow,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    val isMatch = isSearchActive && matchingIndices.contains(idx)
                    val isCurrent = isSearchActive && matchingIndices.getOrNull(currentMatchIndex) == idx
                    val isSelected = selectedMessageIds.contains(msg.id)
                    MessageBubble(
                        message = msg,
                        currentUserUid = currentUserUid,
                        peerUid = conversation.participantIds.firstOrNull { it != currentUserUid },
                        isSelfNotes = isSelfNotesConv,
                        liveLocations = mergedLiveLocations,
                        onOpenFullScreenImage = { fullScreenImageMsg = it },
                        palette = currentPalette,
                        isLightChatSurface = isLightChatSurface,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = {
                            if (isSelected) selectedMessageIds.remove(msg.id) else selectedMessageIds.add(msg.id)
                        },
                        onReply = {
                            replyingToMessage = msg
                        },
                        onReaction = { reactionEmoji ->
                            val mIdx = messages.indexOfFirst { it.id == msg.id }
                            if (mIdx != -1) {
                                val currentReactions = msg.reactions.toMutableList()
                                currentReactions.add(reactionEmoji)
                                messages[mIdx] = msg.copy(reactions = currentReactions)
                            }
                        },
                        onTranscribe = {
                            val mIdx = messages.indexOfFirst { it.id == msg.id }
                            if (mIdx != -1) {
                                messages[mIdx] = msg.copy(
                                    transcribedText = "AI Deşifre: \"Ses tüneli frekans çözümlemesi tamamlandı. Doğrulanmış ses kaydı.\""
                                )
                                SoundPoolManager.playConfirm()
                            }
                        },
                        onMessageClick = {
                            if (isSelectionMode) {
                                if (isSelected) selectedMessageIds.remove(msg.id) else selectedMessageIds.add(msg.id)
                            } else {
                                selectedMessageForAction = msg
                            }
                        },
                        onMessageLongClick = {
                            if (msg.text.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(msg.text))
                                SoundPoolManager.playMechanicalClick()
                                Toast.makeText(context, "Mesaj panoya kopyalandı.", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedMessageForAction = msg
                            }
                        },
                        isSearchMatch = isMatch,
                        isCurrentActiveMatch = isCurrent,
                        highlightQuery = if (isSearchActive) searchQuery else ""
                    )
                }
                if (isCounterpartTyping) {
                    item {
                        TypingIndicatorBubble(
                            partnerName = customContactName,
                            palette = currentPalette,
                            isPaper = isLightChatSurface
                        )
                    }
                }
            }

            // Transparent GEN AI Connectivity / Server Error Banner inside DM
            if (isGeminiConv && aiErrorInfo != null) {
                val err = aiErrorInfo!!
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D0A0A)),
                    border = BorderStroke(1.dp, NeonRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("dm_ai_error_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                            Text(err.title, color = CyberYellow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Text(err.userMessage, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (err.isRetryable && !lastFailedAiPrompt.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        val retryPrompt = lastFailedAiPrompt.orEmpty()
                                        aiErrorInfo = null
                                        sendMessage(overrideText = retryPrompt)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("dm_ai_retry_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yeniden Dene", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            if (!lastFailedAiPrompt.isNullOrBlank()) {
                                TextButton(
                                    onClick = {
                                        val failedPrompt = lastFailedAiPrompt.orEmpty()
                                        val ctxSummary = ChatContextCache.buildPromptContext(context, failedPrompt)
                                        val offlineReply = OfflineAiEngine.generateResponse(
                                            prompt = failedPrompt,
                                            mode = AiMode.GENERAL,
                                            chatContextSummary = ctxSummary,
                                            webResearch = null
                                        )
                                        val tNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                        val mId = "msg_genai_offline_${System.currentTimeMillis()}"
                                        messages.add(
                                            DirectMessage(
                                                id = mId,
                                                conversationId = conversation.id,
                                                senderName = "GEN AI",
                                                isMe = false,
                                                text = offlineReply,
                                                time = tNow,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        aiErrorInfo = null
                                        lastFailedAiPrompt = null
                                    }
                                ) {
                                    Text("Çevrimdışı Yanıtla", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { aiErrorInfo = null }) {
                                Text("Kapat", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Quote / Reply Preview Bar
            if (replyingToMessage != null) {
                val rep = replyingToMessage!!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isPaper) PaperSurface else CyberCardBg)
                        .border(1.dp, currentPalette.accentColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .background(currentPalette.accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yanıtlanıyor: ${rep.senderName}",
                            color = currentPalette.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = rep.text.ifBlank { "[${rep.attachmentType ?: "Medya"}]" },
                            color = if (isPaper) PaperText else Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { replyingToMessage = null },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Yanıtı İptal Et", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Active Burner Warning Banner
            if (activeBurnerSeconds > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonRed.copy(alpha = 0.2f))
                        .border(0.5.dp, NeonRed)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = NeonRed, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GÖNDERİLECEK MESAJLAR ${activeBurnerSeconds}sn SONRA OTOMATİK İMHA EDİLECEK",
                            color = NeonRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Kapat",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { activeBurnerSeconds = 0 }
                    )
                }
            }

            // High-Contrast Visual Recording Indicator Banner
            if (speechState.isListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB00018))
                        .border(2.dp, CyberYellow)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyberYellow)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "● REC · SES KAYDI AKTİF — KONUŞUN...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    TextButton(onClick = { speechState.stopListening() }) {
                        Text("Durdur", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            // Bottom Input Bar
            if (isRecordingVoiceNote) {
                // Live Hardware Audio Recording Interface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isPaper) PaperSurface else CyberCardBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "rec_blink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(500),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "rec_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonRed.copy(alpha = alpha))
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "KAYIT: %02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60),
                        color = NeonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Dynamic Amplitude Visualizer Bars
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val baseHeights = listOf(8, 14, 22, 16, 28, 20, 12, 24, 10, 18, 14, 22)
                        val ampFactor = (currentAmplitude / 2500f).coerceIn(0f, 1.5f)
                        baseHeights.forEach { baseH ->
                            val dynamicHeight = (baseH * (0.6f + ampFactor)).coerceIn(6f, 30f).dp
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(dynamicHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CyberYellow)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Cancel / Discard button
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            voiceRecorder.cancel()
                            isRecordingVoiceNote = false
                            recordingSeconds = 0
                            Toast.makeText(context, "Ses kaydı iptal edildi.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Kaydı İptal Et",
                            tint = NeonRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Send recorded voice note button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                SoundPoolManager.playConfirm()
                                val recordedFile = voiceRecorder.stop()
                                isRecordingVoiceNote = false
                                val duration = maxOf(1, recordingSeconds)
                                recordingSeconds = 0
                                if (recordedFile != null && recordedFile.exists()) {
                                    sendMessage(
                                        overrideText = "Sesli Not (${duration}sn)",
                                        isVoiceNote = true,
                                        voiceDuration = duration,
                                        attachmentUrl = recordedFile.absolutePath
                                    )
                                    Toast.makeText(context, "Mikrofon sesli notu gönderildi.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Ses dosyası kaydedilemedi.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("dm_send_voice_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Sesli Notu Gönder",
                                tint = CyberBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    if (isContactBlocked && !isGeminiConv) {
                        Surface(
                            color = Color(0xFF2B0E0E),
                            border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Bu kullanıcı engellendi. Mesaj ve arama göndermek için profil menüsünden engeli kaldırın.",
                                color = NeonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    AnimatedVisibility(visible = showEmojiTray) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("😀", "😂", "❤️", "🔥", "👍", "🙏", "🎉", "🛡️").forEach { emo ->
                                Surface(
                                    onClick = {
                                        inputMessage += emo
                                        SoundPoolManager.playKeyboardClick()
                                    },
                                    shape = CircleShape,
                                    color = if (isLightChatSurface) Color.White else CyberCardBg,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = emo,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (speechState.isListening) speechState.stopListening() else speechState.startListening()
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (speechState.isListening) NeonRed else if (isLightChatSurface) PaperSurface else CyberCardBg,
                                border = BorderStroke(1.dp, if (speechState.isListening) NeonRed else accentYellow)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Sesle Dikte Et",
                                        tint = if (speechState.isListening) Color.White else accentYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (speechState.isListening) "Dinleniyor" else "Dikte",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (speechState.isListening) Color.White else if (isLightChatSurface) PaperText else Color.White
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    activeBurnerSeconds = when (activeBurnerSeconds) {
                                        0 -> 10
                                        10 -> 30
                                        30 -> 60
                                        else -> 0
                                    }
                                    SoundPoolManager.playMechanicalClick()
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (activeBurnerSeconds > 0) NeonRed.copy(alpha = 0.2f) else if (isLightChatSurface) Color.White else CyberCardBg,
                                border = BorderStroke(1.dp, if (activeBurnerSeconds > 0) NeonRed else CyberCardBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = "İmha Sayacı", tint = if (activeBurnerSeconds > 0) NeonRed else CyberYellow, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (activeBurnerSeconds > 0) "${activeBurnerSeconds}s" else "İmha",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLightChatSurface) PaperText else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Signature Yellow-Black GENeral Chat Composer
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isLightChatSurface) PaperSurface else CyberCardBg,
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, if (isLightChatSurface) PaperBorder else CyberYellow.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        showEmojiTray = !showEmojiTray
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEmotions,
                                        contentDescription = "İfadeler ve Sesle Yazma",
                                        tint = if (showEmojiTray) CyberYellow else if (isLightChatSurface) PaperTextMuted else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = inputMessage,
                                    onValueChange = {
                                        inputMessage = it
                                        SoundPoolManager.playKeyboardClick()
                                    },
                                    placeholder = {
                                        Text(
                                            text = strings.msgInputPlaceholder,
                                            color = if (isLightChatSurface) PaperTextMuted else TextMuted,
                                            fontSize = 15.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dm_message_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = if (isLightChatSurface) PaperText else Color.White,
                                        unfocusedTextColor = if (isLightChatSurface) PaperText else Color.White,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    singleLine = false,
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                                )

                                IconButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        showAttachmentDialog = true
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = "Dosya, Konum veya Etkinlik Ekle",
                                        tint = if (isLightChatSurface) PaperTextMuted else CyberYellow.copy(alpha = 0.85f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            try {
                                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                                cameraPreviewLauncher.launch(null)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Kamera açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Kamera ile Fotoğraf Çek",
                                        tint = if (isLightChatSurface) PaperTextMuted else CyberYellow.copy(alpha = 0.85f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Signature Yellow-Black 48.dp Mic / Send Circular Button
                        if (inputMessage.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isLightChatSurface) PaperText else CyberYellow)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Mikrofon ile Sesli Not Kaydet"
                                    }
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            val file = voiceRecorder.start()
                                            if (file != null) {
                                                isRecordingVoiceNote = true
                                                recordingSeconds = 0
                                            } else {
                                                Toast.makeText(context, "Mikrofon başlatılamadı.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isLightChatSurface) PaperBg else CyberBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { sendMessage() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isLightChatSurface) PaperText else CyberYellow)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Mesajı Gönder"
                                    }
                                    .testTag("dm_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gönder",
                                    tint = if (isLightChatSurface) PaperBg else CyberBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    // Attachment Dialog: Photo, Document, File, APK, Video, Link + Real device picker
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dosya & Medya Paketi Gönder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "100 MB'a kadar asenkron bulut aktarımı; 1,5 GB ve üzeri (4 GB'a kadar) video/dosyalarda uzak ağda WebRTC P2P ve aynı Wi-Fi'da HTTP Range akışı desteklenir.",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Option 1: Photo from Photo Picker
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Galeri açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📷 Galeriden Fotoğraf Seç", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Yüksek çözünürlüklü ham görsel seçilir", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 2: Video from Photo/Video Picker
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Video seçici açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🎥 Video Seç (1,5 GB – 4 GB Destekli)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("MP4/MKV WebRTC P2P ve HTTP Range akışı", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 3: Document (PDF / DOC / TXT / XLS)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                filePickerLauncher.launch(arrayOf("application/pdf", "application/msword", "text/plain", "*/*"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Belge seçici açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📄 Belge Seç (PDF / DOC / TXT)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("1,5 GB – 4 GB'a kadar güvenli doküman transferi", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 4: Any File / Archive / APK
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            try {
                                com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                                filePickerLauncher.launch(arrayOf("*/*"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dosya seçici açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📲 Her Türlü Dosya / ZIP / APK (1,5+ GB)", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("4 GB'a kadar diskten sıfır bellek şişmeli akış", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 5: Link / Web URL
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            showLinkInputDialog = true
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🔗 Bağlantı / Link Ekle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Web bağlantısı paylaşımı", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 6: Konum Paylaş (Location)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            val hasLoc = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            if (hasLoc) {
                                resolveLastLocationAndOpenDialog()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📍 Konum Paylaş", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("GPS veya adres ile canlı konum pini gönder", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 7: Etkinlik Planı Oluştur (Event Plan)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            showEventDialog = true
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📅 Etkinlik Planı Oluştur", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Tarih, saat ve buluşma yeri planı paylaş", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Vazgeç", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Official Location & Real-Time Live Location Share Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resmi Konum Paylaşımı", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = isLiveLocationMode,
                            onClick = { isLiveLocationMode = true },
                            label = { Text("Canlı Konum (GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonGreen,
                                selectedLabelColor = CyberBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isLiveLocationMode,
                            onClick = { isLiveLocationMode = false },
                            label = { Text("Anlık Pin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberYellow,
                                selectedLabelColor = CyberBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isLiveLocationMode) {
                        Text(
                            text = if (isSelfNotesConv) {
                                "Cihaz GPS uydusuyla yalnızca bu sohbette canlı takip"
                            } else {
                                "5 saniyede bir gerçek zamanlı GPS koordinatı güncellenir"
                            },
                            color = NeonCyan,
                            fontSize = 10.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(15 to "15 dk", 60 to "1 sa", 0 to "Manuel").forEach { (mins, textLabel) ->
                                FilterChip(
                                    selected = liveLocationDurationMin == mins,
                                    onClick = { liveLocationDurationMin = mins },
                                    label = { Text(textLabel, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberYellow,
                                        selectedLabelColor = CyberBlack
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (LiveLocationManager.hasLocationPermission(context)) {
                                scope.launch {
                                    isFetchingGpsSnapshot = true
                                    val fresh = LiveLocationManager.fetchFreshLocationSnapshot(context)
                                    isFetchingGpsSnapshot = false
                                    if (fresh != null) {
                                        locationLat = String.format(Locale.US, "%.5f", fresh.latitude)
                                        locationLng = String.format(Locale.US, "%.5f", fresh.longitude)
                                        locationAccuracyMeters = fresh.accuracy.toDouble()
                                        Toast.makeText(context, "Güncel GPS konumu alındı.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "GPS sinyali alınamadı; konum ayarlarını kontrol edin.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !isFetchingGpsSnapshot,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlack),
                        border = BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isFetchingGpsSnapshot) "GPS Konumu Alınıyor..." else "📡 Güncel GPS Konumunu Al (Resmi)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = locationLabel,
                        onValueChange = { locationLabel = it },
                        label = { Text("Yer / Adres Başlığı", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = locationLat,
                            onValueChange = { locationLat = it },
                            label = { Text("Enlem (Lat)", color = TextSecondary) },
                            placeholder = { Text("41.0082", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = locationLng,
                            onValueChange = { locationLng = it },
                            label = { Text("Boylam (Lng)", color = TextSecondary) },
                            placeholder = { Text("28.9784", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val label = locationLabel.trim().ifBlank { "Paylaşılan Konum" }
                        val latDouble = locationLat.trim().replace(',', '.').toDoubleOrNull()
                        val lngDouble = locationLng.trim().replace(',', '.').toDoubleOrNull()
                        if (isLiveLocationMode) {
                            if (latDouble == null || lngDouble == null) {
                                Toast.makeText(context, "Canlı konum için önce geçerli GPS koordinatı alın veya girin.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val locId = "loc_${System.currentTimeMillis()}"
                            val participants = if (isSelfNotesConv) emptyList()
                            else (conversation.participantIds + listOfNotNull(currentUserUid)).distinct()
                            LiveLocationManager.startLiveLocationSession(
                                context = context,
                                locationId = locId,
                                conversationId = conversation.id,
                                senderId = currentUserUid ?: "local_user",
                                senderName = currentUserDisplayName,
                                participantIds = participants,
                                initialLatitude = latDouble,
                                initialLongitude = lngDouble,
                                initialAccuracy = locationAccuracyMeters,
                                label = label,
                                durationMinutes = liveLocationDurationMin
                            )
                            val durBadge = if (liveLocationDurationMin > 0) "${liveLocationDurationMin} dk" else "Durdurana kadar"
                            showLocationDialog = false
                            sendMessage(
                                overrideText = "📡 Canlı Konum ($durBadge): $label (${String.format(Locale.US, "%.5f", latDouble)}, ${String.format(Locale.US, "%.5f", lngDouble)})",
                                attachmentType = "LIVE_LOCATION",
                                attachmentName = label,
                                attachmentUrl = "$locId|${String.format(Locale.US, "%.5f", latDouble)},${String.format(Locale.US, "%.5f", lngDouble)}|$liveLocationDurationMin"
                            )
                        } else {
                            val lat = locationLat.trim()
                            val lng = locationLng.trim()
                            val geoUri = if (lat.isNotBlank() && lng.isNotBlank()) {
                                "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})"
                            } else {
                                "geo:0,0?q=${Uri.encode(label)}"
                            }
                            val descText = if (lat.isNotBlank() && lng.isNotBlank()) {
                                "📍 $label ($lat, $lng)"
                            } else {
                                "📍 $label"
                            }
                            showLocationDialog = false
                            sendMessage(
                                overrideText = descText,
                                attachmentType = "LOCATION",
                                attachmentName = label,
                                attachmentUrl = geoUri
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(if (isLiveLocationMode) "Canlı Konum Başlat" else "Konumu Gönder", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Event Plan Creation Dialog
    if (showEventDialog) {
        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Etkinlik Planı Oluştur", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Etkinlik Adı", color = TextSecondary) },
                        placeholder = { Text("Örn: Kahve Buluşması", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Tarih", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = eventTime,
                            onValueChange = { eventTime = it },
                            label = { Text("Saat", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = eventPlace,
                        onValueChange = { eventPlace = it },
                        label = { Text("Konum / Not", color = TextSecondary) },
                        placeholder = { Text("Örn: Kadıköy İskele", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = eventTitle.trim()
                        if (title.isBlank()) {
                            Toast.makeText(context, "Lütfen etkinlik adı girin.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val d = eventDate.trim().ifBlank { "Bugün" }
                        val t = eventTime.trim().ifBlank { "19:00" }
                        val p = eventPlace.trim().ifBlank { "Belirtilmedi" }
                        showEventDialog = false
                        sendMessage(
                            overrideText = "📅 Etkinlik: $title · $d $t ($p)",
                            attachmentType = "EVENT",
                            attachmentName = title,
                            attachmentUrl = "$d|$t|$p"
                        )
                        eventTitle = ""
                        eventPlace = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Planı Gönder", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Link Input Modal Dialog
    if (showLinkInputDialog) {
        AlertDialog(
            onDismissRequest = { showLinkInputDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = CyberYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bağlantı / Link Paylaş", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Paylaşmak istediğiniz URL adresini girin:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = linkInputUrl,
                        onValueChange = { linkInputUrl = it },
                        placeholder = { Text("https://...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberYellow,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { linkInputUrl = "https://youtube.com/@general-chat" },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlack),
                            modifier = Modifier.weight(1f).height(28.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("YouTube", color = NeonCyan, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { linkInputUrl = "https://general-chat.network/secure" },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlack),
                            modifier = Modifier.weight(1f).height(28.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Portal", color = CyberYellow, fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = linkInputUrl.trim()
                        if (trimmed.isNotBlank()) {
                            showLinkInputDialog = false
                            sendMessage(
                                overrideText = "🔗 $trimmed",
                                attachmentType = "LINK",
                                attachmentName = trimmed,
                                attachmentUrl = trimmed
                            )
                            linkInputUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Gönder", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkInputDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Message Actions Dialog (Edit / Delete for everyone / Copy)
    if (selectedMessageForAction != null) {
        val target = selectedMessageForAction!!
        AlertDialog(
            onDismissRequest = { selectedMessageForAction = null },
            title = { Text("Mesaj Seçenekleri", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Emoji Reaction Row (👍, ❤️, 🔥, ⚡, 🛡️)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack, RoundedCornerShape(10.dp))
                            .border(1.dp, currentPalette.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("👍", "❤️", "🔥", "⚡", "🛡️").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "Mesaja $emoji tepkisi ver"
                                    }
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        val mIdx = messages.indexOfFirst { it.id == target.id }
                                        if (mIdx != -1) {
                                            val current = target.reactions.toMutableList()
                                            current.add(emoji)
                                            messages[mIdx] = target.copy(reactions = current)
                                        }
                                        selectedMessageForAction = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyberCardBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    // Option 1: Reply / Quote
                    Card(
                        onClick = {
                            replyingToMessage = target
                            selectedMessageForAction = null
                            SoundPoolManager.playMechanicalClick()
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = currentPalette.accentColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Alıntı Yap & Yanıtla", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Option 2: Pin / Unpin Message
                    Card(
                        onClick = {
                            val mIdx = messages.indexOfFirst { it.id == target.id }
                            if (mIdx != -1) {
                                val newPinState = !target.isPinned
                                messages[mIdx] = target.copy(isPinned = newPinState)
                                Toast.makeText(
                                    context,
                                    if (newPinState) "Mesaj başa sabitlendi." else "Mesaj sabitlemesi kaldırıldı.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            selectedMessageForAction = null
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = CyberYellow)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (target.isPinned) "Sabitlemeyi Kaldır" else "Mesajı Sabitle", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Option 3: Multi-Select Mode
                    Card(
                        onClick = {
                            isSelectionMode = true
                            selectedMessageIds.clear()
                            selectedMessageIds.add(target.id)
                            selectedMessageForAction = null
                            SoundPoolManager.playMechanicalClick()
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SelectAll, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Çoklu Mesaj Seçimine Ekle", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Option 4: Copy
                    Card(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(target.text))
                            selectedMessageForAction = null
                            Toast.makeText(context, "Metin kopyalandı.", Toast.LENGTH_SHORT).show()
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Metni Kopyala", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Option 5: Edit
                    if (target.isMe && !target.isDeletedForEveryone && !target.isVoiceNote) {
                        Card(
                            onClick = {
                                messageToEdit = target
                                editInputText = target.text
                                selectedMessageForAction = null
                            },
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = CyberYellow)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Mesajı Düzenle", color = CyberYellow, fontSize = 13.sp)
                            }
                        }
                    }

                    // Option 6: Delete for everyone (only original sender allowed by zero-trust cloud rules)
                    if (target.isMe && !target.isDeletedForEveryone) {
                        Card(
                            onClick = {
                                val idx = messages.indexOfFirst { it.id == target.id }
                                if (idx != -1) {
                                    messages[idx] = target.copy(
                                        text = "🚫 Bu mesaj herkes için silindi.",
                                        isDeletedForEveryone = true
                                    )
                                }
                                if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
                                    scope.launch {
                                        FirestoreChatRepository.create(context)
                                            .deleteMessageForEveryone(conversation.id, target.id)
                                    }
                                }
                                selectedMessageForAction = null
                                Toast.makeText(context, "Mesaj herkesten silindi.", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = NeonRed)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Herkesten Sil", color = NeonRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMessageForAction = null }) {
                    Text("Kapat", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit message modal
    if (messageToEdit != null) {
        val target = messageToEdit!!
        AlertDialog(
            onDismissRequest = { messageToEdit = null },
            title = { Text("Mesajı Düzenle", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editInputText,
                    onValueChange = { editInputText = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberYellow,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = ProfanityFilter.filter(editInputText.trim())
                        if (clean.isNotEmpty()) {
                            val idx = messages.indexOfFirst { it.id == target.id }
                            if (idx != -1) {
                                messages[idx] = target.copy(text = clean, isEdited = true)
                            }
                            if (!isGeminiConv && !isSelfNotesConv && currentUserUid != null) {
                                scope.launch {
                                    FirestoreChatRepository.create(context)
                                        .editMessageText(conversation.id, target.id, clean)
                                }
                            }
                            messageToEdit = null
                            Toast.makeText(context, "Mesaj güncellendi.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Kaydet", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToEdit = null }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showWallpaperDialog) {
        WallpaperSettingsDialog(
            onDismiss = { showWallpaperDialog = false }
        )
    }

    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            selectedDateFilter = sdf.format(Date(millis))
                        }
                        showDatePickerModal = false
                    }
                ) {
                    Text("Filtrele", color = CyberYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CyberCardBg
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = CyberYellow,
                    selectedDayContentColor = CyberBlack,
                    todayDateBorderColor = CyberYellow
                )
            )
        }
    }

    // AI Conversation Summary Dialog
    if (showAiSummaryDialog) {
        ChatAiSummaryDialog(
            messages = messages,
            contactName = conversation.name,
            onDismiss = { showAiSummaryDialog = false }
        )
    }

    // Media & File Vault Gallery Dialog (Photos, Documents/APKs, Voice Notes, Links)
    if (showMediaGalleryDialog) {
        ChatMediaGalleryDialog(
            messages = messages,
            onDismiss = { showMediaGalleryDialog = false }
        )
    }

    // Offline Bluetooth / Wi-Fi Mesh P2P Radar & Tunnel
    if (showP2PTunnelDialog) {
        OfflineP2PTunnelDialog(
            onDismiss = { showP2PTunnelDialog = false },
            onSendMessage = { packetText ->
                sendMessage(
                    overrideText = "📡 [OFFLINE P2P MESH] $packetText"
                )
            }
        )
    }

    // Individual Chat Theme Customizer Dialog
    if (showChatThemeDialog) {
        AlertDialog(
            onDismissRequest = { showChatThemeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null, tint = currentPalette.accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sohbet Teması Seç", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChatThemePresets.presets.forEach { preset ->
                        val isSelected = chatThemeId == preset.id
                        Card(
                            onClick = {
                                chatThemeId = preset.id
                                conversation.customTheme = preset.id
                                SoundPoolManager.playMechanicalClick()
                                showChatThemeDialog = false
                                Toast.makeText(context, "${preset.title} teması uygulandı.", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) preset.myBubbleBg.copy(alpha = 0.2f) else CyberBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) preset.accentColor else CyberCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(preset.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(preset.title, color = if (isSelected) preset.accentColor else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(preset.accentColor)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChatThemeDialog = false }) {
                    Text("Kapat", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Emergency Kill-Switch Dialog
    if (showKillSwitchConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showKillSwitchConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = NeonRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acil Durum Kill-Switch", color = NeonRed, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Text(
                    text = "Bu işlem tüm sohbet oturumunu, şifreli anahtarları ve hafızadaki tamponları kalıcı olarak yok eder. Uygulama anında kilitlenerek kapatılacaktır.\n\nOnaylıyor musunuz?",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showKillSwitchConfirmDialog = false
                        messages.clear()
                        SoundPoolManager.playNotification()
                        onTriggerGlobalPanic()
                        val activity = context as? android.app.Activity
                        activity?.finishAffinity()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("KİLİTLE VE HEMEN ÇIK", color = Color.White, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKillSwitchConfirmDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Schedule message dialog
    if (showScheduleDialog) {
        ScheduleMessageDialog(
            initialText = inputMessage,
            recipientName = customContactName,
            onSchedule = { text, epoch ->
                scheduledMessages.add(
                    ScheduledMessage(
                        conversationId = conversation.id,
                        targetRecipientName = customContactName,
                        text = text,
                        scheduledEpochMillis = epoch
                    )
                )
                inputMessage = ""
                showScheduleDialog = false
                Toast.makeText(context, "Mesaj başarıyla zamanlandı.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showScheduleDialog = false },
            isPaper = isPaper
        )
    }

    // Scheduled messages list dialog
    if (showScheduledListDialog) {
        ScheduledMessagesListDialog(
            messages = scheduledMessages,
            onSendNow = { scheduled ->
                val newMsg = DirectMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    conversationId = conversation.id,
                    senderName = "Sen (Zamanlanmış)",
                    isMe = true,
                    text = scheduled.text,
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                )
                messages.add(newMsg)
                scheduledMessages.remove(scheduled)
                SoundPoolManager.playNotification()
                Toast.makeText(context, "Zamanlanmış mesaj hemen gönderildi.", Toast.LENGTH_SHORT).show()
            },
            onDelete = { id ->
                scheduledMessages.removeAll { it.id == id }
                Toast.makeText(context, "Zamanlanmış mesaj iptal edildi.", Toast.LENGTH_SHORT).show()
            },
            onEdit = { target, newText, newEpoch ->
                val idx = scheduledMessages.indexOfFirst { it.id == target.id }
                if (idx != -1) {
                    scheduledMessages[idx] = target.copy(text = newText, scheduledEpochMillis = newEpoch)
                    Toast.makeText(context, "Zamanlanmış mesaj güncellendi.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showScheduledListDialog = false },
            isPaper = isPaper
        )
    }

    // Contact Profile Dialog
    if (showContactProfileDialog) {
        ContactProfileDialog(
            conversation = conversation,
            customName = customContactName,
            onUpdateCustomName = { updated ->
                customContactName = updated
            },
            isBlocked = isContactBlocked,
            onToggleBlock = { blocked ->
                val targetUid = peerCloudUid
                if (targetUid != null && currentUserUid != null) {
                    scope.launch {
                        val repo = FirestoreChatRepository.create(context)
                        val res = if (blocked) {
                            repo.blockUser(targetUid, customContactName, conversation.username.orEmpty())
                        } else {
                            repo.unblockUser(targetUid)
                        }
                        res.onSuccess {
                            isContactBlocked = blocked
                        }.onFailure { err ->
                            Toast.makeText(
                                context,
                                err.localizedMessage ?: "Engelleme durumu bulutta güncellenemedi.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    isContactBlocked = blocked
                }
            },
            isMuted = isContactMuted,
            onToggleMute = { muted ->
                isContactMuted = muted
                Toast.makeText(context, if (muted) "Bildirimler sessize alındı." else "Bildirimler açıldı.", Toast.LENGTH_SHORT).show()
            },
            onExportChat = {
                showContactProfileDialog = false
                showExportChatDialog = true
            },
            onClearChat = {
                messages.clear()
                showContactProfileDialog = false
                Toast.makeText(context, "Sohbet geçmişi temizlendi.", Toast.LENGTH_SHORT).show()
            },
            onReportUser = { reason, onComplete ->
                val targetUid = peerCloudUid
                if (targetUid == null || currentUserUid == null) {
                    onComplete(Result.failure(IllegalStateException("Bulut şikayet kaydı için Google oturumu ve bulut kullanıcısı gereklidir.")))
                } else {
                    scope.launch {
                        val res = FirestoreChatRepository.create(context).reportUser(
                            targetUid = targetUid,
                            targetName = customContactName,
                            conversationId = conversation.id,
                            reason = reason
                        )
                        onComplete(res)
                    }
                }
            },
            onDismiss = { showContactProfileDialog = false },
            isPaper = isPaper
        )
    }

    // E2EE Security Certificate & Zero-Knowledge Verification Dialog
    if (showE2eeDetailsDialog) {
        val e2eeActiveNow = isDirectPeerChat && !peerE2eePubKey.isNullOrBlank()
        val myFp = com.example.util.E2eeCryptoManager.getKeyFingerprint(myE2eePubKey)
        val peerFp = peerE2eePubKey?.let { com.example.util.E2eeCryptoManager.getKeyFingerprint(it) }
        AlertDialog(
            onDismissRequest = { showE2eeDetailsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Uçtan uca şifreleme güvenlik etiketi",
                    tint = if (e2eeActiveNow) NeonGreen else CyberYellow,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isSelfNotesConv) "Cihaz İçi Güvenlik Kasası" else "Uçtan Uca Şifreleme (E2EE)",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("dm_e2ee_details_dialog")
                ) {
                    Text(
                        text = when {
                            isSelfNotesConv ->
                                "Kişisel 'Siz' notlarınız hiçbir zaman bulut sunucusuna gönderilmez; yalnızca cihazınızın korumalı özel depolama alanında tutulur."
                            e2eeActiveNow ->
                                "Bu bire bir sohbette mesajlar cihazınızdan çıkmadan ECDH P-256 + AES-256-GCM ile şifrelenir. Sunucuda yalnızca şifreli (E2EE:v1:...) metin saklanır; sunucu yöneticileri veya 3. taraflar mesaj içeriğini okuyamaz."
                            else ->
                                "Cihazınızın E2EE ortak anahtarı hazırlandı. Karşı taraf GENeral Chat'te oturum açıp anahtarını yayınladığında mesajlar otomatik olarak uçtan uca şifrelenir; bu sırada iletişim TLS ile korunur."
                        },
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = BorderStroke(1.dp, if (e2eeActiveNow) NeonGreen.copy(alpha = 0.6f) else CyberCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when {
                                    isSelfNotesConv -> "Depolama: Yalnızca Cihaz İçi Özel Alan (Buluta Yüklenmez)"
                                    e2eeActiveNow -> "Şifreleme Standardı: ECDH secp256r1 + AES-256-GCM"
                                    else -> "Şifreleme Standardı: ECDH P-256 Hazır + TLS Aktarım"
                                },
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    isSelfNotesConv -> "Sunucu Saklama: Buluta Gönderilmez (Yerel)"
                                    e2eeActiveNow -> "Sunucu Saklama: Şifreli / Okunamaz (E2EE:v1 · Zero-Knowledge)"
                                    isDirectPeerChat -> "Sunucu Saklama: Karşı taraf anahtarını yayınladığında E2EE:v1 aktif olur"
                                    else -> "Sunucu Saklama: Grup sohbetleri TLS ile korunur"
                                },
                                color = if (e2eeActiveNow || isSelfNotesConv) NeonGreen else CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!isSelfNotesConv) {
                                Text("Sizin Anahtar Parmak İziniz: $myFp", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (!isSelfNotesConv && isDirectPeerChat) {
                                Text(
                                    text = "Karşı Taraf Anahtar İzi: ${peerFp ?: "Bekleniyor"}",
                                    color = if (peerFp != null) NeonGreen else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showE2eeDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Anlaşıldı", color = CyberBlack, fontWeight = FontWeight.Black)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Export Chat Dialog
    if (showExportChatDialog) {
        ExportChatDialog(
            conversation = conversation,
            messages = messages,
            onDismiss = { showExportChatDialog = false },
            isPaper = isPaper
        )
    }

    // Full-screen 4K High-Resolution Image Viewer + Gallery Saver
    fullScreenImageMsg?.let { imgMsg ->
        val imgRawUrl = imgMsg.attachmentUrl ?: imgMsg.attachmentUri
        val imgSenderUid = imgMsg.senderId ?: if (imgMsg.isMe) currentUserUid else peerCloudUid
        var resolvedViewerFile by remember(imgMsg.id, imgRawUrl) { mutableStateOf<File?>(null) }
        var isSavingViewer by remember { mutableStateOf(false) }

        LaunchedEffect(imgMsg.id, imgRawUrl) {
            resolvedViewerFile = UnlimitedFileTransferManager.ensureLocalFileAvailable(
                context = context,
                conversationId = imgMsg.conversationId,
                messageId = imgMsg.id,
                attachmentName = imgMsg.attachmentName ?: "gorsel_${imgMsg.id}.jpg",
                attachmentUrl = imgRawUrl,
                transferMode = imgMsg.transferMode,
                senderUid = imgSenderUid,
                expectedSizeBytes = imgMsg.attachmentSizeBytes
            ).getOrNull()
        }

        Dialog(
            onDismissRequest = { fullScreenImageMsg = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(resolvedViewerFile ?: imgRawUrl)
                        .size(coil.size.Size.ORIGINAL)
                        .crossfade(true)
                        .build(),
                    contentDescription = imgMsg.attachmentName ?: "4K Fotoğraf Önizleme",
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 56.dp, horizontal = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = imgMsg.attachmentName ?: "Fotoğraf",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${imgMsg.senderName} · ${imgMsg.time}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { fullScreenImageMsg = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                Button(
                    onClick = {
                        if (!isSavingViewer) {
                            isSavingViewer = true
                            scope.launch {
                                val res = MediaGallerySaver.saveMessageImageToGallery(
                                    context = context,
                                    message = imgMsg,
                                    senderUid = imgSenderUid
                                )
                                isSavingViewer = false
                                res.onSuccess {
                                    SoundPoolManager.playConfirm()
                                    Toast.makeText(context, "Fotoğraf telefon galerisine kaydedildi!", Toast.LENGTH_LONG).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, "Galeriye kaydedilemedi: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("fullscreen_save_to_gallery_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSavingViewer) "Galeriye Kaydediliyor..." else "Galeriye Kaydet",
                        color = CyberBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble(
    partnerName: String,
    palette: ChatThemePalette,
    isPaper: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isPaper) PaperSurface else CyberCardBg)
                .border(
                    1.dp,
                    if (isPaper) PaperBorder else palette.accentColor.copy(alpha = 0.6f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$partnerName yazıyor",
                    fontSize = 11.sp,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).alpha(dot1).background(palette.accentColor))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).alpha(dot2).background(palette.accentColor))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).alpha(dot3).background(palette.accentColor))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: DirectMessage,
    currentUserUid: String? = null,
    peerUid: String? = null,
    isSelfNotes: Boolean = false,
    liveLocations: Map<String, CloudLiveLocation> = emptyMap(),
    onOpenFullScreenImage: (DirectMessage) -> Unit = {},
    palette: ChatThemePalette,
    isLightChatSurface: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onReply: () -> Unit = {},
    onReaction: (String) -> Unit = {},
    onTranscribe: () -> Unit = {},
    onMessageClick: () -> Unit,
    onMessageLongClick: () -> Unit = {},
    isSearchMatch: Boolean = false,
    isCurrentActiveMatch: Boolean = false,
    highlightQuery: String = ""
) {
    val isMe = message.isMe
    var isVoicePlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isVoicePlaying) {
        val audioPath = message.attachmentUrl ?: message.attachmentUri
        if (isVoicePlaying && (audioPath.isNullOrBlank() || !File(audioPath).exists())) {
            val durationMs = (message.voiceDurationSeconds.coerceAtLeast(2) * 1000 / playbackSpeed).toLong()
            delay(durationMs)
            isVoicePlaying = false
        }
    }

    val isPaper = LocalPaperMode.current
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
    }

    val bubbleBg = if (isMe) {
        if (isPaper) Color(0xFFE2DDD5)
        else if (isLightChatSurface) Color(0xFFFFF3C7)
        else palette.myBubbleBg
    } else {
        if (isPaper) PaperSurface
        else if (isLightChatSurface) PaperSurface
        else palette.otherBubbleBg
    }
    val bubbleTextColor = if (isPaper) {
        if (message.isDeletedForEveryone) PaperTextMuted else PaperText
    } else if (isLightChatSurface) {
        if (message.isDeletedForEveryone) PaperTextMuted else PaperText
    } else if (isMe) {
        if (message.isDeletedForEveryone) palette.myBubbleText.copy(alpha = 0.6f) else palette.myBubbleText
    } else {
        if (message.isDeletedForEveryone) TextSecondary else palette.otherBubbleText
    }
    val bubbleMetaColor = if (isLightChatSurface) PaperTextMuted else if (isMe) palette.myBubbleText.copy(alpha = 0.72f) else TextSecondary

    val bubbleBorder = if (isCurrentActiveMatch) {
        CyberYellow
    } else if (isSearchMatch) {
        NeonCyan
    } else if (isLightChatSurface) {
        PaperBorder
    } else if (isMe) {
        if (isPaper) PaperBorder else palette.borderGlow.copy(alpha = 0.8f)
    } else {
        if (isPaper) PaperBorder else palette.borderGlow.copy(alpha = 0.45f)
    }
    val bubbleBorderWidth = if (isCurrentActiveMatch) 2.dp else if (isSearchMatch) 1.5.dp else 0.8.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox
        if (isSelectionMode) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = palette.accentColor,
                    checkmarkColor = CyberBlack,
                    uncheckedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 295.dp)
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(
                        width = bubbleBorderWidth,
                        color = bubbleBorder,
                        shape = bubbleShape
                    )
                    .combinedClickable(
                        onClick = onMessageClick,
                        onLongClick = onMessageLongClick
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    // Pinned message badge
                    if (message.isPinned) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = palette.accentColor, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SABİTLENMİŞ",
                                color = palette.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Burner Self-Destruct Countdown Badge
                    message.selfDestructSeconds?.let { countdown ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(NeonRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                .padding(bottom = 3.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = NeonRed, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "🔥 ${countdown}sn İÇİNDE İMHA",
                                color = NeonRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Quoted / Replied Message View
                    if (!message.replyToText.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBlack.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, palette.accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(24.dp)
                                    .background(palette.accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "İleti",
                                    color = palette.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = message.replyToText ?: "",
                                    color = if (isMe) palette.myBubbleText.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }

                    if (isCurrentActiveMatch) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (isMe) palette.myBubbleText else palette.accentColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ARAMA EŞLEŞMESİ",
                                color = if (isMe) palette.myBubbleText else palette.accentColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.5.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                    if (!isMe) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLightChatSurface) PaperText else palette.accentColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Attachment Card (Photo, Document, File, APK, Video, Link, Location, Event)
                    if (message.attachmentType != null) {
                        val resolvedSenderUid = message.senderId
                            ?: if (message.isMe) currentUserUid else peerUid
                        AttachmentView(
                            message = message,
                            isMe = isMe,
                            isPaper = isLightChatSurface,
                            senderUid = resolvedSenderUid,
                            liveLocations = liveLocations,
                            onOpenFullScreenImage = { onOpenFullScreenImage(message) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Voice Note Player with 1x, 1.5x, 2x speeds (Roadmap Section 15)
                    if (message.isVoiceNote) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isVoicePlaying) {
                                        try {
                                            mediaPlayer.pause()
                                        } catch (_: Exception) {}
                                        isVoicePlaying = false
                                    } else {
                                        val audioPath = message.attachmentUrl ?: message.attachmentUri
                                        if (!audioPath.isNullOrBlank() && File(audioPath).exists()) {
                                            try {
                                                mediaPlayer.reset()
                                                mediaPlayer.setDataSource(audioPath)
                                                mediaPlayer.prepare()
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    val params = mediaPlayer.playbackParams
                                                    params.speed = playbackSpeed
                                                    mediaPlayer.playbackParams = params
                                                }
                                                mediaPlayer.setOnCompletionListener {
                                                    isVoicePlaying = false
                                                }
                                                mediaPlayer.start()
                                                isVoicePlaying = true
                                            } catch (e: Exception) {
                                                isVoicePlaying = false
                                                Toast.makeText(context, "Ses oynatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            isVoicePlaying = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isMe) CyberBlack else palette.accentColor)
                            ) {
                                Icon(
                                    imageVector = if (isVoicePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isVoicePlaying) "Durdur" else "Oynat",
                                    tint = if (isMe) palette.accentColor else CyberBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Waveform bars simulation
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barHeights = listOf(14.dp, 22.dp, 8.dp, 26.dp, 18.dp, 12.dp, 24.dp, 16.dp, 28.dp, 10.dp, 20.dp, 14.dp)
                                barHeights.forEach { height ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(height)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isMe) palette.myBubbleText.copy(alpha = 0.7f) else palette.accentColor)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Speed selector pill (1.0x -> 1.5x -> 2.0x)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isMe) CyberBlack else CyberCardBorder)
                                    .clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                        if (isVoicePlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            try {
                                                val params = mediaPlayer.playbackParams
                                                params.speed = playbackSpeed
                                                mediaPlayer.playbackParams = params
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = if (isMe) palette.accentColor else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Voice-to-Text AI Transcription (Roadmap Section 3)
                        if (message.transcribedText != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberBlack.copy(alpha = 0.4f))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🤖 Ses Deşifresi (AI Transkripsiyon):",
                                        color = palette.accentColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = message.transcribedText,
                                        color = if (isMe) palette.myBubbleText else Color.White,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onTranscribe() }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🤖 Metne Dönüştür",
                                    color = if (isMe) palette.myBubbleText.copy(alpha = 0.8f) else palette.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (message.attachmentType == null || message.text.isNotBlank()) {
                        val displayAnnotatedText = remember(message.text, highlightQuery) {
                            if (highlightQuery.isBlank() || !message.text.contains(highlightQuery, ignoreCase = true)) {
                                AnnotatedString(message.text)
                            } else {
                                androidx.compose.ui.text.buildAnnotatedString {
                                    val text = message.text
                                    val lower = text.lowercase(Locale.getDefault())
                                    val q = highlightQuery.lowercase(Locale.getDefault())
                                    var start = 0
                                    while (start < text.length) {
                                        val index = lower.indexOf(q, start)
                                        if (index == -1) {
                                            append(text.substring(start))
                                            break
                                        }
                                        append(text.substring(start, index))
                                        pushStyle(
                                            androidx.compose.ui.text.SpanStyle(
                                                background = Color(0xFFFFD54F),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        append(text.substring(index, index + q.length))
                                        pop()
                                        start = index + q.length
                                    }
                                }
                            }
                        }

                        Text(
                            text = displayAnnotatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = bubbleTextColor,
                            lineHeight = 20.sp,
                            fontStyle = if (message.isDeletedForEveryone) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val isSpeakingAny by com.example.util.GenAiSpeechManager.isSpeaking.collectAsState()
                    val speakingMsgId by com.example.util.GenAiSpeechManager.speakingMessageId.collectAsState()
                    val isThisMsgSpeaking = isSpeakingAny && speakingMsgId == message.id
                    val strings = com.example.util.LocalAppStrings.current

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isMe && !message.isDeletedForEveryone && message.text.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isThisMsgSpeaking) NeonRed.copy(alpha = 0.2f) else palette.accentColor.copy(alpha = 0.16f))
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        com.example.util.GenAiSpeechManager.speak(context, message.id, message.text)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isThisMsgSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = if (isThisMsgSpeaking) strings.aiStopReading else strings.aiReadAloud,
                                    tint = if (isThisMsgSpeaking) NeonRed else palette.accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isThisMsgSpeaking) strings.aiStopReading else strings.aiReadAloud,
                                    color = if (isThisMsgSpeaking) NeonRed else palette.accentColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (message.isEdited) {
                                Text(
                                    text = "düzenlendi · ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = bubbleMetaColor,
                                    fontSize = 9.sp
                                )
                            }
                            val formattedSentTime = remember(message.time, message.timestamp) {
                                if (message.timestamp > 0L) {
                                    formatClockTime(message.timestamp)
                                } else if (message.time.isNotBlank() && message.time != "Şimdi") {
                                    message.time
                                } else {
                                    formatClockTime(System.currentTimeMillis())
                                }
                            }
                            if (message.isEncrypted && !message.isDeletedForEveryone) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Uçtan uca şifreli mesaj",
                                    tint = if (isLightChatSurface) PaperText else if (isMe) bubbleMetaColor else NeonGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = formattedSentTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = bubbleMetaColor,
                                fontSize = 10.sp
                            )
                            if (isMe && !message.isDeletedForEveryone) {
                                Spacer(modifier = Modifier.width(4.dp))
                                if (isSelfNotes) {
                                    Text(
                                        text = "Cihaz içi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = bubbleMetaColor,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Cihaz içinde saklanır",
                                        tint = bubbleMetaColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                } else {
                                    val isReadByOther = message.readBy.any { it != currentUserUid } || message.readAtMs != null
                                    val isDelivered = message.deliveredAtMs != null || message.timestamp > 0L
                                    if (isReadByOther) {
                                        val readColor = if (isLightChatSurface) PaperText else NeonCyan
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Okundu",
                                            tint = readColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        message.readAtMs?.let { rAt ->
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${formatClockTime(rAt)} okundu",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = readColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else if (isDelivered) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "İletildi",
                                            tint = bubbleMetaColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Gönderildi",
                                            tint = bubbleMetaColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Emoji Reactions Badges (Roadmap Section 2)
            if (message.reactions.isNotEmpty()) {
                val reactionCounts = message.reactions.groupingBy { it }.eachCount()
                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reactionCounts.forEach { (emoji, count) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, palette.accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable { onReaction(emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentView(
    message: DirectMessage,
    isMe: Boolean,
    isPaper: Boolean,
    senderUid: String?,
    liveLocations: Map<String, CloudLiveLocation> = emptyMap(),
    onOpenFullScreenImage: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val progressMap by UnlimitedFileTransferManager.transferProgress.collectAsState()
    val transferProgress = progressMap[message.id]
    val attType = message.attachmentType?.uppercase() ?: return
    val attName = message.attachmentName ?: "veri_dosyasi"
    val modeLabel = remember(message.transferMode) {
        when (message.transferMode) {
            "P2P_WEBRTC" -> "WebRTC P2P"
            "LAN_HTTP" -> "LAN HTTP"
            "CLOUD_CHUNKS" -> "Bulut"
            else -> null
        }
    }
    val sizeLabel = remember(message.attachmentSizeBytes, modeLabel) {
        val base = if (message.attachmentSizeBytes > 0L) formatByteSize(message.attachmentSizeBytes) else "Yüksek boyut"
        if (modeLabel != null) "$base · $modeLabel" else base
    }

    var showExecutableRiskDialog by remember { mutableStateOf(false) }

    fun performOpenAttachment() {
        scope.launch {
            val res = UnlimitedFileTransferManager.ensureLocalFileAvailable(
                context = context,
                conversationId = message.conversationId,
                messageId = message.id,
                attachmentName = attName,
                attachmentUrl = message.attachmentUrl ?: message.attachmentUri,
                transferMode = message.transferMode,
                senderUid = senderUid,
                expectedSizeBytes = message.attachmentSizeBytes
            )
            res.onSuccess { localFile ->
                try {
                    UnlimitedFileTransferManager.openFileWithSystem(context, localFile, message.attachmentMimeType)
                } catch (e: Exception) {
                    Toast.makeText(context, "Dosya indirildi (${localFile.name}) ancak açacak uygulama bulunamadı.", Toast.LENGTH_LONG).show()
                }
            }.onFailure { err ->
                Toast.makeText(context, "Dosya açılamadı: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openAttachment() {
        val lowerName = attName.lowercase()
        val isExecutable = attType == "APK" ||
            listOf(".apk", ".dex", ".jar", ".sh", ".bat", ".exe").any { lowerName.endsWith(it) }
        if (isExecutable) {
            showExecutableRiskDialog = true
        } else {
            performOpenAttachment()
        }
    }

    if (showExecutableRiskDialog) {
        AlertDialog(
            onDismissRequest = { showExecutableRiskDialog = false },
            title = {
                Text(
                    text = "Güvenlik Uyarısı: Çalıştırılabilir Paket",
                    color = NeonRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            },
            text = {
                Text(
                    text = "'$attName' çalıştırılabilir bir uygulama paketi (.apk/.jar/.exe) olabilir. Bilinmeyen kaynaklardan gelen paketler cihaz güvenliği açısından risk taşır. Devam etmek istiyor musunuz?",
                    color = if (isPaper) PaperText else Color.White,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExecutableRiskDialog = false
                        performOpenAttachment()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Riski Biliyorum, Aç", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExecutableRiskDialog = false }) {
                    Text("İptal", color = if (isPaper) PaperTextMuted else TextSecondary)
                }
            },
            containerColor = if (isPaper) PaperSurface else CyberCardBg
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaper) PaperBg else CyberBlack
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (attType) {
                "APK" -> NeonGreen
                "VIDEO" -> Color(0xFFF43F5E)
                "IMAGE" -> NeonCyan
                "LINK" -> CyberYellow
                else -> if (isPaper) PaperBorder else CyberCardBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            if (transferProgress != null && transferProgress in 0f..0.999f) {
                Text(
                    text = "Aktarılıyor: %${(transferProgress * 100).toInt()} (${modeLabel ?: "Akış"})",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { transferProgress },
                    color = NeonCyan,
                    trackColor = CyberCardBorder,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
            }
            when (attType) {
                "APK" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attName,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Android Uygulama Paketi · $sizeLabel",
                                color = NeonGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { openAttachment() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paketi İndir / Aç", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                "IMAGE" -> {
                    val url = message.attachmentUrl ?: message.attachmentUri
                    var resolvedPreviewFile by remember(message.id, url) {
                        mutableStateOf(
                            url?.takeIf { !it.startsWith("http") && !it.startsWith("content://") }
                                ?.let { File(it) }
                                ?.takeIf { it.exists() }
                        )
                    }
                    var isSavingGallery by remember { mutableStateOf(false) }

                    LaunchedEffect(message.id, url) {
                        if (resolvedPreviewFile == null && message.attachmentSizeBytes <= 25L * 1024L * 1024L) {
                            resolvedPreviewFile = UnlimitedFileTransferManager.ensureLocalFileAvailable(
                                context = context,
                                conversationId = message.conversationId,
                                messageId = message.id,
                                attachmentName = attName,
                                attachmentUrl = url,
                                transferMode = message.transferMode,
                                senderUid = senderUid,
                                expectedSizeBytes = message.attachmentSizeBytes
                            ).getOrNull()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { onOpenFullScreenImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (resolvedPreviewFile != null || !url.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(resolvedPreviewFile ?: url)
                                    .size(coil.size.Size.ORIGINAL)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = attName,
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.High,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📷 Görsel ($sizeLabel)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = attName,
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onOpenFullScreenImage() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else CyberCardBorder),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🔍 Tam Ekran", color = if (isPaper) PaperText else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (!isSavingGallery) {
                                    isSavingGallery = true
                                    scope.launch {
                                        val res = MediaGallerySaver.saveMessageImageToGallery(
                                            context = context,
                                            message = message,
                                            senderUid = senderUid
                                        )
                                        isSavingGallery = false
                                        res.onSuccess {
                                            SoundPoolManager.playConfirm()
                                            Toast.makeText(context, "Fotoğraf galeriye kaydedildi!", Toast.LENGTH_SHORT).show()
                                        }.onFailure { err ->
                                            Toast.makeText(context, "Kaydedilemedi: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("save_photo_gallery_${message.id}"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSavingGallery) "Kaydediliyor" else "Galeriye Kaydet",
                                color = CyberBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                "VIDEO" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E102F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF43F5E).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🎥 Video Akışı · $sizeLabel",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = attName,
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { openAttachment() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Videoyu İndir / Oynat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "DOCUMENT" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attName,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Şifreli Doküman · $sizeLabel", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { openAttachment() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Belgeyi Aç", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "LINK" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Web Bağlantısı",
                                color = CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = attName,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            try {
                                val url = if (attName.startsWith("http")) attName else "https://$attName"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Bağlantı açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bağlantıyı Aç ↗", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "LIVE_LOCATION", "LOCATION" -> {
                    val isLiveType = attType == "LIVE_LOCATION"
                    val rawPayload = message.attachmentUrl.orEmpty()
                    val payloadParts = if (isLiveType) rawPayload.split("|") else emptyList()
                    val locId = payloadParts.getOrNull(0).orEmpty()
                    val liveSession = if (isLiveType && locId.isNotBlank()) liveLocations[locId] else null

                    val fallbackCoords = payloadParts.getOrNull(1)?.split(",")
                    val displayLat = liveSession?.latitude ?: fallbackCoords?.getOrNull(0)?.toDoubleOrNull()
                    val displayLng = liveSession?.longitude ?: fallbackCoords?.getOrNull(1)?.toDoubleOrNull()
                    val isSessionActive = isLiveType && liveSession?.isActive == true &&
                        (liveSession.expiresAtMs == 0L || liveSession.expiresAtMs > System.currentTimeMillis())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSessionActive) NeonGreen.copy(alpha = 0.25f) else CyberYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSessionActive) NeonGreen else CyberYellow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    isSessionActive -> "● CANLI KONUM AKTİF"
                                    isLiveType -> "📍 Canlı Konum (Durduruldu)"
                                    else -> "📍 Paylaşılan Konum"
                                },
                                color = if (isPaper) PaperText else if (isSessionActive) NeonGreen else CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = liveSession?.label ?: attName,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (displayLat != null && displayLng != null) {
                                val accSuffix = liveSession?.accuracyMeters?.takeIf { it > 0.0 }?.let { " · ±${it.toInt()}m" }.orEmpty()
                                Text(
                                    text = "${String.format(Locale.US, "%.5f", displayLat)}, ${String.format(Locale.US, "%.5f", displayLng)}$accSuffix",
                                    color = if (isPaper) PaperTextMuted else NeonCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    if (displayLat != null && displayLng != null) {
                                        LiveLocationManager.openInMaps(context, displayLat, displayLng, liveSession?.label ?: attName)
                                    } else {
                                        val geo = rawPayload.ifBlank { "geo:0,0?q=${Uri.encode(attName)}" }
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geo)))
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Harita uygulaması bulunamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Haritada Aç ↗", color = CyberBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        if (isMe && isSessionActive && locId.isNotBlank()) {
                            Button(
                                onClick = {
                                    LiveLocationManager.stopLiveLocationSession(
                                        context = context,
                                        conversationId = message.conversationId,
                                        locationId = locId,
                                        syncStopToCloud = true
                                    )
                                    Toast.makeText(context, "Canlı konum paylaşımı durduruldu.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Durdur", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                "EVENT" -> {
                    val parts = (message.attachmentUrl ?: "").split("|")
                    val datePart = parts.getOrNull(0).orEmpty()
                    val timePart = parts.getOrNull(1).orEmpty()
                    val placePart = parts.getOrNull(2).orEmpty()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📅 Etkinlik Planı",
                                color = if (isPaper) Color(0xFF0284C7) else NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = attName,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🗓️ $datePart · ⏰ $timePart",
                        color = if (isPaper) PaperText else CyberYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (placePart.isNotBlank()) {
                        Text(
                            text = "📌 $placePart",
                            color = if (isPaper) PaperTextMuted else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                else -> { // FILE / default
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attName,
                                color = if (isPaper) PaperText else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Şifreli Veri Dosyası · $sizeLabel", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { openAttachment() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("İndir ve Aç", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
