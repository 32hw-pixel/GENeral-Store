package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import com.example.data.defaultConversations
import com.example.util.EncryptedVaultUtil
import com.example.util.InstantNotificationManager
import com.example.util.VaultExportResult
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomAiDisguise
import com.example.data.MaskType
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.DisguiseManager
import com.example.util.RememberShakeDetector
import com.example.util.SoundPoolManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamouflageMaskScreen(
    maskType: MaskType,
    onSecretExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMask by remember { mutableStateOf(maskType) }
    var isInSelectorMode by remember { mutableStateOf(false) }
    var secretTapCount by remember { mutableIntStateOf(0) }

    val activeMaskState by DisguiseManager.activeMask.collectAsState()
    val customAiDisguise by DisguiseManager.customAiDisguise.collectAsState()
    val isPaper = com.example.ui.theme.LocalPaperMode.current

    // SHAKE DETECTOR: Shaking the device instantly exits camouflage and returns to GENeral Chat
    RememberShakeDetector {
        SoundPoolManager.playShakeOverride()
        Toast.makeText(context, "Sallama Algılandı: GENeral Chat Güvenli Moduna Geçildi", Toast.LENGTH_SHORT).show()
        onSecretExit()
    }

    fun handleSecretTap() {
        secretTapCount++
        SoundPoolManager.playMechanicalClick()
        if (secretTapCount >= 3) {
            SoundPoolManager.playShakeOverride()
            onSecretExit()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { handleSecretTap() }
                    ) {
                        Text(
                            text = if (isInSelectorMode) "Gizlilik Maskeleri & İkon Yönetimi" else currentMask.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (isPaper) PaperText else Color.White
                        )
                    }
                },
                navigationIcon = {
                    if (isInSelectorMode) {
                        IconButton(onClick = { isInSelectorMode = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = if (isPaper) PaperText else Color.White
                            )
                        }
                    } else {
                        // Discreet shake icon hint
                        IconButton(onClick = { handleSecretTap() }) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Sallama Koruması",
                                tint = if (isPaper) PaperTextMuted else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (!isInSelectorMode) {
                        // Switch disguise or open AI mask creator
                        IconButton(
                            onClick = {
                                SoundPoolManager.playMenuNav()
                                isInSelectorMode = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Maskeleri Değiştir",
                                tint = if (isPaper) PaperText else CyberYellow
                            )
                        }
                    }
                    // Secret corner exit
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                SoundPoolManager.playShakeOverride()
                                onSecretExit()
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPaper) PaperTextMuted else Color.Gray.copy(alpha = 0.4f))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isPaper) PaperSurface else Color(0xFF161616),
                    titleContentColor = if (isPaper) PaperText else Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isPaper) PaperBg else Color(0xFF101010))
        ) {
            if (isInSelectorMode) {
                MaskSelectorAndAiDesigner(
                    currentMask = currentMask,
                    customDisguise = customAiDisguise,
                    onSelectMask = { selected ->
                        currentMask = selected
                        DisguiseManager.activateMask(context, selected)
                        Toast.makeText(context, "${selected.title} başlatıcı ikonu ve adı aktifleştirildi", Toast.LENGTH_SHORT).show()
                        isInSelectorMode = false
                    },
                    onApplyAiDisguise = { aiDisguise ->
                        Toast.makeText(context, "Özel AI Maskesi: ${aiDisguise.title} devrede!", Toast.LENGTH_SHORT).show()
                        isInSelectorMode = false
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Discreet top banner explaining shake exit
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isPaper) PaperSurface else Color(0xFF1A1A1A))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isPaper) PaperTextMuted else CyberYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Telefonu salla veya başlığa 3 kez dokun -> Anında GENeral Chat",
                                    fontSize = 11.sp,
                                    color = if (isPaper) PaperTextMuted else TextSecondary
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentMask) {
                            MaskType.CALCULATOR -> CalculatorCamouflageView(isPaper = isPaper)
                            MaskType.WEATHER -> WeatherCamouflageView(isPaper = isPaper)
                            MaskType.COMPASS -> CompassCamouflageView(isPaper = isPaper)
                            MaskType.NOTES -> NotesCamouflageView(isPaper = isPaper)
                            MaskType.BATTERY -> BatteryCamouflageView(isPaper = isPaper)
                            MaskType.CALENDAR -> CalendarCamouflageView(isPaper = isPaper)
                            MaskType.CLOCK -> ClockCamouflageView(isPaper = isPaper)
                            MaskType.STOPWATCH -> StopwatchCamouflageView(isPaper = isPaper)
                            MaskType.DICTIONARY -> DictionaryCamouflageView(isPaper = isPaper)
                            MaskType.QR_SCANNER -> QrScannerCamouflageView(isPaper = isPaper)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 10 Mask Selector Grid + AI Custom Disguise Designer
 */
@Composable
fun MaskSelectorAndAiDesigner(
    currentMask: MaskType,
    customDisguise: CustomAiDisguise?,
    onSelectMask: (MaskType) -> Unit,
    onApplyAiDisguise: (CustomAiDisguise) -> Unit
) {
    var aiPromptInput by remember { mutableStateOf("") }
    var generatedAiDisguise by remember { mutableStateOf(customDisguise) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val isPaper = com.example.ui.theme.LocalPaperMode.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = if (isPaper) PaperSurface else CyberCardBg,
            contentColor = if (isPaper) PaperText else CyberYellow
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    SoundPoolManager.playMenuNav()
                    selectedTab = 0
                },
                text = { Text("10 Hazır Maske", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    SoundPoolManager.playMenuNav()
                    selectedTab = 1
                },
                text = { Text("✨ AI Özel", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    SoundPoolManager.playMenuNav()
                    selectedTab = 2
                },
                text = { Text("🔒 Şifreli .txt Aktar", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Direct banner for 10th Privacy Feature: Encrypted Chat Transcript Export
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else Color(0xFF1E1A04)),
                border = BorderStroke(1.2.dp, if (isPaper) PaperBorder else CyberYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        SoundPoolManager.playMenuNav()
                        selectedTab = 2
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPaper) PaperBorder else Color(0xFF262000))
                            .border(1.dp, if (isPaper) PaperText else CyberYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isPaper) PaperText else CyberYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "10. GİZLİLİK KALKANI: ŞİFRELİ METİN AKTARIMI",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isPaper) PaperText else CyberYellow
                        )
                        Text(
                            text = "Sohbet kayıtlarını yerel AES-256 şifreli metin dosyasına (.enc.txt) aktarın.",
                            fontSize = 10.sp,
                            color = if (isPaper) PaperTextMuted else TextSecondary
                        )
                    }
                    Button(
                        onClick = {
                            SoundPoolManager.playMenuNav()
                            selectedTab = 2
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperText else CyberYellow),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Aç", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isPaper) PaperBg else CyberBlack)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bir maske seçtiğinizde uygulama ikonu ve launcher adı o yardımcı araca dönüşür.",
                fontSize = 12.sp,
                color = if (isPaper) PaperTextMuted else TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MaskType.entries.toTypedArray()) { mask ->
                    val isSelected = mask == currentMask
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (isPaper) PaperBorder else Color(0xFF242200)
                            } else {
                                if (isPaper) PaperSurface else CyberCardBg
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CyberYellow else if (isPaper) PaperBorder else CyberCardBorder
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundPoolManager.playMechanicalClick()
                                onSelectMask(mask)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyberYellow else if (isPaper) PaperBg else Color(0xFF1E1E1E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getMaskIcon(mask),
                                    contentDescription = null,
                                    tint = if (isSelected) CyberBlack else if (isPaper) PaperText else CyberYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = mask.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isPaper) PaperText else Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = mask.subtitle,
                                fontSize = 10.sp,
                                color = if (isPaper) PaperTextMuted else TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isSelected) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (isPaper) PaperText else CyberYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Aktif Maske",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaper) PaperText else CyberYellow
                                    )
                                }
                            } else {
                                Text(
                                    text = "Uygula",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaper) PaperTextMuted else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // AI Mask Generator
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                        border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Yapay Zeka ile Özel Maske Tasarla",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isPaper) PaperText else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Aklınızdaki kamuflaj fikrini yazın (Örn: 'Borsa Takipçisi', 'Ders Notlarım', 'Yemek Tarifleri', 'Gitar Akort Cihazı'). GEN AI sizin için özel uygulama adı, ikonu ve gizli tetikleyici tasarlasın.",
                                fontSize = 12.sp,
                                color = if (isPaper) PaperTextMuted else TextSecondary,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = aiPromptInput,
                                onValueChange = { aiPromptInput = it },
                                placeholder = { Text("Örn: Borsa ve hisse takip programı...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberYellow,
                                    unfocusedBorderColor = if (isPaper) PaperBorder else CyberCardBorder,
                                    focusedTextColor = if (isPaper) PaperText else Color.White,
                                    unfocusedTextColor = if (isPaper) PaperText else Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (aiPromptInput.isNotBlank()) {
                                        generatedAiDisguise = DisguiseManager.generateAiDisguise(aiPromptInput)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberBlack
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("YAPAY ZEKA İLE ÜRET", fontWeight = FontWeight.Black, color = CyberBlack)
                            }
                        }
                    }
                }

                generatedAiDisguise?.let { disguise ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else Color(0xFF1E2838)),
                            border = BorderStroke(1.5.dp, CyberYellow),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ÜRETİLEN ÖZEL KAMUFLAJ PROFİLİ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberYellow,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = disguise.iconEmoji, fontSize = 34.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = disguise.title,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = if (isPaper) PaperText else Color.White
                                        )
                                        Text(
                                            text = disguise.subtitle,
                                            fontSize = 12.sp,
                                            color = if (isPaper) PaperTextMuted else TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Gizli Giriş Yöntemi: ${disguise.secretTrigger} veya telefonu salla",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        SoundPoolManager.playConfirm()
                                        onApplyAiDisguise(disguise)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("BU MASKELİ BAŞLATICIYI AKTİFLEŞTİR", fontWeight = FontWeight.Black, color = CyberBlack)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 10th Privacy Feature: Encrypted Chat Transcript Export
            EncryptedChatExportTab(isPaper = isPaper)
        }
    }
}

@Composable
private fun EncryptedChatExportTab(isPaper: Boolean) {
    val context = LocalContext.current
    var exportAll by remember { mutableStateOf(true) }
    var passphrase by remember { mutableStateOf("GENERAL_CHAT_MASTER_E2EE") }
    var exportResult by remember { mutableStateOf<VaultExportResult?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else CyberCardBg),
                border = BorderStroke(1.2.dp, if (isPaper) PaperBorder else CyberYellow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isPaper) PaperBorder else Color(0xFF262000))
                                .border(1.dp, if (isPaper) PaperText else CyberYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isPaper) PaperText else CyberYellow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sohbet Kayıtlarını Şifreli Dışa Aktar",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isPaper) PaperText else Color.White
                            )
                            Text(
                                text = "10 Gizlilik Maskeleme Özelliği · Yerel AES-256 Metin Dosyası (.txt)",
                                fontSize = 10.sp,
                                color = if (isPaper) PaperTextMuted else CyberYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Konuşmalarınızı askeri düzeyde AES-256-CBC algoritması ve SHA-256 sağlama toplamı ile şifreleyerek cihazınızın yerel depolama alanına metin dosyası olarak aktarır. Dosya yalnızca belirlediğiniz parola ile çözülebilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaper) PaperTextMuted else TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ŞİFRELEME PAROLASI (MASTER KEY)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaper) PaperText else CyberYellow
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        placeholder = { Text("Güvenlik parolası belirleyin...", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPaper) PaperText else CyberYellow,
                            unfocusedBorderColor = if (isPaper) PaperBorder else CyberCardBorder,
                            focusedTextColor = if (isPaper) PaperText else Color.White,
                            unfocusedTextColor = if (isPaper) PaperText else Color.White,
                            focusedContainerColor = if (isPaper) PaperBg else CyberBlack,
                            unfocusedContainerColor = if (isPaper) PaperBg else CyberBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            SoundPoolManager.playConfirm()
                            isExporting = true

                            val messageList = mutableListOf<Pair<String, String>>()
                            defaultConversations.forEach { conv ->
                                conv.messages.forEach { msg ->
                                    messageList.add(Pair(msg.senderName, msg.text))
                                }
                            }

                            val result = EncryptedVaultUtil.encryptTranscript(
                                context = context,
                                chatTitle = "GENeral Chat Tüm Sohbet Arşivi (10 Gizlilik Kalkanı)",
                                messages = messageList,
                                passphrase = passphrase.ifBlank { "GENERAL_CHAT_MASTER_E2EE" }
                            )

                            exportResult = result
                            isExporting = false

                            InstantNotificationManager.show(
                                title = "Sohbet Kaydı Şifrelendi",
                                message = "AES-256 şifreli metin dosyası oluşturuldu (${result.messageCount} mesaj).",
                                iconEmoji = "🔒"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaper) PaperText else CyberYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = if (isPaper) PaperBg else CyberBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExporting) "Şifreleniyor..." else "AES-256 İLE ŞİFRELE VE DIŞA AKTAR",
                            fontWeight = FontWeight.Black,
                            color = if (isPaper) PaperBg else CyberBlack,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        exportResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else Color(0xFF0C1929)),
                    border = BorderStroke(1.2.dp, NeonGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ŞİFRELİ METİN DOSYASI OLUŞTURULDU",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "${res.messageCount} Mesaj",
                                color = if (isPaper) PaperTextMuted else TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Dosya Yolu: ${res.file.absolutePath}",
                            fontSize = 10.sp,
                            color = if (isPaper) PaperTextMuted else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SHA-256 İmza: ${res.checksum}",
                            fontSize = 10.sp,
                            color = if (isPaper) PaperText else NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preview of encrypted ciphertext payload
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPaper) PaperBg else Color(0xFF040A12))
                                .border(1.dp, if (isPaper) PaperBorder else Color(0xFF1E3A5F), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = res.previewEncryptedText,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isPaper) PaperText else NeonGreen.copy(alpha = 0.9f),
                                lineHeight = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                SoundPoolManager.playConfirm()
                                EncryptedVaultUtil.shareEncryptedFile(context, res.file)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DOSYAYI PAYLAŞ VEYA CİHAZA KAYDET (.txt)", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun getMaskIcon(mask: MaskType): ImageVector = when (mask) {
    MaskType.CALCULATOR -> Icons.Default.Calculate
    MaskType.WEATHER -> Icons.Default.Cloud
    MaskType.NOTES -> Icons.Default.EditNote
    MaskType.COMPASS -> Icons.Default.Explore
    MaskType.CALENDAR -> Icons.Default.CalendarMonth
    MaskType.CLOCK -> Icons.Default.Schedule
    MaskType.STOPWATCH -> Icons.Default.Timer
    MaskType.DICTIONARY -> Icons.Default.MenuBook
    MaskType.QR_SCANNER -> Icons.Default.QrCodeScanner
    MaskType.BATTERY -> Icons.Default.BatteryFull
}

// -------------------------------------------------------------
// Interactive Camouflage Mock Views
// -------------------------------------------------------------

@Composable
fun CalculatorCamouflageView(isPaper: Boolean) {
    var display by remember { mutableStateOf("0") }

    fun onKey(key: String) {
        SoundPoolManager.playMechanicalClick()
        when (key) {
            "C" -> display = "0"
            "=" -> {
                try {
                    val parts = display.split("+", "-", "×", "÷")
                    if (parts.size >= 2) {
                        val n1 = parts[0].toDoubleOrNull() ?: 0.0
                        val n2 = parts[1].toDoubleOrNull() ?: 0.0
                        val res = when {
                            display.contains("+") -> n1 + n2
                            display.contains("-") -> n1 - n2
                            display.contains("×") -> n1 * n2
                            display.contains("÷") -> if (n2 != 0.0) n1 / n2 else 0.0
                            else -> n1
                        }
                        display = if (res % 1.0 == 0.0) res.toLong().toString() else "%.2f".format(res)
                    }
                } catch (_: Exception) {
                    display = "0"
                }
            }
            else -> {
                if (display == "0") display = key else display += key
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.displayLarge,
            fontFamily = FontFamily.Monospace,
            color = if (isPaper) PaperText else Color.White,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        val keys = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { k ->
                    val isAction = k in listOf("÷", "×", "-", "+", "=")
                    Button(
                        onClick = { onKey(k) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAction) {
                                if (isPaper) PaperBorder else Color(0xFFFF9500)
                            } else {
                                if (isPaper) PaperSurface else Color(0xFF2C2C2C)
                            }
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(if (k == "0") 2f else 1f)
                            .height(62.dp)
                    ) {
                        Text(
                            text = k,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaper) PaperText else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = if (isPaper) PaperText else Color(0xFF4FC3F7),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "İstanbul",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
        Text(
            text = "Parçalı Bulutlu • Nem %54 • Rüzgar 14 km/s",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "22°C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = if (isPaper) PaperText else Color.White
        )
    }
}

@Composable
fun CompassCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .border(2.dp, if (isPaper) PaperBorder else Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = if (isPaper) PaperText else Color(0xFFFF5252),
                modifier = Modifier.size(130.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "342° KB",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
        Text(
            text = "Manyetik Kuzey Sapması: 4.2°",
            style = MaterialTheme.typography.bodySmall,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
    }
}

@Composable
fun NotesCamouflageView(isPaper: Boolean) {
    val sampleNotes = remember {
        mutableStateListOf(
            "Market Listesi: Süt, ekmek, filtre kahve, zeytin",
            "Haftalık Egzersiz: Salı koşu, Perşembe yüzme",
            "Kitap Listesi: 1984, Cesur Yeni Dünya, Siberpunk Günlükleri",
            "Toplantı: Proje teslimi Çarşamba saat 14:00"
        )
    }
    var newNoteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Not Defterim",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleNotes) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPaper) PaperSurface else Color(0xFF222222)),
                    border = BorderStroke(1.dp, if (isPaper) PaperBorder else CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note,
                        color = if (isPaper) PaperText else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newNoteInput,
                onValueChange = { newNoteInput = it },
                placeholder = { Text("Yeni not ekle...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newNoteInput.isNotBlank()) {
                        sampleNotes.add(0, newNoteInput.trim())
                        newNoteInput = ""
                        SoundPoolManager.playMechanicalClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else CyberYellow)
            ) {
                Text("Ekle", color = if (isPaper) PaperText else CyberBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BatteryCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BatteryFull,
            contentDescription = null,
            tint = if (isPaper) PaperText else Color(0xFF4CAF50),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "%88 - Pil Durumu Mükemmel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sıcaklık: 28.4°C • Voltaj: 4.1V • Sağlık: %98",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
    }
}

@Composable
fun CalendarCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = if (isPaper) PaperText else CyberYellow,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kasım 2026",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
        Text(
            text = "Bugün planlanmış herhangi bir etkinlik yok.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
    }
}

@Composable
fun ClockCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "14:30",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (isPaper) PaperText else Color.White
        )
        Text(
            text = "İstanbul, Türkiye (GMT+3)",
            style = MaterialTheme.typography.titleMedium,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Kurulu Alarm: 07:00 (Hafta içi)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPaper) PaperText else CyberYellow
        )
    }
}

@Composable
fun StopwatchCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "00:00:00.00",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isPaper) PaperText else Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { SoundPoolManager.playMechanicalClick() },
            colors = ButtonDefaults.buttonColors(containerColor = if (isPaper) PaperBorder else Color(0xFF4CAF50))
        ) {
            Text("BAŞLAT", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun DictionaryCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Türkçe - İngilizce Sözlük",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (isPaper) PaperText else Color.White
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Kelime arayın...") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QrScannerCamouflageView(isPaper: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = if (isPaper) PaperText else NeonCyan,
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kamera Vizörü Hazır",
            fontWeight = FontWeight.Bold,
            color = if (isPaper) PaperText else Color.White
        )
        Text(
            text = "Kodu çerçevenin içine hizalayın",
            fontSize = 12.sp,
            color = if (isPaper) PaperTextMuted else Color.Gray
        )
    }
}
