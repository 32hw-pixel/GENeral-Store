package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdvancedChatPrivacyConfig(
    val advancedPrivacyEnabled: Boolean = false,
    val blockScreenCapture: Boolean = false,
    val blockExportAndForward: Boolean = false,
    val blockAutoMediaSave: Boolean = false,
    val excludeFromAi: Boolean = false
)

/**
 * Stores global and per-conversation "Gelişmiş Sohbet Gizliliği" rules:
 * - Ekran görüntüsü ve ekran kaydı engelleme (FLAG_SECURE)
 * - Sohbeti dışa aktarma ve mesaj iletme kısıtlaması
 * - Medya/dosyaları otomatik cihaza kaydetmeme
 * - GEN AI asistan bağlamından hariç tutma
 */
object PrivacyPreferencesManager {
    private const val PREFS = "general_chat_privacy_prefs"
    private const val KEY_GLOBAL_SCREEN_LOCK = "global_screen_lock"
    private const val KEY_GLOBAL_AI_OPT_OUT = "global_ai_opt_out"
    private const val KEY_GLOBAL_BLOCK_EXPORT = "global_block_export"
    private const val KEY_GLOBAL_BLOCK_MEDIA_SAVE = "global_block_media_save"

    private val _revision = MutableStateFlow(0)
    val revision = _revision.asStateFlow()

    fun getChatConfig(context: Context, conversationId: String): AdvancedChatPrivacyConfig {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val globalAdvScreen = prefs.getBoolean(KEY_GLOBAL_SCREEN_LOCK, false)
        val globalBlockExport = prefs.getBoolean(KEY_GLOBAL_BLOCK_EXPORT, false)
        val globalBlockMedia = prefs.getBoolean(KEY_GLOBAL_BLOCK_MEDIA_SAVE, false)
        val globalAiOptOut = prefs.getBoolean(KEY_GLOBAL_AI_OPT_OUT, false)

        val enabled = prefs.getBoolean("chat_${conversationId}_adv", false)
        if (!enabled) {
            return AdvancedChatPrivacyConfig(
                advancedPrivacyEnabled = false,
                blockScreenCapture = globalAdvScreen,
                blockExportAndForward = globalBlockExport,
                blockAutoMediaSave = globalBlockMedia,
                excludeFromAi = globalAiOptOut
            )
        }
        return AdvancedChatPrivacyConfig(
            advancedPrivacyEnabled = true,
            blockScreenCapture = prefs.getBoolean("chat_${conversationId}_screen", true),
            blockExportAndForward = prefs.getBoolean("chat_${conversationId}_export", true),
            blockAutoMediaSave = prefs.getBoolean("chat_${conversationId}_media", true),
            excludeFromAi = prefs.getBoolean("chat_${conversationId}_ai_exclude", true)
        )
    }

    fun setChatConfig(context: Context, conversationId: String, config: AdvancedChatPrivacyConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("chat_${conversationId}_adv", config.advancedPrivacyEnabled)
            .putBoolean("chat_${conversationId}_screen", config.blockScreenCapture)
            .putBoolean("chat_${conversationId}_export", config.blockExportAndForward)
            .putBoolean("chat_${conversationId}_media", config.blockAutoMediaSave)
            .putBoolean("chat_${conversationId}_ai_exclude", config.excludeFromAi)
            .apply()
        _revision.value = _revision.value + 1
    }

    fun toggleMasterPrivacy(context: Context, conversationId: String, enable: Boolean): AdvancedChatPrivacyConfig {
        val next = AdvancedChatPrivacyConfig(
            advancedPrivacyEnabled = enable,
            blockScreenCapture = enable,
            blockExportAndForward = enable,
            blockAutoMediaSave = enable,
            excludeFromAi = enable
        )
        setChatConfig(context, conversationId, next)
        return next
    }

    fun getGlobalDefaults(context: Context): AdvancedChatPrivacyConfig {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val s = prefs.getBoolean(KEY_GLOBAL_SCREEN_LOCK, false)
        val e = prefs.getBoolean(KEY_GLOBAL_BLOCK_EXPORT, false)
        val m = prefs.getBoolean(KEY_GLOBAL_BLOCK_MEDIA_SAVE, false)
        val a = prefs.getBoolean(KEY_GLOBAL_AI_OPT_OUT, false)
        return AdvancedChatPrivacyConfig(
            advancedPrivacyEnabled = s || e || m || a,
            blockScreenCapture = s,
            blockExportAndForward = e,
            blockAutoMediaSave = m,
            excludeFromAi = a
        )
    }

    fun setGlobalDefaults(context: Context, config: AdvancedChatPrivacyConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_SCREEN_LOCK, config.blockScreenCapture)
            .putBoolean(KEY_GLOBAL_BLOCK_EXPORT, config.blockExportAndForward)
            .putBoolean(KEY_GLOBAL_BLOCK_MEDIA_SAVE, config.blockAutoMediaSave)
            .putBoolean(KEY_GLOBAL_AI_OPT_OUT, config.excludeFromAi)
            .apply()
        _revision.value = _revision.value + 1
    }
}
