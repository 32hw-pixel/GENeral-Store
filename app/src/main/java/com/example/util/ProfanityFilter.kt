package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Toggleable Profanity & Abuse Filter with Verification Bypass Gate.
 * - Enabled by default.
 * - Turning censorship OFF requires:
 *   1) Adult status (18+; <18 minor or parentally restricted accounts cannot disable)
 *   2) Completing the on-screen safety confirmation + verification code challenge.
 */
object ProfanityFilter {
    private const val PREFS = "general_chat_profanity_prefs"
    private var appContext: Context? = null

    private val _isFilterEnabled = MutableStateFlow(true)
    val isFilterEnabled: StateFlow<Boolean> = _isFilterEnabled.asStateFlow()

    private val _isBypassVerified = MutableStateFlow(false)
    val isBypassVerified: StateFlow<Boolean> = _isBypassVerified.asStateFlow()

    private val badWords = listOf(
        "küfür", "aptal", "salak", "gerizekalı", "ahmak", "moron", "piç", "lanet", "bok", "yavşak", "it", "köpek", "mal"
    )

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val verified = prefs.getBoolean("bypass_verified", false)
        val enabled = if (ParentalControlManager.state.value.isMinorMode) {
            true
        } else {
            prefs.getBoolean("filter_enabled", true)
        }
        _isBypassVerified.value = verified
        _isFilterEnabled.value = enabled
    }

    fun generateChallengeCode(): String {
        return ((1000..9999).random()).toString()
    }

    fun requestDisableWithVerification(
        context: Context? = appContext,
        adultConfirmed: Boolean,
        expectedCode: String,
        enteredCode: String
    ): Result<Unit> {
        context?.let { init(it) }
        if (!ParentalControlManager.isAdultVerified()) {
            _isFilterEnabled.value = true
            return Result.failure(IllegalStateException("18 yaş altı hesaplarda küfür filtresi ebeveyn koruması gereği kapatılamaz."))
        }
        if (!adultConfirmed) {
            return Result.failure(IllegalArgumentException("Lütfen 18 yaş ve üzeri sorumluluk onayını işaretleyin."))
        }
        if (expectedCode.isBlank() || enteredCode.trim() != expectedCode.trim()) {
            return Result.failure(IllegalArgumentException("Doğrulama kodu eşleşmedi. Lütfen ekrandaki 4 haneli kodu doğru girin."))
        }
        _isBypassVerified.value = true
        _isFilterEnabled.value = false
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("bypass_verified", true)
            ?.putBoolean("filter_enabled", false)
            ?.apply()
        return Result.success(Unit)
    }

    fun enableFilter(context: Context? = appContext) {
        context?.let { init(it) }
        _isFilterEnabled.value = true
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("filter_enabled", true)
            ?.apply()
    }

    fun forceEnableForMinor(context: Context? = appContext) {
        context?.let { init(it) }
        _isFilterEnabled.value = true
        _isBypassVerified.value = false
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("filter_enabled", true)
            ?.putBoolean("bypass_verified", false)
            ?.apply()
    }

    fun filter(text: String, isEnabled: Boolean = _isFilterEnabled.value): String {
        if (!isEnabled || text.isBlank()) return text
        var result = text
        for (word in badWords) {
            val regex = Regex("(?i)\\b$word\\b")
            result = result.replace(regex, "*".repeat(word.length))
        }
        return result
    }
}
