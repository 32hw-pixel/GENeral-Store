package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager

enum class QuickActionItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badgeColor: Color
) {
    VOICE_NOTE("voice", "Hızlı Ses", Icons.Default.Mic, CyberYellow),
    PHOTO("photo", "Kamera/Foto", Icons.Default.CameraAlt, NeonCyan),
    FILE("file", "Dosya/APK", Icons.Default.Folder, NeonGreen),
    BURNER("burner", "İmha Sayacı", Icons.Default.Timer, NeonRed),
    AI_SUMMARY("ai_summary", "Yapay Zeka Özeti", Icons.Default.AutoAwesome, CyberYellow),
    GALLERY("gallery", "Medya Galerisi", Icons.Default.PhotoLibrary, NeonCyan),
    P2P_TUNNEL("p2p", "Çevrimdışı P2P", Icons.Default.WifiTethering, NeonGreen),
    THEME("theme", "Sohbet Teması", Icons.Default.Palette, Color(0xFFC084FC))
}

@Composable
fun QuickActionBar(
    burnerSeconds: Int?,
    onCycleBurner: () -> Unit,
    onTriggerVoiceNote: () -> Unit,
    onPickPhoto: () -> Unit,
    onPickFile: () -> Unit,
    onOpenAiSummary: () -> Unit,
    onOpenMediaGallery: () -> Unit,
    onOpenP2PTunnel: () -> Unit,
    onOpenThemePicker: () -> Unit,
    isPaper: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showCustomizerDialog by remember { mutableStateOf(false) }

    val enabledActionIds = remember {
        mutableStateListOf(
            QuickActionItem.VOICE_NOTE.id,
            QuickActionItem.PHOTO.id,
            QuickActionItem.FILE.id,
            QuickActionItem.BURNER.id,
            QuickActionItem.AI_SUMMARY.id,
            QuickActionItem.GALLERY.id,
            QuickActionItem.P2P_TUNNEL.id,
            QuickActionItem.THEME.id
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF141208))
                    .border(0.5.dp, if (isPaper) Color(0xFFD6CEBF) else CyberCardBorder)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cyberpunk Header Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPaper) Color(0xFFDFD7C7) else Color(0xFF262000))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚡ HIZLI",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPaper) Color(0xFF3E3A32) else CyberYellow,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Scrollable actions row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionItem.values().forEach { action ->
                        if (enabledActionIds.contains(action.id)) {
                            val isBurnerActive = action == QuickActionItem.BURNER && burnerSeconds != null
                            val chipBg = if (isBurnerActive) NeonRed else if (isPaper) Color(0xFFF4EEDD) else CyberBlack
                            val chipBorder = if (isBurnerActive) NeonRed else if (isPaper) Color(0xFFD6CEBF) else action.badgeColor.copy(alpha = 0.5f)
                            val contentColor = if (isBurnerActive) Color.White else if (isPaper) Color(0xFF2B2721) else action.badgeColor

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        when (action) {
                                            QuickActionItem.VOICE_NOTE -> onTriggerVoiceNote()
                                            QuickActionItem.PHOTO -> onPickPhoto()
                                            QuickActionItem.FILE -> onPickFile()
                                            QuickActionItem.BURNER -> onCycleBurner()
                                            QuickActionItem.AI_SUMMARY -> onOpenAiSummary()
                                            QuickActionItem.GALLERY -> onOpenMediaGallery()
                                            QuickActionItem.P2P_TUNNEL -> onOpenP2PTunnel()
                                            QuickActionItem.THEME -> onOpenThemePicker()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    tint = contentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (action == QuickActionItem.BURNER && burnerSeconds != null) "${burnerSeconds}s İmha" else action.title,
                                    color = contentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Customize Gear
                IconButton(
                    onClick = {
                        SoundPoolManager.playMenuNav()
                        showCustomizerDialog = true
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Çubuğu Özelleştir",
                        tint = if (isPaper) Color(0xFF6B655B) else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    if (showCustomizerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomizerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hızlı Eylem Çubuğunu Özelleştir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Alt çubukta görünmesini istediğiniz siber fonksiyonları seçin:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    QuickActionItem.values().forEach { item ->
                        val isChecked = enabledActionIds.contains(item.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) {
                                        if (enabledActionIds.size > 2) enabledActionIds.remove(item.id)
                                    } else {
                                        enabledActionIds.add(item.id)
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) enabledActionIds.add(item.id)
                                    else if (enabledActionIds.size > 2) enabledActionIds.remove(item.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyberYellow,
                                    checkmarkColor = CyberBlack,
                                    uncheckedColor = CyberCardBorder
                                )
                            )
                            Icon(item.icon, contentDescription = null, tint = item.badgeColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCustomizerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Kaydet", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
