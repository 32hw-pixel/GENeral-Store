package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextSecondary

@Composable
fun AppLockScreen(
    isSetupMode: Boolean,
    currentPasscode: String = "",
    onLockPassed: (String) -> Unit,
    onResetLock: (() -> Unit)? = null,
    onWipeData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputPasscode by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showWeakWarningDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isStrong = remember(inputPasscode) {
        inputPasscode.length >= 8 &&
                inputPasscode.any { it.isDigit() } &&
                inputPasscode.any { it.isLetter() } &&
                inputPasscode.any { !it.isLetterOrDigit() }
    }

    val dynamicColor by animateColorAsState(
        targetValue = when {
            inputPasscode.isEmpty() -> CyberCardBorder
            isStrong -> NeonGreen
            inputPasscode.length in 4..20 -> NeonYellow
            else -> NeonRed
        },
        label = "passcode_color"
    )

    fun submitPasscode() {
        if (isSetupMode) {
            if (inputPasscode.length < 4) {
                errorMessage = "Şifre en az 4 karakter olmalıdır."
                return
            }
            if (!isStrong) {
                // Weak password triggers the accountability protocol
                showWeakWarningDialog = true
            } else {
                onLockPassed(inputPasscode)
            }
        } else {
            if (inputPasscode == currentPasscode) {
                onLockPassed(inputPasscode)
            } else {
                errorMessage = "Hatalı şifre. Erişim reddedildi."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberCardBg)
                    .border(2.dp, dynamicColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = dynamicColor,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isSetupMode) "Uygulama Kilidi Ayarla" else "Sistem Kilitli",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSetupMode) "4-20 karakter uzunluğunda güvenlik şifrenizi belirleyin." else "Operasyon merkezine erişmek için şifrenizi girin.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = inputPasscode,
                onValueChange = {
                    if (it.length <= 20) {
                        inputPasscode = it
                        errorMessage = null
                    }
                },
                label = { Text("Güvenlik Şifresi (4-20 Karakter)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dynamicColor,
                    unfocusedBorderColor = dynamicColor.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_lock_input")
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    color = NeonRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color coding status badge
            if (isSetupMode && inputPasscode.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dynamicColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isStrong) "Güçlü Şifre (Yeşil): Özel karakter & sayı içeriyor." else "Standart Şifre (Sarı): Rakam veya harf.",
                            style = MaterialTheme.typography.labelSmall,
                            color = dynamicColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { submitPasscode() },
                enabled = inputPasscode.length >= 4,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = dynamicColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("app_lock_submit_button")
            ) {
                Text(
                    text = if (isSetupMode) "Kilidi Onayla ve Kaydet" else "Kilidi Aç",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = CyberBlack
                )
            }

            if (!isSetupMode) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.testTag("app_lock_forgot_button")
                ) {
                    Text(
                        text = "Şifremi Unuttum / Kilidi Sıfırla",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Güvenlik Kilidini Sıfırla",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Şifrenizi hatırlamıyorsanız kilidi kaldırabilir veya tüm verileri sıfırlayarak temiz bir başlangıç yapabilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetLock?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.testTag("confirm_remove_lock_button")
                ) {
                    Text(
                        text = "Kilidi Kaldır",
                        color = CyberBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Row {
                    if (onWipeData != null) {
                        TextButton(
                            onClick = {
                                showResetDialog = false
                                onWipeData.invoke()
                            },
                            modifier = Modifier.testTag("confirm_wipe_data_lock_button")
                        ) {
                            Text(
                                text = "Verileri Sıfırla",
                                color = NeonRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    TextButton(
                        onClick = { showResetDialog = false }
                    ) {
                        Text(
                            text = "İptal",
                            color = TextSecondary
                        )
                    }
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Zayıf Şifre Tespiti & Akıllı Onay Ekranı ("EMİN MİSİN?")
    if (showWeakWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWeakWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "EMİN MİSİN?",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = NeonYellow
                )
            },
            text = {
                Text(
                    text = "Kendi kalenin duvarlarını bu kadar zayıf örüyorsun. Bu özgürlüğün anahtarını korumak da, doğabilecek tüm güvenlik açıklarının sorumluluğunu üstlenmek de tamamen senin elinde. Sonuçların yöneticisi sensin; karar senin.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWeakWarningDialog = false
                        onLockPassed(inputPasscode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                    modifier = Modifier.testTag("weak_password_confirm_button")
                ) {
                    Text(
                        text = "Tamam",
                        color = CyberBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWeakWarningDialog = false }
                ) {
                    Text(
                        text = "Geri Gel",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
