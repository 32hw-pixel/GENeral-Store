package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

class SpeechToTextState(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onRequestPermission: () -> Unit,
    private val onLaunchRecognizerIntent: () -> Unit
) {
    var isListening by mutableStateOf(false)
        private set

    var recognizedText by mutableStateOf("")
        private set

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            isListening = true
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            isListening = false
                        }
                        override fun onError(error: Int) {
                            isListening = false
                            // Fallback to system modal intent if service errors
                            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                // silent timeout
                            }
                        }
                        override fun onResults(results: Bundle?) {
                            isListening = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()
                            if (!text.isNullOrBlank()) {
                                recognizedText = text
                                onResult(text)
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let {
                                recognizedText = it
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (_: Exception) {}
        }
    }

    fun startListening() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onRequestPermission()
            return
        }

        if (speechRecognizer != null) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                isListening = true
                speechRecognizer?.startListening(intent)
                return
            } catch (e: Exception) {
                isListening = false
            }
        }

        // Fallback to system activity intent
        onLaunchRecognizerIntent()
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberSpeechToTextLauncher(
    onResult: (String) -> Unit
): SpeechToTextState {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val intentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                currentOnResult(spokenText)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            Toast.makeText(context, "Mikrofon izni verildi. Konuşmak için tekrar dokunun.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Sesli mesaj için mikrofon izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    val state = remember(context) {
        SpeechToTextState(
            context = context,
            onResult = { currentOnResult(it) },
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onLaunchRecognizerIntent = {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Mesajınızı konuşun...")
                    }
                    com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
                    intentLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cihazda ses tanıma servisi bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    DisposableEffect(state) {
        onDispose {
            state.destroy()
        }
    }

    return state
}
