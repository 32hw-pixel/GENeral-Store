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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GeneralChatLogo
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.75f) }
    val contentAlpha = remember { Animatable(0f) }

    // Cyber shield rotation and pulse
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_pulse")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_anim"
    )

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(150, easing = LinearEasing))
        scale.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
        delay(250)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .clickable { onFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle cyber matrix scanlines background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Sweeping neon beam
            val currentBeamY = scanlineY * canvasHeight
            drawLine(
                color = NeonCyan.copy(alpha = 0.25f),
                start = Offset(0f, currentBeamY),
                end = Offset(canvasWidth, currentBeamY),
                strokeWidth = 3f
            )

            // Horizontal grid lines
            val step = 40f
            var y = 0f
            while (y < canvasHeight) {
                drawLine(
                    color = Color(0xFF16233B).copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // Ambient radial background glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseGlow)
                .alpha(0.22f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan, NeonPurple, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(contentAlpha.value)
        ) {
            // Authentic Yellow Squircle "G" Logo with Rotating Subtle Cyber Ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Subtle yellow orbit ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .rotate(ringRotation)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(CyberYellow, Color.Transparent, CyberYellow.copy(alpha = 0.5f), Color.Transparent, CyberYellow)
                            ),
                            shape = CircleShape
                        )
                )

                GeneralChatLogo(size = 90.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Brand Title with Cyberpunk Glitch Effect
            GlitchCyberText(
                text = "GENeral Chat",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                baseColor = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Version and operation status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v7.7.3 PRO // OPERASYON BAŞLATILIYOR",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            // Iconic Slogan with Chromatic Aberration Glitch & Cyber Brackets
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF081220))
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 22.dp, vertical = 12.dp)
                    .testTag("splash_glitch_slogan")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[ ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )

                    GlitchCyberText(
                        text = "BAĞLANTIDA KAL.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 5.sp,
                        baseColor = NeonCyan
                    )

                    Text(
                        text = " ]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Audio Frequency Spectrum Visualizer Bars (Bölüm 1 & 10: Siberpunk Ses Mimarisi)
            CyberFrequencyBars()
        }
    }
}

/**
 * Cyberpunk chromatic aberration (RGB split) glitch text component.
 * Periodically splits cyan & magenta shadows horizontally to simulate CRT/digital glitching.
 */
@Composable
fun GlitchCyberText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    letterSpacing: TextUnit,
    baseColor: Color,
    modifier: Modifier = Modifier
) {
    var glitchX by remember { mutableFloatStateOf(0f) }
    var glitchY by remember { mutableFloatStateOf(0f) }
    var isGlitching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(600, 1600))
            isGlitching = true
            // Quick random jitter burst
            repeat(4) {
                glitchX = (Random.nextFloat() * 12f - 6f)
                glitchY = (Random.nextFloat() * 4f - 2f)
                delay(40)
            }
            glitchX = 0f
            glitchY = 0f
            isGlitching = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isGlitching) {
            // Cyan split layer
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                color = NeonCyan.copy(alpha = 0.85f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset((-glitchX).toInt(), (-glitchY).toInt()) }
            )

            // Magenta/Pink split layer
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                color = NeonPink.copy(alpha = 0.85f),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset((glitchX).toInt(), (glitchY).toInt()) }
            )
        }

        // Center sharp base layer
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            color = baseColor,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Cyberpunk audio frequency visualizer bars reflecting the sound/music architecture.
 */
@Composable
fun CyberFrequencyBars(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val heights = (0 until 14).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = (10 + (i * 7) % 22).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350 + (i * 65), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { index, animHeight ->
            val color = when {
                index < 4 -> NeonCyan
                index < 9 -> NeonPurple
                else -> NeonPink
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animHeight.value.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color.copy(alpha = 0.7f))
            )
        }
    }
}
