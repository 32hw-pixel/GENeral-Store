package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaskType
import com.example.data.PanicConfig
import com.example.data.PanicTrigger
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.CyberTechButton
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.ShakeDetector
import com.example.util.SoundPoolManager

@Composable
fun PanicConfigScreen(
    currentConfig: PanicConfig,
    onConfigCompleted: (PanicConfig) -> Unit,
    onCancel: () -> Unit,
    onTestPanicOverride: (MaskType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }
    var selectedTrigger by remember { mutableStateOf(currentConfig.trigger) }
    var selectedMask by remember { mutableStateOf(currentConfig.targetMask) }
    var wipeHistory by remember { mutableStateOf(currentConfig.wipeHistoryOnPanic) }
    var dontShowAgain by remember { mutableStateOf(currentConfig.dontShowAgain) }
    var sensitivityG by remember { mutableFloatStateOf(2.4f) }
    var showAudioDialog by remember { mutableStateOf(false) }

    // Live accelerometer sensor instance for real-time telemetry demonstration
    val shakeDetector = remember {
        ShakeDetector(context) {
            // Live test shake detected
            SoundPoolManager.playPanicWipe()
        }.apply {
            this.sensitivityG = sensitivityG
            this.isEnabled = true
        }
    }

    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    val liveGForce by shakeDetector.currentGForce.collectAsState()
    val accelX by shakeDetector.accelX.collectAsState()
    val accelY by shakeDetector.accelY.collectAsState()
    val accelZ by shakeDetector.accelZ.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("panic_config_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "panic_step_transition",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    1 -> PanicIntroStep(
                        onNext = {
                            SoundPoolManager.playMenuNav()
                            step = 2
                        },
                        onCancel = {
                            SoundPoolManager.playMenuNav()
                            onCancel()
                        }
                    )
                    2 -> PanicSelectTriggerStep(
                        selectedTrigger = selectedTrigger,
                        onSelectTrigger = {
                            SoundPoolManager.playMechanicalClick()
                            selectedTrigger = it
                        },
                        selectedMask = selectedMask,
                        onSelectMask = {
                            SoundPoolManager.playMechanicalClick()
                            selectedMask = it
                        },
                        wipeHistory = wipeHistory,
                        onToggleWipe = {
                            SoundPoolManager.playMechanicalClick()
                            wipeHistory = !wipeHistory
                        },
                        liveGForce = liveGForce,
                        accelX = accelX,
                        accelY = accelY,
                        accelZ = accelZ,
                        sensitivityG = sensitivityG,
                        onSensitivityChange = {
                            sensitivityG = it
                            shakeDetector.sensitivityG = it
                        },
                        onBiometricTouchTest = {
                            SoundPoolManager.playPanicWipe()
                            onTestPanicOverride(selectedMask)
                        },
                        onComplete = {
                            SoundPoolManager.playMenuNav()
                            step = 3
                        },
                        onBack = {
                            SoundPoolManager.playMenuNav()
                            step = 1
                        }
                    )
                    3 -> PanicFinalStep(
                        targetMask = selectedMask,
                        trigger = selectedTrigger,
                        wipeHistory = wipeHistory,
                        dontShowAgain = dontShowAgain,
                        onToggleDontShow = {
                            SoundPoolManager.playMechanicalClick()
                            dontShowAgain = !dontShowAgain
                        },
                        onFinish = {
                            SoundPoolManager.playConfirm()
                            onConfigCompleted(
                                currentConfig.copy(
                                    trigger = selectedTrigger,
                                    targetMask = selectedMask,
                                    wipeHistoryOnPanic = wipeHistory,
                                    dontShowAgain = dontShowAgain
                                )
                            )
                        }
                    )
                }
            }

            // Bottom Audio Status Footer matching reference
            CyberAudioStatusBar(
                onOpenAudioCenter = { showAudioDialog = true }
            )
        }
    }

    if (showAudioDialog) {
        MusicSettingsDialog(
            onDismiss = { showAudioDialog = false }
        )
    }
}

/**
 * Step 1: Matching the user's reference screenshot right pane:
 * Header: PANİK MODU (Bold Neon Red)
 * Center: 3D High-Tech Biometric Cyber Lock with EMERGENCY PROTOCOL ACTIVE
 * Description & [ VAZGEÇ ] / [ İLERİ ] buttons
 * Bottom indicator: 3 kez dokun veya salla
 */
@Composable
private fun PanicIntroStep(
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Red Circuit Glow Header
        Text(
            text = "PANİK MODU",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NeonRed,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3D High-Tech Biometric Cyber Lock Frame
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF14070A))
                .border(2.dp, NeonRed.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Concentric red glow rings
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .border(2.dp, NeonRed.copy(alpha = 0.6f), CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonRed.copy(alpha = 0.35f), Color(0xFF1E0408))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, NeonRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "EMERGENCY\nPROTOCOL\nACTIVE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonRed,
                            textAlign = TextAlign.Center,
                            lineHeight = 9.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Panik anını yönetme vakti!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeonRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Telefonunu sallayarak ya da parmak izi sensörüne dokunarak saniyeler içinde devreye sokabileceğin kusursuz bir görünmezlik kalkanı sunuyoruz.\nTehlike kapıdayken tüm izler tek hamlede siliniyor.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Dual Action Buttons: [ VAZGEÇ ] and [ İLERİ ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CyberTechButton(
                text = "[ VAZGEÇ ]",
                containerColor = Color(0xFF1F1215),
                contentColor = TextSecondary,
                borderColor = Color(0xFF4A1E24),
                onClick = onCancel,
                testTag = "panic_intro_cancel_button",
                modifier = Modifier.weight(1f)
            )

            CyberTechButton(
                text = "[ İLERİ ]",
                containerColor = Color(0xFF380812),
                contentColor = Color.White,
                borderColor = NeonRed,
                onClick = onNext,
                testTag = "panic_intro_next_button",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bottom gesture reminder icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = NeonRed.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "3 kez dokun veya salla",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PanicSelectTriggerStep(
    selectedTrigger: PanicTrigger,
    onSelectTrigger: (PanicTrigger) -> Unit,
    selectedMask: MaskType,
    onSelectMask: (MaskType) -> Unit,
    wipeHistory: Boolean,
    onToggleWipe: () -> Unit,
    liveGForce: Float,
    accelX: Float,
    accelY: Float,
    accelZ: Float,
    sensitivityG: Float,
    onSensitivityChange: (Float) -> Unit,
    onBiometricTouchTest: () -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "KALKAN YAPILANDIRMASI",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = NeonRed,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Gerçek ivmeölçer telemetrisi ve acil durum kamufle ayarları:",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Live G-force & Accelerometer Telemetry Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14070A)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonRed.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CANLI G-FORCE RADARI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonRed,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "%.2f G".format(liveGForce),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (liveGForce > sensitivityG) NeonRed else NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress gauge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B0E14))
                ) {
                    val progress = (liveGForce / 4.0f).coerceIn(0.0f, 1.0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (liveGForce > sensitivityG) NeonRed else NeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("X: %.1f".format(accelX), fontSize = 10.sp, color = TextMuted)
                    Text("Y: %.1f".format(accelY), fontSize = 10.sp, color = TextMuted)
                    Text("Z: %.1f m/s²".format(accelZ), fontSize = 10.sp, color = TextMuted)
                    Text("Eşik: %.1fG".format(sensitivityG), fontSize = 10.sp, color = NeonRed)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sensitivity Slider
                Text(
                    text = "Sallama Hassasiyeti: %.1f G".format(sensitivityG),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Slider(
                    value = sensitivityG,
                    onValueChange = onSensitivityChange,
                    valueRange = 1.8f..3.8f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonRed,
                        activeTrackColor = NeonRed,
                        inactiveTrackColor = Color(0xFF2B0E14)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Biometric Instant Trigger Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF380812), Color(0xFF1F040A))
                    )
                )
                .border(1.5.dp, NeonRed, RoundedCornerShape(14.dp))
                .clickable { onBiometricTouchTest() }
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "[ PARMAK İZİ SENSÖRÜ İLE TETİKLE ]",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Dokunulduğu anda acil durum protokolünü test et",
                        fontSize = 10.sp,
                        color = NeonRed.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Trigger Method Selection
        Text(
            text = "VARSAYILAN TETİKLEME YÖNTEMİ:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonRed,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        PanicTrigger.entries.forEach { trigger ->
            val isSelected = trigger == selectedTrigger
            val triggerTitle = when (trigger) {
                PanicTrigger.SHAKE -> "Cihazı Sallama (Shake Detection - >%.1fG)".format(sensitivityG)
                PanicTrigger.TRIPLE_TAP -> "Parmak İzi / Arkaya 3 Dokunma"
            }
            val triggerSubtitle = when (trigger) {
                PanicTrigger.SHAKE -> "Hızlı bir bilek hareketiyle anında kamuflaja geç"
                PanicTrigger.TRIPLE_TAP -> "Saniyeler içinde devreye sokulabilen sessiz kalkan"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF280B12) else CyberCardBg)
                    .border(1.5.dp, if (isSelected) NeonRed else CyberCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelectTrigger(trigger) }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = triggerTitle,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(text = triggerSubtitle, fontSize = 10.sp, color = TextMuted)
                    }
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = NeonRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Camouflage Decoy Selector
        Text(
            text = "HEDEF KAMUFLAJ MASKESİ:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = NeonRed,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(MaskType.CALCULATOR, MaskType.WEATHER, MaskType.NOTES, MaskType.COMPASS).forEach { mask ->
                val isSelected = mask == selectedMask
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF380812) else CyberCardBg)
                        .border(1.2.dp, if (isSelected) NeonRed else CyberCardBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectMask(mask) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mask.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Instant Wipe History Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B070B))
                .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable { onToggleWipe() }
                .padding(10.dp)
        ) {
            Checkbox(
                checked = wipeHistory,
                onCheckedChange = { onToggleWipe() },
                colors = CheckboxDefaults.colors(checkedColor = NeonRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Sohbet Geçmişini & Oturumu Kalıcı Sil (Panic Wipe)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tetiklendiğinde veritabanındaki hassas mesajlar anında temizlenir",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CyberTechButton(
                text = "[ GERİ ]",
                containerColor = Color(0xFF1F1215),
                contentColor = TextSecondary,
                borderColor = Color(0xFF4A1E24),
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )

            CyberTechButton(
                text = "[ KİLİTLE VE DEVAM ET ]",
                containerColor = Color(0xFF380812),
                contentColor = Color.White,
                borderColor = NeonRed,
                onClick = onComplete,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
private fun PanicFinalStep(
    targetMask: MaskType,
    trigger: PanicTrigger,
    wipeHistory: Boolean,
    dontShowAgain: Boolean,
    onToggleDontShow: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F321E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Kalkan Devreye Alındı!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Acil durum kalkanı arka planda sürekli hazır bekliyor.\nKamuflaj Hedefi: ${targetMask.title}\nSohbet Silme: ${if (wipeHistory) "Aktif (Tam Temizlik)" else "Pasif"}\n\nSeçtiğin tetikleyici veya telefonunu sallayarak kamuflaja geçebilir, kamuflaj ekranındayken tekrar salladığında ise güvenle GENeral Chat'e dönebilirsin.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onToggleDontShow() }
                .padding(8.dp)
        ) {
            Checkbox(
                checked = dontShowAgain,
                onCheckedChange = { onToggleDontShow() },
                colors = CheckboxDefaults.colors(checkedColor = NeonGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Bu sihirbazı bir daha gösterme",
                color = Color.White,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CyberTechButton(
            text = "[ SİSTEME GİRİŞ YAP ]",
            containerColor = Color(0xFF0F321E),
            contentColor = Color.White,
            borderColor = NeonGreen,
            onClick = onFinish,
            testTag = "panic_finish_button"
        )
    }
}
