package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
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
import com.example.util.LocalMeshServer
import com.example.util.ServerMessage
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 100% Real Zero-Cost Offline / LAN Messaging & NSD Auto-Discovery Tunnel.
 * Connects two devices on the same Wi-Fi or Hotspot without requiring internet access.
 */
@Composable
fun OfflineP2PTunnelDialog(
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val isHostRunning by LocalMeshServer.isRunning.collectAsState()
    val hostUrl by LocalMeshServer.serverUrl.collectAsState()
    val hostMessages by LocalMeshServer.messages.collectAsState()
    val discoveredPeers by LocalMeshServer.discoveredPeers.collectAsState()
    val isDiscovering by LocalMeshServer.isDiscoveringPeers.collectAsState()

    var manualPeerUrl by remember { mutableStateOf("") }
    var activeRemoteUrl by remember { mutableStateOf<String?>(null) }
    var remoteMessages by remember { mutableStateOf<List<ServerMessage>>(emptyList()) }
    var connectionStatus by remember { mutableStateOf<String?>(null) }
    var offlineInputText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        LocalMeshServer.startPeerDiscovery(context)
        onDispose {
            LocalMeshServer.stopPeerDiscovery()
        }
    }

    LaunchedEffect(activeRemoteUrl) {
        val target = activeRemoteUrl ?: return@LaunchedEffect
        while (true) {
            val res = LocalMeshServer.fetchRemoteMessages(target)
            res.onSuccess { list ->
                remoteMessages = list
                connectionStatus = "Bağlı: $target (${list.size} mesaj)"
            }.onFailure { err ->
                connectionStatus = "Erişilemedi ($target): ${err.localizedMessage}"
            }
            delay(1500)
        }
    }

    val shownMessages = if (activeRemoteUrl != null) remoteMessages else hostMessages

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiTethering, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("İnternetsiz Yerel Ağ (Wi-Fi / Hotspot)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("NSD Otomatik Keşif + Ücretsiz LAN HTTP Sunucusu (0 ₺)", color = NeonGreen, fontSize = 10.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Local Server Host Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberBlack),
                    border = BorderStroke(1.dp, if (isHostRunning) NeonGreen else CyberYellow),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isHostRunning) NeonGreen else CyberYellow)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isHostRunning) "Sunucun Açık · $hostUrl" else "Bu Cihazı Yerel Sunucu Yap",
                                        color = if (isHostRunning) NeonGreen else CyberYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Aynı Wi-Fi veya telefon Hotspot'unda internet gerekmez.",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    SoundPoolManager.playMechanicalClick()
                                    if (isHostRunning) LocalMeshServer.stop() else LocalMeshServer.start(context = context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isHostRunning) NeonRed else CyberYellow),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(if (isHostRunning) "Durdur" else "Başlat", color = if (isHostRunning) Color.White else CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        if (isHostRunning) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(hostUrl))
                                    Toast.makeText(context, "Adres kopyalandı: $hostUrl", Toast.LENGTH_SHORT).show()
                                },
                                border = BorderStroke(1.dp, NeonCyan),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Adresi Paylaş ($hostUrl)", color = NeonCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Discovered LAN Peers + Manual IP Connect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDiscovering) "AYNI AĞDAKİ CİHAZLAR TARANIYOR..." else "KEŞFEDİLEN CİHAZLAR (${discoveredPeers.size})",
                        color = CyberYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMechanicalClick()
                            LocalMeshServer.startPeerDiscovery(context)
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yeniden Tara", tint = CyberYellow, modifier = Modifier.size(18.dp))
                    }
                }

                if (discoveredPeers.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(78.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(discoveredPeers) { peer ->
                            val isSelected = activeRemoteUrl == peer.baseUrl
                            Card(
                                onClick = {
                                    activeRemoteUrl = peer.baseUrl
                                    manualPeerUrl = peer.baseUrl
                                },
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF0F2617) else CyberBlack),
                                border = BorderStroke(1.dp, if (isSelected) NeonGreen else CyberCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${peer.serviceName} (${peer.baseUrl})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = manualPeerUrl,
                        onValueChange = { manualPeerUrl = it },
                        placeholder = { Text("Diğer telefon IP (192.168.1.X:8765)", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (manualPeerUrl.isNotBlank()) {
                                val normalized = if (manualPeerUrl.trim().startsWith("http")) manualPeerUrl.trim() else "http://${manualPeerUrl.trim()}"
                                activeRemoteUrl = normalized
                            } else {
                                activeRemoteUrl = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (activeRemoteUrl != null && manualPeerUrl.isBlank()) "Yerel" else "Bağlan", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                if (connectionStatus != null) {
                    Text(connectionStatus!!, color = NeonCyan, fontSize = 10.sp)
                }

                // Real LAN Message Log
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberBlack),
                    border = BorderStroke(1.dp, CyberCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    if (shownMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                text = "Henüz yerel ağ mesajı yok. Yukarıdan sunucuyu başlatın veya aynı Wi-Fi/Hotspot'taki arkadaşınızın IP adresini girip yazışın.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(shownMessages) { m ->
                                Text(
                                    text = "[${m.time}] ${m.sender}: ${m.text}",
                                    color = if (m.fromWebClient) NeonCyan else CyberYellow,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Real LAN Send Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = offlineInputText,
                        onValueChange = { offlineInputText = it },
                        placeholder = { Text("İnternetsiz LAN mesajı yaz...", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val txt = offlineInputText.trim()
                            if (txt.isEmpty()) return@Button
                            SoundPoolManager.playConfirm()
                            if (activeRemoteUrl != null) {
                                val target = activeRemoteUrl!!
                                scope.launch {
                                    val res = LocalMeshServer.sendRemoteMessage(target, "GEN Cihaz", txt)
                                    res.onSuccess {
                                        offlineInputText = ""
                                        onSendMessage("📡 [LAN -> $target]: $txt")
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Gönderilemedi: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                if (!isHostRunning) LocalMeshServer.start(context = context)
                                LocalMeshServer.postHostMessage("Ana Cihaz", txt)
                                onSendMessage("📡 [Yerel Sunucu]: $txt")
                                offlineInputText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Gönder", tint = CyberBlack, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = CyberYellow)
            }
        },
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(18.dp)
    )
}
