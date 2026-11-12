package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ScheduledMessage
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleMessageDialog(
    initialText: String,
    recipientName: String,
    onSchedule: (text: String, scheduledEpoch: Long) -> Unit,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    var messageText by remember { mutableStateOf(initialText) }
    var selectedMinutesOffset by remember { mutableIntStateOf(10) } // 1, 5, 10, 30, 60, 1440 (1 day)
    var customDelayMinutes by remember { mutableStateOf("") }

    val presetDelays = listOf(
        Pair(1, "1 Dakika Sonra"),
        Pair(5, "5 Dakika Sonra"),
        Pair(10, "10 Dakika Sonra"),
        Pair(30, "30 Dakika Sonra"),
        Pair(60, "1 Saat Sonra"),
        Pair(1440, "Yarın Aynı Saat")
    )

    val targetDeliveryTime = remember(selectedMinutesOffset) {
        val epoch = System.currentTimeMillis() + (selectedMinutesOffset * 60 * 1000L)
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(epoch))
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
                        .background(if (isPaper) Color(0xFFE2DDD5) else Color(0xFF262000))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "İleri Tarihli Mesaj Zamanla",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Text(
                        text = "Alıcı: $recipientName",
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Mesajınız:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Zamanlanacak mesajı yazın...", color = textMutedColor, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack,
                        unfocusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Gönderim Zamanı Seç:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetDelays.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (mins, label) ->
                                val isSelected = selectedMinutesOffset == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        SoundPoolManager.playMechanicalClick()
                                        selectedMinutesOffset = mins
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000),
                                        selectedLabelColor = accentColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target preview card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPaper) Color(0xFFE5DFC9) else Color(0xFF141414)
                    ),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Otomatik Teslimat Tarihi:", fontSize = 10.sp, color = textMutedColor)
                            Text(targetDeliveryTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = messageText.trim()
                    if (clean.isNotBlank()) {
                        val epoch = System.currentTimeMillis() + (selectedMinutesOffset * 60 * 1000L)
                        onSchedule(clean, epoch)
                        SoundPoolManager.playConfirm()
                        onDismiss()
                    }
                },
                enabled = messageText.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Zamanla", color = if (isPaper) Color.White else CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = textMutedColor)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ScheduledMessagesListDialog(
    messages: List<ScheduledMessage>,
    onSendNow: (ScheduledMessage) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (ScheduledMessage, String, Long) -> Unit,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    var messageToEdit by remember { mutableStateOf<ScheduledMessage?>(null) }
    var editedText by remember { mutableStateOf("") }

    val cardBg = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg
    val textColor = if (isPaper) Color(0xFF1E1C1A) else Color.White
    val textMutedColor = if (isPaper) Color(0xFF8A8275) else TextSecondary
    val borderColor = if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)
    val accentColor = if (isPaper) Color(0xFF1E1C1A) else CyberYellow

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.80f),
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(1.5.dp, borderColor),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaper) Color(0xFFE2DDD5) else Color(0xFF262000))
                                .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bekleyen Zamanlanmış Mesajlar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "${messages.size} adet mesaj zamanlandı",
                                fontSize = 11.sp,
                                color = textMutedColor
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = textMutedColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bekleyen zamanlanmış mesaj bulunmuyor.\nSohbet içindeki saat simgesine dokunarak mesaj zamanlayabilirsiniz.",
                                color = textMutedColor,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(Date(msg.scheduledEpochMillis))
                            val isPastDue = System.currentTimeMillis() >= msg.scheduledEpochMillis

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPaper) Color.White else CyberBlack
                                ),
                                border = BorderStroke(1.dp, if (isPastDue) NeonGreen else borderColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isPastDue) NeonGreen else CyberYellow)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isPastDue) "İletilmeye Hazır" else "Teslimat: $dateStr",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPastDue) NeonGreen else CyberYellow
                                            )
                                        }

                                        Text(
                                            text = "Alıcı: ${msg.targetRecipientName}",
                                            fontSize = 10.sp,
                                            color = textMutedColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = msg.text,
                                        color = textColor,
                                        fontSize = 13.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Button
                                        OutlinedButton(
                                            onClick = {
                                                SoundPoolManager.playMechanicalClick()
                                                messageToEdit = msg
                                                editedText = msg.text
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Düzenle", fontSize = 10.sp)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Send Now Button
                                        Button(
                                            onClick = {
                                                SoundPoolManager.playNotification()
                                                onSendNow(msg)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Şimdi Gönder", color = CyberBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Cancel / Delete Button
                                        IconButton(
                                            onClick = {
                                                SoundPoolManager.playMechanicalClick()
                                                onDelete(msg.id)
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "İptal Et", tint = NeonRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Modal Dialog
    if (messageToEdit != null) {
        val target = messageToEdit!!
        AlertDialog(
            onDismissRequest = { messageToEdit = null },
            title = { Text("Zamanlanmış Mesajı Düzenle", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = editedText.trim()
                        if (clean.isNotBlank()) {
                            onEdit(target, clean, target.scheduledEpochMillis)
                            SoundPoolManager.playConfirm()
                        }
                        messageToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Kaydet", color = if (isPaper) Color.White else CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToEdit = null }) {
                    Text("İptal", color = textMutedColor)
                }
            },
            containerColor = cardBg
        )
    }
}
