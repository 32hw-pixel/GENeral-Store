package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val description: String, val icon: String) {
    CYBER_DARK("Siber Koyu", "Saf siberpunk neon sarı & oled siyah", "⚡"),
    LIGHT_MODERN("Açık Modern", "Ferah, net ve modern aydınlık arayüz", "☀️"),
    WARM_SEPIA("Sıcak Ekran (2700K)", "Göz yormayan kehribar & sepia gece filtresi", "🕯️"),
    COOL_OLED("Soğuk Buz (OLED)", "Derin uzay mavisi ve fütüristik neon camgöbeği", "❄️"),
    ULTRA_PAPER("Kağıt Ekran (E-Ink)", "Yansıma yapmayan mat mürekkep & saf kitap konforu", "📜")
}

val LocalPaperMode = staticCompositionLocalOf { false }
val LocalAppThemeMode = staticCompositionLocalOf { AppThemeMode.CYBER_DARK }

private val CyberColorScheme = darkColorScheme(
    primary = CyberYellow,
    onPrimary = Color.Black,
    primaryContainer = CyberYellowContainer,
    onPrimaryContainer = CyberYellow,
    secondary = CyberYellowLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E1A05),
    onSecondaryContainer = CyberYellowLight,
    tertiary = NeonCyan,
    onTertiary = Color.Black,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberCardBg,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder,
    error = NeonRed
)

private val LightModernColorScheme = lightColorScheme(
    primary = Color(0xFFEAB308),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFFFEF08A),
    onPrimaryContainer = Color(0xFF713F12),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = Color(0xFF06B6D4),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444)
)

private val WarmSepiaColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF1C1308),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFD97706),
    onSecondary = Color(0xFF1C1308),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    background = Color(0xFF1E170E),
    onBackground = Color(0xFFFDE68A),
    surface = Color(0xFF292014),
    onSurface = Color(0xFFFDE68A),
    surfaceVariant = Color(0xFF382C1C),
    onSurfaceVariant = Color(0xFFD97706),
    outline = Color(0xFF574126),
    error = Color(0xFFEF4444)
)

private val CoolOledColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF001E2B),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFF80DEEA),
    secondary = Color(0xFF00B0FF),
    onSecondary = Color(0xFF001E2B),
    tertiary = Color(0xFF7C4DFF),
    onTertiary = Color.White,
    background = Color(0xFF050B14),
    onBackground = Color(0xFFE0F7FA),
    surface = Color(0xFF0A192F),
    onSurface = Color(0xFFE0F7FA),
    surfaceVariant = Color(0xFF112240),
    onSurfaceVariant = Color(0xFF8892B0),
    outline = Color(0xFF1E3A5F),
    error = Color(0xFFFF5252)
)

private val UltraPaperColorScheme = lightColorScheme(
    primary = PaperText,
    onPrimary = PaperBg,
    primaryContainer = PaperSurface,
    onPrimaryContainer = PaperText,
    secondary = PaperTextMuted,
    onSecondary = PaperBg,
    background = PaperBg,
    onBackground = PaperText,
    surface = PaperSurface,
    onSurface = PaperText,
    surfaceVariant = PaperSurface,
    onSurfaceVariant = PaperTextMuted,
    outline = PaperBorder,
    error = Color(0xFF900C3F)
)

fun getThemeColorScheme(mode: AppThemeMode): ColorScheme = when (mode) {
    AppThemeMode.CYBER_DARK -> CyberColorScheme
    AppThemeMode.LIGHT_MODERN -> LightModernColorScheme
    AppThemeMode.WARM_SEPIA -> WarmSepiaColorScheme
    AppThemeMode.COOL_OLED -> CoolOledColorScheme
    AppThemeMode.ULTRA_PAPER -> UltraPaperColorScheme
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.CYBER_DARK,
    paperMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val activeMode = if (paperMode) AppThemeMode.ULTRA_PAPER else themeMode
    val colorScheme = getThemeColorScheme(activeMode)
    val isPaper = activeMode == AppThemeMode.ULTRA_PAPER

    CompositionLocalProvider(
        LocalPaperMode provides isPaper,
        LocalAppThemeMode provides activeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
