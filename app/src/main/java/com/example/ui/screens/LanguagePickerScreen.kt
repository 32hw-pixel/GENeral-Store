package com.example.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageOption
import com.example.data.defaultLanguages
import com.example.ui.components.CyberAudioStatusBar
import com.example.ui.components.CyberTechButton
import com.example.ui.components.MusicSettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.CyberYellowContainer
import com.example.ui.theme.LocalPaperMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PaperBg
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.PaperText
import com.example.ui.theme.PaperTextMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SoundPoolManager
import kotlinx.coroutines.launch

data class CountryOption(
    val name: String,
    val flag: String,
    val code: String
)

val defaultCountries = listOf(
    CountryOption("Türkiye", "🇹🇷", "TR"),
    CountryOption("Almanya", "🇩🇪", "DE"),
    CountryOption("Amerika B.D.", "🇺🇸", "US"),
    CountryOption("Birleşik Krallık", "🇬🇧", "UK"),
    CountryOption("Japonya", "🇯🇵", "JP"),
    CountryOption("Fransa", "🇫🇷", "FR"),
    CountryOption("İtalya", "🇮🇹", "IT"),
    CountryOption("Güney Kore", "🇰🇷", "KR"),
    CountryOption("Azerbaycan", "🇦🇿", "AZ"),
    CountryOption("İspanya", "🇪🇸", "ES"),
    CountryOption("Kanada", "🇨🇦", "CA"),
    CountryOption("Brezilya", "🇧🇷", "BR")
)

/**
 * Section 2: Küresel Ülke ve Dil Seçim Ekranı
 * Completely resolves the scrolling up issue with smooth bi-directional wheel navigation,
 * tap-to-select, discrete stepper arrows, and crisp mechanical tok switch & scroll ticks.
 */
@Composable
fun LanguagePickerScreen(
    onLanguageSelected: (LanguageOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaper = LocalPaperMode.current
    val scope = rememberCoroutineScope()
    var showAudioDialog by remember { mutableStateOf(false) }

    var selectedCountryIndex by remember { mutableIntStateOf(0) }
    var selectedLanguageIndex by remember { mutableIntStateOf(0) }

    val countryListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val languageListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    // Listen to country scroll changes & play crisp tick sound
    LaunchedEffect(countryListState) {
        var last = countryListState.firstVisibleItemIndex
        snapshotFlow { countryListState.firstVisibleItemIndex }.collect { index ->
            if (index != last) {
                last = index
                selectedCountryIndex = index.coerceIn(0, defaultCountries.size - 1)
                SoundPoolManager.playScrollTick()
            }
        }
    }

    // Listen to language scroll changes & play crisp tick sound
    LaunchedEffect(languageListState) {
        var last = languageListState.firstVisibleItemIndex
        snapshotFlow { languageListState.firstVisibleItemIndex }.collect { index ->
            if (index != last) {
                last = index
                selectedLanguageIndex = index.coerceIn(0, defaultLanguages.size - 1)
                SoundPoolManager.playScrollTick()
            }
        }
    }

    val bgColor = if (isPaper) PaperBg else CyberBlack
    val cardBg = if (isPaper) PaperSurface else CyberCardBg
    val textColor = if (isPaper) PaperText else Color.White
    val textMutedColor = if (isPaper) PaperTextMuted else TextSecondary
    val borderColor = if (isPaper) PaperBorder else CyberCardBorder
    val accentColor = if (isPaper) PaperText else CyberYellow

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("language_picker_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val headerBgModifier = if (isPaper) {
                Modifier.background(PaperBorder)
            } else {
                Modifier.background(
                    Brush.radialGradient(
                        colors = listOf(CyberYellow.copy(alpha = 0.35f), Color(0xFF1E1A05))
                    )
                )
            }

            // Header Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .then(headerBgModifier)
                    .border(1.5.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Küresel Frekansı Ayarla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sistem dilini ve bulunduğun ülkeyi seç. Arayüz anında seçtiğin frekansa kilitlenecek.",
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Roller Wheel Layout (Country & Language Separated)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // COLUMN 1: ÜLKE SEÇİCİ
                WheelContainer(
                    title = "BULUNDUĞUN ÜLKE",
                    accentColor = accentColor,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    isPaper = isPaper,
                    onStepUp = {
                        val target = (selectedCountryIndex - 1).coerceAtLeast(0)
                        scope.launch {
                            countryListState.animateScrollToItem(target)
                        }
                    },
                    onStepDown = {
                        val target = (selectedCountryIndex + 1).coerceAtMost(defaultCountries.size - 1)
                        scope.launch {
                            countryListState.animateScrollToItem(target)
                        }
                    }
                ) {
                    LazyColumn(
                        state = countryListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(defaultCountries.size) { index ->
                            val country = defaultCountries[index]
                            val isSelected = index == selectedCountryIndex

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isPaper) PaperBorder else CyberYellowContainer.copy(alpha = 0.7f)
                                        } else Color.Transparent
                                    )
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        scope.launch {
                                            countryListState.animateScrollToItem(index)
                                        }
                                    }
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = country.flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = country.name,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) textColor else textMutedColor,
                                        fontSize = if (isSelected) 13.sp else 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // COLUMN 2: DİL SEÇİCİ
                WheelContainer(
                    title = "SİSTEM DİLİ",
                    accentColor = if (isPaper) PaperText else NeonCyan,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    isPaper = isPaper,
                    onStepUp = {
                        val target = (selectedLanguageIndex - 1).coerceAtLeast(0)
                        scope.launch {
                            languageListState.animateScrollToItem(target)
                        }
                    },
                    onStepDown = {
                        val target = (selectedLanguageIndex + 1).coerceAtMost(defaultLanguages.size - 1)
                        scope.launch {
                            languageListState.animateScrollToItem(target)
                        }
                    }
                ) {
                    LazyColumn(
                        state = languageListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(defaultLanguages.size) { index ->
                            val lang = defaultLanguages[index]
                            val isSelected = index == selectedLanguageIndex

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isPaper) PaperBorder else Color(0xFF0E2235)
                                        } else Color.Transparent
                                    )
                                    .clickable {
                                        SoundPoolManager.playMechanicalClick()
                                        scope.launch {
                                            languageListState.animateScrollToItem(index)
                                        }
                                    }
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = lang.flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lang.languageName,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) (if (isPaper) PaperText else NeonCyan) else textMutedColor,
                                        fontSize = if (isSelected) 13.sp else 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Asphalt Style Bounce Press
            CyberTechButton(
                text = "[ FREKANSI SABİTLE VE İLERLE ]",
                containerColor = if (isPaper) PaperText else CyberYellow,
                contentColor = if (isPaper) PaperBg else CyberBlack,
                borderColor = accentColor,
                testTag = "confirm_language_button",
                onClick = {
                    SoundPoolManager.playConfirm()
                    val selectedLang = defaultLanguages.getOrElse(selectedLanguageIndex) { defaultLanguages[0] }
                    onLanguageSelected(selectedLang)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Status Indicator
            CyberAudioStatusBar(
                onOpenAudioCenter = { showAudioDialog = true }
            )
        }
    }

    if (showAudioDialog) {
        MusicSettingsDialog(
            onDismiss = { showAudioDialog = false }
        )
    }
}

@Composable
private fun WheelContainer(
    title: String,
    accentColor: Color,
    cardBg: Color,
    borderColor: Color,
    isPaper: Boolean,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))

        // Stepper Up Button (Outside the scroll area)
        IconButton(
            onClick = {
                SoundPoolManager.playScrollTick()
                onStepUp()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Yukarı Kaydır",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        // Stepper Down Button (Outside the scroll area)
        IconButton(
            onClick = {
                SoundPoolManager.playScrollTick()
                onStepDown()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Aşağı Kaydır",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
