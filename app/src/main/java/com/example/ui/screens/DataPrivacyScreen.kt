package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreChatRepository
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.ParentalControlManager
import com.example.util.PrivacyPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(
    onBack: () -> Unit,
    onWipeLocalData: () -> Unit,
    onLoginClick: () -> Unit,
    isLoggedIn: Boolean = false,
    isCloudSignedIn: Boolean = false,
    currentDisplayName: String = "",
    currentEmail: String = "",
    onUpdateDisplayName: ((String, (Result<String>) -> Unit) -> Unit)? = null,
    onDeleteCloudAccount: (((Result<Unit>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val privacyRev by PrivacyPreferencesManager.revision.collectAsState()
    val globalDefaults = remember(privacyRev) {
        PrivacyPreferencesManager.getGlobalDefaults(context)
    }
    val parentalState by ParentalControlManager.state.collectAsState()

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Aydınlatma, 1: Madde 11, 2: Gizlilik & İmha
    var showConfirmLocalWipe by remember { mutableStateOf(false) }
    var showConfirmCloudDelete by remember { mutableStateOf(false) }

    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var isExportingJson by remember { mutableStateOf(false) }

    var editNameInput by remember(currentDisplayName) { mutableStateOf(currentDisplayName) }
    var isSavingName by remember { mutableStateOf(false) }
    var nameStatusMsg by remember { mutableStateOf<String?>(null) }

    var kvkkReqType by remember { mutableStateOf("ACCESS") }
    var kvkkContactEmail by remember(currentEmail) { mutableStateOf(currentEmail) }
    var kvkkDetails by remember { mutableStateOf("") }
    var isSubmittingKvkk by remember { mutableStateOf(false) }
    var kvkkFeedback by remember { mutableStateOf<String?>(null) }

    var isDeletingCloud by remember { mutableStateOf(false) }
    var cloudDeleteError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val jsonContent = pendingExportJson
        pendingExportJson = null
        if (uri != null && jsonContent != null) {
            scope.launch {
                val writeOk = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(jsonContent.toByteArray(Charsets.UTF_8))
                        } ?: throw IllegalStateException("Dosya akışı açılamadı.")
                    }
                }
                writeOk.onSuccess {
                    Toast.makeText(context, "KVKK veri dışa aktarımı JSON olarak kaydedildi.", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Kaydedilemedi: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun launchKvkkJsonExport() {
        isExportingJson = true
        scope.launch {
            val root = JSONObject().apply {
                put("standard", "6698_SAYILI_KVKK_MADDE_11_EXPORT")
                put("appName", "GENeral Chat")
                put("localProfile", JSONObject().apply {
                    put("displayName", currentDisplayName)
                    put("email", currentEmail)
                    put("isLoggedIn", isLoggedIn)
                    put("isCloudSignedIn", isCloudSignedIn)
                })
                put("privacyDefaults", JSONObject().apply {
                    put("blockScreenCapture", globalDefaults.blockScreenCapture)
                    put("blockExportAndForward", globalDefaults.blockExportAndForward)
                    put("blockAutoMediaSave", globalDefaults.blockAutoMediaSave)
                    put("excludeFromAi", globalDefaults.excludeFromAi)
                })
                put("parentalControls", JSONObject().apply {
                    put("ageYears", parentalState.ageYears ?: JSONObject.NULL)
                    put("isMinorMode", parentalState.isMinorMode)
                    put("dailyLimitMinutes", parentalState.dailyLimitMinutes)
                    put("parentEmail", parentalState.parentEmail ?: "")
                    put("isParentEmailVerified", parentalState.isParentEmailVerified)
                })
            }

            if (isCloudSignedIn) {
                val cloudRes = FirestoreChatRepository.create(context).exportUserCloudDataJson()
                cloudRes.onSuccess { cloudJsonStr ->
                    runCatching { JSONObject(cloudJsonStr) }.onSuccess { parsed ->
                        root.put("cloudData", parsed)
                    }
                }.onFailure { err ->
                    root.put("cloudDataError", err.localizedMessage ?: "Bulut verisi okunamadı")
                }
            } else {
                root.put("cloudData", "Google bulut oturumu açık değil; yalnızca bu cihazdaki veriler aktarıldı.")
            }

            isExportingJson = false
            pendingExportJson = root.toString(2)
            com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
            exportDocumentLauncher.launch("general_chat_kvkk_verilerim_${System.currentTimeMillis()}.json")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KVKK 6698 & APTOIDE UYUMLULUK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberYellow,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Gizlilik, KVKK ve Uyumluluk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("data_privacy_back_button")
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Aydınlatma",
                    1 to "Haklarım (Md.11)",
                    2 to "Gizlilik & İmha"
                ).forEach { (idx, label) ->
                    val selected = selectedSection == idx
                    FilterChip(
                        selected = selected,
                        onClick = { selectedSection = idx },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberYellow,
                            selectedLabelColor = CyberBlack,
                            containerColor = CyberCardBg,
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = CyberCardBorder,
                            selectedBorderColor = CyberYellow
                        ),
                        modifier = Modifier.testTag("kvkk_tab_$idx")
                    )
                }
            }

            when (selectedSection) {
                0 -> {
                    val developerYoutubeUrl = "https://youtube.com/@GENeralYTB?si=agYQ6GON7Hrqe2vx"
                    KvkkNoticeCard(
                        title = "1. Veri Sorumlusu ve İletişim Kanalları (Md. 10)",
                        accentColor = CyberYellow,
                        body = "Uygulama Adı: GENeral Chat\n" +
                            "Veri Sorumlusu: GENeralYTB (Bireysel Geliştirici)\n" +
                            "Adres / Kişisel E-posta Gizliliği: Bireysel geliştirici güvenliği nedeniyle fiziksel ikamet/açık adres veya kişisel e-posta adresi kamuya açık olarak paylaşılmamaktadır.\n" +
                            "Resmî İletişim & Başvuru Kanalları:\n" +
                            "• Uygulama İçi Elektronik KVKK Başvuru Portalı ('Haklarım (Md. 11)' sekmesi — doğrudan dijital başvuru, JSON dışa aktarım ve anında silme)\n" +
                            "• Resmî Geliştirici YouTube Kanalı & Topluluk İletişimi: @GENeralYTB (https://youtube.com/@GENeralYTB)\n\n" +
                            "KVKK Madde 4 uyarınca veriler hukuka ve dürüstlük kurallarına uygun, doğru ve güncel, belirli/meşru amaçlarla bağlantılı, sınırlı ve ölçülü olarak işlenir."
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(developerYoutubeUrl))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    "YouTube / tarayıcı uygulaması bulunamadı: @GENeralYTB",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("kvkk_youtube_channel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Geliştirici YouTube Kanalını Aç",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Geliştirici YouTube Kanalından İletişime Geç (@GENeralYTB)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    KvkkNoticeCard(
                        title = "2. İşlenen Kişisel Veri Kategorileri ve Amaçlar (Md. 4, 5, 6)",
                        accentColor = NeonCyan,
                        body = "• Kimlik ve Hesap Bilgileri (Md. 5/2-c Sözleşmenin İfası): Görünen ad, kullanıcı kimliği (@kullanici), e-posta adresi, Google OAuth oturum kimliği (UID).\n" +
                            "• İletişim İçerikleri ve Zaman Damgası (Md. 5/2-c): Bire bir/grup sohbet mesajları, dosya/medya ekleri, okundu/teslim bilgisi ve saat dilimine uyumlu tarih kayıtları.\n" +
                            "• Çevrimdışı (Offline-First) Kuyruk ve Yerel Önbellek: İnternet kesildiğinde gönderilmeyi bekleyen mesajlar ile son konuşma özetleri cihaz içi depolamada tutulur ve bağlantı döndüğünde otomatik eşitlenir.\n" +
                            "• Gerçek Zamanlı Ses & Görüntü (Md. 5/2-c): WebRTC aramaları sırasında çağrı başlatma, ICE aday değişimi ve medya iletimi.\n" +
                            "• Yapay Zekâ (GEN AI) Etkileşimi: Kullanıcı soruyu açıkça gönderdiğinde GEN AI yanıtı üretmek için işlenir. Özel gizlilik açılan odalar bağlama dahil edilmez.\n" +
                            "• Güvenlik, Kamuflaj & Ebeveyn Denetimi (Md. 5/2-f Meşru Menfaat): Uygulama kilidi (rastgele tuzlu SHA-256), uygulama içi görünüm kamuflajı (sistem ikonu değiştirilmez), küfür filtresi, kötüye kullanım şikayeti, engelleme listesi ve 18 yaş altı kullanım süresi sınırı.\n" +
                            "• Özel Nitelikli Veriler (Md. 6): Biyometrik veri şablonları uygulama sunucusuna kaydedilmez; sağlık, siyasi görüş veya özel nitelikli veri toplanmaz."
                    )

                    KvkkNoticeCard(
                        title = "3. Aktarım, Yurt Dışı Alıcılar ve Açık Rıza (Md. 8, 9)",
                        accentColor = NeonGreen,
                        body = "• Google Firebase (Firestore, Authentication, App Check, Vertex/Firebase AI): Bulut sohbet senkronizasyonu ve GEN AI yanıtları için kullanılır; veriler Google Cloud sunucularında işlenebilir (KVKK Md. 9 kapsamında yalnızca kullanıcı Google ile oturum açtığında veya GEN AI'ye ileti gönderdiğinde devreye girer).\n" +
                            "• WebRTC Medya Akışı: Ses ve görüntü paketleri DTLS-SRTP ile aktarılır.\n" +
                            "• Yerel Ağ (Wi-Fi / Hotspot LAN) & NSD Modu: Kullanıcı sunucuyu kendi açmadıkça yerel ağda yayın (_generalchat._tcp.) veya 8765 portu dinlemesi yapılmaz; bağlantılar yalnızca özel yerel ağ IP'leriyle (LAN) sınırlıdır.\n" +
                            "• İnternet Web Araması: GEN AI açık bilgi sorularında yalnızca başlık/özet amaçlı Wikipedia ve DuckDuckGo anlık özeti sorgulanabilir; kişisel sohbet geçmişi arama motoruna gönderilmez."
                    )

                    KvkkNoticeCard(
                        title = "4. Veri Güvenliği, E2EE Şifreleme ve İhlal Bildirimi (Md. 7, 12)",
                        accentColor = CyberYellow,
                        body = "• Uçtan Uca Şifreleme (E2EE): Bire bir bulut mesajlaşmalarında ECDH P-256 anahtar değişimi ve AES-256-GCM şifrelemesi cihaz içinde uygulanır; metin mesajları sunucuda yalnızca şifreli (E2EE:v1:...) biçimde saklanır ve sunucu yöneticilerince okunamaz.\n" +
                            "• Aktarım ve Ağ Güvenliği: Tüm internet/bulut trafiğinde HTTPS/TLS zorunludur (network_security_config ile genel ağda şifresiz HTTP kapalıdır); yerel 4 haneli PIN ve kilit parolaları tuzlu SHA-256 ile korunur.\n" +
                            "• Bulut Yedekleme Engeli (allowBackup=false): Özel mesaj kuyruğu, E2EE özel anahtarı, Room veritabanı ve güvenlik tercihleri Google Auto Backup / cihaz aktarımına dahil edilmez.\n" +
                            "• Grup & Eşleşmemiş Sohbet Notu: Grup odaları veya karşı tarafın henüz E2EE ortak anahtarını yayınlamadığı oturumlarda iletişim Google Cloud/TLS koruması altında aktarılır.\n" +
                            "• İhlal Bildirimi (Md. 12/5): Kişisel verilerin kanuni olmayan yollarla başkalarınca elde edilmesi hâlinde durum gecikmeksizin ilgilisine ve Kişisel Verileri Koruma Kuruluna bildirilir.\n" +
                            "• Aptoide & Mağaza Politika Uyumu: Gizli arka plan veri toplama veya gereksiz depolama izni içermez; tüm hassas izinler (Kamera, Mikrofon, Konum, Bildirim) yalnızca özellik kullanılırken çalışma zamanında sorulur ve galeri seçimi Android Photo Picker ile izinsiz yapılır."
                    )
                }

                1 -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberYellow.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verilere Erişim & JSON Dışa Aktarım (Md. 11/1-a,b)", fontWeight = FontWeight.Bold, color = CyberYellow, fontSize = 14.sp)
                            }
                            Text(
                                text = "Hakkınızda tutulan profil, gizlilik tercihleri, ebeveyn ayarları ve bulut oturumunuz açıksa Firestore kullanıcı/sohbet/engelleme kayıtlarınızı okunabilir JSON dosyası olarak cihazınıza indirin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { launchKvkkJsonExport() },
                                enabled = !isExportingJson,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("kvkk_export_json_button")
                            ) {
                                if (isExportingJson) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CyberBlack, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Kişisel Verilerimi JSON İndir", color = CyberBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Veri Düzeltme Hakkı (Md. 11/1-d)", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 14.sp)
                            }
                            Text(
                                text = "Eksik veya hatalı görünen görünen ad bilginizi doğrudan yerel veritabanında ve bulut profilinizde güncelleyin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            OutlinedTextField(
                                value = editNameInput,
                                onValueChange = { editNameInput = it },
                                label = { Text("Görünen Ad / Ad Soyad") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("kvkk_display_name_input")
                            )
                            Button(
                                onClick = {
                                    val handler = onUpdateDisplayName
                                    if (handler == null || !isLoggedIn) {
                                        nameStatusMsg = "Düzeltme kaydı için önce Google veya yerel hesabınızla giriş yapın."
                                        return@Button
                                    }
                                    isSavingName = true
                                    nameStatusMsg = null
                                    handler(editNameInput) { res ->
                                        isSavingName = false
                                        res.onSuccess { updated ->
                                            nameStatusMsg = "Profil adınız güncellendi: $updated"
                                        }.onFailure { err ->
                                            nameStatusMsg = err.localizedMessage ?: "Güncelleme başarısız."
                                        }
                                    }
                                },
                                enabled = !isSavingName && editNameInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("kvkk_save_name_button")
                            ) {
                                Text(
                                    text = if (isSavingName) "Güncelleniyor..." else "Adımı Düzelt ve Kaydet",
                                    color = CyberBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (nameStatusMsg != null) {
                                Text(nameStatusMsg!!, fontSize = 12.sp, color = NeonGreen)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resmi KVKK Md. 11 Başvurusu Oluştur", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Text(
                                text = "KVKK Md. 11 kapsamındaki bilgi alma, düzeltme, silme veya itiraz taleplerinizi Google oturumu ile doğrudan bulut kaydına (kvkk_requests) iletebilir; çevrimdışı iseniz metni kopyalayıp @GENeralYTB YouTube kanalı üzerinden geliştiriciyle paylaşabilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "ACCESS" to "Erişim",
                                    "CORRECTION" to "Düzeltme",
                                    "DELETION" to "Silme",
                                    "OBJECTION" to "İtiraz"
                                ).forEach { (key, title) ->
                                    FilterChip(
                                        selected = kvkkReqType == key,
                                        onClick = { kvkkReqType = key },
                                        label = { Text(title, fontSize = 10.sp) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = kvkkContactEmail,
                                onValueChange = { kvkkContactEmail = it },
                                label = { Text("İsteğe Bağlı Yanıt E-postası / Kullanıcı Adı") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberYellow,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = kvkkDetails,
                                onValueChange = { kvkkDetails = it },
                                label = { Text("Talep Açıklaması (Md. 11)") },
                                minLines = 2,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberYellow,
                                    unfocusedBorderColor = CyberCardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("kvkk_request_details_input")
                            )
                            Button(
                                onClick = {
                                    if (kvkkDetails.isBlank()) {
                                        kvkkFeedback = "Lütfen talep açıklamasını yazın."
                                        return@Button
                                    }
                                    if (isCloudSignedIn) {
                                        isSubmittingKvkk = true
                                        kvkkFeedback = null
                                        scope.launch {
                                            val res = FirestoreChatRepository.create(context).submitKvkkRequest(
                                                requestType = kvkkReqType,
                                                details = kvkkDetails,
                                                contactEmail = kvkkContactEmail
                                            )
                                            isSubmittingKvkk = false
                                            res.onSuccess { reqId ->
                                                kvkkDetails = ""
                                                kvkkFeedback = "Başvurunuz veritabanına kaydedildi. Kayıt No: $reqId"
                                            }.onFailure { err ->
                                                kvkkFeedback = err.localizedMessage ?: "Başvuru kaydedilemedi."
                                            }
                                        }
                                    } else {
                                        val petitionText = "GENeral Chat KVKK Madde 11 Başvurusu ($kvkkReqType)\n" +
                                            "Yanıt İletişimi: ${kvkkContactEmail.ifBlank { "Belirtilmedi" }}\n" +
                                            "Açıklama:\n$kvkkDetails"
                                        runCatching {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("KVKK Md.11 Başvurusu", petitionText))
                                        }
                                        kvkkFeedback = "Talep metni panoya kopyalandı. Doğrudan kayıt için Google ile bağlanabilir veya aşağıdaki @GENeralYTB YouTube kanalından geliştiriciye ulaşabilirsiniz."
                                    }
                                },
                                enabled = !isSubmittingKvkk,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("kvkk_submit_request_button")
                            ) {
                                Icon(
                                    imageVector = if (isCloudSignedIn) Icons.Default.Send else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = CyberBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSubmittingKvkk) "Gönderiliyor..." else if (isCloudSignedIn) "Bulut Sistemine Başvur" else "Başvuru Metnini Kopyala",
                                    color = CyberBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val ytIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://youtube.com/@GENeralYTB?si=agYQ6GON7Hrqe2vx")
                                    )
                                    try {
                                        context.startActivity(ytIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "YouTube uygulaması bulunamadı: @GENeralYTB", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.75f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("kvkk_portal_youtube_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "YouTube İletişim Kanalı", tint = NeonRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Geliştirici YouTube Kanalına Git (@GENeralYTB)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            if (kvkkFeedback != null) {
                                Text(kvkkFeedback!!, fontSize = 12.sp, color = NeonGreen)
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Gelişmiş Sohbet Gizliliği Varsayılanları",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                            Text(
                                text = "Bu kurallar tüm sohbet odalarında varsayılan koruma olarak uygulanır. Her sohbetin profil menüsünden oda bazlı ek kilit de açılabilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                            listOf(
                                Triple(
                                    "Ekran görüntüsü ve ekran kaydı engeli (FLAG_SECURE)",
                                    globalDefaults.blockScreenCapture
                                ) { v: Boolean ->
                                    PrivacyPreferencesManager.setGlobalDefaults(context, globalDefaults.copy(blockScreenCapture = v))
                                },
                                Triple(
                                    "Sohbet dışa aktarma ve mesaj iletmeyi kilitle",
                                    globalDefaults.blockExportAndForward
                                ) { v: Boolean ->
                                    PrivacyPreferencesManager.setGlobalDefaults(context, globalDefaults.copy(blockExportAndForward = v))
                                },
                                Triple(
                                    "Medya dosyalarını cihaza otomatik kaydetme",
                                    globalDefaults.blockAutoMediaSave
                                ) { v: Boolean ->
                                    PrivacyPreferencesManager.setGlobalDefaults(context, globalDefaults.copy(blockAutoMediaSave = v))
                                },
                                Triple(
                                    "Sohbet geçmişini GEN AI analizinden hariç tut",
                                    globalDefaults.excludeFromAi
                                ) { v: Boolean ->
                                    PrivacyPreferencesManager.setGlobalDefaults(context, globalDefaults.copy(excludeFromAi = v))
                                }
                            ).forEach { (title, checked, onChanged) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )
                                    Switch(
                                        checked = checked,
                                        onCheckedChange = onChanged,
                                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Bu cihazdaki uygulama verilerini sil",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bu işlem yalnızca bu cihazdaki yerel Room veritabanını, görünüm ayarlarını, kilit hash'ini ve önbelleği temizler. Bulut hesabınızı silmez.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showConfirmLocalWipe = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("wipe_device_data_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Text(
                                    text = "Bu cihazın verilerini sil",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hesabımı ve bulut verilerimi kalıcı sil (Md. 7 & Md. 11)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Firebase Authentication hesabınızı, bulut kullanıcı profilinizi (@kullanıcı index'i dahil), bağlı cihazlarınızı, engelleme listenizi ve KVKK kayıtlarınızı kalıcı olarak siler. Not: Çok kullanıcılı sohbetlerde karşı tarafın gördüğü mevcut mesaj kopyaları hemen kaldırılmayabilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            if (!isCloudSignedIn) {
                                OutlinedButton(
                                    onClick = onLoginClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberYellow),
                                    border = BorderStroke(1.dp, CyberYellow.copy(alpha = 0.6f)),
                                    modifier = Modifier.testTag("login_first_button")
                                ) {
                                    Text(
                                        text = "Google Bulut Hesabı ile Bağlan",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        cloudDeleteError = null
                                        showConfirmCloudDelete = true
                                    },
                                    enabled = !isDeletingCloud,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("delete_cloud_account_button")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isDeletingCloud) "Siliniyor..." else "Hesabı ve Bulut Verilerini Sil",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            if (cloudDeleteError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(cloudDeleteError!!, color = NeonRed, fontSize = 12.sp)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Silme seçenekleri arasındaki fark",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberYellow
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Cihaz verilerini sil: Yalnızca bu cihazdaki yerel ayar, şifre ve önbelleği temizler.\n" +
                                    "• Bulut hesabını sil: Firebase Authentication ve Firestore üzerindeki kullanıcı kayıtlarınızı kalıcı siler.\n" +
                                    "• GEN AI sohbetini temizle: GEN AI ekranındaki 'Sohbeti Temizle' düğmesini kullanın.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberCardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("return_to_settings_button")
            ) {
                Text(
                    text = "Ayarlara dön",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showConfirmLocalWipe) {
        AlertDialog(
            onDismissRequest = { showConfirmLocalWipe = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Bu Cihazın Verilerini Sil?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Yerel oturum, önbellek ve kaydedilmiş tercihler temizlenecek. Uygulama fabrika ayarlarına döndürülecektir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmLocalWipe = false
                        onWipeLocalData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.testTag("confirm_wipe_local_button")
                ) {
                    Text(
                        text = "Evet, Cihazı Temizle",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLocalWipe = false }) {
                    Text(text = "Vazgeç", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showConfirmCloudDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmCloudDelete = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = NeonRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Bulut Hesabını Kalıcı Sil?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Bu işlem geri alınamaz! Google bulut hesabınızdan oluşturulan profiliniz, @kullanıcı kaydınız, cihaz eşleşmeleriniz ve bu cihazdaki tüm veriler kalıcı olarak silinecektir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCloudDelete = false
                        val handler = onDeleteCloudAccount
                        if (handler == null) {
                            cloudDeleteError = "Bulut silme işleyicisi bağlı değil."
                            return@Button
                        }
                        isDeletingCloud = true
                        cloudDeleteError = null
                        handler { res ->
                            isDeletingCloud = false
                            res.onFailure { err ->
                                cloudDeleteError = err.localizedMessage ?: "Hesap silinemedi."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.testTag("confirm_delete_cloud_button")
                ) {
                    Text("Kalıcı Olarak Sil", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCloudDelete = false }) {
                    Text("Vazgeç", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun KvkkNoticeCard(
    title: String,
    accentColor: Color,
    body: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = accentColor, fontSize = 13.sp)
            }
            HorizontalDivider(color = CyberCardBorder)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
