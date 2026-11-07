package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.data.CustomAiDisguise
import com.example.data.MaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Dynamic Android Launcher Icon & App Name Disguise Aliases
 * and AI-Powered Custom Disguise Profiles.
 */
object DisguiseManager {

    private val _activeMask = MutableStateFlow<MaskType?>(null)
    val activeMask = _activeMask.asStateFlow()

    private val _customAiDisguise = MutableStateFlow<CustomAiDisguise?>(null)
    val customAiDisguise = _customAiDisguise.asStateFlow()

    private val _activeDisguiseName = MutableStateFlow("GENeral Chat")
    val activeDisguiseName = _activeDisguiseName.asStateFlow()

    /**
     * Activates an official system camouflage mask, toggling the corresponding
     * activity-alias in AndroidManifest so the app icon and name appear as that utility.
     */
    fun activateMask(context: Context, mask: MaskType): Boolean {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName

            // Enable selected alias
            pm.setComponentEnabledSetting(
                ComponentName(pkg, mask.aliasClass),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disable other mask aliases
            MaskType.entries.filter { it != mask }.forEach { other ->
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, other.aliasClass),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

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
     * Restores default GENeral Chat icon & identity.
     */
    fun restoreDefaultIdentity(context: Context) {
        try {
            val pm = context.packageManager
            val pkg = context.packageName

            MaskType.entries.forEach { mask ->
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, mask.aliasClass),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            _activeMask.value = null
            _activeDisguiseName.value = "GENeral Chat"
            SoundPoolManager.playConfirm()
        } catch (_: Exception) {
            _activeMask.value = null
            _activeDisguiseName.value = "GENeral Chat"
        }
    }

    /**
     * AI-Powered Custom Disguise Generator:
     * Generates a realistic decoy utility based on user's concept prompt.
     */
    fun generateAiDisguise(userPrompt: String): CustomAiDisguise {
        val clean = userPrompt.trim()
        val (title, subtitle, category, trigger, emoji) = when {
            clean.contains("borsa", ignoreCase = true) || clean.contains("kripto", ignoreCase = true) || clean.contains("para", ignoreCase = true) ->
                listOf("Borsa Portföyüm", "Canlı hisse ve endeks verileri", "Finans & Yatırım", "BIST100 grafiğine 3 kez dokun", "📈")
            clean.contains("yemek", ignoreCase = true) || clean.contains("tarif", ignoreCase = true) || clean.contains("mutfak", ignoreCase = true) ->
                listOf("Mutfak Rehberi", "Pratik ev yemekleri ve tarifler", "Yemek & Yaşam", "'Günün Çorbası'na 3 kez dokun", "🍲")
            clean.contains("spor", ignoreCase = true) || clean.contains("fit", ignoreCase = true) || clean.contains("adım", ignoreCase = true) ->
                listOf("FitStep Sayacı", "Günlük adım ve kalori takibi", "Sağlık & Egzersiz", "Kalori halkasına 3 kez dokun", "🏃")
            clean.contains("kitap", ignoreCase = true) || clean.contains("roman", ignoreCase = true) || clean.contains("oku", ignoreCase = true) ->
                listOf("E-Kitap Okuyucu", "Çevrimdışı kütüphane ve epikler", "Eğitim & Kitap", "Sayfa numarasına 3 kez dokun", "📖")
            clean.contains("araba", ignoreCase = true) || clean.contains("hız", ignoreCase = true) || clean.contains("araç", ignoreCase = true) ->
                listOf("Araç Takip & OBD", "Motor devri ve yakıt tüketimi", "Otomotiv", "Hız göstergesine 3 kez dokun", "🏎️")
            else ->
                listOf(
                    if (clean.isNotBlank()) clean.capitalize() else "Kişisel Kasa",
                    "Şifrelenmiş özel veri deposu ve yedekleme",
                    "Sistem Yardımcı Programı",
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
