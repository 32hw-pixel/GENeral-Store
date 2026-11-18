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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070B12), Color(0xFF0E1626), Color(0xFF06090F))
                )
            )
            .clickable { onFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Soft ambient radial highlights
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberYellow.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.22f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.25f, size.height * 0.22f),
                radius = size.width * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.80f, size.height * 0.78f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.80f, size.height * 0.78f),
                radius = size.width * 0.65f
            )
            val currentBeamY = scanlineY * size.height
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.28f), Color.Transparent)
                ),
                start = Offset(0f, currentBeamY),
                end = Offset(size.width, currentBeamY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Ambient radial center glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(pulseGlow)
                .alpha(0.24f)
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
            modifier = Modifier
                .padding(28.dp)
                .alpha(contentAlpha.value)
        ) {
            // Modern Squircle Logo with Clean Halo Orbit
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .rotate(ringRotation)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(CyberYellow, NeonCyan.copy(alpha = 0.6f), Color.Transparent, CyberYellow)
                            ),
                            shape = CircleShape
                        )
                )

                GeneralChatLogo(size = 92.dp)
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "GENeral Chat",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Modern Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v7.7.3 Pro · Anlık ve Güvenli İletişim",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Sleek Glassmorphic Slogan Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF121C2E), Color(0xFF0E1626))
                        )
                    )
                    .border(1.dp, CyberYellow.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("splash_glitch_slogan")
            ) {
                Text(
                    text = "BAĞLANTIDA KAL.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = CyberYellow
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

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
