package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
        title = "Uçtan Uca Şifreli Mesajlaşma (E2EE)",
        subtitle = "Sarı-Siyah Siberpunk İletişim Ağı",
        category = "MESAJLAŞMA & ARAMALAR",
        icon = Icons.Default.Chat,
        description = "Kişisel ve grup sohbetlerinde SHA-256 ve AES-256 şifreleme devrededir. WebRTC ile yüksek kaliteli, şifreli sesli ve görüntülü görüşmeler yapabilirsiniz.",
        highlightBadge = "E2EE AKTİF"
    ),
    ShowcaseScene(
        title = "GEN AI Operasyon Merkezi",
        subtitle = "Gemini 3.8 Flash Derin Zekâ Altyapısı",
        category = "YAPAY ZEKÂ",
        icon = Icons.Default.SmartToy,
        description = "Akıllı otomatik, kodlama, matematik, çeviri ve içerik fikirleri modlarıyla sorularınızı yanıtlar. 40 turluk bağlam hafızası ve 24.576 token bütçesiyle yanınızda.",
        highlightBadge = "GEMINI 3.8 FLASH"
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
        description = "Gecenin köründe bile gözlerin yorulmasın. Dijital ışığı kesen mat mikro-kapsül moduyla saf kitap okuma konforu ve gizlilik sunar.",
        highlightBadge = "E-INK PAPER"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-Tech Cyber Tablet Frame (GENeral Pro Architecture)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F0F0F))
                .border(2.dp, CyberYellow.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Screen Glass Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))
                            )
                        )
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
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
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(CyberYellowContainer)
                                    .border(2.dp, CyberYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = scene.icon,
                                    contentDescription = null,
                                    tint = CyberYellow,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberYellow)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = scene.highlightBadge,
                                    color = CyberBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

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
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Watermark / Brand Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        GeneralChatLogo(size = 28.dp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timeline Scrubber & Controls
                LinearProgressIndicator(
                    progress = { sceneProgress },
                    color = CyberYellow,
                    trackColor = CyberCardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentScene.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberYellow)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                            tint = CyberBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Scene Explanation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.dp, CyberCardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = currentScene.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberYellow
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentScene.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Scene Navigation Pills
        Text(
            text = "Bölüm Seç:",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            showcaseScenes.forEachIndexed { index, sc ->
                Button(
                    onClick = { onSelectScene(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sc == currentScene) CyberYellow else CyberCardBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${index + 1}",
                        fontWeight = FontWeight.Black,
                        color = if (sc == currentScene) CyberBlack else Color.White,
                        fontSize = 12.sp
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
            content = "• 'Sohbetler' sekmesinden Ahmet Yılmaz, Zeynep Kaya veya grup kanallarına erişebilirsiniz.\n" +
                    "• Her mesaj SHA-256 ve AES uçtan uca şifrelemeyle (E2EE) korunur. Mesajlar yerel veritabanında güvenle saklanır.\n" +
                    "• Sağ üstteki '+' butonuna dokunarak dilediğiniz kişiyle anında yeni şifreli sohbet başlatabilirsiniz."
        )

        ManualSection(
            title = "2. GEN AI (Gemini 3.8 Flash Operasyon Merkezi)",
            content = "• Alt navigasyondaki 'GEN AI' sekmesinden Gemini 3.8 Flash zekâsına anında soru sorabilirsiniz.\n" +
                    "• 'Yanıt tarzı' seçicisinden Akıllı otomatik, Kodlama, Matematik, Çeviri, İçerik fikirleri veya Rehber hazırla modlarını seçebilirsiniz.\n" +
                    "• 40 önceki soru-cevap turunu hatırlar, bağlamı kaybetmeden karmaşık görevlerinizi çözer."
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CyberYellow
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                lineHeight = 20.sp
            )
        }
    }
}
