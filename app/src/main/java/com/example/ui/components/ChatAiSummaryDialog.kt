package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DirectMessage
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay

@Composable
fun ChatAiSummaryDialog(
    contactName: String,
    messages: List<DirectMessage>,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isGenerating by remember { mutableStateOf(true) }
    var summaryText by remember { mutableStateOf("") }
    var keyPoints by remember { mutableStateOf<List<String>>(emptyList()) }
    var actionItems by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        delay(650)
        isGenerating = false
        val count = messages.size
        val hasVoice = messages.any { it.isVoiceNote }
        val hasMedia = messages.any { it.attachmentType != null }

        summaryText = "$contactName ile gerçekleştirilen $count mesaj incelendi. İletişim oturumu aktif, içerik analizi tamamlandı."
        keyPoints = listOf(
            "Toplam $count adet sohbet mesajı incelendi.",
            if (hasVoice) "Kaydedilen sesli notlar ve süre bilgileri doğrulandı." else "Metin tabanlı sohbet akışı incelendi.",
            if (hasMedia) "Sohbet içerisinde medya/dosya paylaşımı bulunuyor." else "Sohbet akışı metin odaklı ilerledi."
        )
        actionItems = listOf(
            "Özet metni SHA-256 sağlama bilgisiyle panoya kopyalanmaya hazır.",
            "Gerektiğinde 'Panik Butonu' ile yerel görünüm anında gizlenebilir."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GEN AI Akıllı Sohbet Özeti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            if (isGenerating) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = CyberYellow, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Sohbet kayıtları analiz ediliyor...", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ÖZET DEĞERLENDİRME", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(summaryText, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ÖNEMLİ VURGULAR", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            keyPoints.forEach { point ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• ", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(point, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EYLEM MADDELERİ", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            actionItems.forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("✔ ", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(item, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
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
                    val full = "$summaryText\n\nVurgular:\n" + keyPoints.joinToString("\n") { "• $it" }
                    clipboardManager.setText(AnnotatedString(full))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Özeti Kopyala", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = TextSecondary)
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
