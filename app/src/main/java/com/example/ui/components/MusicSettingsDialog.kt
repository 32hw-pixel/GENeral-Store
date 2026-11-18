package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
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
import com.example.util.BackgroundMusicManager
import com.example.util.MusicTrack
import com.example.util.SoundPoolManager

data class RoyaltyFreeSong(
    val title: String,
    val artist: String,
    val genre: String,
    val duration: String,
    val isChill: Boolean,
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f
)

val curatedRoyaltyFreeTracks = listOf(
    RoyaltyFreeSong("Neon Glitch Protocol", "GEN Cybernetics", "Siberpunk", "03:42", false, 1.20f, 1.15f),
    RoyaltyFreeSong("Tokyo Rain Lo-Fi Beat", "Sub-Zero Audio", "Lo-Fi Chill", "02:58", true, 0.88f, 0.90f),
    RoyaltyFreeSong("Midnight Synthwave Drift", "Vector Wave", "Synthwave", "04:15", false, 1.05f, 0.95f),
    RoyaltyFreeSong("Deep Night Ambient (2700K)", "Acoustic Silence", "Derin Gece", "05:10", true, 0.78f, 0.82f),
    RoyaltyFreeSong("Neural Network Pulse", "Binary Soul", "Siberpunk", "03:20", false, 1.30f, 1.22f),
    RoyaltyFreeSong("Coffee & Matte Ink", "Paper Beats", "Lo-Fi Chill", "02:44", true, 0.95f, 1.00f),
    RoyaltyFreeSong("Grid Runner 2099", "Retrograde", "Synthwave", "03:50", false, 1.12f, 1.04f),
    RoyaltyFreeSong("Zero-Day Exploit Groove", "Ghost In Shell", "Siberpunk", "03:12", false, 0.92f, 0.86f),
    RoyaltyFreeSong("Safe Harbor Sleepwave", "Sleep Architect", "Derin Gece", "06:05", true, 0.70f, 0.75f)
)

/**
 * Cyber status footer matching the reference design:
 * "🔊 Müzik Aktif %50 | Tok Tuş Sesleri"
 * Clicking it opens the comprehensive audio control center.
 */
@Composable
fun CyberAudioStatusBar(
    modifier: Modifier = Modifier,
    onOpenAudioCenter: () -> Unit
) {
    val isPaper = LocalPaperMode.current
    val isPlaying by BackgroundMusicManager.isPlaying.collectAsState()
    val volume by BackgroundMusicManager.volume.collectAsState()
    val currentTrack by BackgroundMusicManager.currentTrack.collectAsState()
    val volPercent = (volume * 100).toInt()

    val textColor = if (isPaper) PaperTextMuted else TextSecondary
    val iconColor = if (isPaper) PaperText else (if (isPlaying) NeonCyan else TextMuted)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPaper) PaperSurface else Color(0xFF0F1722))
            .border(1.dp, if (isPaper) PaperBorder else CyberCardBorder, RoundedCornerShape(8.dp))
            .clickable {
                SoundPoolManager.playMenuNav()
                onOpenAudioCenter()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (volume > 0.4f) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isPlaying) "Müzik Aktif %$volPercent" else "Müzik Kapalı",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPlaying) (if (isPaper) PaperText else NeonCyan) else textColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "| Tok Tuş Sesleri",
            fontSize = 11.sp,
            color = textColor
        )
    }
}

@Composable
fun MusicSettingsDialog(
    onDismiss: () -> Unit
) {
    val isPaper = LocalPaperMode.current
    val context = LocalContext.current
    val isPlaying by BackgroundMusicManager.isPlaying.collectAsState()
    val volume by BackgroundMusicManager.volume.collectAsState()
    val currentTrack by BackgroundMusicManager.currentTrack.collectAsState()
    val customTitle by BackgroundMusicManager.customMusicTitle.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Tümü") }
    var isMutedTok by remember { mutableStateOf(SoundPoolManager.isMuted()) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Cihaz Müziği"
            BackgroundMusicManager.setCustomAudio(uri, fileName)
            SoundPoolManager.playConfirm()
        }
    }

    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else CyberYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isPaper) PaperText else NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ses ve Müzik Merkezi",
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs between Sound Settings & Royalty-Free Catalog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("🎛️ Genel Ses & Klavye", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isPaper) PaperBorder else CyberYellow,
                            selectedLabelColor = if (isPaper) PaperText else CyberBlack,
                            containerColor = if (isPaper) PaperSurface else CyberBlack,
                            labelColor = if (isPaper) PaperTextMuted else TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("🌐 Telifsiz Müzik Ara", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isPaper) PaperBorder else NeonCyan,
                            selectedLabelColor = if (isPaper) PaperText else CyberBlack,
                            containerColor = if (isPaper) PaperSurface else CyberBlack,
                            labelColor = if (isPaper) PaperTextMuted else TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        text = {
            if (selectedTab == 0) {
                // TAB 0: Master Volume, Keyboard click & notification sounds
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    // Play / Pause Master Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPlaying) Color(0xFF0F2537) else (if (isPaper) PaperBorder else Color(0xFF1E1E1E)))
                            .border(1.dp, if (isPlaying) NeonCyan else borderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                SoundPoolManager.playMenuNav()
                                BackgroundMusicManager.togglePlayPause()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) NeonCyan else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = CyberBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPlaying) "Arka Plan Müziği: Çalıyor" else "Arka Plan Müziği: Duraklatıldı",
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (currentTrack == MusicTrack.CUSTOM) customTitle else currentTrack.displayName,
                                    fontSize = 11.sp,
                                    color = textMutedColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Volume Slider
                    Text(
                        text = "Müzik Ses Düzeyi: %${(volume * 100).toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Slider(
                        value = volume,
                        onValueChange = { newVol ->
                            BackgroundMusicManager.setVolume(newVol)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isPaper) PaperText else CyberYellow,
                            activeTrackColor = if (isPaper) PaperText else CyberYellow,
                            inactiveTrackColor = borderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "STANDART ARKA PLAN PARÇALARI:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 1: Stable Cyber Beat
                    TrackSelectionCard(
                        title = "1. Parça: Cyber Electro Beat",
                        subtitle = "Fütüristik elektronik siberpunk vuruşları",
                        isSelected = currentTrack == MusicTrack.STABLE_BEAT,
                        isPaper = isPaper,
                        trailingIcon = Icons.Default.Audiotrack,
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            BackgroundMusicManager.playTrack(MusicTrack.STABLE_BEAT)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 2: Chill Ambient
                    TrackSelectionCard(
                        title = "2. Parça: Chill Ambient & Paper",
                        subtitle = "Sakin, derin odaklanma ve gece modu için",
                        isSelected = currentTrack == MusicTrack.CHILL_AMBIENT,
                        isPaper = isPaper,
                        trailingIcon = Icons.Default.MusicNote,
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            BackgroundMusicManager.playTrack(MusicTrack.CHILL_AMBIENT)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 3: Custom from Device Storage
                    TrackSelectionCard(
                        title = if (currentTrack == MusicTrack.CUSTOM) "Cihaz Müziği: $customTitle" else "Cihazdan Müzik Seç (MP3/WAV)",
                        subtitle = "Telefon hafızasındaki indirilen müzikleri arka plana ata",
                        isSelected = currentTrack == MusicTrack.CUSTOM,
                        isPaper = isPaper,
                        trailingIcon = Icons.Default.FolderOpen,
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                            audioPickerLauncher.launch("audio/*")
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    // Mechanical Tok sound switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mekanik Tok Tuş Sesleri",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "Menü, liste kaydırma ve tuşlarda tok tıklama",
                                fontSize = 11.sp,
                                color = textMutedColor
                            )
                        }
                        Switch(
                            checked = !isMutedTok,
                            onCheckedChange = { active ->
                                isMutedTok = !active
                                SoundPoolManager.setMuted(!active)
                                if (active) SoundPoolManager.playMechanicalClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isPaper) PaperBg else CyberBlack,
                                checkedTrackColor = if (isPaper) PaperText else CyberYellow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Keyboard sound selector
                    Text(
                        text = "KLAVYE TIKLAMA SESİ (MEKANİK & SİBER):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    var currentKb by remember { mutableStateOf(SoundPoolManager.getKeyboardProfile()) }
                    var currentNotif by remember { mutableStateOf(SoundPoolManager.getNotificationProfile()) }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        com.example.util.KeyboardSoundProfile.entries.forEach { kbProfile ->
                            val isSelected = kbProfile == currentKb
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) (if (isPaper) PaperBorder else Color(0xFF1B2B3E)) else (if (isPaper) PaperSurface else CyberBlack))
                                    .border(1.dp, if (isSelected) (if (isPaper) PaperText else CyberYellow) else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentKb = kbProfile
                                        SoundPoolManager.setKeyboardProfile(kbProfile)
                                        SoundPoolManager.testKeyboardSound(kbProfile)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = kbProfile.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) (if (isPaper) PaperText else CyberYellow) else (if (isPaper) PaperText else Color.White)
                                    )
                                    Text(
                                        text = kbProfile.subtitle,
                                        fontSize = 9.sp,
                                        color = if (isPaper) PaperTextMuted else TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (isPaper) PaperText else CyberYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notification Sounds Selector
                    Text(
                        text = "BİLDİRİM SESİ (SİBERPUNK):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        com.example.util.NotificationSoundProfile.entries.forEach { notifProfile ->
                            val isSelected = notifProfile == currentNotif
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) (if (isPaper) PaperBorder else Color(0xFF162A22)) else (if (isPaper) PaperSurface else CyberBlack))
                                    .border(1.dp, if (isSelected) (if (isPaper) PaperText else NeonGreen) else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentNotif = notifProfile
                                        SoundPoolManager.setNotificationProfile(notifProfile)
                                        SoundPoolManager.testNotificationSound(notifProfile)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notifProfile.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) (if (isPaper) PaperText else NeonGreen) else (if (isPaper) PaperText else Color.White)
                                    )
                                    Text(
                                        text = notifProfile.subtitle,
                                        fontSize = 9.sp,
                                        color = if (isPaper) PaperTextMuted else TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (isPaper) PaperText else NeonGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Royalty-Free Music Search & CC0 Guarantee
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    // CC0 Guarantee Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "TELİFSİZLİK %100 GARANTİLİ (CC0)",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Tüm müzikler kamu malıdır (Public Domain). YouTube videolarında veya canlı yayınlarda telif ihtarı (Content ID) veya açıklama linki gerektirmez.",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Müzik, tür veya sanatçı ara...", color = textMutedColor, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = if (isPaper) PaperBg else CyberBlack,
                            unfocusedContainerColor = if (isPaper) PaperBg else CyberBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Genre Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Tümü", "Siberpunk", "Lo-Fi Chill", "Synthwave", "Derin Gece").forEach { genre ->
                            val isChosen = selectedGenre == genre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) (if (isPaper) PaperBorder else CyberYellow) else (if (isPaper) PaperSurface else CyberBlack))
                                    .border(1.dp, if (isChosen) (if (isPaper) PaperText else CyberYellow) else borderColor, RoundedCornerShape(6.dp))
                                    .clickable { selectedGenre = genre }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) (if (isPaper) PaperText else CyberBlack) else textMutedColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredList = curatedRoyaltyFreeTracks.filter { track ->
                        val matchesSearch = track.title.contains(searchQuery, ignoreCase = true) ||
                                track.artist.contains(searchQuery, ignoreCase = true) ||
                                track.genre.contains(searchQuery, ignoreCase = true)
                        val matchesGenre = selectedGenre == "Tümü" || track.genre == selectedGenre
                        matchesSearch && matchesGenre
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredList.forEach { track ->
                            val isSelectedTrack = customTitle == track.title
                            val isCurrentPlaying = isSelectedTrack && isPlaying
                            Card(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isSelectedTrack) {
                                        BackgroundMusicManager.togglePlayPause()
                                    } else {
                                        BackgroundMusicManager.setCustomRoyaltyFreeTrack(
                                            track.title,
                                            track.isChill,
                                            track.playbackSpeed,
                                            track.playbackPitch
                                        )
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedTrack) Color(0xFF142436) else (if (isPaper) PaperSurface else CyberBlack)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelectedTrack) NeonCyan else borderColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelectedTrack) NeonCyan else CyberCardBorder),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (isSelectedTrack) CyberBlack else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = track.title,
                                                color = if (isSelectedTrack) NeonCyan else textColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${track.artist} · ${track.genre} · ${track.duration}",
                                                color = textMutedColor,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            BackgroundMusicManager.setCustomRoyaltyFreeTrack(
                                                track.title,
                                                track.isChill,
                                                track.playbackSpeed,
                                                track.playbackPitch
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelectedTrack) NeonCyan else (if (isPaper) PaperBorder else CyberCardBorder)
                                        ),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isCurrentPlaying -> "Çalıyor 🔊"
                                                isSelectedTrack -> "Seçildi ⏸"
                                                else -> "Seç"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelectedTrack) CyberBlack else textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaper) PaperText else CyberYellow
                )
            ) {
                Text(
                    text = "Kapat",
                    color = if (isPaper) PaperBg else CyberBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = cardBg
    )
}

@Composable
private fun TrackSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPaper: Boolean,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    val cardBg = if (isSelected) {
        if (isPaper) PaperBorder else Color(0xFF142436)
    } else {
        if (isPaper) PaperSurface else CyberCardBg
    }
    val borderColor = if (isSelected) {
        if (isPaper) PaperText else NeonCyan
    } else {
        if (isPaper) PaperBorder else CyberCardBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) (if (isPaper) PaperText else NeonCyan) else (if (isPaper) PaperText else Color.White)
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isPaper) PaperTextMuted else TextSecondary
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isPaper) PaperText else NeonGreen,
                modifier = Modifier.size(18.dp)
            )
        } else if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = if (isPaper) PaperTextMuted else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
