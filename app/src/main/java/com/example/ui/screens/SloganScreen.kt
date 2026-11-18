package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.GeneralChatLogo
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
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
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    fun triggerEnter() {
        if (isGlitching) return
        isGlitching = true
        SoundPoolManager.playWhoosh()
    }

    LaunchedEffect(isGlitching) {
        if (isGlitching) {
            repeat(6) {
                glitchOffsetX.snapTo((Random.nextFloat() * 18 - 9))
                delay(35)
            }
            glitchOffsetX.snapTo(0f)
            glitchAlpha.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
            mainWordScale.animateTo(1.22f, tween(240, easing = FastOutSlowInEasing))
            delay(120)
            onTriggerShatter()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060A14),
                        Color(0xFF0D1528),
                        Color(0xFF050811)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .semantics {
                role = Role.Button
                contentDescription = "GENeral Chat'e girmek için ekrana dokunun"
            }
            .clickable { triggerEnter() }
            .padding(24.dp)
            .testTag("slogan_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial aura
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseGlow)
                .alpha(0.24f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan, CyberYellow.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
        ) {
            // Modern Top Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF101C33))
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GİRİŞ YETKİLENDİ · ŞİFRELİ FREKANS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Modern Concentric Halo Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(112.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = ringRotation }
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(CyberYellow, NeonCyan, Color.Transparent, CyberYellow)
                        ),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                GeneralChatLogo(size = 76.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sleek Glassmorphic Slogan Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F182B).copy(alpha = 0.9f)),
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        if (isGlitching) listOf(NeonCyan, NeonCyan) else listOf(CyberYellow.copy(alpha = 0.8f), NeonCyan.copy(alpha = 0.8f))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GENÇ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            color = CyberYellow,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.scale(mainWordScale.value)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "'in dilinden",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 27.sp,
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
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.White.copy(alpha = 0.82f),
                        modifier = Modifier
                            .alpha(glitchAlpha.value)
                            .offset { IntOffset((-glitchOffsetX.value).toInt(), 0) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GENç ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp,
                            color = NeonCyan,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.scale(mainWordScale.value)
                        )
                        Text(
                            text = "anlar",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 31.sp,
                            color = Color.White,
                            modifier = Modifier
                                .alpha(glitchAlpha.value)
                                .offset { IntOffset(glitchOffsetX.value.toInt(), 0) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Button: Modern gradient pill entry button
            Button(
                onClick = { triggerEnter() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberYellow,
                    contentColor = CyberBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("slogan_enter_button")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGlitching) "Şifreler Çözülüyor..." else "Sisteme Bağlan",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Devam etmek için ekranda herhangi bir yere dokunun",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

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
