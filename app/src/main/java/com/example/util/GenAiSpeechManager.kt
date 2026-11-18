package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Native Android Text-to-Speech helper providing Web Speech API SpeechSynthesis
 * parity for reading GEN AI responses aloud in Turkish.
 */
object GenAiSpeechManager {
    private const val PREFS = "general_chat_speech_prefs"
    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false
    private var pendingMessageId: String? = null
    private var pendingText: String? = null
    private var currentLocale: Locale = Locale("tr", "TR")

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _autoReadEnabled = MutableStateFlow(false)
    val autoReadEnabled: StateFlow<Boolean> = _autoReadEnabled.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _autoReadEnabled.value = prefs.getBoolean("auto_read_enabled", false)
    }

    fun setAutoReadEnabled(context: Context, enabled: Boolean) {
        init(context)
        _autoReadEnabled.value = enabled
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()?.putBoolean("auto_read_enabled", enabled)?.apply()
        if (!enabled) stop()
    }

    fun setLanguageCode(languageCode: String) {
        currentLocale = when (languageCode.lowercase()) {
            "tr" -> Locale("tr", "TR")
            "de" -> Locale.GERMANY
            "fr" -> Locale.FRANCE
            "es" -> Locale("es", "ES")
            "it" -> Locale.ITALY
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "az" -> Locale("az", "AZ")
            "en-gb" -> Locale.UK
            else -> Locale.US
        }
        if (isReady) {
            try {
                val res = tts?.setLanguage(currentLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun speak(context: Context, messageId: String, rawText: String, forceStart: Boolean = false) {
        init(context)
        val cleanText = sanitizeForSpeech(rawText)
        if (cleanText.isBlank()) return

        if (!forceStart && _isSpeaking.value && _speakingMessageId.value == messageId) {
            stop()
            return
        }

        val currentTts = tts
        if (currentTts != null && isReady) {
            performSpeak(currentTts, messageId, cleanText)
            return
        }

        pendingMessageId = messageId
        pendingText = cleanText
        if (currentTts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isReady = true
                    tts?.let { engine ->
                        val langRes = engine.setLanguage(currentLocale)
                        if (langRes == TextToSpeech.LANG_MISSING_DATA || langRes == TextToSpeech.LANG_NOT_SUPPORTED) {
                            engine.language = Locale.getDefault()
                        }
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                                _speakingMessageId.value = utteranceId
                            }

                            override fun onDone(utteranceId: String?) {
                                if (_speakingMessageId.value == utteranceId) {
                                    _isSpeaking.value = false
                                    _speakingMessageId.value = null
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                                _speakingMessageId.value = null
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                _isSpeaking.value = false
                                _speakingMessageId.value = null
                            }
                        })
                        val id = pendingMessageId
                        val txt = pendingText
                        pendingMessageId = null
                        pendingText = null
                        if (!id.isNullOrBlank() && !txt.isNullOrBlank()) {
                            performSpeak(engine, id, txt)
                        }
                    }
                } else {
                    isReady = false
                    _isSpeaking.value = false
                    _speakingMessageId.value = null
                }
            }
        }
    }

    private fun performSpeak(engine: TextToSpeech, messageId: String, text: String) {
        _speakingMessageId.value = messageId
        _isSpeaking.value = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isReady = false
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    private fun sanitizeForSpeech(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), " Kod bloğu ekranda gösteriliyor. ")
            .replace(Regex("[*_`#>]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
