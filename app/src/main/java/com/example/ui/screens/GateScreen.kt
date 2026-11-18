package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.CyberTechButton
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager

@Composable
fun GateScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onGuest: () -> Unit,
    onGoogleSignIn: (() -> Unit)? = null,
    onWipeData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing Portal Icon
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.4f), Color(0xFF101B2E))
                            )
                        )
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Giriş Kapısı",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Farklı cihaz ve internet ağlarından TLS korumalı gerçek zamanlı mesajlaşmak için Google ile bağlanabilir veya hesapsız olarak GEN AI ve cihaz içi 'Siz' notlarını kullanabilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (onGoogleSignIn != null) {
                    CyberTechButton(
                        text = "[ GOOGLE İLE BAĞLAN (İNTERNET SOHBETİ) ]",
                        containerColor = Color(0xFF262000),
                        contentColor = com.example.ui.theme.CyberYellow,
                        borderColor = com.example.ui.theme.CyberYellow,
                        icon = Icons.Default.Shield,
                        onClick = onGoogleSignIn,
                        testTag = "gate_google_signin_button"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Primary Option 1: Create Account
                CyberTechButton(
                    text = "[ HESAP OLUŞTUR / KAYIT OL ]",
                    containerColor = Color(0xFF0C243B),
                    contentColor = NeonCyan,
                    borderColor = NeonCyan,
                    icon = Icons.Default.PersonAdd,
                    onClick = onRegister,
                    testTag = "gate_register_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Option 2: Sign In
                CyberTechButton(
                    text = "[ MEVCUT HESABA GİRİŞ YAP ]",
                    containerColor = Color(0xFF230E38),
                    contentColor = NeonPurple,
                    borderColor = NeonPurple,
                    icon = Icons.Default.Key,
                    onClick = onLogin,
                    testTag = "gate_login_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: Local GEN AI & Private "Siz" Notes without cloud account
                CyberTechButton(
                    text = "[ HESAPSIZ DEVAM ET (GEN AI & SİZ) ]",
                    containerColor = Color(0xFF0F2B1D),
                    contentColor = NeonGreen,
                    borderColor = NeonGreen,
                    icon = Icons.Default.Fingerprint,
                    onClick = { onGuest() },
                    testTag = "gate_guest_button"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Security assurance pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TLS & SHA-256 Korumalı İletişim",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                if (onWipeData != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            showWipeConfirmDialog = true
                        },
                        modifier = Modifier.testTag("gate_wipe_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verileri ve Şifreleri Sıfırla (Temiz Başlangıç)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Bottom Audio Status Footer
            CyberAudioStatusBar(
                onOpenAudioCenter = { showAudioDialog = true }
            )
        }
    }

    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Tüm Verileri Sıfırla",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Önceki demoda oluşturduğunuz hesaplar, unutulan şifreler ve kayıtlı tüm veriler silinip sistem sıfırlanacaktır. Bu işlem geri alınamaz. Devam etmek istiyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeConfirmDialog = false
                        SoundPoolManager.playPanicWipe()
                        onWipeData?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.testTag("gate_confirm_wipe_button")
                ) {
                    Text(
                        text = "Evet, Tümünü Sıfırla",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWipeConfirmDialog = false }
                ) {
                    Text(
                        text = "Vazgeç",
                        color = TextSecondary
                    )
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showAudioDialog) {
        MusicSettingsDialog(
            onDismiss = { showAudioDialog = false }
        )
    }
}
