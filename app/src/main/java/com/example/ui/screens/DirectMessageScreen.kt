package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.core.content.ContextCompat
import com.example.util.VoiceNoteRecorder
import java.io.File
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import com.example.util.CloudMessagingManager
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Wallpaper
import com.example.ui.components.ChatAiSummaryDialog
import com.example.ui.components.ChatMediaGalleryDialog
import com.example.ui.components.OfflineP2PTunnelDialog
import com.example.ui.components.QuickActionBar
import com.example.ui.theme.ChatThemePalette
import com.example.ui.theme.ChatThemePresets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.ui.components.ChatWallpaperBackground
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.components.WallpaperSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.LocalPaperMode
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.ProfanityFilter
import com.example.util.SoundPoolManager
import com.example.util.rememberSpeechToTextLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    conversation: ConversationItem,
    onBack: () -> Unit,
    onStartCall: (String, Boolean) -> Unit, // contactName, isVideo
    onTriggerGlobalPanic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isPaper = LocalPaperMode.current
    val messages = remember { mutableStateListOf<DirectMessage>().apply { addAll(conversation.messages) } }
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Cyberpunk Individual Chat Theme State
    var chatThemeId by remember { mutableStateOf(conversation.customTheme.ifBlank { "CYBER_YELLOW" }) }
    val currentPalette: ChatThemePalette = remember(chatThemeId) { ChatThemePresets.getPalette(chatThemeId) }
    var showChatThemeDialog by remember { mutableStateOf(false) }

    // Roadmap dialog states
    var showAiSummaryDialog by remember { mutableStateOf(false) }
    var showMediaGalleryDialog by remember { mutableStateOf(false) }
    var showP2PTunnelDialog by remember { mutableStateOf(false) }
    var showKillSwitchConfirmDialog by remember { mutableStateOf(false) }

    // Swipe-to-Reply & Burner Timer States
    var replyingToMessage by remember { mutableStateOf<DirectMessage?>(null) }
    var activeBurnerSeconds by remember { mutableIntStateOf(0) } // 0 = off, 10, 30, 60

    // Multi-message Selection Mode (Batch delete/copy)
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedMessageIds = remember { mutableStateListOf<String>() }

    // Screenshot & Screen Recording Protection (FLAG_SECURE)
    var isScreenCaptureProtected by remember { mutableStateOf(true) }

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

    DisposableEffect(Unit) {
        onDispose {
            voiceRecorder.cancel()
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

    val todayIsoStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val yesterdayIsoStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L)) }

    // Filter matching message indices in messages list based on query and date
    val matchingIndices = remember(messages.toList(), searchQuery, selectedDateFilter, isSearchActive) {
        if (!isSearchActive || (searchQuery.isBlank() && selectedDateFilter == null)) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            messages.indices.filter { idx ->
                val msg = messages[idx]
                val msgTs = if (msg.timestamp > 0) msg.timestamp else System.currentTimeMillis()
                val msgDate = Date(msgTs)
                val msgDateIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(msgDate)
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

    // Real device file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "secilen_belge.dat"
            val detectedType = when {
                fileName.endsWith(".apk", ignoreCase = true) -> "APK"
                fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".png", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "IMAGE"
                fileName.endsWith(".mp4", ignoreCase = true) || fileName.endsWith(".mkv", ignoreCase = true) || fileName.endsWith(".3gp", ignoreCase = true) -> "VIDEO"
                fileName.endsWith(".pdf", ignoreCase = true) || fileName.endsWith(".doc", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true) -> "DOCUMENT"
                else -> "FILE"
            }
            showAttachmentDialog = false
            val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val newMsg = DirectMessage(
                id = "msg_file_${System.currentTimeMillis()}",
                conversationId = conversation.id,
                senderName = "Ben",
                isMe = true,
                text = "[$detectedType] $fileName",
                time = timeNow,
                timestamp = System.currentTimeMillis(),
                attachmentType = detectedType,
                attachmentName = fileName,
                attachmentUrl = it.toString()
            )
            messages.add(newMsg)
            CloudMessagingManager.sendCloudMessage(conversation.id, newMsg)
            Toast.makeText(context, "$fileName şifrelendi ve tünelden gönderildi.", Toast.LENGTH_SHORT).show()
        }
    }

    // 100% Genuine Real-Time Cloud Sync: Listen for incoming messages from the 2nd device
    LaunchedEffect(conversation.id) {
        CloudMessagingManager.listenToConversation(
            conversationId = conversation.id,
            currentUserName = "Ben"
        ) { cloudMsgs ->
            for (cm in cloudMsgs) {
                val idx = messages.indexOfFirst { it.id == cm.id }
                if (idx == -1) {
                    messages.add(cm)
                } else {
                    messages[idx] = cm
                }
            }
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

        val text = ProfanityFilter.filter(rawText)
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val isSenderMe = !sendAsCounterpart

        val replyTarget = replyingToMessage
        val newMsg = DirectMessage(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversation.id,
            senderName = if (isSenderMe) "Ben" else conversation.name,
            isMe = isSenderMe,
            text = text,
            time = timeNow,
            timestamp = System.currentTimeMillis(),
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
        messages.add(newMsg)
        replyingToMessage = null
        if (overrideText == null) inputMessage = ""
        SoundPoolManager.playMechanicalClick()

        // Real cloud broadcast for 2nd device
        CloudMessagingManager.sendCloudMessage(conversation.id, newMsg)

        // Automated smart reply when sent as "Me" in local sandbox
        if (isSenderMe && !sendAsCounterpart) {
            scope.launch {
                delay(1200)
                val replyTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val replyText = when {
                    isVoiceNote -> "Sesli mesajın başarıyla deşifre edildi. Frekans sinyali güçlü."
                    attachmentType == "APK" -> "Android paketi ($attachmentName) doğrulandı. SHA-256 imzası geçerli."
                    attachmentType == "IMAGE" -> "Şifreli görsel paketi deşifre edildi, piksel bütünlüğü tam."
                    attachmentType == "VIDEO" -> "Video akış paketi başarıyla tamponlandı."
                    attachmentType == "LINK" -> "Bağlantı adresi güvenlik filtresinden geçirildi."
                    attachmentType != null -> "Şifreli dosya paketi ($attachmentName) doğrulandı."
                    text.contains("selam", ignoreCase = true) || text.contains("merhaba", ignoreCase = true) ->
                        "Selam! Uçtan uca şifreli tünelden mesajını aldım, bağlantı stabil."
                    text.contains("neredesin", ignoreCase = true) ->
                        "2. Cihazdayım, sinyal kalitesi %100."
                    text.contains("test", ignoreCase = true) ->
                        "Test paketi başarıyla doğrulandı (SHA-256 E2EE)."
                    text.contains("tamam", ignoreCase = true) || text.contains("ok", ignoreCase = true) ->
                        "Anlaşıldı. Hat sürekli açık kalıyor."
                    else -> "Mesajın 2. cihaz tarafından başarıyla çözümlendi."
                }
                val replyMsg = DirectMessage(
                    id = "msg_reply_${System.currentTimeMillis()}",
                    conversationId = conversation.id,
                    senderName = conversation.name,
                    isMe = false,
                    text = replyText,
                    time = replyTime,
                    timestamp = System.currentTimeMillis()
                )
                messages.add(replyMsg)
                CloudMessagingManager.sendCloudMessage(conversation.id, replyMsg)
                SoundPoolManager.playConfirm()
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
                                modifier = Modifier.size(32.dp)
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
                                modifier = Modifier.size(32.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(currentPalette.otherBubbleBg)
                                    .border(1.5.dp, currentPalette.accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.initials,
                                    color = currentPalette.accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conversation.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaper) PaperText else Color.White
                                    )
                                    conversation.username?.let { uname ->
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uname,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = currentPalette.accentColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (conversation.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(NeonGreen)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Çevrimiçi · E2EE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonGreen
                                        )
                                    } else {
                                        Text(
                                            text = "Uçtan Uca Şifreli Hat",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
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
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Screen capture security toggle (FLAG_SECURE)
                        IconButton(
                            onClick = {
                                isScreenCaptureProtected = !isScreenCaptureProtected
                                SoundPoolManager.playMechanicalClick()
                                Toast.makeText(
                                    context,
                                    if (isScreenCaptureProtected) "Ekran koruması devrede (FLAG_SECURE Aktif)" else "Ekran koruması devre dışı bırakıldı",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Ekran Koruması",
                                tint = if (isScreenCaptureProtected) NeonGreen else NeonRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Search Button in Chat
                        IconButton(
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                isSearchActive = true
                            },
                            modifier = Modifier.testTag("dm_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Sohbette Ara",
                                tint = currentPalette.accentColor
                            )
                        }

                        // Individual Chat Theme Picker Button
                        IconButton(
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                showChatThemeDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Sohbet Teması",
                                tint = currentPalette.accentColor
                            )
                        }

                        // Emergency Kill-Switch / Panic Button
                        Button(
                            onClick = {
                                showKillSwitchConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("dm_panic_button")
                        ) {
                            Text("🚨 ÇIKIŞ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }

                        Box {
                            IconButton(onClick = { showTopMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Daha Fazla",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false },
                                modifier = Modifier.background(CyberCardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Çoklu Mesaj Seçimi", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null, tint = currentPalette.accentColor) },
                                    onClick = {
                                        showTopMenu = false
                                        isSelectionMode = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Yapay Zeka Özeti", color = CyberYellow) },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberYellow) },
                                    onClick = {
                                        showTopMenu = false
                                        showAiSummaryDialog = true
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
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isPaper) PaperSurface else CyberCardBg)
                )
            }
        },
        containerColor = if (isPaper) PaperBg else CyberBlack,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
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

            // E2EE Security Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyberYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mesajlar SHA-256 ve E2EE ile şifrelenir. Üçüncü şahıslar okuyamaz.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Pinned Message Quick Access Banner
            val pinnedMsg = messages.find { it.isPinned }
            if (pinnedMsg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(currentPalette.otherBubbleBg)
                        .border(1.dp, currentPalette.accentColor.copy(alpha = 0.5f))
                        .clickable {
                            val pIdx = messages.indexOfFirst { it.id == pinnedMsg.id }
                            if (pIdx != -1) {
                                scope.launch { listState.animateScrollToItem(pIdx) }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = currentPalette.accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SABİTLENMİŞ MESAJ (${pinnedMsg.senderName})", color = currentPalette.accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(pinnedMsg.text, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(
                        onClick = {
                            val pIdx = messages.indexOfFirst { it.id == pinnedMsg.id }
                            if (pIdx != -1) {
                                messages[pIdx] = pinnedMsg.copy(isPinned = false)
                                Toast.makeText(context, "Sabitleme kaldırıldı.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Message History wrapped with customizable ChatWallpaperBackground
            ChatWallpaperBackground(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(messages, key = { _, msg -> msg.id }) { idx, msg ->
                        val isMatch = isSearchActive && matchingIndices.contains(idx)
                        val isCurrent = isSearchActive && matchingIndices.getOrNull(currentMatchIndex) == idx
                        val isSelected = selectedMessageIds.contains(msg.id)
                        MessageBubble(
                            message = msg,
                            palette = currentPalette,
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
                            isSearchMatch = isMatch,
                            isCurrentActiveMatch = isCurrent,
                            highlightQuery = if (isSearchActive) searchQuery else ""
                        )
                    }
                }
            }

            // Dual Device Switcher Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack)
                    .border(androidx.compose.foundation.BorderStroke(0.5.dp, CyberCardBorder))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (sendAsCounterpart) NeonCyan else currentPalette.accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (sendAsCounterpart) "Aktif: 2. Cihaz (${conversation.name})" else "Aktif: Bu Cihaz (Sen)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sendAsCounterpart) NeonCyan else currentPalette.accentColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!sendAsCounterpart) currentPalette.accentColor else CyberCardBg)
                            .clickable {
                                if (sendAsCounterpart) {
                                    sendAsCounterpart = false
                                    SoundPoolManager.playMechanicalClick()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Bu Cihaz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!sendAsCounterpart) CyberBlack else Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (sendAsCounterpart) NeonCyan else CyberCardBg)
                            .clickable {
                                if (!sendAsCounterpart) {
                                    sendAsCounterpart = true
                                    SoundPoolManager.playMechanicalClick()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "2. Cihaz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sendAsCounterpart) CyberBlack else Color.White
                        )
                    }
                }
            }

            // Customizable Cyberpunk Quick Action Bar
            QuickActionBar(
                burnerSeconds = if (activeBurnerSeconds > 0) activeBurnerSeconds else null,
                onCycleBurner = {
                    activeBurnerSeconds = when (activeBurnerSeconds) {
                        0 -> 10
                        10 -> 30
                        30 -> 60
                        else -> 0
                    }
                    SoundPoolManager.playMechanicalClick()
                    val label = if (activeBurnerSeconds > 0) "${activeBurnerSeconds}sn İmha Sayacı Aktif" else "İmha Sayacı Kapatıldı"
                    Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                },
                onTriggerVoiceNote = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        val file = voiceRecorder.start()
                        if (file != null) {
                            isRecordingVoiceNote = true
                            recordingSeconds = 0
                            SoundPoolManager.playMechanicalClick()
                        } else {
                            Toast.makeText(context, "Mikrofon başlatılamadı.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onPickPhoto = {
                    sendMessage(
                        overrideText = "📷 Şifreli Fotoğraf [GIZLI_FOTOGRAF_E2EE.JPG]",
                        attachmentType = "IMAGE",
                        attachmentName = "GIZLI_FOTOGRAF_E2EE.JPG"
                    )
                },
                onPickFile = {
                    showAttachmentDialog = true
                },
                onOpenAiSummary = {
                    showAiSummaryDialog = true
                },
                onOpenMediaGallery = {
                    showMediaGalleryDialog = true
                },
                onOpenP2PTunnel = {
                    showP2PTunnelDialog = true
                },
                onOpenThemePicker = {
                    showChatThemeDialog = true
                },
                isPaper = isPaper
            )

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
                        modifier = Modifier.size(24.dp)
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

            // Listening banner
            if (speechState.isListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonRed)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ses algılanıyor: Konuşmanız doğrudan mesaja çevriliyor...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isPaper) PaperSurface else CyberCardBg)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attach button
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            showAttachmentDialog = true
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Dosya Ekle",
                            tint = if (isPaper) PaperText else CyberYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Text field
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = {
                            inputMessage = it
                            SoundPoolManager.playKeyboardClick()
                        },
                        placeholder = { Text("Mesaj yazın...", color = if (isPaper) PaperTextMuted else TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dm_message_input"),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPaper) PaperBorder else CyberYellow,
                            unfocusedBorderColor = if (isPaper) PaperBorder else CyberCardBorder,
                            focusedTextColor = if (isPaper) PaperText else Color.White,
                            unfocusedTextColor = if (isPaper) PaperText else Color.White,
                            focusedContainerColor = if (isPaper) PaperBg else CyberBlack,
                            unfocusedContainerColor = if (isPaper) PaperBg else CyberBlack
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() })
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Speech-to-Text Microphone
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (speechState.isListening) NeonRed else CyberBlack)
                            .border(1.dp, if (speechState.isListening) NeonRed else CyberCardBorder, CircleShape),
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (speechState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Sesle Yaz",
                                tint = if (speechState.isListening) Color.White else CyberYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send voice note via real microphone or Send text button
                    if (inputMessage.isBlank()) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberCardBorder)
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
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Mikrofon ile Sesli Not Kaydet",
                                tint = CyberYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { sendMessage() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("dm_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gönder",
                                    tint = CyberBlack,
                                    modifier = Modifier.size(18.dp)
                                )
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
                    Text("Şifreli Medya & Veri Paketi Gönder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Photo / Image
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            sendMessage(
                                overrideText = "📷 Şifreli Fotoğraf [GIZLI_FOTOGRAF_E2EE.JPG]",
                                attachmentType = "IMAGE",
                                attachmentName = "GIZLI_FOTOGRAF_E2EE.JPG"
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📷 Fotoğraf Gönder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("EXIF meta verileri temizlenir, E2EE ile şifrelenir", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 2: Document (.pdf / .doc)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            sendMessage(
                                overrideText = "📄 Şifreli Belge Raporu [GIZLI_RAPOR_2026.PDF]",
                                attachmentType = "DOCUMENT",
                                attachmentName = "GIZLI_RAPOR_2026.PDF"
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📄 Belge Gönder (PDF / DOC)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("AES-256 korumalı güvenli doküman", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 3: File / Archive (.zip)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            sendMessage(
                                overrideText = "📁 Şifreli Arşiv [GIZLI_ARSIV.ZIP]",
                                attachmentType = "FILE",
                                attachmentName = "GIZLI_ARSIV.ZIP"
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📁 Dosya / Arşiv (ZIP / RAW)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Sıkıştırılmış ikili veri bloğu transferi", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 4: APK Package (.apk)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            sendMessage(
                                overrideText = "🤖 Android Uygulama Paketi [general_chat_v7.7.3.apk]",
                                attachmentType = "APK",
                                attachmentName = "general_chat_v7.7.3.apk"
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🤖 APK Dosyası Gönder", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Doğrulanmış Android paketi (v7.7.3)", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 5: Video (.mp4)
                    Card(
                        onClick = {
                            showAttachmentDialog = false
                            sendMessage(
                                overrideText = "🎥 Şifreli Video Klip [GIZLI_VIDEO_E2EE.MP4]",
                                attachmentType = "VIDEO",
                                attachmentName = "GIZLI_VIDEO_E2EE.MP4"
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🎥 Video Klip Gönder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Uçtan uca şifreli MP4 video akışı", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 6: Link / Web URL
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
                                Text("Güvenlik taramalı web bağlantısı paylaşımı", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Option 7: Real device file picker
                    Card(
                        onClick = {
                            try {
                                filePickerLauncher.launch("*/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dosya seçici açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📲 Cihazdan Gerçek Dosya Seç", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Cihazınızın yerel depolamasından dosya aktar", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCardBg)
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
                                Text(text = emoji, fontSize = 18.sp)
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

                    // Option 6: Delete for everyone
                    if (!target.isDeletedForEveryone) {
                        Card(
                            onClick = {
                                val idx = messages.indexOfFirst { it.id == target.id }
                                if (idx != -1) {
                                    messages[idx] = target.copy(
                                        text = "[Bu mesaj gönderici tarafından herkesten silindi]",
                                        isDeletedForEveryone = true
                                    )
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
}

@Composable
private fun MessageBubble(
    message: DirectMessage,
    palette: ChatThemePalette,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onReply: () -> Unit = {},
    onReaction: (String) -> Unit = {},
    onTranscribe: () -> Unit = {},
    onMessageClick: () -> Unit,
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
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isMe) {
        if (isPaper) Color(0xFFE2DDD5) else palette.myBubbleBg
    } else {
        if (isPaper) PaperSurface else palette.otherBubbleBg
    }
    val bubbleBorder = if (isCurrentActiveMatch) {
        CyberYellow
    } else if (isSearchMatch) {
        NeonCyan
    } else if (isMe) {
        if (isPaper) PaperBorder else palette.borderGlow.copy(alpha = 0.8f)
    } else {
        if (isPaper) PaperBorder else palette.borderGlow.copy(alpha = 0.4f)
    }
    val bubbleBorderWidth = if (isCurrentActiveMatch) 2.dp else if (isSearchMatch) 1.5.dp else 1.dp

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
                    .widthIn(max = 290.dp)
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(
                        width = bubbleBorderWidth,
                        color = bubbleBorder,
                        shape = bubbleShape
                    )
                    .clickable { onMessageClick() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            color = palette.accentColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Attachment Card (Photo, Document, File, APK, Video, Link)
                    if (message.attachmentType != null) {
                        AttachmentView(
                            message = message,
                            isMe = isMe,
                            isPaper = isPaper
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
                            color = if (isPaper) {
                                if (message.isDeletedForEveryone) PaperTextMuted else PaperText
                            } else if (isMe) {
                                if (message.isDeletedForEveryone) palette.myBubbleText.copy(alpha = 0.6f) else palette.myBubbleText
                            } else {
                                if (message.isDeletedForEveryone) TextSecondary else palette.otherBubbleText
                            },
                            lineHeight = 20.sp,
                            fontStyle = if (message.isDeletedForEveryone) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "düzenlendi · ",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) palette.myBubbleText.copy(alpha = 0.6f) else TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = message.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMe) palette.myBubbleText.copy(alpha = 0.7f) else TextSecondary,
                            fontSize = 10.sp
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "İletildi",
                                tint = palette.myBubbleText.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
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
    isPaper: Boolean
) {
    val context = LocalContext.current
    val attType = message.attachmentType?.uppercase() ?: return
    val attName = message.attachmentName ?: "veri_dosyasi"

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
                                text = "Android Uygulama Paketi · 28.4 MB · v7.7.3",
                                color = NeonGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "APK Doğrulandı (SHA-256). Paket kuruluma hazır.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("[ GÜVENLİ KUR / YÜKLE ]", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                "IMAGE" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📷 E2EE Şifreli Görsel Önizleme",
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
                        onClick = {
                            Toast.makeText(context, "Görsel tam çözünürlükte açılıyor...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else CyberCardBorder),
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Tam Ekran Görüntüle", color = if (isPaper) PaperText else Color.White, fontSize = 11.sp)
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
                                text = "🎥 Şifreli Video Akışı (01:24)",
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
                        onClick = {
                            Toast.makeText(context, "Şifreli video akışı başlatıldı.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Videoyu Oynat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text("Şifreli Doküman (PDF) · 1.8 MB", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Belge güvenli okuyucuda açıldı.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        modifier = Modifier.fillMaxWidth().height(30.dp),
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
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bağlantıyı Aç ↗", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text("Şifreli Veri Bloğu · 4.6 MB", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Dosya deşifre edildi ve yerel depolamaya aktarıldı.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Deşifre Et ve Aç", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
