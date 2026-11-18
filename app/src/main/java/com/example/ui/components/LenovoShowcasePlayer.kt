package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.delay

data class LenovoFeatureCard(
    val id: String,
    val categoryBadge: String,
    val turkishTitle: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val statBadge: String,
    val specDetails: List<Pair<String, String>>
)

val lenovoFeatures = listOf(
    LenovoFeatureCard(
        id = "ips_display",
        categoryBadge = "ARAYÜZ & TEMA",
        turkishTitle = "Uyarlanabilir Ekran & Kağıt Modu",
        subtitle = "Açık, Siber Koyu, 2700K Sıcak Sepia ve göz yormayan Kağıt Ekran temalarıyla her ortamda net görünüm.",
        icon = Icons.Default.Visibility,
        accentColor = NeonCyan,
        statBadge = "5 Tema // Ekran Yakınlaştırma",
        specDetails = listOf(
            "Görünüm Seçenekleri" to "5 Ekran & Renk Frekansı + Duvar Kağıtları",
            "Göz Konforu" to "Kağıt Ekran (Paper Mode) ve Sepia Modu",
            "Erişilebilirlik" to "Ayarlanabilir Yazı Boyutu & Zoom"
        )
    ),
    LenovoFeatureCard(
        id = "front_camera",
        categoryBadge = "GÜVENLİ İLETİŞİM",
        turkishTitle = "WebRTC Görüntülü & Sesli Arama",
        subtitle = "Uçtan uca DTLS/SRTP şifrelemeyle telefonunuzun kamerası ve mikrofonu üzerinden gerçek zamanlı görüşme.",
        icon = Icons.Default.Videocam,
        accentColor = Color(0xFF38BDF8),
        statBadge = "WebRTC // Gerçek Zamanlı",
        specDetails = listOf(
            "Ses & Görüntü Şifreleme" to "WebRTC DTLS / SRTP",
            "İzin Yönetimi" to "Anlık Kamera ve Mikrofon İzin Kontrolü",
            "Bağlantı" to "Farklı Ağlardan Çapraz Cihaz Görüşmesi"
        )
    ),
    LenovoFeatureCard(
        id = "big_battery",
        categoryBadge = "GÜÇ & VERİMLİLİK",
        turkishTitle = "Hafif Mimari & Düşük Güç Tüketimi",
        subtitle = "Gereksiz arka plan servisleri çalıştırmayan verimli Jetpack Compose arayüzü ile dengeli pil kullanımı.",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = NeonGreen,
        statBadge = "Optimize // Jetpack Compose",
        specDetails = listOf(
            "Pil Optimizasyonu" to "Olay Tabanlı Dinleyiciler",
            "Veri Depolama" to "Yerel Room SQLite + Firebase Firestore",
            "Ağ Kullanımı" to "Yalnızca Aktif Oturumda Senkronizasyon"
        )
    ),
    LenovoFeatureCard(
        id = "fast_processor",
        categoryBadge = "AKICI PERFORMANS",
        turkishTitle = "Hızlı Mesajlaşma & Akıllı GEN AI",
        subtitle = "Cihazınızın desteklediği en yüksek yenileme hızında pürüzsüz kaydırma ve anlık yapay zekâ yanıtları.",
        icon = Icons.Default.Memory,
        accentColor = CyberYellow,
        statBadge = "Akıcı UI // GEN AI Pro",
        specDetails = listOf(
            "Ekran Akıcılığı" to "Cihazın Doğal Yüksek Yenileme Hızı",
            "Oyun & Eğlence" to "Dahili Siber Mini Oyunlar",
            "Tuş Geri Bildirimi" to "Düşük Gecikmeli Ses Efektleri"
        )
    )
)

/**
 * Interactive commercial video player presenting GENeral Hardware and Ecosystem.
 */
@Composable
fun LenovoTechShowcasePlayer(
    modifier: Modifier = Modifier
) {
    var currentFeatureIndex by remember { mutableIntStateOf(0) }
    var isPlayingCommercial by remember { mutableStateOf(true) }
    var showAllOverview by remember { mutableStateOf(false) }

    // Auto-advance commercial slides
    LaunchedEffect(isPlayingCommercial, showAllOverview) {
        while (isPlayingCommercial && !showAllOverview) {
            delay(3800)
            currentFeatureIndex = (currentFeatureIndex + 1) % lenovoFeatures.size
            SoundPoolManager.playScrollTick()
        }
    }

    val feature = lenovoFeatures[currentFeatureIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lenovo_showcase_player"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07111F)),
        border = BorderStroke(1.5.dp, Color(0xFF1E5B94))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0F4C81), Color(0xFF1A73E8), Color(0xFF0A2540))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GÜVENLİ MESAJLAŞMA & AKICI MİMARİ",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "GENeral Chat Sistem Özellikleri",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Commercial controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                showAllOverview = !showAllOverview
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "4'lü Genel Bakış",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                isPlayingCommercial = !isPlayingCommercial
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (isPlayingCommercial) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Oynat / Durdur",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Body content
            if (showAllOverview) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ÖZET // 4 TEMEL DONANIM ÖZELLİĞİ",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        lenovoFeatures.take(2).forEach { item ->
                            MiniFeatureCard(item = item, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        lenovoFeatures.takeLast(2).forEach { item ->
                            MiniFeatureCard(item = item, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Single Feature Commercial Showcase Slide
                AnimatedContent(
                    targetState = feature,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "lenovo_feature_anim"
                ) { current ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = current.categoryBadge,
                                color = current.accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(current.accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, current.accentColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = current.statBadge,
                                    color = current.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = current.turkishTitle,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = current.subtitle,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Device Screen Visual Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF03070E))
                                .border(1.dp, current.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(current.accentColor.copy(alpha = 0.15f))
                                        .border(2.dp, current.accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = current.icon,
                                        contentDescription = null,
                                        tint = current.accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = current.turkishTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = current.statBadge,
                                    color = current.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hardware Specs Breakdown
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF05101E))
                                .padding(10.dp)
                        ) {
                            current.specDetails.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = value,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF040A14))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        SoundPoolManager.playScrollTick()
                        currentFeatureIndex = if (currentFeatureIndex == 0) lenovoFeatures.lastIndex else currentFeatureIndex - 1
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Önceki", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lenovoFeatures.forEachIndexed { idx, item ->
                        val isSel = idx == currentFeatureIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSel) 20.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) item.accentColor else Color.Gray.copy(alpha = 0.4f))
                                .clickable {
                                    SoundPoolManager.playScrollTick()
                                    currentFeatureIndex = idx
                                }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        SoundPoolManager.playScrollTick()
                        currentFeatureIndex = (currentFeatureIndex + 1) % lenovoFeatures.size
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Sonraki", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MiniFeatureCard(
    item: LenovoFeatureCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF05101E)),
        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.categoryBadge,
                color = item.accentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.turkishTitle,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
