package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    isSetupMode: Boolean,
    initialPinMode: Boolean = true,
    currentPasscode: String = "",
    onVerifyPasscode: ((String) -> Boolean)? = null,
    onLockPassed: (String) -> Unit,
    onCancel: (() -> Unit)? = null,
    onWipeData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var usePinMode by remember(initialPinMode, isSetupMode) {
        mutableStateOf(initialPinMode)
    }
    var pinDigits by remember { mutableStateOf("") }
    var inputPasscode by remember { mutableStateOf("") }
    var showImhaDialog by remember { mutableStateOf(false) }
    var showWeakWarningDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockoutRemainingSec by remember { mutableIntStateOf(0) }
    var lockoutStage by remember { mutableIntStateOf(0) }

    fun formatCountdown(totalSec: Int): String {
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s)
    }

    LaunchedEffect(isSetupMode) {
        if (!isSetupMode) {
            val persistedRem = com.example.data.SecurityUtils.getLockoutRemainingSeconds(context)
            failedAttempts = com.example.data.SecurityUtils.getFailedLockAttempts(context)
            lockoutStage = com.example.data.SecurityUtils.getLockoutStage(context)
            lockoutRemainingSec = persistedRem
            if (persistedRem > 0) {
                errorMessage = "Çok fazla hatalı deneme! Kalan bekleme süresi: ${formatCountdown(persistedRem)}"
            }
        }
    }

    if (isSetupMode && onCancel != null) {
        BackHandler { onCancel() }
    }

    LaunchedEffect(lockoutRemainingSec) {
        if (lockoutRemainingSec > 0) {
            delay(1000L)
            val nextRem = com.example.data.SecurityUtils.getLockoutRemainingSeconds(context)
            lockoutRemainingSec = nextRem
            errorMessage = if (nextRem <= 0) {
                null
            } else {
                "Çok fazla hatalı deneme (${lockoutStage}. aşama). Kalan bekleme süresi: ${formatCountdown(nextRem)}"
            }
        }
    }

    val activeValue = if (usePinMode) pinDigits else inputPasscode

    val isStrong = remember(inputPasscode) {
        inputPasscode.length >= 8 &&
            inputPasscode.any { it.isDigit() } &&
            inputPasscode.any { it.isLetter() } &&
            inputPasscode.any { !it.isLetterOrDigit() }
    }

    val dynamicColor by animateColorAsState(
        targetValue = when {
            errorMessage != null -> NeonRed
            usePinMode && pinDigits.length == 4 -> NeonGreen
            usePinMode -> NeonYellow
            inputPasscode.isEmpty() -> CyberCardBorder
            isStrong -> NeonGreen
            inputPasscode.length in 4..20 -> NeonYellow
            else -> NeonRed
        },
        label = "passcode_color"
    )

    fun handleVerify(candidate: String) {
        val activeLockout = com.example.data.SecurityUtils.getLockoutRemainingSeconds(context)
        if (activeLockout > 0) {
            lockoutRemainingSec = activeLockout
            errorMessage = "Çok fazla hatalı deneme. Kalan bekleme süresi: ${formatCountdown(activeLockout)}"
            return
        }
        val ok = onVerifyPasscode?.invoke(candidate) ?: (candidate == currentPasscode)
        if (ok) {
            com.example.data.SecurityUtils.resetLockAttemptState(context)
            failedAttempts = 0
            lockoutStage = 0
            lockoutRemainingSec = 0
            errorMessage = null
            SoundPoolManager.playConfirm()
            onLockPassed(candidate)
        } else {
            SoundPoolManager.playMechanicalClick()
            if (usePinMode) {
                pinDigits = ""
            }
            val status = com.example.data.SecurityUtils.recordFailedLockAttempt(context)
            failedAttempts = status.failedAttemptsInRound
            lockoutStage = status.lockoutStage
            if (status.isLockedOut) {
                lockoutRemainingSec = status.remainingSeconds
                errorMessage = "5 kez hatalı deneme yapıldı! Güvenlik için ${status.lockoutMinutes} dakika beklenecek (${formatCountdown(status.remainingSeconds)}). Sonraki her 5 hatalı denemede bekleme +5 dakika artar."
            } else {
                errorMessage = if (usePinMode) {
                    "Hatalı 4 haneli PIN. Kalan deneme hakkı: ${status.remainingAttempts}"
                } else {
                    "Hatalı şifre. Kalan deneme hakkı: ${status.remainingAttempts}"
                }
            }
        }
    }

    fun submitPasscode() {
        if (isSetupMode) {
            if (usePinMode) {
                if (pinDigits.length != 4 || !pinDigits.all { it.isDigit() }) {
                    errorMessage = "Lütfen tam 4 haneli sayısal PIN belirleyin."
                    return
                }
                SoundPoolManager.playConfirm()
                onLockPassed(pinDigits)
            } else {
                if (inputPasscode.length < 4) {
                    errorMessage = "Şifre en az 4 karakter olmalıdır."
                    return
                }
                if (!isStrong) {
                    showWeakWarningDialog = true
                } else {
                    onLockPassed(inputPasscode)
                }
            }
        } else {
            handleVerify(activeValue)
        }
    }

    fun appendPinDigit(digit: Char) {
        if (!isSetupMode && lockoutRemainingSec > 0) return
        if (pinDigits.length < 4) {
            SoundPoolManager.playMechanicalClick()
            errorMessage = null
            val next = pinDigits + digit
            pinDigits = next
            if (!isSetupMode && next.length == 4) {
                handleVerify(next)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (isSetupMode && onCancel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("app_lock_cancel_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Ayarlara Dön",
                            tint = Color.White
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CyberCardBg)
                    .border(2.dp, dynamicColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Ekran Kilidi",
                    tint = dynamicColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when {
                    isSetupMode && usePinMode -> "4 Haneli Ekran Kilidi PIN"
                    isSetupMode -> "Gelişmiş Kilit Şifresi"
                    usePinMode -> "Ekran Kilitli · PIN Girin"
                    else -> "Sistem Kilitli"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    isSetupMode && usePinMode ->
                        "Panik butonuna basıldığında veya uygulama arka plana alındığında devreye girecek 4 haneli PIN kodunuzu girin."
                    isSetupMode ->
                        "4-20 karakter uzunluğunda alfanümerik kilit şifrenizi belirleyin."
                    usePinMode ->
                        "Devam etmek için 4 haneli gizlilik PIN kodunuzu girin."
                    else ->
                        "Operasyon merkezine erişmek için şifrenizi girin."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            if (isSetupMode) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = usePinMode,
                        onClick = {
                            usePinMode = true
                            errorMessage = null
                        },
                        label = { Text("4 Haneli PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = CyberCardBg,
                            labelColor = Color.White
                        ),
                        modifier = Modifier.testTag("app_lock_mode_pin")
                    )
                    FilterChip(
                        selected = !usePinMode,
                        onClick = {
                            usePinMode = false
                            errorMessage = null
                        },
                        label = { Text("Gelişmiş Şifre (4-20)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = CyberBlack,
                            containerColor = CyberCardBg,
                            labelColor = Color.White
                        ),
                        modifier = Modifier.testTag("app_lock_mode_password")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (usePinMode) {
                // 4-Digit Visual PIN Slots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .semantics { contentDescription = "${pinDigits.length} / 4 rakam girildi" }
                        .testTag("app_lock_pin_slots")
                ) {
                    for (i in 0 until 4) {
                        val filled = i < pinDigits.length
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (filled) dynamicColor.copy(alpha = 0.16f) else CyberCardBg)
                                .border(
                                    width = if (i == pinDigits.length) 2.dp else 1.5.dp,
                                    color = if (filled) dynamicColor else if (i == pinDigits.length) NeonYellow else CyberCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("pin_slot_$i"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (filled) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(dynamicColor)
                                )
                            } else {
                                Text(
                                    text = "•",
                                    color = CyberCardBorder,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        color = NeonRed,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("app_lock_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Accessible 0-9 Numeric Keypad (56dp+ touch targets)
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    keypadRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowKeys.forEach { key ->
                                when (key) {
                                    "C" -> OutlinedButton(
                                        onClick = {
                                            SoundPoolManager.playMechanicalClick()
                                            pinDigits = ""
                                            errorMessage = null
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CyberCardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .minimumInteractiveComponentSize()
                                            .semantics { contentDescription = "PIN girişini temizle" }
                                            .testTag("pin_key_clear")
                                    ) {
                                        Text("Temizle", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    "DEL" -> OutlinedButton(
                                        onClick = {
                                            if (pinDigits.isNotEmpty()) {
                                                SoundPoolManager.playMechanicalClick()
                                                pinDigits = pinDigits.dropLast(1)
                                                errorMessage = null
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CyberCardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .minimumInteractiveComponentSize()
                                            .semantics { contentDescription = "Son rakamı sil" }
                                            .testTag("pin_key_backspace")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Sil",
                                            tint = NeonYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    else -> Button(
                                        onClick = { appendPinDigit(key.first()) },
                                        enabled = isSetupMode || lockoutRemainingSec == 0,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberCardBg,
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, CyberCardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .minimumInteractiveComponentSize()
                                            .semantics { contentDescription = "Rakam $key" }
                                            .testTag("pin_key_$key")
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { submitPasscode() },
                    enabled = pinDigits.length == 4 && (isSetupMode || lockoutRemainingSec == 0),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("app_lock_submit_button")
                ) {
                    Text(
                        text = if (isSetupMode) "4 Haneli PIN'i Kaydet" else "Kilidi Aç",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = CyberBlack
                    )
                }
            } else {
                OutlinedTextField(
                    value = inputPasscode,
                    onValueChange = {
                        if (it.length <= 20) {
                            inputPasscode = it
                            errorMessage = null
                        }
                    },
                    enabled = isSetupMode || lockoutRemainingSec == 0,
                    label = { Text("Güvenlik Şifresi (4-20 Karakter)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dynamicColor,
                        unfocusedBorderColor = dynamicColor.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { submitPasscode() },
                    enabled = inputPasscode.length >= 4 && (isSetupMode || lockoutRemainingSec == 0),
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
            }

            if (!isSetupMode) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showImhaDialog = true },
                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("app_lock_imha_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NeonRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Şifremi Hatırlamıyorum · İMHA",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonRed
                    )
                }
            }
        }
    }

    if (showImhaDialog) {
        AlertDialog(
            onDismissRequest = { showImhaDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(38.dp)
                )
            },
            title = {
                Text(
                    text = "İMHA · Kalıcı Hesap ve Sohbet Silme",
                    fontWeight = FontWeight.Black,
                    color = NeonRed
                )
            },
            text = {
                Text(
                    text = "Güvenlik kilidi şifrenizi hatırlamıyorsanız İMHA işlemi ile kilit kaldırılır; ancak aynı anda TÜM sohbetleriniz, yerel notlarınız ve bulut hesabınız kalıcı olarak silinir, hesabınızı kaybedersiniz. Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImhaDialog = false
                        onWipeData?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("confirm_imha_button")
                ) {
                    Text(
                        text = "İMHA",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImhaDialog = false },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = "Vazgeç",
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }

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
