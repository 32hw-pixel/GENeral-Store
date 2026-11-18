package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.LocalPaperMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import androidx.compose.runtime.DisposableEffect
import com.example.util.BackgroundMusicManager
import com.example.util.SoundPoolManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSupportScreen(
    onBack: () -> Unit,
    onWipeAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaper = LocalPaperMode.current
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showAiSupportDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var diagnosticCopied by remember { mutableStateOf(false) }

    // Roadmap Section 10: In About & Support, pause background music completely; only tok clicks remain active
    DisposableEffect(Unit) {
        val wasPlaying = BackgroundMusicManager.isPlaying.value
        if (wasPlaying) {
            BackgroundMusicManager.pause()
        }
        onDispose {
            if (wasPlaying) {
                BackgroundMusicManager.resume()
            }
        }
    }

    val bgColor = if (isPaper) PaperBg else CyberBlack
    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else CyberYellow
    val currentDensity = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hakkında & Cihaz Analizi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            onBack()
                        },
                        modifier = Modifier.testTag("about_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBg,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = bgColor,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor),
            contentAlignment = Alignment.TopCenter
        ) {
            val viewportWidthDp = maxWidth.value.toInt()
            val viewportHeightDp = maxHeight.value.toInt()
            val densityDpi = (currentDensity.density * 160f).toInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isPaper) PaperBorder else Color(0xFF0F1E36)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isPaper) PaperText else NeonCyan,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "GENeral Chat v7.7.3 Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Geliştirici: GENeralYTB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaper) PaperTextMuted else NeonPurple
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Türkiye Siber Operasyon Merkezi",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GENeral Donanım ve Sistem Tanıtım Vitrini
            com.example.ui.components.LenovoTechShowcasePlayer()

            Spacer(modifier = Modifier.height(16.dp))

            // DESTEKLE İLETİŞİME GEÇ (GEN AI Yüksek Zeka & YouTube Kanalı)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isPaper) PaperText else CyberYellow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isPaper) PaperBorder else CyberYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isPaper) PaperText else CyberYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Destekle İletişime Geç",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Yüksek zekalı GEN AI veya doğrudan geliştirici",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMutedColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Teknik sorunlar, güvenlik mimarisi veya soru işaretleriniz için analitik düşünen GEN AI Destek Asistanı 7/24 hizmetinizdedir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.9f),
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // GEN AI Yüksek Zeka Destek
                    Button(
                        onClick = {
                            SoundPoolManager.playConfirm()
                            showAiSupportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) PaperText else CyberYellow
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_gen_ai_support_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isPaper) PaperBg else CyberBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GEN AI Destek Asistanı (Yüksek Zeka)",
                            fontWeight = FontWeight.Black,
                            color = if (isPaper) PaperBg else CyberBlack,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resmi Destek Bileti Oluştur
                    Button(
                        onClick = {
                            SoundPoolManager.playConfirm()
                            showTicketDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) PaperSurface else Color(0xFF132238)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaper) PaperBorder else NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_support_ticket_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = if (isPaper) PaperText else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanı & Destek E-postası Hazırla",
                            fontWeight = FontWeight.Bold,
                            color = if (isPaper) PaperText else NeonCyan,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tanı Raporu Kopyala
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            SoundPoolManager.playConfirm()
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText(
                                "GENeral Chat Tanı Raporu",
                                """
                                ========================================
                                GENeral CHAT v7.7.3 Pro SİSTEM TANI RAPORU
                                ========================================
                                Cihaz Modeli: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}
                                Donanım: ${Build.BOARD} (${Build.HARDWARE})
                                Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                                Ekran: ${viewportWidthDp}x${viewportHeightDp} dp (DPI: ${densityDpi})
                                Çekirdek Sayısı: ${Runtime.getRuntime().availableProcessors()}
                                JVM RAM Maks: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB
                                Aktarım & Kasa: TLS (Bulut) + AES-256-CBC (Yerel Kasa)
                                Telemetri: 0 (Zero-Tracker)
                                Zaman Damgası: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                                ========================================
                                """.trimIndent()
                            )
                            clipboard.setPrimaryClip(clip)
                            diagnosticCopied = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) PaperSurface else Color(0xFF0F1E32)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaper) PaperBorder else Color(0xFF284872)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("copy_diagnostics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (isPaper) PaperTextMuted else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (diagnosticCopied) "✓ Tanı Raporu Panoya Kopyalandı!" else "Cihaz Tanı & Güvenlik Raporunu Kopyala",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPaper) PaperText else Color.White,
                            fontSize = 11.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Geliştirici YouTube
                    Button(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://youtube.com/@GENeralYTB?si=agYQ6GON7Hrqe2vx")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) Color(0xFF6B1D2F) else NeonRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_developer_youtube_button")
                    ) {
                        Text(
                            text = "▶ Geliştirici YouTube Kanalı (@GENeralYTB)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "İleride doğrudan e-posta ve özel hat entegre edilecektir. Şu an tüm geliştirme güncellemeleri YouTube kanalımızdan paylaşılmaktadır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMutedColor,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TANITIM KILAVUZU AKORDİYON BÖLÜMÜ
            SectionCard(
                title = "Tanıtım Kılavuzu",
                icon = Icons.Default.Security,
                isPaper = isPaper,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            ) {
                val faqs = listOf(
                    "Mesaj güvenliği ve veri saklama nasıl çalışır?" to "Bulut sohbetleri HTTPS/TLS ile aktarılır ve Google Cloud altyapısında korunur; sesli/görüntülü WebRTC aramaları DTLS-SRTP kullanır. İsteğe bağlı yerel sohbet dışa aktarımları belirlediğiniz parolayla AES-256 olarak cihazınızda şifrelenir.",
                    "Karşı tarafla mesajlaşırken siyah ekran hatası çözüldü mü?" to "Evet! Ekran yakalama koruma modu akıllı yapılandırmaya taşındı. Cihaz ID'si girip 'Mesaj At' dediğinizde doğrudan mesajlaşma ekranı sıfır gecikmeyle açılır.",
                    "Zamanlanmış mesajlar nasıl çalışır?" to "Doğrudan mesajlaşma ekranında saat simgesine dokunarak ileri tarih ve saat seçebilirsiniz. Belirlenen vakit geldiğinde iletiniz arka planda otomatik olarak şifrelenip gönderilir.",
                    "Kağıt Ekran (Paper Mode) ne sağlar?" to "OLED ve LCD mavi ışık spektrumunu sıfırlayarak dijital ekranınızı mat bir e-ink kitap sayfasına dönüştürür. Göz yorgunluğunu önler ve meraklı gözlerden gizler.",
                    "Panik butonu bastığımda ne olur?" to "Anında kill-switch devreye girer. Aktif oturum bellekten sıfırlanır ve uygulama anında kilit ekranına fırlatılır."
                )

                faqs.forEachIndexed { index, (question, answer) ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPaper) PaperSurface else Color(0xFF0C1420))
                            .border(1.dp, if (isExpanded) accentColor else borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                isExpanded = !isExpanded
                                SoundPoolManager.playMenuNav()
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = textMutedColor,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    if (index < faqs.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İŞLETİM SİSTEMİ BİLGİSİ
            SectionCard(
                title = "İşletim Sistemi Bilgisi",
                icon = Icons.Default.Build,
                isPaper = isPaper,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            ) {
                DossierRow("İşletim Sistemi", "Android ${Build.VERSION.RELEASE}", isPaper)
                DossierDivider(borderColor)
                DossierRow("API Düzeyi", "API ${Build.VERSION.SDK_INT}", isPaper)
                DossierDivider(borderColor)
                DossierRow("Cihaz Modeli", "${Build.MANUFACTURER} ${Build.MODEL}", isPaper)
                DossierDivider(borderColor)
                DossierRow("Uygulama Sürümü", "GENeral Chat 7.7.3 Pro", isPaper)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. SİBER ŞİFRELEME & SIFIR GÖZETİM MİMARİSİ
            SectionCard(
                title = "Kriptografi & Sıfır Gözetim Mimarisi",
                icon = Icons.Default.Security,
                isPaper = isPaper,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            ) {
                DossierRow("Aktarım & Arşiv", "TLS Aktarım + Yerel AES-256 Arşiv", isPaper)
                DossierDivider(borderColor)
                DossierRow("Şifre Güvenliği", "PBKDF2WithHmacSHA256 (10.000 İterasyon)", isPaper)
                DossierDivider(borderColor)
                DossierRow("Yerel Veritabanı", "SQLite Room (Cihaz İçi Veri Deposu)", isPaper)
                DossierDivider(borderColor)
                DossierRow("Telemetri & İzleyiciler", "SIFIR (Zero-Tracker, Reklamsız, Telemetrisiz)", isPaper)
                DossierDivider(borderColor)
                DossierRow("2 Cihaz İletişimi", "Bulut (Firebase Firestore) + Yerel Ağ Companion Sunucusu", isPaper)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. SENSÖRLER & KAMUFLAJ SAVUNMASI
            SectionCard(
                title = "Sensörler & Kamuflaj Savunma Mekanizması",
                icon = Icons.Default.Sensors,
                isPaper = isPaper,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            ) {
                DossierRow("İvmeölçer (Accelerometer)", "Sallama Algılama (>2.2G - Anında GENeral Chat Override)", isPaper)
                DossierDivider(borderColor)
                DossierRow("Taktil Haptik Motoru", "Mekanik Tok Anahtar ve Geri Bildirim Titreşimi", isPaper)
                DossierDivider(borderColor)
                DossierRow("10 Ekran Maskesi", "Uygulama İçi Ekran Kalkanları (Hesap Makinesi, Hava Durumu, Notlar)", isPaper)
                DossierDivider(borderColor)
                DossierRow("Yapay Zeka Kamuflajı", "Özel Decoy ve Gizli Tetikleyici Tasarımcısı", isPaper)
                DossierDivider(borderColor)
                DossierRow("Kağıt Ekran (Paper Mode)", "Mavi Işığı Azaltan Mat Okuma Teması", isPaper)
                DossierDivider(borderColor)
                DossierRow("3 Siber Mini Oyun", "Subway Surfers Tarzı Tren Koşucusu, Flappy Bird, XOX", isPaper)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BÖLÜM: TÜM VERİLERİMİ SİL (KESİN İMHA)
            Button(
                onClick = {
                    SoundPoolManager.playMenuNav()
                    showWipeConfirmDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaper) Color(0xFF8B0000) else NeonRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("wipe_all_data_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tüm Verilerimi Sil (Kesin İmha)",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tüm yerel veritabanı, oturum anahtarları ve şifreli iletiler tek hamlede tamamen yok edilir.",
                style = MaterialTheme.typography.labelSmall,
                color = textMutedColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            }
        }
    }

    // WIPE CONFIRM DIALOG
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = {
                Text(
                    text = "TÜM VERİLERİ SİL (KESİN İMHA)",
                    fontWeight = FontWeight.Black,
                    color = if (isPaper) Color(0xFF8B0000) else NeonRed
                )
            },
            text = {
                Text(
                    text = "DİKKAT: Bu işlem geri alınamaz!\n\nTüm hesaplar, şifreli mesajlar, kilit şifresi ve veritabanı kalıcı olarak sıfırlanacaktır. Devam etmek istiyor musunuz?",
                    color = textColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeConfirmDialog = false
                        SoundPoolManager.playPanicWipe()
                        onWipeAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaper) Color(0xFF8B0000) else NeonRed
                    )
                ) {
                    Text("EVET, TÜMÜNÜ İMHA ET", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text("İptal", color = textMutedColor)
                }
            },
            containerColor = cardBg
        )
    }

    // GEN AI SUPPORT DIALOG
    if (showAiSupportDialog) {
        GenAiSupportDialog(
            isPaper = isPaper,
            onDismiss = { showAiSupportDialog = false }
        )
    }

    // SUPPORT TICKET DIALOG
    if (showTicketDialog) {
        SupportTicketDialog(
            isPaper = isPaper,
            onDismiss = { showTicketDialog = false }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    isPaper: Boolean,
    cardBg: Color,
    borderColor: Color,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPaper) PaperText else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun DossierRow(label: String, value: String, isPaper: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isPaper) PaperTextMuted else TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
    }
}

@Composable
private fun DossierDivider(color: Color) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = color)
}

@Composable
private fun GenAiSupportDialog(
    isPaper: Boolean,
    onDismiss: () -> Unit
) {
    var userQuestion by remember { mutableStateOf("") }
    val supportMessages = remember {
        mutableStateListOf(
            Pair(
                false,
                "GEN AI Destek Merkezi (v7.7.3 Pro - Yüksek Zekalı Muhakeme Çekirdeği) devrede.\n\n" +
                "Siberpunk şifreleme, kamuflaj maskeleri, 2 cihaz arası mesajlaşma, sıfırlama veya hesap güvenliğiyle ilgili tüm sorularınızı adım adım çözebilirim. Sorunuzu aşağıya yazabilirsiniz."
            )
        )
    }

    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else CyberYellow

    fun handleSend(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        supportMessages.add(Pair(true, q))
        userQuestion = ""
        SoundPoolManager.playConfirm()

        val response = when {
            q.contains("cihaz", ignoreCase = true) || q.contains("mesaj", ignoreCase = true) ->
                "【DERİNLEMESİNE ANALİZ - 2 CİHAZ ARASI MESAJLAŞMA】\n\n" +
                "1. Sohbetler sekmesinde sağ üstteki '+' tuşuna dokunarak karşı tarafın cihaz kodunu (örneğin 'Cihaz 2' veya telefon numarasını) ekleyin.\n" +
                "2. Açılan doğrudan mesajlaşma penceresinde tüm iletiler bulut (TLS) veya aynı Wi-Fi üzerindeki Companion sunucusu üzerinden anında iletilir.\n" +
                "3. Bulut sohbetleri hesap güvenliği kurallarıyla Firestore üzerinde saklanır; yerel şifreli arşivler cihazınızda tutulur."

            q.contains("kağıt", ignoreCase = true) || q.contains("paper", ignoreCase = true) ->
                "【DERİNLEMESİNE ANALİZ - KAĞIT EKRAN (PAPER MODE)】\n\n" +
                "Kağıt Ekran modu, dijital OLED/LCD piksellerini mat e-ink mikro-kapsül renk uzayına (#F2EFE9 ve #1A1A18) dönüştürür. Mavi ve neon ışığı sıfırlayarak karanlıkta mutlak göz konforu ve gizlilik sağlar. Ayarlar veya Sohbet ekranındaki kitap/ekran simgesinden anında geçiş yapabilirsiniz."

            q.contains("maske", ignoreCase = true) || q.contains("kamuflaj", ignoreCase = true) || q.contains("sallama", ignoreCase = true) ->
                "【DERİNLEMESİNE ANALİZ - GİZLİLİK MASKELERİ VE SALLAMA】\n\n" +
                "10 farklı ekran koruma maskesi (Hesap Makinesi, Hava Durumu, Notlar, Pusula vb.) ile uygulama içi ekran görünümünü gizleyebilirsiniz (Android sistem ikonu ve resmi adı korunur).\n" +
                "Ayrıca 'AI Maske Tasarla' seçeneği ile özel ekran profili üretebilirsiniz.\n" +
                "Maskedeyken telefonu salladığınızda (Accelerometer 2.2G) sistem anında GENeral Chat'e döner."

            q.contains("oyun", ignoreCase = true) ->
                "【SİBER MİNİ OYUNLAR】\n\n" +
                "Ayarlar menüsünden 3 adet çevrimdışı mini oyuna erişebilirsiniz:\n" +
                "1. Cyber Runner: Subway Surfers tarzı 3 şeritli demiryolu tren kaçışı, atlama ve kayma mekaniği.\n" +
                "2. Cyber Bird: Flappy Bird tarzı lazer engelleri arasından süzülme.\n" +
                "3. Cyber XOX: Akıllı yapay zeka veya 2 kişilik karşılıklı strateji."

            q.contains("şifre", ignoreCase = true) || q.contains("kilit", ignoreCase = true) ->
                "【GÜVENLİK VE ŞİFRE KURTARMA】\n\n" +
                "Kayıt e-postanız ile 'Şifremi Unuttum' ekranından 6 haneli siber onay kodunu alarak şifrenizi yenileyebilirsiniz. Ayrıca cihaz bazlı 4 haneli PIN kilit ekranı uygulayabilirsiniz."

            else ->
                "【GEN AI YÜKSEK MUHAKEME YANITI】\n\n" +
                "Sorunuzu sistem çekirdeğinde analiz ettim: '$q'\n\n" +
                "GENeral Chat v7.7.3 Pro üzerinde tüm verileriniz sıfır gözetim (Zero-Surveillance) ilkesiyle korunmaktadır. Geliştiriciyle doğrudan iletişime geçmek için lütfen YouTube (@GENeralYTB) kanalımızı ziyaret edin veya Acil Durum Panik butonunu kullanarak tüm izleri anında yok edin."
        }
        supportMessages.add(Pair(false, response))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GEN AI Destek Merkezi",
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Yüksek Muhakeme Modu (Gelişmiş Zeka)",
                        fontWeight = FontWeight.Normal,
                        color = if (isPaper) PaperTextMuted else NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Quick chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("2 Cihaz", "Kağıt Ekran", "Maske & Sallama", "Oyunlar").forEach { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaper) PaperBorder else CyberCardBorder)
                                .clickable { handleSend(chip) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 11.sp,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPaper) PaperBg else CyberBlack)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(supportMessages) { item ->
                        val isUser = item.first
                        val text = item.second
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = if (isUser) "SEN" else "GEN AI DESTEK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) accentColor else (if (isPaper) PaperText else NeonCyan)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isUser) (if (isPaper) PaperSurface else CyberCardBg)
                                        else (if (isPaper) PaperSurface else Color(0xFF0F1E32))
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) accentColor.copy(alpha = 0.5f) else (if (isPaper) PaperBorder else NeonCyan.copy(alpha = 0.5f)),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    color = textColor,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("Sorunuzu yazın...", fontSize = 12.sp, color = textMutedColor) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { handleSend(userQuestion) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gönder",
                            tint = if (isPaper) PaperBg else CyberBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Kapat", color = if (isPaper) PaperBg else CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = cardBg
    )
}

@Composable
private fun SupportTicketDialog(
    isPaper: Boolean,
    onDismiss: () -> Unit
) {
    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else NeonCyan

    val context = LocalContext.current
    var category by remember { mutableStateOf("TLS & Veri Gizliliği") }
    var priority by remember { mutableStateOf("Yüksek") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var preparedSummary by remember { mutableStateOf<String?>(null) }

    val categories = listOf("TLS & Veri Gizliliği", "2 Cihaz Bağlantısı", "Kamuflaj & Maskeler", "Zamanlanmış Mesajlar", "Performans & Akıcılık", "Diğer")
    val priorities = listOf("Düşük", "Normal", "Yüksek", "Kritik Acil")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tanı & Destek E-postası",
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Cihaz E-posta İstemcisi ve Pano Aktarımı",
                        color = textMutedColor,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            if (preparedSummary != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(2.dp, NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 28.sp, color = NeonGreen, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DESTEK ÖZETİ HAZIRLANDI",
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Uygulamada otomatik uzak bilet sunucusu bulunmadığı için destek metniniz cihaz tanı bilgileriyle birlikte panoya kopyalandı ve e-posta uygulamanız yönlendirildi.",
                        color = textMutedColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Kategori:",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMutedColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (category == cat) accentColor else (if (isPaper) PaperBorder else CyberDarkSurface))
                                    .clickable {
                                        category = cat
                                        SoundPoolManager.playMenuNav()
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (category == cat) (if (isPaper) PaperBg else CyberBlack) else textColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Öncelik:",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMutedColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        priorities.forEach { prio ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (priority == prio) (if (prio == "Kritik Acil") NeonRed else CyberYellow) else (if (isPaper) PaperBorder else CyberDarkSurface))
                                    .clickable {
                                        priority = prio
                                        SoundPoolManager.playMenuNav()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (priority == prio) CyberBlack else textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Konu Başlığı", fontSize = 12.sp) },
                        placeholder = { Text("Örn: Mesaj iletilirken zamanlama hatası", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Ayrıntılı Açıklama", fontSize = 12.sp) },
                        placeholder = { Text("Karşılaştığınız sorunu veya önerinizi açıklayın...", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (preparedSummary != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Tamam", color = if (isPaper) PaperBg else CyberBlack, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && description.isNotBlank()) {
                            SoundPoolManager.playConfirm()
                            val body = buildString {
                                appendLine("Kategori: $category")
                                appendLine("Öncelik: $priority")
                                appendLine("Konu: ${subject.trim()}")
                                appendLine()
                                appendLine("Açıklama:")
                                appendLine(description.trim())
                                appendLine()
                                appendLine("Cihaz: ${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                            }
                            runCatching {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("GENeral Chat Destek Özeti", body))
                            }
                            runCatching {
                                val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "[GENeral Chat Destek] ${subject.trim()}")
                                    putExtra(android.content.Intent.EXTRA_TEXT, body)
                                }
                                context.startActivity(emailIntent)
                            }
                            preparedSummary = body
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = subject.isNotBlank() && description.isNotBlank()
                ) {
                    Text("E-posta / Pano Oluştur", color = if (isPaper) PaperBg else CyberBlack, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (preparedSummary == null) {
                TextButton(onClick = onDismiss) {
                    Text("İptal", color = textMutedColor)
                }
            }
        },
        containerColor = cardBg
    )
}
