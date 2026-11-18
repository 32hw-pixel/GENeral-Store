package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GeneralChatLogo
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.CyberYellowContainer
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundEffectsManager
import kotlinx.coroutines.delay

data class ShowcaseScene(
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val description: String,
    val highlightBadge: String
)

val showcaseScenes = listOf(
    ShowcaseScene(
        title = "TLS & DTLS-SRTP Korumalı Mesajlaşma",
        subtitle = "Sarı-Siyah Siberpunk İletişim Ağı",
        category = "MESAJLAŞMA & ARAMALAR",
        icon = Icons.Default.Chat,
        description = "Kişisel ve grup sohbetlerinde bulut TLS aktarımı, WebRTC görüşmelerinde ise DTLS-SRTP medya koruması devrededir. Sesli ve görüntülü görüşmeler yapabilirsiniz.",
        highlightBadge = "TLS & DTLS-SRTP"
    ),
    ShowcaseScene(
        title = "GEN AI Operasyon Merkezi",
        subtitle = "GEN AI Pro Akıllı Muhakeme Altyapısı",
        category = "YAPAY ZEKÂ",
        icon = Icons.Default.SmartToy,
        description = "Kod yazma, hata ayıklama, matematik, çeviri, sohbet özetleme ve sesli okuma modlarıyla yanınızda. 40 turluk bağlam hafızasıyla kesintisiz destek sağlar.",
        highlightBadge = "GEN AI PRO"
    ),
    ShowcaseScene(
        title = "10 Gizlilik Maskesi & Kamuflaj",
        subtitle = "Görünmezlik Kalkanı",
        category = "GİZLİLİK & GÜVENLİK",
        icon = Icons.Default.VisibilityOff,
        description = "Dışarıdan bakanlar Hava Durumu, Hesap Makinesi veya Not Defteri görür. Özel PIN kodunuzu girene kadar gerçek sohbet imparatorluğunuz tamamen gizli kalır.",
        highlightBadge = "10 KAMUFLAJ"
    ),
    ShowcaseScene(
        title = "Kağıt Ekran (Paper Mode)",
        subtitle = "E-Ink Mat Mürekkep Rahatlığı",
        category = "GÖRÜNÜM & ERGONOMİ",
        icon = Icons.Default.MenuBook,
        description = "Gözlerinizin yorulmaması için tasarlanmış mat okuma moduyla düşük ışıkta bile rahat mesajlaşma ve okuma deneyimi sunar.",
        highlightBadge = "KAĞIT MODU"
    ),
    ShowcaseScene(
        title = "Panik Butonu & Acil İmha",
        subtitle = "Tek Harekette İzleri Temizleme",
        category = "ACİL DURUM PROTOKOLÜ",
        icon = Icons.Default.Warning,
        description = "Telefonu sallayarak ya da arkasına üç kez dokunarak kamuflaj moduna geçebilir; tek tıkla 'Bu Cihazın Verilerini Sil' diyerek sisteminizi sıfırlayabilirsiniz.",
        highlightBadge = "SIFIR İZ"
    )
)

/**
 * AppShowcaseScreen: Interactive Cyber-Tablet Video Showcase
 * + Comprehensive inside-out written guide for GENeral Chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShowcaseScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Video Tanıtım, 1: Yazılı Kılavuz
    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var sceneProgress by remember { mutableStateOf(0f) }

    // Auto-advance showcase video scenes
    LaunchedEffect(isPlaying, currentSceneIndex) {
        if (isPlaying) {
            sceneProgress = 0f
            val totalTicks = 50
            for (i in 0..totalTicks) {
                delay(80)
                sceneProgress = i.toFloat() / totalTicks.toFloat()
            }
            currentSceneIndex = (currentSceneIndex + 1) % showcaseScenes.size
            SoundEffectsManager.playMenuNavSound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GENeral CHAT Nasıl Kullanılır?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("showcase_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberCardBg)
            )
        },
        containerColor = CyberBlack,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberDarkSurface,
                contentColor = CyberYellow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberYellow
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        SoundEffectsManager.playMenuNavSound()
                    },
                    text = {
                        Text(
                            text = "🎬 Video Tanıtım Vitrini",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        SoundEffectsManager.playMenuNavSound()
                    },
                    text = {
                        Text(
                            text = "📖 Tam Kullanım Kılavuzu",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Interactive Video Trailer Showcase
                VideoShowcaseContent(
                    currentScene = showcaseScenes[currentSceneIndex],
                    sceneProgress = sceneProgress,
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    onSelectScene = {
                        currentSceneIndex = it
                        sceneProgress = 0f
                        SoundEffectsManager.playMenuNavSound()
                    }
                )
            } else {
                // Comprehensive Written Manual
                WrittenManualContent()
            }
        }
    }
}

@Composable
private fun VideoShowcaseContent(
    currentScene: ShowcaseScene,
    sceneProgress: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSelectScene: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Holographic laser scanline animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanline_loop")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-Tech Cyber Holographic Tablet Frame (GENeral Pro 1080p Architecture)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF141924), Color(0xFF090D14))
                    )
                )
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            listOf(CyberYellow, NeonCyan, CyberYellow)
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Top HUD Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) NeonGreen else NeonRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "CANLI TANITIM AKIŞI" else "DURAKLATILDI",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) NeonGreen else TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "GEN-SYS-4K",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Screen Glass Area with Laser Scanline and Cyber Corners
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B2332), Color(0xFF080D15))
                            )
                        )
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Sci-Fi Cyber Grid
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridStep = 40f
                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = Color(0xFF223554).copy(alpha = 0.25f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                            x += gridStep
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color(0xFF223554).copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += gridStep
                        }

                        // Cyber HUD Corner Brackets
                        val cornerLen = 20f
                        val cornerStroke = 2.5f
                        val cornerColor = CyberYellow.copy(alpha = 0.8f)

                        // Top Left
                        drawLine(cornerColor, Offset(10f, 10f), Offset(10f + cornerLen, 10f), cornerStroke)
                        drawLine(cornerColor, Offset(10f, 10f), Offset(10f, 10f + cornerLen), cornerStroke)

                        // Top Right
                        drawLine(cornerColor, Offset(size.width - 10f, 10f), Offset(size.width - 10f - cornerLen, 10f), cornerStroke)
                        drawLine(cornerColor, Offset(size.width - 10f, 10f), Offset(size.width - 10f, 10f + cornerLen), cornerStroke)

                        // Bottom Left
                        drawLine(cornerColor, Offset(10f, size.height - 10f), Offset(10f + cornerLen, size.height - 10f), cornerStroke)
                        drawLine(cornerColor, Offset(10f, size.height - 10f), Offset(10f, size.height - 10f - cornerLen), cornerStroke)

                        // Bottom Right
                        drawLine(cornerColor, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f - cornerLen, size.height - 10f), cornerStroke)
                        drawLine(cornerColor, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f, size.height - 10f - cornerLen), cornerStroke)

                        // Laser Scanline
                        val scanlineY = size.height * laserY
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.45f),
                            start = Offset(0f, scanlineY),
                            end = Offset(size.width, scanlineY),
                            strokeWidth = 2f
                        )
                    }

                    AnimatedContent(
                        targetState = currentScene,
                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
                        label = "showcase_scene"
                    ) { scene ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(CyberYellow.copy(alpha = 0.35f), Color(0xFF131D2D))
                                        )
                                    )
                                    .border(2.dp, CyberYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = scene.icon,
                                    contentDescription = null,
                                    tint = CyberYellow,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(CyberYellow, Color(0xFFFFB300))
                                        )
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = scene.highlightBadge,
                                    color = CyberBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = scene.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = scene.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberYellow,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Watermark / Brand Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        GeneralChatLogo(size = 30.dp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline Scrubber & Controls
                LinearProgressIndicator(
                    progress = { sceneProgress },
                    color = CyberYellow,
                    trackColor = Color(0xFF182335),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0D1522))
                            .border(1.dp, Color(0xFF1D2F4A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentScene.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                role = Role.Button
                                stateDescription = if (isPlaying) "Oynatılıyor" else "Duraklatıldı"
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyberYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                tint = CyberBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Scene Explanation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.dp, CyberYellow.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberYellow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentScene.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = CyberYellow
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentScene.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Scene Navigation Pills
        Text(
            text = "Bölüm Seç:",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            showcaseScenes.forEachIndexed { index, sc ->
                val isSel = sc == currentScene
                Button(
                    onClick = { onSelectScene(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) CyberYellow else CyberCardBg
                    ),
                    border = BorderStroke(1.dp, if (isSel) CyberYellow else CyberCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${index + 1}",
                        fontWeight = FontWeight.Black,
                        color = if (isSel) CyberBlack else Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WrittenManualContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "GENeral CHAT v7.7.3 Pro Master Kullanım Kılavuzu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = CyberYellow
        )
        Text(
            text = "Uygulamanın içini ve dışını anlatan eksiksiz başvuru kaynağı",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        ManualSection(
            title = "1. Mesajlaşma ve Güvenli İletişim",
            content = "• 'Sohbetler' sekmesinden GEN AI asistanına veya kendi eklediğiniz gerçek kişi ve grup kanallarına erişebilirsiniz.\n" +
                    "• Mesajlarınız, dosyalarınız, konum ve etkinlik planlarınız TLS aktarım ve hesap bazlı güvenlik kurallarıyla korunur.\n" +
                    "• Sağ üstteki '+' butonuna dokunarak dilediğiniz kişiyle anında yeni sohbet başlatabilirsiniz."
        )

        ManualSection(
            title = "2. GEN AI (Akıllı Asistan ve Kodlama Merkezi)",
            content = "• Alt navigasyondaki 'GEN AI' sekmesinden GEN AI asistanına anında soru sorabilir, tam çalışan kodlar ürettirebilirsiniz.\n" +
                    "• 'Yanıt tarzı' seçicisinden Akıllı otomatik, Kodlama, Matematik, Çeviri, İçerik fikirleri veya Rehber hazırla modlarını seçebilirsiniz.\n" +
                    "• Sohbet geçmişini temizleyebilir, JSON/TXT olarak dışa aktarabilir ve yanıtları sesli olarak dinleyebilirsiniz."
        )

        ManualSection(
            title = "3. 10 Gizlilik Maskesi ve Kamuflaj",
            content = "• Ayarlar veya sol üstteki göz simgesinden 10 gizlilik maskesinden (Hava Durumu, Not Defteri, Hesap Makinesi, Pusula vb.) birini seçebilirsiniz.\n" +
                    "• Maske modundayken ekranınız normal bir kamuflaj aracı gibi görünür. Sohbetlerinize sadece gizli çıkış hareketinizle geri dönebilirsiniz."
        )

        ManualSection(
            title = "4. Kağıt Ekran (Paper Mode)",
            content = "• 'Ayarlar' sekmesinden Kağıt Ekran modunu aktif ettiğinizde renk paleti mat e-ink kitap okuma moduna geçer.\n" +
                    "• Mavi ışığı keserek gece kullanımında mutlak göz dinlendirmesi ve ekstra ekran gizliliği sağlar."
        )

        ManualSection(
            title = "5. Panik Butonu ve Veri Sıfırlama",
            content = "• Acil durumlarda telefonunuzu sallayarak veya arkasına dokunarak kamuflaj modunu devreye sokabilirsiniz.\n" +
                    "• 'Ayarlar > Veri ve gizlilik' bölümündeki 'Bu cihazın verilerini sil' butonuyla tüm yerel verileri, şifreleri ve oturumları tek tıkla fabrika ayarlarına döndürebilirsiniz."
        )

        ManualSection(
            title = "6. Ses ve Akustik Mimarisi",
            content = "• Ülke ve dil tekerleklerinde gezinirken alarm saati klik sesleri, menülerde gezinirken ise telif içermeyen futuristik arcade tuş sesleri duyulur."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ManualSection(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CyberYellow)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = CyberYellow
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 21.sp
            )
        }
    }
}
