package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.util.PrivacyPreferencesManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationItem
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

@Composable
fun ContactProfileDialog(
    conversation: ConversationItem,
    customName: String,
    onUpdateCustomName: (String) -> Unit,
    isBlocked: Boolean,
    onToggleBlock: (Boolean) -> Unit,
    isMuted: Boolean,
    onToggleMute: (Boolean) -> Unit,
    onExportChat: () -> Unit,
    onClearChat: () -> Unit,
    onReportUser: ((reason: String, onComplete: (Result<String>) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showEditNameModal by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(customName) }
    var showReportModal by remember { mutableStateOf(false) }
    val privacyRev by PrivacyPreferencesManager.revision.collectAsState()
    val privacyConfig = remember(privacyRev, conversation.id) {
        PrivacyPreferencesManager.getChatConfig(context, conversation.id)
    }

    val resolvedUserId = conversation.username ?: "@id_${conversation.id.takeLast(6)}"

    val cardBg = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg
    val textColor = if (isPaper) Color(0xFF1E1C1A) else Color.White
    val textMutedColor = if (isPaper) Color(0xFF8A8275) else TextSecondary
    val borderColor = if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)
    val accentColor = if (isPaper) Color(0xFF1E1C1A) else CyberYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kullanıcı Profili",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = textColor
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = textColor)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar with Neon Ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414))
                        .border(
                            2.5.dp,
                            if (isBlocked) NeonRed else (if (isPaper) Color(0xFF2B2824) else CyberYellow),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!conversation.logoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = conversation.logoUri,
                            contentDescription = customName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = conversation.avatarEmoji ?: conversation.initials,
                            fontSize = if (conversation.avatarEmoji != null) 36.sp else 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPaper) Color(0xFF2B2824) else CyberYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom App Assigned Name + Edit Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = customName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            editNameInput = customName
                            showEditNameModal = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "İsmi Düzenle",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "Uygulama İçinde Kaydettiğiniz Özel İsim",
                    fontSize = 11.sp,
                    color = textMutedColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // User ID with Copy Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(resolvedUserId))
                        Toast.makeText(context, "Kullanıcı ID kopyalandı: $resolvedUserId", Toast.LENGTH_SHORT).show()
                        SoundPoolManager.playMechanicalClick()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kullanıcı ID: ",
                            fontSize = 11.sp,
                            color = textMutedColor
                        )
                        Text(
                            text = resolvedUserId,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transport & Session Security Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) Color(0xFFE5DFC9) else Color(0xFF0D1B2A)),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TLS Aktarım & Bulut Hesap Doğrulandı",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                            Text(
                                text = "Kimlik Referansı: $resolvedUserId · WebRTC DTLS-SRTP arama koruması",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = textMutedColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mute Notifications Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = textMutedColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bildirimleri Sessize Al", fontSize = 12.sp, color = textColor)
                        }
                        Switch(
                            checked = isMuted,
                            onCheckedChange = {
                                SoundPoolManager.playMechanicalClick()
                                onToggleMute(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentColor,
                                checkedTrackColor = if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000)
                            )
                        )
                    }

                    // Advanced Chat Privacy Panel (Gelişmiş Sohbet Gizliliği)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (privacyConfig.advancedPrivacyEnabled) Color(0xFF151226) else (if (isPaper) Color(0xFFECE6DA) else Color(0xFF141414))
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (privacyConfig.advancedPrivacyEnabled) NeonCyan else borderColor.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = if (privacyConfig.advancedPrivacyEnabled) NeonCyan else accentColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Gelişmiş Sohbet Gizliliği", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                                        Text("Ekran yakalama, dışa aktarma ve AI erişimini kapat", fontSize = 10.sp, color = textMutedColor)
                                    }
                                }
                                Switch(
                                    checked = privacyConfig.advancedPrivacyEnabled,
                                    onCheckedChange = { enabled ->
                                        SoundPoolManager.playMechanicalClick()
                                        PrivacyPreferencesManager.toggleMasterPrivacy(context, conversation.id, enabled)
                                        Toast.makeText(
                                            context,
                                            if (enabled) "Gelişmiş Sohbet Gizliliği aktifleştirildi." else "Gelişmiş Sohbet Gizliliği kapatıldı.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                                )
                            }

                            if (privacyConfig.advancedPrivacyEnabled) {
                                HorizontalDivider(color = borderColor.copy(alpha = 0.25f))
                                listOf(
                                    Triple("Ekran görüntüsü ve kayıt kilidi (FLAG_SECURE)", privacyConfig.blockScreenCapture) { v: Boolean ->
                                        PrivacyPreferencesManager.setChatConfig(context, conversation.id, privacyConfig.copy(blockScreenCapture = v))
                                    },
                                    Triple("Dışa aktarma ve iletme engeli", privacyConfig.blockExportAndForward) { v: Boolean ->
                                        PrivacyPreferencesManager.setChatConfig(context, conversation.id, privacyConfig.copy(blockExportAndForward = v))
                                    },
                                    Triple("Otomatik galeri/medya kaydını engelle", privacyConfig.blockAutoMediaSave) { v: Boolean ->
                                        PrivacyPreferencesManager.setChatConfig(context, conversation.id, privacyConfig.copy(blockAutoMediaSave = v))
                                    },
                                    Triple("GEN AI asistan bağlamından gizle", privacyConfig.excludeFromAi) { v: Boolean ->
                                        PrivacyPreferencesManager.setChatConfig(context, conversation.id, privacyConfig.copy(excludeFromAi = v))
                                    }
                                ).forEach { (label, checked, onChange) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, fontSize = 10.sp, color = textMutedColor, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = checked,
                                            onCheckedChange = {
                                                SoundPoolManager.playMechanicalClick()
                                                onChange(it)
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Export Chat Button (guarded by Gelişmiş Sohbet Gizliliği)
                    val exportLocked = privacyConfig.blockExportAndForward || privacyConfig.advancedPrivacyEnabled
                    OutlinedButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            if (exportLocked) {
                                Toast.makeText(context, "Gelişmiş Sohbet Gizliliği aktif: Dışa aktarma kilitli.", Toast.LENGTH_SHORT).show()
                            } else {
                                onExportChat()
                            }
                        },
                        enabled = !exportLocked,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (exportLocked) NeonRed.copy(alpha = 0.4f) else borderColor)
                    ) {
                        Icon(
                            imageVector = if (exportLocked) Icons.Default.Lock else Icons.Default.Download,
                            contentDescription = null,
                            tint = if (exportLocked) NeonRed else NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (exportLocked) "Dışa Aktarma Kilitli (Gelişmiş Gizlilik)" else "Sohbeti Dışa Aktar (Yerel Kasa / TXT)",
                            color = if (exportLocked) NeonRed else textColor,
                            fontSize = 12.sp
                        )
                    }

                    // Block / Unblock Contact Button
                    Button(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            val nextState = !isBlocked
                            onToggleBlock(nextState)
                            Toast.makeText(
                                context,
                                if (nextState) "$customName engellendi." else "$customName engeli kaldırıldı.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) NeonGreen else NeonRed.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBlocked) "Kullanıcının Engelini Kaldır" else "Kullanıcıyı Engelle",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Report User Button
                    OutlinedButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            showReportModal = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kullanıcıyı Şikayet Et", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = accentColor, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(18.dp)
    )

    // Edit Custom Name Sub-Dialog
    if (showEditNameModal) {
        AlertDialog(
            onDismissRequest = { showEditNameModal = false },
            title = { Text("Kişi İsmini Düzenle", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Column {
                    Text(
                        text = "Bu kişiye uygulamada istediğiniz özel bir takma ad veya isim atayın:",
                        fontSize = 12.sp,
                        color = textMutedColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = editNameInput.trim()
                        if (clean.isNotBlank()) {
                            onUpdateCustomName(clean)
                            SoundPoolManager.playConfirm()
                            Toast.makeText(context, "Kişi adı güncellendi.", Toast.LENGTH_SHORT).show()
                        }
                        showEditNameModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Kaydet", color = if (isPaper) Color.White else CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameModal = false }) {
                    Text("İptal", color = textMutedColor)
                }
            },
            containerColor = cardBg
        )
    }

    // Report User Sub-Dialog
    if (showReportModal) {
        var reportReason by remember { mutableStateOf("Spam veya İstenmeyen Mesajlar") }
        val reasons = listOf(
            "Spam veya İstenmeyen Mesajlar",
            "Taciz ve Uygunsuz Davranış",
            "Sahte Profil veya Taklit",
            "Kötü Amaçlı Bağlantı / Güvenlik Tehdidi"
        )
        AlertDialog(
            onDismissRequest = { showReportModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Report, contentDescription = null, tint = NeonRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kullanıcıyı Şikayet Et", fontWeight = FontWeight.Bold, color = textColor)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$customName ($resolvedUserId) için şikayet nedenini seçin:",
                        fontSize = 12.sp,
                        color = textMutedColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    reasons.forEach { reason ->
                        val isSelected = reportReason == reason
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) (if (isPaper) Color(0xFFD6CEBF) else Color(0xFF261010)) else Color.Transparent)
                                .border(1.dp, if (isSelected) NeonRed else borderColor, RoundedCornerShape(8.dp))
                                .clickable { reportReason = reason }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reason,
                                fontSize = 11.sp,
                                color = if (isSelected) NeonRed else textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val callback = onReportUser
                        showReportModal = false
                        SoundPoolManager.playNotification()
                        if (callback != null) {
                            callback(reportReason) { res ->
                                res.onSuccess { reportId ->
                                    Toast.makeText(
                                        context,
                                        "Şikayet bulut veritabanına kaydedildi (Kayıt No: $reportId).",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }.onFailure { err ->
                                    Toast.makeText(
                                        context,
                                        err.localizedMessage ?: "Şikayet iletilemedi; lütfen Google oturumunu kontrol edin.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Sunucu şikayeti için Google hesabıyla giriş yapılmış bir bulut sohbeti gereklidir.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Şikayeti Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportModal = false }) {
                    Text("Vazgeç", color = textMutedColor)
                }
            },
            containerColor = cardBg
        )
    }
}
