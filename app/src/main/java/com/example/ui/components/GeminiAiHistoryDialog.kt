package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiMode
import com.example.data.ChatSessionEntity
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
fun GeminiAiHistoryDialog(
    sessions: List<ChatSessionEntity>,
    activeSessionId: String?,
    onSelectSession: (String) -> Unit,
    onStartNewChat: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var showConfirmClearAll by remember { mutableStateOf(false) }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            sessions.filter { it.title.lowercase(Locale.getDefault()).contains(q) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = if (isPaper) Color(0xFFF7F4EE) else CyberCardBg,
            border = BorderStroke(1.5.dp, if (isPaper) Color(0xFFDCD5C9) else CyberYellow.copy(alpha = 0.7f)),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Room Database Badge
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
                                .border(1.dp, if (isPaper) Color(0xFFC0B8AA) else CyberYellow, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (isPaper) Color(0xFF2B2824) else CyberYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GEN AI Konuşma Geçmişi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isPaper) Color(0xFF1E1C1A) else Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = if (isPaper) Color(0xFF5A544C) else NeonGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Oda (Room SQLite) Yerel Veritabanı",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaper) Color(0xFF5A544C) else NeonGreen
                                )
                            }
                        }
                    }

                    IconButton(onClick = {
                        SoundPoolManager.playMechanicalClick()
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = if (isPaper) Color(0xFF2B2824) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: New Chat & Clear All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SoundPoolManager.playNotification()
                            onStartNewChat()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) Color(0xFF2B2824) else CyberYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isPaper) Color.White else CyberBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Yeni Sohbet Başlat",
                            color = if (isPaper) Color.White else CyberBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (sessions.isNotEmpty()) {
                        Button(
                            onClick = {
                                SoundPoolManager.playMechanicalClick()
                                showConfirmClearAll = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack
                            ),
                            border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = NeonRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tümünü Sil",
                                color = NeonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar in Past Sessions
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Geçmiş konuşmalarda başlık ara...",
                            fontSize = 12.sp,
                            color = if (isPaper) Color(0xFF8A8275) else TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isPaper) Color(0xFF5A544C) else CyberYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Temizle", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPaper) Color(0xFF5A544C) else CyberYellow,
                        unfocusedBorderColor = if (isPaper) Color(0xFFD6CEBF) else CyberCardBorder,
                        focusedTextColor = if (isPaper) Color(0xFF1E1C1A) else Color.White,
                        unfocusedTextColor = if (isPaper) Color(0xFF1E1C1A) else Color.White,
                        focusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack,
                        unfocusedContainerColor = if (isPaper) Color(0xFFECE6DA) else CyberBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Database Statistics Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPaper) Color(0xFFE5DFC9) else Color(0xFF121212))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kayıtlı Oturum: ${sessions.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaper) Color(0xFF2B2824) else Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = if (isPaper) Color(0xFF2B2824) else CyberYellow, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "AES & SQLite Room Şifreli",
                                fontSize = 10.sp,
                                color = if (isPaper) Color(0xFF5A544C) else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sessions List
                if (filteredSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isPaper) Color(0xFF8A8275) else CyberYellow.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Henüz kayıtlı bir yapay zeka oturumu yok.\nYeni bir sohbet başlatarak soru sorabilirsiniz."
                                else "Aramanızla eşleşen bir oturum bulunamadı.",
                                color = if (isPaper) Color(0xFF8A8275) else TextSecondary,
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
                        items(filteredSessions, key = { it.id }) { session ->
                            val isActive = session.id == activeSessionId
                            val modeObj = AiMode.fromId(session.mode)
                            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")).format(Date(session.updatedAt))

                            Card(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    onSelectSession(session.id)
                                    onDismiss()
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) {
                                        if (isPaper) Color(0xFFE2DDD5) else Color(0xFF262000)
                                    } else {
                                        if (isPaper) Color.White else CyberBlack
                                    }
                                ),
                                border = BorderStroke(
                                    if (isActive) 1.5.dp else 1.dp,
                                    if (isActive) {
                                        if (isPaper) Color(0xFF2B2824) else CyberYellow
                                    } else {
                                        if (isPaper) Color(0xFFD6CEBF) else CyberCardBorder
                                    }
                                ),
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) CyberYellow else (if (isPaper) Color(0xFFECE6DA) else CyberCardBg)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (modeObj) {
                                                AiMode.GENERAL -> "🌐"
                                                AiMode.LEARN -> "📚"
                                                AiMode.CODE -> "⚡"
                                                AiMode.MATH -> "📐"
                                                AiMode.TRANSLATE -> "🌍"
                                                AiMode.IDEAS -> "💡"
                                                AiMode.GUIDE -> "📋"
                                            },
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = session.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPaper) Color(0xFF1E1C1A) else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(NeonGreen.copy(alpha = 0.2f))
                                                        .border(0.5.dp, NeonGreen, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "AKTİF",
                                                        color = NeonGreen,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = dateStr,
                                                fontSize = 10.sp,
                                                color = if (isPaper) Color(0xFF8A8275) else TextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "· ${modeObj.title}",
                                                fontSize = 10.sp,
                                                color = if (isPaper) Color(0xFF5A544C) else CyberYellow
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Action buttons: Rename & Delete
                                    IconButton(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            sessionToRename = session
                                            renameInput = session.title
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Adı Düzenle",
                                            tint = if (isPaper) Color(0xFF5A544C) else CyberYellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            onDeleteSession(session.id)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = NeonRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Session Modal Dialog
    if (sessionToRename != null) {
        val target = sessionToRename!!
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = {
                Text(
                    text = "Konuşma Başlığını Düzenle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Oda veritabanında bu konuşma için yeni bir başlık belirleyin:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = renameInput.trim()
                        if (clean.isNotBlank()) {
                            onRenameSession(target.id, clean)
                            SoundPoolManager.playConfirm()
                        }
                        sessionToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
                ) {
                    Text("Kaydet", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirm Clear All Sessions Modal Dialog
    if (showConfirmClearAll) {
        AlertDialog(
            onDismissRequest = { showConfirmClearAll = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = NeonRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tüm Geçmişi Sil?", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Oda (Room) veritabanında saklanan tüm yapay zeka konuşmaları ve mesaj geçmişi kalıcı olarak silinecektir. Bu işlem geri alınamaz.\n\nOnaylıyor musunuz?",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClearAll = false
                        onClearAll()
                        SoundPoolManager.playNotification()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Tümünü Temizle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearAll = false }) {
                    Text("Vazgeç", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
