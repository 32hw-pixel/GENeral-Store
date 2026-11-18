package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.FeedbackAnalyticsManager
import com.example.util.FeedbackCategory
import com.example.util.FeedbackReportItem
import com.example.util.FeedbackSeverity
import com.example.util.FeedbackStatus
import com.example.util.SoundPoolManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeedbackModuleDialog(
    isCloudSignedIn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val reports by FeedbackAnalyticsManager.reports.collectAsState()
    val summary by FeedbackAnalyticsManager.summary.collectAsState()
    val isSyncing by FeedbackAnalyticsManager.isSyncing.collectAsState()
    val lastSyncMessage by FeedbackAnalyticsManager.lastSyncMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.FEEDBACK) }
    var selectedSeverity by remember { mutableStateOf(FeedbackSeverity.MEDIUM) }
    var selectedRating by remember { mutableIntStateOf(5) }
    var selectedModule by remember { mutableStateOf(FeedbackAnalyticsManager.availableModuleTags.first()) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var kvkkApproved by remember { mutableStateOf(true) }
    var submitStatusMessage by remember { mutableStateOf<String?>(null) }
    var submitIsError by remember { mutableStateOf(false) }

    var analyticsFilter by remember { mutableStateOf<FeedbackCategory?>(null) }

    LaunchedEffect(Unit) {
        FeedbackAnalyticsManager.init(context)
        if (isCloudSignedIn) {
            FeedbackAnalyticsManager.syncWithCloud(context)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CyberYellow.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("feedback_module_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141102))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Feedback, contentDescription = "Geri Bildirim", tint = CyberYellow, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Geri Bildirim & Analiz Merkezi",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Hata, öneri, deneyim raporu ve KVKK uyumlu analiz",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("feedback_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                // Sub-Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0E0E12),
                    contentColor = CyberYellow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberYellow
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("feedback_tab_submit"),
                        text = { Text("📝 Bildir / Öner", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("feedback_tab_analytics"),
                        text = { Text("📊 Analiz (${summary.totalCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    // Tab 1: Submission Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("1. Kayıt Türü", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeedbackCategory.entries.forEach { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text("${cat.badgeEmoji} ${cat.labelTr}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberYellow,
                                        selectedLabelColor = CyberBlack,
                                        containerColor = CyberBlack,
                                        labelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("feedback_cat_${cat.wireValue}")
                                )
                            }
                        }

                        if (selectedCategory == FeedbackCategory.BUG || selectedCategory == FeedbackCategory.PERFORMANCE) {
                            Text("Önem / Öncelik Seviyesi", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FeedbackSeverity.entries.forEach { sev ->
                                    val selected = selectedSeverity == sev
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedSeverity = sev },
                                        label = { Text(sev.labelTr, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (sev == FeedbackSeverity.CRITICAL) NeonRed else CyberYellow,
                                            selectedLabelColor = if (sev == FeedbackSeverity.CRITICAL) Color.White else CyberBlack,
                                            containerColor = CyberBlack,
                                            labelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Text("2. Deneyim Puanı (1–5)", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = {
                                        selectedRating = star
                                        SoundPoolManager.playMechanicalClick()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("feedback_star_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star Yıldız",
                                        tint = if (star <= selectedRating) CyberYellow else TextMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$selectedRating / 5",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Text("3. İlgili Modül / Ekran", color = CyberYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeedbackAnalyticsManager.availableModuleTags.forEach { modTag ->
                                FilterChip(
                                    selected = selectedModule == modTag,
                                    onClick = { selectedModule = modTag },
                                    label = { Text(modTag, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = CyberBlack,
                                        containerColor = CyberBlack,
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { if (it.length <= 120) titleInput = it },
                            label = { Text("Kısa Başlık (Örn: Arama ekranı önerisi)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feedback_title_input")
                        )

                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { if (it.length <= 2000) descriptionInput = it },
                            label = { Text("Detaylı Açıklama / Yeniden Üretme Adımları") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feedback_description_input")
                        )

                        // KVKK Article 4/5/10 Minimization Notice
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1A14)),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "KVKK Veri Minimizasyonu Güvencesi",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Yalnızca yazdığınız metin, kategori, puan, uygulama sürümü ve genel Android SDK bilgisi işlenir. Konum, IP, IMEI veya özel mesajlar kesinlikle eklenmez.",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = kvkkApproved,
                                    onCheckedChange = { kvkkApproved = it },
                                    modifier = Modifier.testTag("feedback_kvkk_switch")
                                )
                            }
                        }

                        if (submitStatusMessage != null) {
                            Text(
                                text = submitStatusMessage!!,
                                color = if (submitIsError) NeonRed else NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val res = FeedbackAnalyticsManager.submitAndSyncReport(
                                        context = context,
                                        category = selectedCategory,
                                        severity = if (selectedCategory == FeedbackCategory.BUG || selectedCategory == FeedbackCategory.PERFORMANCE) selectedSeverity else FeedbackSeverity.LOW,
                                        rating = selectedRating,
                                        moduleTag = selectedModule,
                                        title = titleInput,
                                        description = descriptionInput,
                                        kvkkApproved = kvkkApproved
                                    )
                                    res.onSuccess {
                                        SoundPoolManager.playConfirm()
                                        submitIsError = false
                                        submitStatusMessage = if (it.syncedToCloud) {
                                            "Rapor (#${it.id.take(8)}) kaydedildi ve Firebase analiz deposuna gönderildi!"
                                        } else {
                                            "Rapor (#${it.id.take(8)}) cihaz analiz deposuna kaydedildi!"
                                        }
                                        titleInput = ""
                                        descriptionInput = ""
                                    }.onFailure { err ->
                                        submitIsError = true
                                        submitStatusMessage = err.localizedMessage ?: "Rapor gönderilemedi."
                                    }
                                }
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("feedback_submit_button")
                        ) {
                            Text(
                                text = if (isSyncing) "Kaydediliyor..." else "Raporu Kaydet & Gönder",
                                color = CyberBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Tab 2: Analyzable Dashboard & Export
                    val filteredReports = remember(reports, analyticsFilter) {
                        if (analyticsFilter == null) reports else reports.filter { it.category == analyticsFilter }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top KPI Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeedbackKpiCard(
                                label = "Toplam Rapor",
                                value = "${summary.totalCount}",
                                accent = CyberYellow,
                                modifier = Modifier.weight(1f)
                            )
                            FeedbackKpiCard(
                                label = "Ortalama Puan",
                                value = if (summary.totalCount == 0) "—" else String.format(Locale.US, "★ %.1f", summary.averageRating),
                                accent = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            FeedbackKpiCard(
                                label = "Açık Hata",
                                value = "${summary.openBugCount}",
                                accent = if (summary.openBugCount > 0) NeonRed else NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Category Distribution Breakdown Bars
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberBlack),
                            border = BorderStroke(1.dp, CyberCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kategori Dağılımı & Öncelik Özeti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                val totalSafe = summary.totalCount.coerceAtLeast(1)
                                FeedbackCategory.entries.forEach { cat ->
                                    val count = summary.categoryCounts[cat] ?: 0
                                    val ratio = count.toFloat() / totalSafe.toFloat()
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${cat.badgeEmoji} ${cat.labelTr}", color = TextSecondary, fontSize = 11.sp)
                                            Text("$count", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        LinearProgressIndicator(
                                            progress = { ratio },
                                            color = if (cat == FeedbackCategory.BUG) NeonRed else CyberYellow,
                                            trackColor = Color(0xFF1E222A),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                                summary.topModule?.let { topMod ->
                                    Text(
                                        text = "En Çok Raporlanan Modül: $topMod · Bulut Eşitli: ${summary.syncedCloudCount}/${summary.totalCount}",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Export & Cloud Sync Actions (for external analysis in Excel/BI/JSON)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val jsonPayload = FeedbackAnalyticsManager.exportAnalyticsJson(filteredReports)
                                    clipboardManager.setText(AnnotatedString(jsonPayload))
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "GENeral Chat Analiz Raporu (JSON)")
                                        putExtra(Intent.EXTRA_TEXT, jsonPayload)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "JSON Analiz Dışa Aktar"))
                                    Toast.makeText(context, "JSON analiz panoya kopyalandı & paylaşıma hazır.", Toast.LENGTH_SHORT).show()
                                },
                                border = BorderStroke(1.dp, CyberYellow),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("feedback_export_json_button"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("JSON Analiz", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val csvPayload = FeedbackAnalyticsManager.exportAnalyticsCsv(filteredReports)
                                    clipboardManager.setText(AnnotatedString(csvPayload))
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "GENeral Chat Analiz Tablosu (CSV)")
                                        putExtra(Intent.EXTRA_TEXT, csvPayload)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "CSV Analiz Dışa Aktar"))
                                    Toast.makeText(context, "CSV analiz tablosu panoya kopyalandı & paylaşıma hazır.", Toast.LENGTH_SHORT).show()
                                },
                                border = BorderStroke(1.dp, NeonCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("feedback_export_csv_button"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CSV İndir", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (isCloudSignedIn) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val res = FeedbackAnalyticsManager.syncWithCloud(context)
                                            res.onSuccess { cnt ->
                                                Toast.makeText(context, "$cnt rapor bulut ile eşitlendi.", Toast.LENGTH_SHORT).show()
                                            }.onFailure { err ->
                                                Toast.makeText(context, err.localizedMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, NeonGreen),
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = "Eşitle", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (!lastSyncMessage.isNullOrBlank()) {
                            Text(lastSyncMessage!!, color = TextMuted, fontSize = 10.sp)
                        }

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = analyticsFilter == null,
                                onClick = { analyticsFilter = null },
                                label = { Text("Tümü (${reports.size})", fontSize = 11.sp) }
                            )
                            FeedbackCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = analyticsFilter == cat,
                                    onClick = { analyticsFilter = if (analyticsFilter == cat) null else cat },
                                    label = { Text("${cat.badgeEmoji} ${cat.labelTr} (${summary.categoryCounts[cat] ?: 0})", fontSize = 11.sp) }
                                )
                            }
                        }

                        HorizontalDivider(color = CyberCardBorder)

                        if (filteredReports.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz analiz edilecek kayıt bulunmuyor.\n'Bildir / Öner' sekmesinden ilk kaydınızı oluşturabilirsiniz.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            filteredReports.forEach { item ->
                                FeedbackReportCard(
                                    item = item,
                                    onCycleStatus = {
                                        scope.launch {
                                            FeedbackAnalyticsManager.cycleReportStatus(context, item.id)
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            FeedbackAnalyticsManager.deleteReport(context, item.id)
                                            Toast.makeText(context, "Kayıt KVKK gereği silindi.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackKpiCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberBlack),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun FeedbackReportCard(
    item: FeedbackReportItem,
    onCycleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(item.createdAtMs) {
        SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(item.createdAtMs))
    }
    val statusColor = when (item.status) {
        FeedbackStatus.OPEN -> CyberYellow
        FeedbackStatus.IN_REVIEW -> NeonCyan
        FeedbackStatus.RESOLVED -> NeonGreen
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberBlack),
        border = BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${item.category.badgeEmoji} ${item.category.labelTr}", color = CyberYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("· ${item.moduleTag}", color = TextSecondary, fontSize = 10.sp)
                    if (item.syncedToCloud) {
                        Icon(Icons.Default.CloudDone, contentDescription = "Bulutla eşitli", tint = NeonGreen, modifier = Modifier.size(13.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .clickable { onCycleStatus() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(item.status.labelTr, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(item.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "★ ${item.rating}/5 · ${item.severity.labelTr} · v${item.appVersion} (${item.osVersion}) · $dateStr",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Kaydı Sil", tint = NeonRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
