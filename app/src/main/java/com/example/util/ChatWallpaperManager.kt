package com.example.util

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ChatWallpaper(val displayName: String, val description: String) {
    CYBER_GRID("Siber Izgara & Matris", "Klasik siberpunk HUD koordinat çizgileri"),
    SERVER_ROOM("Sunucu Odası & Hologram", "Veri merkezi mavi neon koridor dokusu"),
    NEON_PURPLE("Synthwave Gece Neon", "Derin mor ve eflatun parıltı"),
    CARBON_TACTICAL("Taktik Karbon Fiber", "Minimalist mat askeri kompozit doku"),
    EMERALD_MATRIX("Zümrüt Yeşil Matris", "Hacker konsolu dijital yağmur ve zümrüt ışıltısı"),
    SUNSET_HORIZON("Siber Şafak & Kızıl Gradyan", "Akşam ufku neon kızıl ve altın parıltı"),
    DEEP_SPACE_NEBULA("Derin Kozmik Nebula", "Yıldız kümeleri ve derin uzay moru"),
    AMOLED_PURE_BLACK("Saf AMOLED Siyahı", "Pil tasarrufu ve maksimum kontrast"),
    PAPER_INK("Kağıt Ekran Mat E-Ink", "Göz yormayan kitap parşömeni dokusu"),
    CUSTOM_PHOTO("Galeriden Özel Görsel", "Cihazından seçtiğin kişisel fotoğraf")
}

object ChatWallpaperManager {

    private val _selectedWallpaper = MutableStateFlow(ChatWallpaper.CYBER_GRID)
    val selectedWallpaper: StateFlow<ChatWallpaper> = _selectedWallpaper.asStateFlow()

    private val _customPhotoUri = MutableStateFlow<Uri?>(null)
    val customPhotoUri: StateFlow<Uri?> = _customPhotoUri.asStateFlow()

    private val _dimAlpha = MutableStateFlow(0.35f)
    val dimAlpha: StateFlow<Float> = _dimAlpha.asStateFlow()

    fun setWallpaper(wallpaper: ChatWallpaper) {
        _selectedWallpaper.value = wallpaper
    }

    fun setCustomPhoto(uri: Uri) {
        _customPhotoUri.value = uri
        _selectedWallpaper.value = ChatWallpaper.CUSTOM_PHOTO
    }

    fun setDimAlpha(alpha: Float) {
        _dimAlpha.value = alpha.coerceIn(0.1f, 0.9f)
    }
}
