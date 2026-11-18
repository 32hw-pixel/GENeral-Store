package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.LocalMeshServer
import com.example.util.NetworkConnectivityAndOfflineManager
import kotlinx.coroutines.launch

/**
 * Dashboard "Hizmet Durumu / Service Status" indicator + custom Offline-First UI notification.
 * Shows real-time connectivity, Firebase Cloud status, Free Local Wi-Fi/Hotspot Server status (0 TRY),
 * and pending LocalStorage outbox count with manual or automatic sync.
 */
@Composable
fun ServiceStatusDashboardCard(
    isCloudSignedIn: Boolean,
    onOpenFreeServer: () -> Unit,
    onOpenOfflineRadar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPaper = LocalPaperMode.current
    val scope = rememberCoroutineScope()

    val status by NetworkConnectivityAndOfflineManager.serviceStatus.collectAsState()
    val localServerRunning by LocalMeshServer.isRunning.collectAsState()
    val localServerUrl by LocalMeshServer.serverUrl.collectAsState()

    val badgeColor = when {
        status.isOnline && isCloudSignedIn -> NeonGreen
        localServerRunning -> NeonCyan
        !status.isOnline -> CyberYellow
        else -> NeonCyan
    }

    val statusHeadline = when {
        status.isOnline && isCloudSignedIn -> "HİZMET DURUMU: ÇEVRİMİÇİ (${status.transportLabel}) · Bulut + Yerel Hazır"
        status.isOnline -> "HİZMET DURUMU: ÇEVRİMİÇİ (${status.transportLabel}) · Ücretsiz Yerel Sunucu Hazır"
        localServerRunning -> "HİZMET DURUMU: İNTERNETSİZ YEREL MOD · Sunucu Aktif (0 ₺)"
        else -> "HİZMET DURUMU: ÇEVRİMDIŞI-ÖNCELİKLİ · Yerel Depolama Aktif"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Service Status Indicator Pill Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isPaper) PaperSurface else CyberCardBg
            ),
            border = BorderStroke(1.dp, if (isPaper) PaperBorder else badgeColor.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("service_status_indicator")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = statusHeadline,
                            color = if (isPaper) PaperText else badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        val detailLine = buildString {
                            append(if (localServerRunning) "Yerel LAN: $localServerUrl (0 ₺)" else "Yerel Sunucu: Hazır (0 ₺)")
                            if (status.pendingOutboxCount > 0) {
                                append(" · ${status.pendingOutboxCount} kuyrukta")
                            }
                            status.lastSyncEvent?.let {
                                append(" · $it")
                            }
                        }
                        Text(
                            text = detailLine,
                            color = if (isPaper) PaperTextMuted else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
                OutlinedButton(
                    onClick = onOpenFreeServer,
                    border = BorderStroke(1.dp, badgeColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("service_status_server_btn")
                ) {
                    Icon(
                        imageVector = if (status.isOnline) Icons.Default.CloudDone else Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sunucu (0 ₺)", color = if (isPaper) PaperText else badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom Animated UI Notification when switched to Offline-First mode or queue active
        AnimatedVisibility(
            visible = status.offlineModeNotificationVisible || (!status.isOnline) || status.pendingOutboxCount > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPaper) PaperSurface else Color(0xFF1F1804)
                ),
                border = BorderStroke(1.5.dp, if (!status.isOnline) CyberYellow else NeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_mode_notification_banner")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Çevrimdışı Bildirim",
                                tint = CyberYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!status.isOnline) {
                                    "ÇEVRİMDIŞI-ÖNCELİKLİ MESAJLAŞMA MODU AKTİF"
                                } else {
                                    "YEREL DEPOLAMA KUYRUĞU (${status.pendingOutboxCount} MESAJ)"
                                },
                                color = if (isPaper) PaperText else CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (status.isOnline) {
                            IconButton(
                                onClick = { NetworkConnectivityAndOfflineManager.dismissOfflineNotification() },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(
                        text = status.offlineNotificationText.ifBlank {
                            "Mesajlarınız cihazın yerel deposuna (Local Storage) kaydedildi. İnternet bağlandığında otomatik buluta iletilir; aynı Wi-Fi/Hotspot içindeki cihazlarla Ücretsiz Yerel Sunucu üzerinden internetsiz anlık yazışabilirsiniz."
                        },
                        color = if (isPaper) PaperTextMuted else Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!localServerRunning) {
                            Button(
                                onClick = {
                                    LocalMeshServer.start(context = context)
                                    onOpenOfflineRadar?.invoke() ?: onOpenFreeServer()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.WifiTethering, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("İnternetsiz Ağ Kur (0 ₺)", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        if (status.isOnline && status.pendingOutboxCount > 0) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        NetworkConnectivityAndOfflineManager.flushOfflineOutbox(context)
                                    }
                                },
                                border = BorderStroke(1.dp, NeonGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Şimdi Eşitle", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
