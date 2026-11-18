package com.example.util

import android.content.Context
import com.example.data.CustomAiDisguise
import com.example.data.MaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages in-app screen shield modes and AI-generated utility shield profiles
 * via SharedPreferences (does not alter OS launcher icons/aliases for store policy compliance).
 */
object DisguiseManager {

    private const val PREFS = "general_chat_disguise_prefs"
    private const val KEY_ACTIVE_MASK = "active_mask_name"

    private val _activeMask = MutableStateFlow<MaskType?>(null)
    val activeMask = _activeMask.asStateFlow()

    private val _customAiDisguise = MutableStateFlow<CustomAiDisguise?>(null)
    val customAiDisguise = _customAiDisguise.asStateFlow()

    private val _activeDisguiseName = MutableStateFlow("GENeral Chat")
    val activeDisguiseName = _activeDisguiseName.asStateFlow()

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_ACTIVE_MASK, null)
        val found = savedName?.let { n -> MaskType.entries.firstOrNull { it.name == n } }
        _activeMask.value = found
        _activeDisguiseName.value = found?.title ?: "GENeral Chat"
    }

    /**
     * Activates an in-app screen shield mode without spoofing the system launcher icon.
     */
    fun activateMask(context: Context, mask: MaskType): Boolean {
        return try {
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACTIVE_MASK, mask.name)
                .apply()
            _activeMask.value = mask
            _activeDisguiseName.value = mask.title
            SoundPoolManager.playConfirm()
            true
        } catch (_: Exception) {
            _activeMask.value = mask
            _activeDisguiseName.value = mask.title
            false
        }
    }

    /**
     * Restores default GENeral Chat in-app identity.
     */
    fun restoreDefaultIdentity(context: Context) {
        try {
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_ACTIVE_MASK)
                .apply()
            _activeMask.value = null
            _activeDisguiseName.value = "GENeral Chat"
            SoundPoolManager.playConfirm()
        } catch (_: Exception) {
            _activeMask.value = null
            _activeDisguiseName.value = "GENeral Chat"
        }
    }

    /**
     * Generates a custom in-app utility shield concept based on user's prompt.
     */
    fun generateAiDisguise(userPrompt: String): CustomAiDisguise {
        val clean = userPrompt.trim()
        val (title, subtitle, category, trigger, emoji) = when {
            clean.contains("borsa", ignoreCase = true) || clean.contains("kripto", ignoreCase = true) || clean.contains("para", ignoreCase = true) ->
                listOf("Borsa Portföyüm", "Canlı hisse ve endeks görünümü", "Finans & Yatırım", "Başlık alanına 3 kez dokun", "📈")
            clean.contains("yemek", ignoreCase = true) || clean.contains("tarif", ignoreCase = true) || clean.contains("mutfak", ignoreCase = true) ->
                listOf("Mutfak Rehberi", "Pratik ev yemekleri ve tarifler", "Yemek & Yaşam", "'Günün Çorbası'na 3 kez dokun", "🍲")
            clean.contains("spor", ignoreCase = true) || clean.contains("fit", ignoreCase = true) || clean.contains("adım", ignoreCase = true) ->
                listOf("FitStep Sayacı", "Günlük adım ve egzersiz notları", "Sağlık & Egzersiz", "Sayaca 3 kez dokun", "🏃")
            clean.contains("kitap", ignoreCase = true) || clean.contains("roman", ignoreCase = true) || clean.contains("oku", ignoreCase = true) ->
                listOf("E-Kitap Okuyucu", "Çevrimdışı okuma notları", "Eğitim & Kitap", "Sayfa numarasına 3 kez dokun", "📖")
            clean.contains("araba", ignoreCase = true) || clean.contains("hız", ignoreCase = true) || clean.contains("araç", ignoreCase = true) ->
                listOf("Araç Bakım Notu", "Bakım ve kilometre defteri", "Otomotiv", "Gösterge başlığına 3 kez dokun", "🏎️")
            else ->
                listOf(
                    if (clean.isNotBlank()) clean.replaceFirstChar { it.uppercase() } else "Kişisel Kasa",
                    "Ekran koruma görünümü",
                    "Yardımcı Görünüm",
                    "Logoya 3 kez dokun veya telefonu salla",
                    "🔒"
                )
        }

        val disguise = CustomAiDisguise(
            id = "ai_mask_${System.currentTimeMillis()}",
            title = title,
            subtitle = subtitle,
            category = category,
            secretTrigger = trigger,
            iconEmoji = emoji
        )
        _customAiDisguise.value = disguise
        _activeDisguiseName.value = title
        SoundPoolManager.playConfirm()
        return disguise
    }
}
