package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay

data class AppIdentityProfile(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val subtitle: String
)

@Composable
fun CamouflageAppIdentityDialog(
    currentName: String = "GENeral Chat",
    currentEmoji: String = "⚡",
    onSaveProfile: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var customName by remember { mutableStateOf(currentName) }
    var selectedEmoji by remember { mutableStateOf(currentEmoji) }
    var isGeneratingByAi by remember { mutableStateOf(false) }

    val presetProfiles = listOf(
        AppIdentityProfile("p1", "GENeral Chat", "⚡", "Orijinal Siberpunk Şifreli Hat"),
        AppIdentityProfile("p2", "Hesap Makinesi Pro", "🔢", "Sıradan Büro Hesap Makinesi"),
        AppIdentityProfile("p3", "Hava Durumu & Radar", "⛅", "Günlük Meteoroloji Uygulaması"),
        AppIdentityProfile("p4", "Sistem Notları", "📝", "Standart Metin & Not Defteri"),
        AppIdentityProfile("p5", "Kripto Cüzdanı", "🪙", "Blokzincir & Varlık İzleyici"),
        AppIdentityProfile("p6", "Linux Terminal", "💻", "Siber Güvenlik Komut Satırı")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Ekran Maskesi & Başlık Özelleştirme", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Uygulama İçi Kamuflaj Görünümü", color = CyberYellow, fontSize = 10.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                // Legal & Battery Saver Notice
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E16)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Şeffaflık Bildirimi: Android sistem ikonu ve resmi uygulama adı her zaman 'GENeral Chat' olarak korunur. Buradaki seçim yalnızca uygulama içi başlık ve ekran maskesi görünümünü değiştirir.",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Name & AI Generator Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Görünen Ad", fontSize = 11.sp, color = CyberYellow) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberYellow,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            isGeneratingByAi = true
                            val presetNames = listOf("Not Defteri", "Hava Durumu", "Hesap Makinesi", "Kronometre", "SiberPulse")
                            val presetEmojis = listOf("📝", "⛅", "🔢", "⏱️", "⚡")
                            val idx = (0..4).random()
                            customName = presetNames[idx]
                            selectedEmoji = presetEmojis[idx]
                            isGeneratingByAi = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Öner", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("HAZIR KAMUFLAJ PROFİLLERİ", color = CyberYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetProfiles) { profile ->
                        val isSelected = customName == profile.name
                        Card(
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                customName = profile.name
                                selectedEmoji = profile.iconEmoji
                            },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E1A05) else CyberBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberYellow else CyberCardBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(CyberCardBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(profile.iconEmoji, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(profile.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(profile.subtitle, color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundPoolManager.playConfirm()
                    onSaveProfile(customName, selectedEmoji)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
            ) {
                Text("Uygula", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondary)
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
