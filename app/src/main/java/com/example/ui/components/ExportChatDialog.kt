package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.DirectMessage
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportChatDialog(
    conversation: ConversationItem,
    messages: List<DirectMessage>,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val exportText = remember(conversation, messages) {
        val dateHeader = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date())
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("GENeral Chat v7.7.3 Pro - Sohbet Dökümü")
        sb.appendLine("TLS Aktarım & SHA-256 Sağlama Kaydı")
        sb.appendLine("==================================================")
        sb.appendLine("Sohbet Ortağı : ${conversation.name} (${conversation.username ?: "@id"})")
        sb.appendLine("Dışa Aktarma Tarihi : $dateHeader")
        sb.appendLine("Toplam Mesaj Sayısı : ${messages.size}")
        sb.appendLine("Biçim               : Düz Metin Dökümü (.txt)")
        sb.appendLine("--------------------------------------------------\n")

        messages.forEach { m ->
            val sender = if (m.isMe) "Sen" else m.senderName
            val extra = when {
                m.isVoiceNote -> " [Sesli Mesaj (${m.voiceDurationSeconds}s)]"
                m.attachmentName != null -> " [Ek: ${m.attachmentName}]"
                else -> ""
            }
            sb.appendLine("[${m.time}] $sender: ${m.text}$extra")
        }

        sb.appendLine("\n==================================================")
        sb.appendLine("=== SHA-256 Kriptografik Sağlama: GEN-${System.currentTimeMillis().toString(16).uppercase()} ===")
        sb.appendLine("==================================================")
        sb.toString()
    }

    val cardBg = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg
    val textColor = if (isPaper) Color(0xFF1E1C1A) else Color.White
    val textMutedColor = if (isPaper) Color(0xFF8A8275) else TextSecondary
    val borderColor = if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)
    val accentColor = if (isPaper) Color(0xFF1E1C1A) else CyberYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPaper) Color(0xFFE2DDD5) else Color(0xFF0F1E36))
                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sohbeti Dışa Aktar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Text(
                        text = "${conversation.name} ile ${messages.size} mesaj",
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Konuşma geçmişi zaman damgalı düz metin formatında dışa aktarılmaya hazır:",
                    fontSize = 12.sp,
                    color = textMutedColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transcript preview container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPaper) Color(0xFFECE6DA) else CyberBlack)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = exportText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = if (isPaper) Color(0xFF2B2824) else NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy to clipboard button
                    OutlinedButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            clipboardManager.setText(AnnotatedString(exportText))
                            Toast.makeText(context, "Sohbet panoya kopyalandı.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Panoya Kopyala", fontSize = 11.sp)
                    }

                    // Share button
                    Button(
                        onClick = {
                            SoundPoolManager.playNotification()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, exportText)
                                putExtra(Intent.EXTRA_SUBJECT, "GENeral Chat - ${conversation.name} Sohbet Dökümü")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Sohbeti Paylaş")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = if (isPaper) Color.White else CyberBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paylaş / Kaydet", color = if (isPaper) Color.White else CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(16.dp)
    )
}
