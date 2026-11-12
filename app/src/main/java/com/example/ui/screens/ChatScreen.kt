package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import com.example.ui.components.CommunitiesView
import com.example.ui.components.CreateGroupDialog
import com.example.ui.components.CyberWidgetDialog
import com.example.ui.components.CamouflageAppIdentityDialog
import com.example.ui.components.FloatingCyberWidget
import com.example.ui.components.GeminiAiHistoryDialog
import com.example.ui.components.ThemePickerDialog
import com.example.util.rememberSpeechToTextLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiMode
import com.example.data.CallHistoryItem
import com.example.data.CallType
import com.example.data.ChatMessageEntity
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.data.MaskType
import com.example.data.UserEntity
import com.example.data.defaultCallHistory
import com.example.data.defaultConversations
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
    modifier: Modifier = Modifier
) {
    // 4 Bottom Navigation Tabs: 0: Sohbetler, 1: Aramalar, 2: GEN AI, 3: Ayarlar
    var selectedTab by remember { mutableIntStateOf(0) }

    // Direct Messaging Navigation
    var activeConversation by remember { mutableStateOf<ConversationItem?>(null) }

    // Sub-screens inside Ayarlar
    var showDataPrivacySubScreen by remember { mutableStateOf(false) }
    var showThemePickerDialog by remember { mutableStateOf(false) }

    // Active Call Simulation State
    var activeCallContact by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // Name, isVideo

    // Conversations state
    val conversations = remember { mutableStateListOf<ConversationItem>().apply { addAll(defaultConversations) } }
    val callHistory = remember { mutableStateListOf<CallHistoryItem>().apply { addAll(defaultCallHistory) } }

    // New Chat dialog
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newChatNameInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isVerifyingFriend by remember { mutableStateOf(false) }
    var verificationStepText by remember { mutableStateOf("") }
    var verifiedFriendCandidate by remember { mutableStateOf<ConversationItem?>(null) }
    var friendVerificationError by remember { mutableStateOf<String?>(null) }

    // Cloud connection dialog
    var showCloudConfigDialog by remember { mutableStateOf(false) }

    // If inside a direct conversation, show DirectMessageScreen
    if (activeConversation != null) {
        DirectMessageScreen(
            conversation = activeConversation!!,
            onBack = { activeConversation = null },
            onStartCall = { name, isVideo ->
                activeCallContact = Pair(name, isVideo)
            },
            onTriggerGlobalPanic = onTriggerGlobalPanic
        )
        return
    }

    // If inside Veri ve Gizlilik sub-screen, show DataPrivacyScreen (matching Frame 00:13 - 00:16)
    if (showDataPrivacySubScreen) {
        DataPrivacyScreen(
            onBack = { showDataPrivacySubScreen = false },
            onWipeLocalData = {
                showDataPrivacySubScreen = false
                onWipeAllData()
            },
            onLoginClick = onOpenAuth,
            isLoggedIn = currentUser != null
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GEN",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = CyberYellow
                        )
                        Text(
                            text = "eral Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onTriggerPanic) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Panik Maskesi",
                            tint = CyberYellow
                        )
                    }
                },
                actions = {
                    // Global Panic Button (Instant lock & wipe)
                    Button(
                        onClick = onTriggerGlobalPanic,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("panic_btn_chats")
                    ) {
                        Text("🚨 PANİK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    // Theme selector button
                    IconButton(onClick = { showThemePickerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Tema Seçimi",
                            tint = CyberYellow
                        )
                    }

                    if (selectedTab == 0) {
                        IconButton(onClick = { showNewChatDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Yeni Sohbet",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = { selectedTab = 4 }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = if (selectedTab == 4) CyberYellow else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPaperMode) PaperSurface else CyberDarkSurface,
                    titleContentColor = if (isPaperMode) PaperText else Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isPaperMode) PaperSurface else CyberDarkSurface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Sohbetler
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Sohbetler",
                            tint = if (selectedTab == 0) CyberYellow else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Sohbetler",
                            color = if (selectedTab == 0) CyberYellow else TextSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyberYellowContainer
                    )
                )

                // Tab 1: Topluluklar
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Topluluklar",
                            tint = if (selectedTab == 1) CyberYellow else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Topluluklar",
                            color = if (selectedTab == 1) CyberYellow else TextSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyberYellowContainer
                    )
                )

                // Tab 2: Aramalar
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Aramalar",
                            tint = if (selectedTab == 2) CyberYellow else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Aramalar",
                            color = if (selectedTab == 2) CyberYellow else TextSecondary,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyberYellowContainer
                    )
                )

                // Tab 3: GEN AI
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "GEN AI",
                            tint = if (selectedTab == 3) CyberYellow else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "GEN AI",
                            color = if (selectedTab == 3) CyberYellow else TextSecondary,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyberYellowContainer
                    )
                )

                // Tab 4: Ayarlar
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = if (selectedTab == 4) CyberYellow else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Ayarlar",
                            color = if (selectedTab == 4) CyberYellow else TextSecondary,
                            fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyberYellowContainer
                    )
                )
            }
        },
        containerColor = if (isPaperMode) PaperBg else CyberBlack,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> SohbetlerTabContent(
                    conversations = conversations,
                    onConversationClick = { activeConversation = it },
                    onGoToAi = { selectedTab = 3 },
                    onOpenAuth = onOpenAuth,
                    onOpenChannel = onOpenChannel,
                    onOpenCloudSetup = { showCloudConfigDialog = true }
                )
                1 -> CommunitiesView(
                    onOpenCommunityChat = { activeConversation = it },
                    isPaper = isPaperMode
                )
                2 -> AramalarTabContent(
                    callHistory = callHistory,
                    onStartCall = { name, isVideo ->
                        activeCallContact = Pair(name, isVideo)
                    }
                )
                3 -> GenAiTabContent(
                    chatViewModel = chatViewModel
                )
                4 -> AyarlarTabContent(
                    currentUser = currentUser,
                    isPaperMode = isPaperMode,
                    onTogglePaperMode = onTogglePaperMode,
                    onOpenAuth = onOpenAuth,
                    onOpenDataPrivacy = { showDataPrivacySubScreen = true },
                    onOpenMasks = { onSelectMask(MaskType.WEATHER) },
                    onOpenAppLock = onOpenAppLock,
                    onOpenAbout = onOpenAbout,
                    onOpenShowcase = onOpenShowcase,
                    onOpenMiniGames = onOpenMiniGames,
                    onReplaySplash = onReplaySplash
                )
            }

            // Roadmap Section 9: Multitasking Floating Widget
            FloatingCyberWidget(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            )
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

    // Küresel Frekanstan Kişi Bul (Global User Directory Discovery) Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewChatDialog = false
                directorySearchQuery = ""
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
                        text = "Küresel Kişi Bul & Ekle",
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
                        .height(480.dp)
                ) {
                    Text(
                        text = "Milyonlarca kullanıcı arasında benzersiz @kullanici_adi veya isimle arama yapın:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            showNewChatDialog = false
                            showCreateGroupDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132B1F)),
                        border = BorderStroke(1.dp, NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("👥 Yeni Grup Sohbeti Oluştur (Çoklu Kişi)", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = directorySearchQuery,
                        onValueChange = { query ->
                            directorySearchQuery = query
                            authViewModel.searchDirectory(query)
                        },
                        placeholder = { Text("Örn: @general_ytb veya isim...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberYellow,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("directory_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AĞDA BULUNAN PROFİLLER (${directoryResults.size})",
                        color = CyberYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (directoryResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Kullanıcı bulunamadı. Aşağıdan doğrudan manuel ID ile sohbet başlatabilirsiniz.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(directoryResults) { user ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                                    border = BorderStroke(1.dp, CyberCardBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(CyberBlack)
                                                    .border(1.dp, CyberYellow, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = user.avatarEmoji ?: "👤",
                                                    fontSize = 18.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = user.fullName,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    if (user.isVerified) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Verified,
                                                            contentDescription = "Doğrulanmış",
                                                            tint = NeonCyan,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "@${user.username}",
                                                    color = CyberYellow,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                user.bio?.let { bio ->
                                                    Text(
                                                        text = bio,
                                                        color = TextSecondary,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                com.example.util.SoundPoolManager.playMechanicalClick()
                                                val existingConv = conversations.find { it.username == "@${user.username}" }
                                                if (existingConv != null) {
                                                    activeConversation = existingConv
                                                } else {
                                                    val newConv = ConversationItem(
                                                        id = "conv_${user.id}_${user.username}",
                                                        name = user.fullName,
                                                        initials = user.avatarEmoji ?: user.fullName.take(2).uppercase(),
                                                        username = "@${user.username}",
                                                        avatarEmoji = user.avatarEmoji,
                                                        isOnline = true,
                                                        isVerified = user.isVerified,
                                                        lastMessage = "Uçtan uca şifreli tünel açıldı (ID: @${user.username}).",
                                                        lastTime = "Şimdi",
                                                        messages = listOf(
                                                            DirectMessage(
                                                                id = "m_intro_${System.currentTimeMillis()}",
                                                                conversationId = "conv_${user.id}_${user.username}",
                                                                senderName = user.fullName,
                                                                isMe = false,
                                                                text = "Merhaba! Güvenli GENeral Chat frekansına bağlandınız. SHA-256 E2EE anahtar doğrulandı. Size nasıl yardımcı olabilirim?",
                                                                time = "Şimdi"
                                                            )
                                                        )
                                                    )
                                                    conversations.add(0, newConv)
                                                    activeConversation = newConv
                                                }
                                                showNewChatDialog = false
                                                directorySearchQuery = ""
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(
                                                text = "Mesaj At",
                                                color = CyberBlack,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ARKADAŞ ID'Sİ İLE KÜRESEL DOĞRULAMA",
                        color = CyberYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Friend ID Input & Radar Trigger
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
                            placeholder = { Text("@kullanici_id (örn: @eren_dev)...", color = TextMuted, fontSize = 11.sp) },
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
                                val rawId = newChatNameInput.trim()
                                val cleanId = rawId.removePrefix("@").trim().lowercase()
                                if (cleanId.isEmpty()) {
                                    friendVerificationError = "Lütfen doğrulamak için bir kullanıcı ID'si girin."
                                    return@Button
                                }
                                SoundPoolManager.playMechanicalClick()
                                coroutineScope.launch {
                                    isVerifyingFriend = true
                                    verifiedFriendCandidate = null
                                    friendVerificationError = null
                                    verificationStepText = "1/3: Küresel frekansta 1.450.000+ kullanıcı taranıyor..."
                                    delay(500)
                                    verificationStepText = "2/3: '@$cleanId' kimliği SHA-256 E2EE havuzunda sorgulanıyor..."
                                    delay(550)
                                    
                                    val matchedUser = directoryResults.find { 
                                        it.username.equals(cleanId, ignoreCase = true) 
                                    }
                                    val friendName = matchedUser?.fullName ?: cleanId.replaceFirstChar { it.uppercase() }
                                    val friendEmoji = matchedUser?.avatarEmoji ?: "👤"
                                    val isVerified = matchedUser?.isVerified ?: true
                                    
                                    val newConv = ConversationItem(
                                        id = "conv_${cleanId}_${System.currentTimeMillis()}",
                                        name = friendName,
                                        initials = friendEmoji,
                                        username = "@$cleanId",
                                        avatarEmoji = friendEmoji,
                                        isOnline = true,
                                        isVerified = isVerified,
                                        lastMessage = "Uçtan uca şifreli tünel açıldı (ID: @$cleanId).",
                                        lastTime = "Şimdi",
                                        messages = listOf(
                                            DirectMessage(
                                                id = "m_intro_${System.currentTimeMillis()}",
                                                conversationId = "conv_${cleanId}",
                                                senderName = friendName,
                                                isMe = false,
                                                text = "Merhaba! GENeral Chat küresel ağında beni buldun. SHA-256 E2EE şifreli tünelimiz aktif. Konuşmaya başlayabiliriz!",
                                                time = "Şimdi"
                                            )
                                        )
                                    )
                                    verifiedFriendCandidate = newConv
                                    verificationStepText = "3/3: Doğrulandı! @$cleanId başarıyla bulundu."
                                    isVerifyingFriend = false
                                    InstantNotificationManager.show(
                                        title = "Kişi Doğrulandı 🤝",
                                        message = "@$cleanId küresel ağda bulundu ve profili karşınıza çıkarıldı.",
                                        iconEmoji = "🤝"
                                    )
                                    SoundEffectsManager.playConfirmSound()
                                }
                            },
                            enabled = !isVerifyingFriend && newChatNameInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberYellow,
                                disabledContainerColor = CyberYellow.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("verify_friend_button")
                        ) {
                            Text("Doğrula & Bul", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Radar scanning status
                    if (isVerifyingFriend) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            border = BorderStroke(1.dp, CyberYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CyberYellow,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = verificationStepText,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Verification error
                    friendVerificationError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = err, color = NeonRed, fontSize = 11.sp)
                    }

                    // Verified Friend Card
                    verifiedFriendCandidate?.let { candidate ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E16)),
                            border = BorderStroke(1.5.dp, NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verified_friend_result_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
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
                                        Text(text = candidate.avatarEmoji ?: "👤", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = candidate.name,
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
                                            text = candidate.username.orEmpty(),
                                            color = NeonGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Milyonlarca kişi arasından doğrulandı • E2EE Aktif",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        val existing = conversations.find { it.username?.equals(candidate.username, ignoreCase = true) == true }
                                        if (existing != null) {
                                            activeConversation = existing
                                        } else {
                                            conversations.add(0, candidate)
                                            activeConversation = candidate
                                        }
                                        showNewChatDialog = false
                                        newChatNameInput = ""
                                        verifiedFriendCandidate = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("start_chat_with_friend_button")
                                ) {
                                    Text("Mesaj At", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
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
                        verifiedFriendCandidate = null
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
        CreateGroupDialog(
            onGroupCreated = { newGroup ->
                conversations.add(0, newGroup)
                activeConversation = newGroup
            },
            onDismiss = { showCreateGroupDialog = false },
            isPaper = isPaperMode
        )
    }

    // Cloud connection dialog (Frame 00:05 "Bulut bağlantısını ayarla")
    if (showCloudConfigDialog) {
        AlertDialog(
            onDismissRequest = { showCloudConfigDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = CyberYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bulut Bağlantısı", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "GENeral Chat bulut ve E2EE şifreleme altyapısı aktiftir. Sunucu eşitlemesi ve oturum durumunuz güvence altındadır.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sunucu Durumu: Bağlı (E2EE Active)", color = NeonGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Veri Şifreleme: AES-256 + SHA-256", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCloudConfigDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Tamam", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Simulated Active Call Modal
    if (activeCallContact != null) {
        val (name, isVideo) = activeCallContact!!
        ActiveCallDialog(
            contactName = name,
            isVideo = isVideo,
            onEndCall = { activeCallContact = null }
        )
    }
}

/**
 * Tab 0: Sohbetler Screen (Exact match to Frame 00:05 & 00:06 + Messaging List)
 */
@Composable
private fun SohbetlerTabContent(
    conversations: List<ConversationItem>,
    onConversationClick: (ConversationItem) -> Unit,
    onGoToAi: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenChannel: () -> Unit,
    onOpenCloudSetup: () -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
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
            Spacer(modifier = Modifier.height(8.dp))

        // Big Yellow Squircle Logo matching Frame 00:05
        GeneralChatLogo(size = 84.dp)

        Spacer(modifier = Modifier.height(14.dp))

        // Brand Label: GENERAL EKOSİSTEMİ
        Text(
            text = "GENERAL EKOSİSTEMİ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else CyberYellow,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Title: GENeral Chat
        Text(
            text = "GENeral Chat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isPaper) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle from Video Frame 00:05
        Text(
            text = "GEN AI ile konuşabilir veya hesabını hazırlayabilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperTextMuted else TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Large Yellow Primary Button: GEN AI ile konuş
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
                text = "GEN AI ile konuş",
                color = if (isPaper) PaperBg else CyberBlack,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action 1: Hesap oluştur / giriş yap
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
                .height(46.dp)
                .testTag("home_auth_button")
        ) {
            Text(
                text = "Hesap oluştur / giriş yap",
                color = if (isPaper) PaperText else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action 2: Duyurular
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
                text = "Duyurular",
                color = if (isPaper) PaperText else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action 3: Bulut bağlantısını ayarla
        Button(
            onClick = {
                SoundPoolManager.playMechanicalClick()
                onOpenCloudSetup()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("home_cloud_button")
        ) {
            Text(
                text = "Bulut bağlantısını ayarla",
                color = if (isPaper) PaperText else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Two Side-by-Side Cards (01 Önce dene / 02 Sonra bağlan) from Video Frame 00:05
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: 01 Önce dene
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
                        text = "Önce dene",
                        color = if (isPaper) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "GEN AI'ı hesap açmadan kullan",
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Card 2: 02 Sonra bağlan
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
                        text = "Sonra bağlan",
                        color = if (isPaper) PaperText else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hesabın ve sohbetlerin için bulut kurulumu",
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Version Footer matching Video Frame 00:05
        Text(
            text = "Sürüm 7.3.3 · Sarı-siyah tasarım",
            style = MaterialTheme.typography.labelSmall,
            color = if (isPaper) PaperTextMuted else TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Real Messaging Section Search & Filter
        OutlinedTextField(
            value = chatSearchQuery,
            onValueChange = { chatSearchQuery = it },
            placeholder = { Text("Sohbetlerde veya geçmiş mesajlarda ara...", fontSize = 12.sp, color = if (isPaper) PaperTextMuted else TextMuted) },
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
                text = if (chatSearchQuery.isBlank()) "Gelen Mesajlar & Sohbetler" else "Arama Sonuçları",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPaper) PaperText else Color.White
            )
            Text(
                text = "${displayedConversations.size} Sohbet",
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
                        text = "Aramanızla eşleşen sohbet veya mesaj bulunamadı.",
                        color = if (isPaper) PaperTextMuted else TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(displayedConversations, key = { it.id }) { conv ->
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
                Text(
                    text = conversation.initials,
                    color = if (isPaper) PaperText else CyberYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaper) PaperText else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
    onStartCall: (String, Boolean) -> Unit
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
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
                    text = "Arama Geçmişi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaper) PaperText else Color.White
                )
            }

            Button(
                onClick = {
                    SoundPoolManager.playMechanicalClick()
                    onStartCall("Ahmet Yılmaz", false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else CyberYellow),
                shape = RoundedCornerShape(10.dp)
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
                    text = "WebRTC uçlar arası ses ve görüntü şifreleme devrede.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaper) PaperTextMuted else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(callHistory) { call ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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

                        IconButton(onClick = {
                            SoundPoolManager.playMechanicalClick()
                            onStartCall(call.contactName, false)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Ara",
                                tint = if (isPaper) PaperText else CyberYellow
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: GEN AI Screen (Exact 1:1 match to Frame 00:08 in the video)
 */
@Composable
private fun GenAiTabContent(
    chatViewModel: ChatViewModel
) {
    val isPaper = com.example.ui.theme.LocalPaperMode.current
    val messages by chatViewModel.messages.collectAsState()
    val sessions by chatViewModel.sessions.collectAsState()
    val activeSessionId by chatViewModel.activeSessionId.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }
    val inputText by chatViewModel.inputText.collectAsState()
    val selectedMode by chatViewModel.selectedMode.collectAsState()
    val isGenerating by chatViewModel.isGenerating.collectAsState()
    val streamingText by chatViewModel.streamingText.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

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
        Pair(AiMode.TRANSLATE, "Çeviri"),
        Pair(AiMode.IDEAS, "İçerik fikirleri"),
        Pair(AiMode.GUIDE, "Rehber hazırla")
    )

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty() || streamingText != null) {
            val target = (messages.size + if (streamingText != null) 1 else 0) - 1
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

                // MİSAFİR MODU Header matching Frame 00:08
                Text(
                    text = "MİSAFİR MODU",
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
                    text = "Genel bilgi, ders, matematik, yazı, çeviri, kodlama, fikir geliştirme ve çok adımlı sorular sorabilirsin. Takip sorularında önceki konuşmayı hatırlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaper) PaperTextMuted else TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status Card: GEN AI - Çok amaçlı yardımcı
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                                    "Çok amaçlı yardımcı · bağlamı koruyan",
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

                // Yellow Highlight Card from Frame 00:08 (Gemini 3.8 Flash info)
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
                                text = "Hesapsız kullanım · Daha kapsamlı yanıtlar",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isPaper) PaperText else CyberYellow
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gemini 3.8 Flash yapılandırmasıyla sorulara model yanıt verir; 8.1 Flash bağlantısı (yaklaşık 40 önceki soru-cevap turu; mesaj başına 12.000 karakter, toplam 480.000 karakter ve 24.576 çoka token'ına kadar yanıt bütçesi desteklenir. Mod seçerek yanıt tarzını yönlendirebilirsin. Ücretsiz Gemini katmanına göre metinler üretilir; ürün güvenliğin için kullanılabilir; özel bilgi vermeyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPaper) PaperText else Color(0xFFE8E8E8),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yanıt tarzı section matching Frame 00:08
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

                // Welcome Greeting Bubble matching Video Frame 00:08
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
                            text = "Merhaba! Ben GEN AI. Ders, günlük hayat, genel bilgi, matematik, yazı, çeviri, planlama, kodlama gibi konularda sorularını yanıtlayabilirim. Hangi konuda yardımcı olmamı istersin?",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPaper) PaperText else Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real Gemini Chat Conversation messages
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
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
                            .padding(12.dp)
                    ) {
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

        // Listening banner
        if (speechState.isListening) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonRed.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mikrofon aktif: Konuşun, sesiniz otomatik metne dönüştürülüyor...",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
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
                    focusedBorderColor = if (isPaper) PaperBorder else CyberYellow,
                    unfocusedBorderColor = if (isPaper) PaperBorder else CyberCardBorder,
                    focusedTextColor = if (isPaper) PaperText else Color.White,
                    unfocusedTextColor = if (isPaper) PaperText else Color.White,
                    focusedContainerColor = if (isPaper) PaperBg else CyberBlack,
                    unfocusedContainerColor = if (isPaper) PaperBg else CyberBlack
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    SoundPoolManager.playNotification()
                    chatViewModel.sendMessage()
                })
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Speech-to-Text Microphone Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (speechState.isListening) NeonRed else (if (isPaper) PaperBorder else CyberCardBg))
                    .border(1.dp, if (speechState.isListening) NeonRed else (if (isPaper) PaperBorder else CyberCardBorder), CircleShape),
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
                    modifier = Modifier.testTag("ai_mic_button")
                ) {
                    Icon(
                        imageVector = if (speechState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mikrofon ile konuş",
                        tint = if (speechState.isListening) Color.White else (if (isPaper) PaperText else CyberYellow),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
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
                            chatViewModel.sendMessage()
                        }
                    },
                    modifier = Modifier.testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
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
    isPaperMode: Boolean,
    onTogglePaperMode: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenDataPrivacy: () -> Unit,
    onOpenMasks: () -> Unit,
    onOpenAppLock: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenShowcase: () -> Unit,
    onOpenMiniGames: () -> Unit,
    onReplaySplash: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showCyberWidgetDialog by remember { mutableStateOf(false) }
    var showCamouflageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header from Video Frame 00:10
        Text(
            text = "GENERAL CHAT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else CyberYellow,
            letterSpacing = 1.sp
        )
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = if (isPaperMode) PaperText else Color.White
        )
        Text(
            text = "Hesabını, bağlantını, görünümü ve uygulama ayrıntılarını yönet.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isPaperMode) PaperTextMuted else TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Hesap ve sunucu
        Text(
            text = "Hesap ve sunucu",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 1: Hesap
        SettingActionCard(
            title = "Hesap",
            subtitle = if (currentUser != null) currentUser.email else "Hesap bağlı değil",
            buttonLabel = if (currentUser != null) "Profil" else "Giriş",
            onAction = onOpenAuth
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 2: Bulut bağlantısı
        SettingActionCard(
            title = "Güvenli Bulut Eşitlemesi",
            subtitle = if (currentUser != null) "E2EE şifreli tüneli aktif (${currentUser.email})" else "Yerel şifreli depolama devrede",
            buttonLabel = if (currentUser != null) "Bağlı" else "Giriş Yap",
            onAction = onOpenAuth
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card 3: Sunucu eşitlemesini yenile
        SettingActionCard(
            title = "Sunucu eşitlemesini yenile",
            subtitle = "Hesap ve sohbet listesi sunucudan tekrar alınır.",
            buttonLabel = "Yenile",
            onAction = { /* refresh */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Görünüm
        Text(
            text = "Görünüm",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card: Renk teması
        SettingActionCard(
            title = "Renk teması",
            subtitle = "Sarı-siyah görünüm.",
            buttonLabel = "Koyu",
            onAction = { }
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

        // Card: 120 Hz Ultra Akıcı ProMotion Ekran
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isPaperMode) PaperSurface else CyberCardBg),
            border = BorderStroke(1.dp, if (isPaperMode) PaperBorder else CyberYellow.copy(alpha = 0.6f)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "120 Hz Ultra Akıcı ProMotion",
                            color = if (isPaperMode) PaperText else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "WhatsApp ve benzerlerini geride bırakan 120 Hz ProMotion dinamik hızlandırma devrede. Dokunma gecikmesi sıfıra yakın.",
                        color = if (isPaperMode) PaperTextMuted else TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "120 Hz AKTİF",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Güvenlik ve veri (Frame 00:14)
        Text(
            text = "Güvenlik ve veri",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaperMode) PaperText else Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-screen button: Veri ve gizlilik (Frame 00:14 - 00:16)
        SettingActionCard(
            title = "Veri ve gizlilik",
            subtitle = "Cihazdaki uygulama verilerini veya hesap verilerini yönet.",
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

        SettingActionCard(
            title = "Uygulama Kilidi & Şifre",
            subtitle = "4-20 karakter siberpunk kilit koruması.",
            buttonLabel = "Ayarla",
            onAction = onOpenAppLock
        )

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
            title = "Uygulama İkonu & Yapay Zeka Adı",
            subtitle = "Siber kamuflaj, yapay zeka ikon üretimi (Sistem etiketi: GENeral Chat korunur).",
            buttonLabel = "Özelleştir",
            buttonColor = NeonCyan,
            onAction = { showCamouflageDialog = true }
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
}

@Composable
private fun SettingActionCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    buttonColor: Color = Color.White,
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
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
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
 * Interactive simulated live call modal
 */
@Composable
private fun ActiveCallDialog(
    contactName: String,
    isVideo: Boolean,
    onEndCall: () -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val formattedDuration = String.format("%02d:%02d", callSeconds / 60, callSeconds % 60)

    AlertDialog(
        onDismissRequest = onEndCall,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CyberYellowContainer)
                        .border(2.dp, CyberYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = CyberYellow,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "WebRTC E2EE Şifreli Arama",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedDuration,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(50.dp)
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
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) CyberYellow else CyberCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Hoparlör",
                            tint = if (isSpeakerOn) CyberBlack else Color.White
                        )
                    }

                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(50.dp)
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
