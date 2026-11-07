package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GeneralChatLogo
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.CyberTechButton
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SloganScreen(
    onTriggerShatter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGlitching by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    val glitchAlpha = remember { Animatable(1f) }
    val glitchOffsetX = remember { Animatable(0f) }
    val mainWordScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    fun triggerGlitchAndEnter() {
        if (isGlitching) return
        isGlitching = true
        SoundPoolManager.playWhoosh()
    }

    LaunchedEffect(isGlitching) {
        if (isGlitching) {
            // Intense glitch burst
            repeat(8) {
                glitchOffsetX.snapTo((Random.nextFloat() * 24 - 12))
                delay(40)
            }
            glitchOffsetX.snapTo(0f)
            glitchAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
            mainWordScale.animateTo(1.3f, tween(280, easing = FastOutSlowInEasing))
            delay(150)
            onTriggerShatter()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .clickable { triggerGlitchAndEnter() }
            .padding(24.dp)
            .testTag("slogan_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Sci-Fi Cyber Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 50f
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color(0xFF131D2E).copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color(0xFF131D2E).copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // Ambient center radial glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseGlow)
                .alpha(0.18f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberYellow, NeonCyan, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top HUD Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B1424))
                    .border(1.dp, CyberYellow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GİRİŞ YETKİLENDİ // FREKANS BAĞLANTISI",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberYellow,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Authentic Logo
            GeneralChatLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(36.dp))

            // High-End Cinematic Slogan Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF080F1C).copy(alpha = 0.9f))
                    .border(1.5.dp, if (isGlitching) NeonCyan else CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Line 1: GENÇ + 'in dilinden yaşlı değil
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GENÇ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = CyberYellow,
                            letterSpacing = 2.sp,
                            modifier = Modifier.scale(mainWordScale.value)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "'in dilinden",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White,
                            modifier = Modifier
                                .alpha(glitchAlpha.value)
                                .offset { IntOffset(glitchOffsetX.value.toInt(), 0) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "yaşlı değil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .alpha(glitchAlpha.value)
                            .offset { IntOffset((-glitchOffsetX.value).toInt(), 0) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line 2: GENç + anlar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GENç ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            color = NeonCyan,
                            letterSpacing = 2.sp,
                            modifier = Modifier.scale(mainWordScale.value)
                        )
                        Text(
                            text = "anlar",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier
                                .alpha(glitchAlpha.value)
                                .offset { IntOffset(glitchOffsetX.value.toInt(), 0) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Button: Asphalt style bounce button
            CyberTechButton(
                text = if (isGlitching) "[ ŞİFRELER ÇÖZÜLÜYOR... ]" else "[ SİSTEME SIZ ]",
                containerColor = CyberYellow,
                contentColor = CyberBlack,
                borderColor = CyberYellow,
                icon = Icons.Default.LockOpen,
                onClick = { triggerGlitchAndEnter() },
                testTag = "slogan_enter_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "— Veya ekranda herhangi bir yere dokun —",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Audio Status Footer
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
