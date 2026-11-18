package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
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
        badge = "01 · BAŞLANGIÇ",
        title = "GENeral CHAT'e Hoş Geldiniz",
        description = "Sınırları kaldıran yeni nesil mesajlaşma deneyimi. Uçtan uca şifreli sohbetler, canlı görüntülü görüşme ve akıllı asistan tek bir modern arayüzde.",
        icon = Icons.Default.RocketLaunch,
        accentColor = NeonCyan,
        isWelcome = true
    ),
    OnboardingSlideData(
        step = 2,
        badge = "02 · ACİL PROTOKOL",
        title = "PANİK MODU",
        description = "Telefonunu sallayarak veya acil çıkışa dokunarak saniyeler içinde devreye giren gizlilik kalkanı. Oturum anında kilitlenir ve hassas ekran gizlenir.",
        icon = Icons.Default.Lock,
        accentColor = NeonRed,
        isPanicMode = true
    ),
    OnboardingSlideData(
        step = 3,
        badge = "03 · BULUT SENKRONİZASYON",
        title = "Kendi Kaleni Kurma Vakti",
        description = "Gerçek zamanlı ücretsiz bulut senkronizasyonuyla mesajların tüm cihazlarında güvende. Benzersiz @kullanici_adi ile anında bağlan.",
        icon = Icons.Default.CloudDone,
        accentColor = NeonPurple
    ),
    OnboardingSlideData(
        step = 4,
        badge = "04 · GEN AI DESTEK",
        title = "GEN AI Operasyon Merkezin",
        description = "GEN AI burada sadece bir sohbet botu değil; kod yazabilen, sohbetlerini hatırlayan, sesli okuyabilen ve üretkenliğini artıran akıllı yardımcın.",
        icon = Icons.Default.AutoAwesome,
        accentColor = CyberYellow
    ),
    OnboardingSlideData(
        step = 5,
        badge = "05 · GİZLİLİK MASKESİ",
        title = "Kişiselleştirme & Maskeleme",
        description = "Dışarıdan bakanlar yalnızca sıradan bir hesap makinesi ya da hava durumu uygulaması görür. PIN veya gizli kombinasyonla gerçek sohbetine ulaşırsın.",
        icon = Icons.Default.VisibilityOff,
        accentColor = NeonPink
    ),
    OnboardingSlideData(
        step = 6,
        badge = "06 · KONFOR MODU",
        title = "Kağıt Ekran & Renk Frekansı",
        description = "Gecenin köründe bile gözlerin yorulmasın. Düşük parlaklık, sıcak kehribar ve kağıt ekran okuma modlarıyla uzun sohbetlerde göz konforu sağlar.",
        icon = Icons.Default.MenuBook,
        accentColor = Color(0xFFF2EFE9)
    ),
    OnboardingSlideData(
        step = 7,
        badge = "07 · HAZIRSIN",
        title = "Sisteme Bağlanmaya Hazır Mısın?",
        description = "Sıfır takipçi telemetrisi, TLS & DTLS-SRTP medya koruması, konum & etkinlik paylaşımı ve gelişmiş gizlilik seni bekliyor.",
        icon = Icons.Default.Shield,
        accentColor = NeonGreen
    )
)

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060913),
                        Color(0xFF0C1426),
                        Color(0xFF05070E)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("onboarding_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Soft ambient radial accent aura
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            currentSlide.accentColor.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Modern animated pill progress + Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onboardingSlides.forEachIndexed { index, _ ->
                        val active = index == currentIndex
                        val pillWidth by animateDpAsState(
                            targetValue = if (active) 28.dp else 8.dp,
                            animationSpec = tween(260),
                            label = "pill_w_$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(pillWidth)
                                .clip(CircleShape)
                                .background(
                                    if (active) currentSlide.accentColor
                                    else Color.White.copy(alpha = 0.18f)
                                )
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
                            text = "Atla",
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Animated Slide Card
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    (fadeIn(tween(240)) + slideInHorizontally(tween(260)) { it / 8 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { -it / 8 })
                },
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
                    // Clean Modern Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (slide.isPanicMode) "PANİK MODU" else "GENeral CHAT",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (slide.isPanicMode) NeonRed else Color.White,
                            letterSpacing = 0.8.sp
                        )
                        if (!slide.isPanicMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(slide.accentColor.copy(alpha = 0.18f))
                                    .border(1.dp, slide.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v7.7.3",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = slide.accentColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val slideImageRes = when (slide.step) {
                        1 -> R.drawable.img_cyber_welcome_server
                        2 -> R.drawable.img_cyber_panic_vault
                        3 -> R.drawable.img_cyber_cloud_fortress
                        else -> null
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1729).copy(alpha = 0.88f)),
                        border = BorderStroke(1.dp, slide.accentColor.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            if (slideImageRes != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(175.dp)
                                        .clip(RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = slideImageRes),
                                        contentDescription = slide.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color(0xAA070D19))
                                                )
                                            )
                                    )
                                    if (slide.isPanicMode) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 10.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xE622050B))
                                                .border(1.dp, NeonRed, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ACİL KORUMA PROTOKOLÜ",
                                                color = NeonRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(136.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(slide.accentColor.copy(alpha = 0.24f), Color(0xFF0A1220))
                                            )
                                        )
                                        .border(1.5.dp, slide.accentColor.copy(alpha = 0.55f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = slide.icon,
                                        contentDescription = null,
                                        tint = slide.accentColor,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(slide.accentColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = slide.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slide.accentColor,
                                    letterSpacing = 0.9.sp
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
                                color = Color.White.copy(alpha = 0.86f),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            if (slide.isPanicMode) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NeonRed.copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = NeonRed,
                                        modifier = Modifier.size(18.dp)
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
                }
            }

            // Bottom Navigation Buttons (clean rounded pill design + >= 52.dp touch targets)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFinal) {
                    Button(
                        onClick = {
                            SoundPoolManager.playWhoosh()
                            onFinished()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberYellow,
                            contentColor = CyberBlack
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("onboarding_start_button")
                    ) {
                        Text(
                            text = "Sisteme Bağlan & Başla",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentIndex > 0) {
                            OutlinedButton(
                                onClick = {
                                    SoundPoolManager.playMenuNav()
                                    currentIndex--
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isPanic) NeonRed.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.24f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isPanic) NeonRed else Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("onboarding_prev_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPanic) "Vazgeç" else "Geri",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                currentIndex++
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPanic) NeonRed else currentSlide.accentColor,
                                contentColor = if (isPanic) Color.White else CyberBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text(
                                text = if (isPanic) "İleri" else "Devam Et",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
