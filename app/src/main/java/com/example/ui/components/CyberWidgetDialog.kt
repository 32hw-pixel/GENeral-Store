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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager

@Composable
fun CyberWidgetDialog(
    onTriggerPanic: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var isQuickPanicWidgetEnabled by remember { mutableStateOf(true) }
    var isUnreadBadgeWidgetEnabled by remember { mutableStateOf(true) }
    var isIncognitoModeEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Widgets, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Masaüstü / Glance Widget", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Ana Ekran Hızlı Siber Erişim Paneli", color = CyberYellow, fontSize = 10.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Interactive Widget Live Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberBlack),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GENeral Chat Hızlı Widget", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text("ÖNİZLEME", color = CyberYellow, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Şifreli Tünel: Çevrimiçi", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Okunmamış: 2 şifreli mesaj", color = TextSecondary, fontSize = 9.sp)
                            }

                            // Widget panic trigger
                            Button(
                                onClick = {
                                    SoundPoolManager.playPanicWipe()
                                    onTriggerPanic()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("🚨 Acil Çıkış", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Widget Settings Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Masaüstü Panik Butonu Widget'ı", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Uygulamayı açmadan ana ekrandan kilitler", color = TextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = isQuickPanicWidgetEnabled,
                        onCheckedChange = { isQuickPanicWidgetEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberYellow, checkedTrackColor = CyberYellow.copy(alpha = 0.5f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gizli Önizleme (Mesaj Metnini Gizle)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Widget üzerinde sadece 'Yeni şifreli mesaj' yazar", color = TextSecondary, fontSize = 10.sp)
                    }
                    Switch(
                        checked = isIncognitoModeEnabled,
                        onCheckedChange = { isIncognitoModeEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundPoolManager.playConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow)
            ) {
                Text("Widget'ı Ekle / Kaydet", color = CyberBlack, fontWeight = FontWeight.Bold)
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
