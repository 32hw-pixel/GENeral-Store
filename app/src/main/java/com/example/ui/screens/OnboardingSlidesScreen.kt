package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.CyberTechButton
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager

data class OnboardingSlideData(
    val step: Int,
    val badge: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isPanicMode: Boolean = false,
    val isWelcome: Boolean = false
)

val onboardingSlides = listOf(
    OnboardingSlideData(
        step = 1,
        badge = "01 // BAŞLANGIÇ",
        title = "GENeral CHAT'e hoş geldiniz!",
        description = "Vites yükseltiyoruz, ilk genişleme hamlemizi yapıyoruz. GENeral Chat yepyeni bir mesajlaşma deneyimi sunuyor; duvarlar ve sınırlar yıkılıyor. Daha fazlası için devam et.",
        icon = Icons.Default.RocketLaunch,
        accentColor = NeonCyan,
        isWelcome = true
    ),
    OnboardingSlideData(
        step = 2,
        badge = "02 // ACİL PROTOKOL",
        title = "PANİK MODU",
        description = "Panik anını yönetme vakti! Telefonunu sallayarak ya da arkasına üç kez dokunarak saniyeler içinde devreye sokabileceğin kusursuz bir görünmezlik kalkanı sunuyoruz. Tehlike kapıdayken tüm izler tek hamlede siliniyor.",
        icon = Icons.Default.Lock,
        accentColor = NeonRed,
        isPanicMode = true
    ),
    OnboardingSlideData(
        step = 3,
        badge = "03 // KALE PROTOKOLÜ",
        title = "Kendi Kaleni Kurma Vakti",
        description = "Kendi kaleni kurma vakti. E-posta tabanlı bulut hesap sistemiyle verilerin tamamen senin kontrolünde. Benzersiz @kullanici_adi ile kaydını yap, dünyayla olan güvenli bağını anında sabitle.",
        icon = Icons.Default.CloudDone,
        accentColor = NeonPurple
    ),
    OnboardingSlideData(
        step = 4,
        badge = "04 // GEN AI DESTEK",
        title = "Yapay Zekâ Operasyon Merkezin",
        description = "Yapay zekâ burada sadece bir araç değil, senin operasyon merkezin. GEN AI; derinlemesine düşünen yapısıyla tüm sorularını yanıtlıyor, rehberlik ediyor ve sistemi senin için kusursuzlaştırıyor.",
        icon = Icons.Default.AutoAwesome,
        accentColor = CyberYellow
    ),
    OnboardingSlideData(
        step = 5,
        badge = "05 // 10 GİZLİLİK MASKESİ",
        title = "Kişiselleştirme & Maskeleme",
        description = "Dışarıdan bakanlar sadece masum bir Hava Durumu veya Hesap Makinesi görür. 10 farklı gizlilik maskesi ya da yapay zekâyla tamamen sana özel üreteceğin sırlar arkasında gizli bir imparatorluk yatıyor.",
        icon = Icons.Default.VisibilityOff,
        accentColor = NeonPink
    ),
    OnboardingSlideData(
        step = 6,
        badge = "06 // KAĞIT EKRAN",
        title = "Kağıt Ekran: Gözleri Yormayan Gece",
        description = "Gecenin köründe bile gözlerin yorulmasın. Kağıt Ekran modu, pikselleri mat mürekkep mikro-kapsüllerine çevirerek dijital ışığı keser; karanlıkta bile saf kitap okuma konforu sunar.",
        icon = Icons.Default.MenuBook,
        accentColor = Color(0xFFF2EFE9)
    ),
    OnboardingSlideData(
        step = 7,
        badge = "07 // SİSTEME SIZMA",
        title = "Sisteme Sızma Vakti",
        description = "Sıfır gözetim, uçtan uca şifreleme (E2EE). Sınırlar kalktı, oyunun kuralını sen yazıyorsun. Şimdi sisteme sızma vakti.",
        icon = Icons.Default.Shield,
        accentColor = NeonGreen
    )
)

/**
 * Onboarding Slides Screen:
 * 1:1 match to the uploaded reference photo (IMG-20261101-WA0000.jpg),
 * with blue/red server racks, central holographic terminal, heavy emergency protocol lock,
 * and glowing action buttons.
 */
@Composable
fun OnboardingSlidesScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAudioDialog by remember { mutableStateOf(false) }

    val currentSlide = onboardingSlides[currentIndex]
    val isFinal = currentIndex == onboardingSlides.lastIndex
    val isPanic = currentSlide.isPanicMode

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
            .testTag("onboarding_screen")
    ) {
        // Perspective Server Racks Background Art (Matches reference image)
        ServerRoomAtmosphereCanvas(accentColor = currentSlide.accentColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Step indicators and Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Progress Bars
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    onboardingSlides.forEachIndexed { index, _ ->
                        val active = index == currentIndex
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(if (active) 24.dp else 7.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) currentSlide.accentColor else Color(0xFF1E283C))
                        )
                    }
                }

                if (!isFinal) {
                    TextButton(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            onFinished()
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "ATLA",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Animated Slide Content
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "slide_transition",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { slide ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Brand
                    if (slide.isPanicMode) {
                        Text(
                            text = "PANİK MODU",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = NeonRed,
                            letterSpacing = 2.sp
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "GENeral",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 3.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "CHAT ",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = slide.accentColor,
                                    letterSpacing = 3.sp
                                )
                                Text(
                                    text = "v7.7.3 Pro",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = slide.accentColor.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Center Visual: High-Tech Cyber Illustrations matching reference photo
                    val slideImageRes = when (slide.step) {
                        1 -> com.example.R.drawable.img_cyber_welcome_server
                        2 -> com.example.R.drawable.img_cyber_panic_vault
                        3 -> com.example.R.drawable.img_cyber_cloud_fortress
                        else -> null
                    }

                    if (slideImageRes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, slide.accentColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = slideImageRes),
                                contentDescription = slide.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle cyberpunk gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0x9904060A))
                                        )
                                    )
                            )
                            if (slide.isPanicMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xD91E0408))
                                        .border(1.2.dp, NeonRed, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "EMERGENCY PROTOCOL ACTIVE",
                                        color = NeonRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Holographic HUD Radar Core
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF091220))
                                .border(1.5.dp, slide.accentColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, slide.accentColor.copy(alpha = 0.5f), CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(slide.accentColor.copy(alpha = 0.3f), Color.Transparent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = slide.accentColor,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1B2B))
                            .border(1.dp, slide.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = slide.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = slide.accentColor,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    if (slide.isPanicMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = NeonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3 kez dokun veya salla",
                                color = NeonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Buttons matching reference screenshot
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFinal) {
                    CyberTechButton(
                        text = "[ SİSTEME SIZ & BAŞLA ]",
                        containerColor = CyberYellow,
                        contentColor = CyberBlack,
                        borderColor = CyberYellow,
                        onClick = {
                            SoundPoolManager.playWhoosh()
                            onFinished()
                        },
                        testTag = "onboarding_start_button"
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentIndex > 0) {
                            CyberTechButton(
                                text = if (isPanic) "[ VAZGEÇ ]" else "[ GERİ ]",
                                containerColor = Color(0xFF141923),
                                contentColor = if (isPanic) NeonRed else TextSecondary,
                                borderColor = if (isPanic) NeonRed else Color(0xFF243248),
                                onClick = {
                                    SoundPoolManager.playMenuNav()
                                    currentIndex--
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "onboarding_prev_button"
                            )
                        }

                        CyberTechButton(
                            text = if (isPanic) "[ İLERİ ]" else "[ DEVAM ET ]",
                            containerColor = if (isPanic) Color(0xFF2B070A) else Color(0xFF0D253B),
                            contentColor = Color.White,
                            borderColor = currentSlide.accentColor,
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "onboarding_next_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Audio Status Footer: "🔊 Müzik Aktif %50 | Tok Tuş Sesleri"
                CyberAudioStatusBar(
                    onOpenAudioCenter = { showAudioDialog = true }
                )
            }
        }
    }

    if (showAudioDialog) {
        MusicSettingsDialog(
            onDismiss = { showAudioDialog = false }
        )
    }
}

/**
 * Atmospheric canvas drawing perspective server racks and neon data lines
 * matching the background of IMG-20261101-WA0000.jpg.
 */
@Composable
private fun ServerRoomAtmosphereCanvas(accentColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Left server rack perspective lines
        val rackColor = accentColor.copy(alpha = 0.12f)
        val beamColor = accentColor.copy(alpha = 0.20f)

        // Draw left server rack columns
        for (i in 0..4) {
            val x = i * (w * 0.08f)
            drawLine(
                color = rackColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.5f
            )
        }

        // Draw right server rack columns
        for (i in 0..4) {
            val x = w - (i * (w * 0.08f))
            drawLine(
                color = rackColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.5f
            )
        }

        // Horizontal data bus lines
        for (j in 1..8) {
            val y = j * (h * 0.11f)
            drawLine(
                color = beamColor,
                start = Offset(0f, y),
                end = Offset(w * 0.32f, y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = beamColor,
                start = Offset(w * 0.68f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
