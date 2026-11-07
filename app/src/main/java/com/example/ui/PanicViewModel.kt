package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.data.MaskType
import com.example.data.PanicConfig
import com.example.data.PanicTrigger
import com.example.util.ShakeDetector
import com.example.util.SoundPoolManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PanicViewModel manages the hardware accelerometer sensor shake-to-trigger
 * functionality and system override panic protocol.
 *
 * When triggered (via violent device shake or biometric touch), it:
 * 1. Instantly wipes sensitive chat history and sessions from local storage (if enabled)
 * 2. Emits an audible cyber panic wipe sound effect
 * 3. Switches the entire app interface to a decoy camouflage mask (e.g. Calculator, Weather, Notes)
 * 4. Listens for the secret counter-shake or secret gesture to disengage panic mode safely.
 */
class PanicViewModel(
    context: Context,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _isPanicActive = MutableStateFlow(false)
    val isPanicActive: StateFlow<Boolean> = _isPanicActive.asStateFlow()

    private val _triggerType = MutableStateFlow(PanicTrigger.SHAKE)
    val triggerType: StateFlow<PanicTrigger> = _triggerType.asStateFlow()

    private val _targetMask = MutableStateFlow(MaskType.CALCULATOR)
    val targetMask: StateFlow<MaskType> = _targetMask.asStateFlow()

    private val _purgeChatHistoryOnPanic = MutableStateFlow(true)
    val purgeChatHistoryOnPanic: StateFlow<Boolean> = _purgeChatHistoryOnPanic.asStateFlow()

    private val _isShakeEnabled = MutableStateFlow(true)
    val isShakeEnabled: StateFlow<Boolean> = _isShakeEnabled.asStateFlow()

    private val _sensitivityG = MutableStateFlow(2.4f)
    val sensitivityG: StateFlow<Float> = _sensitivityG.asStateFlow()

    private val _panicCount = MutableStateFlow(0)
    val panicCount: StateFlow<Int> = _panicCount.asStateFlow()

    private val _lastTriggerTime = MutableStateFlow(0L)
    val lastTriggerTime: StateFlow<Long> = _lastTriggerTime.asStateFlow()

    // Event emitted when panic mode is triggered so UI can navigate to decoy
    private val _panicTriggerEvents = MutableSharedFlow<MaskType>(extraBufferCapacity = 1)
    val panicTriggerEvents: SharedFlow<MaskType> = _panicTriggerEvents.asSharedFlow()

    // Event emitted when panic mode is disengaged so UI can return to chat
    private val _panicDisengageEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val panicDisengageEvents: SharedFlow<Unit> = _panicDisengageEvents.asSharedFlow()

    // Event emitted when Global Panic Button is clicked to lock app and wipe session
    private val _panicLockEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val panicLockEvents: SharedFlow<Unit> = _panicLockEvents.asSharedFlow()

    // Hardware Shake Detector instance
    private val shakeDetector = ShakeDetector(
        context = context,
        onShakeListener = {
            onHardwareShakeDetected()
        }
    ).apply {
        this.sensitivityG = _sensitivityG.value
        this.isEnabled = _isShakeEnabled.value
    }

    val currentGForce: StateFlow<Float> = shakeDetector.currentGForce
    val accelX: StateFlow<Float> = shakeDetector.accelX
    val accelY: StateFlow<Float> = shakeDetector.accelY
    val accelZ: StateFlow<Float> = shakeDetector.accelZ

    init {
        shakeDetector.start()
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
    }

    fun startListening() {
        shakeDetector.start()
    }

    fun stopListening() {
        shakeDetector.stop()
    }

    fun setSensitivity(g: Float) {
        val clamped = g.coerceIn(1.5f, 4.5f)
        _sensitivityG.value = clamped
        shakeDetector.sensitivityG = clamped
    }

    fun setShakeEnabled(enabled: Boolean) {
        _isShakeEnabled.value = enabled
        shakeDetector.isEnabled = enabled
    }

    fun setTriggerType(type: PanicTrigger) {
        _triggerType.value = type
    }

    fun setTargetMask(mask: MaskType) {
        _targetMask.value = mask
    }

    fun setPurgeChatHistory(purge: Boolean) {
        _purgeChatHistoryOnPanic.value = purge
    }

    fun applyConfig(config: PanicConfig) {
        _targetMask.value = config.targetMask
        _triggerType.value = config.trigger
        setShakeEnabled(config.isEnabled)
    }

    /**
     * Called whenever the hardware accelerometer detects rapid violent shaking (>sensitivityG)
     */
    private fun onHardwareShakeDetected() {
        if (_isPanicActive.value) {
            // If already in camouflage decoy, shaking restores GENeral Chat!
            disengagePanic()
        } else {
            // If in normal app, shaking activates emergency panic lockdown!
            triggerPanicOverride()
        }
    }

    /**
     * Executes the Emergency System Override:
     * - Wipes sensitive chat history immediately
     * - Plays high-impact panic audio cue
     * - Switches state to decoy camouflage
     */
    fun triggerPanicOverride() {
        val now = System.currentTimeMillis()
        _lastTriggerTime.value = now
        _panicCount.value++
        _isPanicActive.value = true

        SoundPoolManager.playPanicWipe()

        if (_purgeChatHistoryOnPanic.value) {
            viewModelScope.launch {
                chatRepository.clearAllHistory()
            }
        }

        _panicTriggerEvents.tryEmit(_targetMask.value)
    }

    /**
     * Disengages panic mode and safely restores normal messaging state
     */
    fun disengagePanic() {
        _isPanicActive.value = false
        SoundPoolManager.playConfirm()
        _panicDisengageEvents.tryEmit(Unit)
    }

    /**
     * Biometric / Fingerprint sensor touch override
     */
    fun triggerBiometricOverride() {
        triggerPanicOverride()
    }

    /**
     * Global Panic Button:
     * Instantly terminates/clears current chat session, plays wipe audio, and locks the app
     * with biometric or passcode verification.
     */
    fun triggerGlobalPanicLock() {
        val now = System.currentTimeMillis()
        _lastTriggerTime.value = now
        _panicCount.value++
        SoundPoolManager.playPanicWipe()
        viewModelScope.launch {
            chatRepository.clearAllHistory()
        }
        _panicLockEvents.tryEmit(Unit)
    }
}

class PanicViewModelFactory(
    private val context: Context,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PanicViewModel::class.java)) {
            return PanicViewModel(context.applicationContext, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
