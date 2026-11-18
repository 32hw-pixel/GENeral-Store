package com.example.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ChatWallpaper(val displayName: String, val description: String) {
    CYBER_4K_ART("4K Siber Altın Obsidian (Ultra HD)", "GENeral Chat 4K yüksek çözünürlüklü imzalı arka plan"),
    CYBER_GRID("Sarı-Siyah Siber Izgara", "GENeral Chat imzası sarı-siyah koordinat matrisi"),
    CYBER_HEX_PATTERN("Altıgen Siber Doku", "Orijinal geometrik altıgen & siber nokta dokusu"),
    SERVER_ROOM("Sunucu Odası & Hologram", "Veri merkezi mavi neon koridor dokusu"),
    NEON_PURPLE("Synthwave Gece Neon", "Derin mor ve eflatun parıltı"),
    CARBON_TACTICAL("Taktik Karbon Fiber", "Minimalist mat askeri kompozit doku"),
    EMERALD_MATRIX("Zümrüt Yeşil Matris", "Hacker konsolu dijital yağmur ve zümrüt ışıltısı"),
    SUNSET_HORIZON("Siber Şafak & Kızıl Gradyan", "Akşam ufku neon kızıl ve altın parıltı"),
    DEEP_SPACE_NEBULA("Derin Kozmik Nebula", "Yıldız kümeleri ve derin uzay moru"),
    AMOLED_PURE_BLACK("Saf AMOLED Siyahı", "Pil tasarrufu ve maksimum kontrast"),
    PAPER_INK("Kağıt Ekran Mat E-Ink", "Göz yormayan kitap parşömeni dokusu"),
    CUSTOM_PHOTO("Galeriden Özel Görsel", "Cihazından seçtiğin kişisel 4K fotoğraf")
}

object ChatWallpaperManager {

    private const val PREFS_NAME = "general_chat_wallpaper_prefs"
    private const val KEY_WALLPAPER = "selected_wallpaper"
    private const val KEY_CUSTOM_URI = "custom_photo_uri"
    private const val KEY_DIM_ALPHA = "dim_alpha"

    private var appContext: Context? = null

    private val _selectedWallpaper = MutableStateFlow(ChatWallpaper.CYBER_GRID)
    val selectedWallpaper: StateFlow<ChatWallpaper> = _selectedWallpaper.asStateFlow()

    private val _customPhotoUri = MutableStateFlow<Uri?>(null)
    val customPhotoUri: StateFlow<Uri?> = _customPhotoUri.asStateFlow()

    private val _dimAlpha = MutableStateFlow(0.35f)
    val dimAlpha: StateFlow<Float> = _dimAlpha.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_WALLPAPER, null)
        if (savedName == "WHATSAPP_DOODLE") {
            _selectedWallpaper.value = ChatWallpaper.CYBER_GRID
            prefs.edit().putString(KEY_WALLPAPER, ChatWallpaper.CYBER_GRID.name).apply()
        } else if (!savedName.isNullOrBlank()) {
            ChatWallpaper.entries.firstOrNull { it.name == savedName }?.let {
                _selectedWallpaper.value = it
            }
        }
        val savedUri = prefs.getString(KEY_CUSTOM_URI, null)
        if (!savedUri.isNullOrBlank()) {
            _customPhotoUri.value = Uri.parse(savedUri)
        }
        _dimAlpha.value = prefs.getFloat(KEY_DIM_ALPHA, 0.35f).coerceIn(0.1f, 0.85f)
    }

    fun setWallpaper(wallpaper: ChatWallpaper) {
        _selectedWallpaper.value = wallpaper
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_WALLPAPER, wallpaper.name)
            ?.apply()
    }

    fun setCustomPhoto(uri: Uri) {
        _customPhotoUri.value = uri
        _selectedWallpaper.value = ChatWallpaper.CUSTOM_PHOTO
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_CUSTOM_URI, uri.toString())
            ?.putString(KEY_WALLPAPER, ChatWallpaper.CUSTOM_PHOTO.name)
            ?.apply()
    }

    fun setDimAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.1f, 0.9f)
        _dimAlpha.value = clamped
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putFloat(KEY_DIM_ALPHA, clamped)
            ?.apply()
    }
}
