package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

data class FriendContact(
    val id: String,
    val name: String,
    val username: String,
    val avatarEmoji: String
)

val defaultAvailableFriends = listOf(
    FriendContact("u1", "Eren Developer", "@eren_dev", "⚡"),
    FriendContact("u2", "Ayşe Güvenlik Uzmanı", "@ayse_sec", "🛡️"),
    FriendContact("u3", "Mehmet Yapay Zekâ", "@mehmet_ai", "🤖"),
    FriendContact("u4", "Zeynep Cloud Mimarı", "@zeynep_cloud", "☁️"),
    FriendContact("u5", "GENeral Resmi Destek", "@general_ytb", "👑"),
    FriendContact("u6", "Canberk Siber Savunma", "@canberk_def", "🎯")
)

@Composable
fun CreateGroupDialog(
    onGroupCreated: (ConversationItem) -> Unit,
    onDismiss: () -> Unit,
    isPaper: Boolean
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    val selectedFriendIds = remember { mutableStateListOf<String>() }

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
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPaper) Color(0xFFE2DDD5) else Color(0xFF262000))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Yeni Grup Sohbeti Oluştur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Text(
                        text = "Çoklu kişi desteği · E2EE Grup Anahtarı",
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Grup Adı", fontSize = 12.sp) },
                    placeholder = { Text("Örn: Siber Tim Operasyonu", color = textMutedColor, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Grup Üyelerini Seçin (${selectedFriendIds.size} seçildi):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(defaultAvailableFriends) { friend ->
                        val isSelected = friend.id in selectedFriendIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isPaper) Color(0xFFD6CEBF) else Color(0xFF262000)
                                    } else {
                                        if (isPaper) Color(0xFFECE6DA) else CyberBlack
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) accentColor else borderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isSelected) selectedFriendIds.remove(friend.id)
                                    else selectedFriendIds.add(friend.id)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPaper) Color.White else Color(0xFF141414)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(friend.avatarEmoji, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = friend.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = friend.username,
                                        fontSize = 10.sp,
                                        color = NeonCyan
                                    )
                                }
                            }

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedFriendIds.add(friend.id)
                                    else selectedFriendIds.remove(friend.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = accentColor,
                                    uncheckedColor = borderColor
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = groupName.trim()
                    if (cleanName.isNotBlank() && selectedFriendIds.isNotEmpty()) {
                        val memberNames = defaultAvailableFriends
                            .filter { it.id in selectedFriendIds }
                            .map { it.name }
                        val newGroup = ConversationItem(
                            id = "grp_${System.currentTimeMillis()}",
                            name = cleanName,
                            initials = cleanName.take(2).uppercase(),
                            username = "@grup_${cleanName.lowercase().replace(" ", "_").take(10)}",
                            avatarEmoji = "👥",
                            isGroup = true,
                            isOnline = true,
                            lastMessage = "Grup oluşturuldu. Üyeler: ${memberNames.joinToString(", ")}",
                            lastTime = "Şimdi",
                            isVerified = true,
                            messages = listOf(
                                DirectMessage(
                                    id = "gm_init",
                                    conversationId = "grp_${System.currentTimeMillis()}",
                                    senderName = "Sistem",
                                    isMe = false,
                                    text = "👥 '$cleanName' grubu oluşturuldu. Uçtan uca şifreli (E2EE) grup anahtarı dağıtıldı. Üyeler: Sen, ${memberNames.joinToString(", ")}",
                                    time = "Şimdi"
                                )
                            )
                        )
                        SoundPoolManager.playNotification()
                        onGroupCreated(newGroup)
                        onDismiss()
                    }
                },
                enabled = groupName.trim().isNotBlank() && selectedFriendIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Grubu Oluştur", color = if (isPaper) Color.White else CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = textMutedColor)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(16.dp)
    )
}
