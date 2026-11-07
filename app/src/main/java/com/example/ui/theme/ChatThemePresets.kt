package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class ChatThemePalette(
    val id: String,
    val title: String,
    val emoji: String,
    val accentColor: Color,
    val myBubbleBg: Color,
    val myBubbleText: Color,
    val otherBubbleBg: Color,
    val otherBubbleText: Color,
    val borderGlow: Color,
    val chatWallpaperBg: Color
)

object ChatThemePresets {
    val presets = listOf(
        ChatThemePalette(
            id = "CYBER_YELLOW",
            title = "Sarı Siberpunk",
            emoji = "⚡",
            accentColor = CyberYellow,
            myBubbleBg = CyberYellow,
            myBubbleText = CyberBlack,
            otherBubbleBg = CyberCardBg,
            otherBubbleText = Color.White,
            borderGlow = CyberYellow,
            chatWallpaperBg = Color(0xFF0C0A00)
        ),
        ChatThemePalette(
            id = "MATRIX_GREEN",
            title = "Matrix Zümrüt",
            emoji = "🟢",
            accentColor = Color(0xFF00FF66),
            myBubbleBg = Color(0xFF00E65C),
            myBubbleText = Color(0xFF001F0A),
            otherBubbleBg = Color(0xFF081C10),
            otherBubbleText = Color(0xFFE8FDF0),
            borderGlow = Color(0xFF00FF66),
            chatWallpaperBg = Color(0xFF020C06)
        ),
        ChatThemePalette(
            id = "SYNTH_PURPLE",
            title = "Neon Sentetik",
            emoji = "🔮",
            accentColor = Color(0xFFE056FD),
            myBubbleBg = Color(0xFFBE2EDD),
            myBubbleText = Color.White,
            otherBubbleBg = Color(0xFF1B0B2B),
            otherBubbleText = Color(0xFFFBEAFF),
            borderGlow = Color(0xFFE056FD),
            chatWallpaperBg = Color(0xFF0D0417)
        ),
        ChatThemePalette(
            id = "AMBER_TERMINAL",
            title = "Kehribar Monitör",
            emoji = "🟠",
            accentColor = Color(0xFFFF9F1A),
            myBubbleBg = Color(0xFFFF9F1A),
            myBubbleText = Color(0xFF261200),
            otherBubbleBg = Color(0xFF1F1204),
            otherBubbleText = Color(0xFFFFF1E0),
            borderGlow = Color(0xFFFF9F1A),
            chatWallpaperBg = Color(0xFF0F0800)
        ),
        ChatThemePalette(
            id = "GLACIER_BLUE",
            title = "Buzul Camgöbeği",
            emoji = "❄️",
            accentColor = Color(0xFF00E5FF),
            myBubbleBg = Color(0xFF00E5FF),
            myBubbleText = Color(0xFF00222B),
            otherBubbleBg = Color(0xFF071926),
            otherBubbleText = Color(0xFFE0F7FA),
            borderGlow = Color(0xFF00E5FF),
            chatWallpaperBg = Color(0xFF030D14)
        )
    )

    fun getThemeById(id: String?): ChatThemePalette {
        return presets.find { it.id == id } ?: presets.first()
    }

    fun getPalette(id: String?): ChatThemePalette = getThemeById(id)
}
