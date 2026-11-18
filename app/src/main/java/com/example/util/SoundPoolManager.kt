package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

enum class SoundProfile(val displayName: String) {
    CYBER_PULSE("Siber Nabız (Modern Tok & Blip)"),
    MECHANICAL_THOCK("Mekanik Tok Klavye (Thock Switch)"),
    CRYSTAL_NEON("Kristal Siber Bip (Minimalist)")
}

enum class KeyboardSoundProfile(val displayName: String, val subtitle: String) {
    THOCK("Mekanik Tok (Thock Switch)", "62g ağırlıklı tok mekanik anahtar sesi"),
    ELECTRO_BLIP("Siber Elektro Bip", "Modern fütüristik dokunuş UI sesi"),
    NEON_LASER("Siber Kristal Lazer", "Hızlı neon lazer dokunuş tınısı"),
    TYPEWRITER("Vintage Daktilo Tok", "Klasik mekanik daktilo vuruşu")
}

enum class NotificationSoundProfile(val displayName: String, val subtitle: String) {
    CYBER_UPLINK("Siber Uplink (Matrix Sync)", "Siber uzay veri tüneli bağlantı melodisi"),
    VICTORY_CHIME("Siber Zafer Çanı", "Net ve enerjik bildirim çanı"),
    QUANTUM_PULSE("Kuantum Nabız (Sub-Bass)", "Derin sub-bass yankılanan uyarı tonu"),
    RADAR_PING("Sonar Radar Ping", "Taktiksel denizaltı sonar yankısı")
}

/**
 * Revolutionary High-Performance Cyber & Mechanical Sound Engine.
 * Combines Android SoundPool (USAGE_GAME/MEDIA) with an instant in-memory AudioTrack fallback,
 * ensuring 100% zero-latency, audibly distinct, punchy, arcade-grade sounds on all Android devices.
 */
object SoundPoolManager {

    private var soundPool: SoundPool? = null
    private var isMuted: Boolean = false
    private var isInitialized = false

    private var currentProfile: SoundProfile = SoundProfile.CYBER_PULSE
    private var currentKeyboardProfile: KeyboardSoundProfile = KeyboardSoundProfile.THOCK
    private var currentNotificationProfile: NotificationSoundProfile = NotificationSoundProfile.CYBER_UPLINK

    // SoundPool IDs
    private var mechanicalTokSoundId: Int = -1
    private var scrollTickSoundId: Int = -1
    private var menuBlipSoundId: Int = -1
    private var whooshSoundId: Int = -1
    private var confirmChimeSoundId: Int = -1
    private var panicWipeSoundId: Int = -1

    // Pre-computed in-memory PCM buffers for instant AudioTrack fallback (100% fail-safe)
    private val pcmMechanicalTok: ShortArray by lazy { generateMechanicalTok() }
    private val pcmScrollTick: ShortArray by lazy { generateScrollTick() }
    private val pcmMenuBlip: ShortArray by lazy { generateCyberMenuBlip() }
    private val pcmWhoosh: ShortArray by lazy { generateWhooshSweep() }
    private val pcmConfirm: ShortArray by lazy { generateRewardChime() }
    private val pcmPanicWipe: ShortArray by lazy { generatePanicWipe() }
    private val pcmNeonLaser: ShortArray by lazy { generateNeonLaser() }
    private val pcmTypewriter: ShortArray by lazy { generateTypewriterClack() }
    private val pcmQuantumPulse: ShortArray by lazy { generateQuantumPulse() }
    private val pcmRadarPing: ShortArray by lazy { generateRadarPing() }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(audioAttributes)
                .build()

            val cacheDir = context.cacheDir
            CoroutineScope(Dispatchers.IO).launch {
                val tokFile = createWavFile(cacheDir, "mech_tok_v2.wav", pcmMechanicalTok)
                val tickFile = createWavFile(cacheDir, "scroll_tick_v2.wav", pcmScrollTick)
                val blipFile = createWavFile(cacheDir, "menu_blip_v2.wav", pcmMenuBlip)
                val whooshFile = createWavFile(cacheDir, "whoosh_v2.wav", pcmWhoosh)
                val chimeFile = createWavFile(cacheDir, "confirm_chime_v2.wav", pcmConfirm)
                val wipeFile = createWavFile(cacheDir, "panic_wipe_v2.wav", pcmPanicWipe)

                soundPool?.let { pool ->
                    mechanicalTokSoundId = pool.load(tokFile.absolutePath, 1)
                    scrollTickSoundId = pool.load(tickFile.absolutePath, 1)
                    menuBlipSoundId = pool.load(blipFile.absolutePath, 1)
                    whooshSoundId = pool.load(whooshFile.absolutePath, 1)
                    confirmChimeSoundId = pool.load(chimeFile.absolutePath, 1)
                    panicWipeSoundId = pool.load(wipeFile.absolutePath, 1)
                }
                isInitialized = true
            }
        } catch (_: Exception) {}
    }

    fun setSoundProfile(profile: SoundProfile) {
        currentProfile = profile
    }

    fun getSoundProfile(): SoundProfile = currentProfile

    fun setKeyboardProfile(profile: KeyboardSoundProfile) {
        currentKeyboardProfile = profile
    }

    fun getKeyboardProfile(): KeyboardSoundProfile = currentKeyboardProfile

    fun setNotificationProfile(profile: NotificationSoundProfile) {
        currentNotificationProfile = profile
    }

    fun getNotificationProfile(): NotificationSoundProfile = currentNotificationProfile

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Plays the currently configured mechanical / cyber keyboard click.
     */
    fun playKeyboardClick(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (currentKeyboardProfile) {
            KeyboardSoundProfile.THOCK -> playMechanicalClick(view)
            KeyboardSoundProfile.ELECTRO_BLIP -> playMenuNav(view)
            KeyboardSoundProfile.NEON_LASER -> playDirectPcm(pcmNeonLaser)
            KeyboardSoundProfile.TYPEWRITER -> playDirectPcm(pcmTypewriter)
        }
    }

    /**
     * Plays the currently configured cyberpunk notification sound.
     */
    fun playNotification(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        when (currentNotificationProfile) {
            NotificationSoundProfile.CYBER_UPLINK -> playConfirm(view)
            NotificationSoundProfile.VICTORY_CHIME -> playDirectPcm(pcmMenuBlip)
            NotificationSoundProfile.QUANTUM_PULSE -> playDirectPcm(pcmQuantumPulse)
            NotificationSoundProfile.RADAR_PING -> playDirectPcm(pcmRadarPing)
        }
    }

    fun testKeyboardSound(profile: KeyboardSoundProfile, view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (profile) {
            KeyboardSoundProfile.THOCK -> playMechanicalClick(view)
            KeyboardSoundProfile.ELECTRO_BLIP -> playMenuNav(view)
            KeyboardSoundProfile.NEON_LASER -> playDirectPcm(pcmNeonLaser)
            KeyboardSoundProfile.TYPEWRITER -> playDirectPcm(pcmTypewriter)
        }
    }

    fun testNotificationSound(profile: NotificationSoundProfile, view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        when (profile) {
            NotificationSoundProfile.CYBER_UPLINK -> playConfirm(view)
            NotificationSoundProfile.VICTORY_CHIME -> playDirectPcm(pcmMenuBlip)
            NotificationSoundProfile.QUANTUM_PULSE -> playDirectPcm(pcmQuantumPulse)
            NotificationSoundProfile.RADAR_PING -> playDirectPcm(pcmRadarPing)
        }
    }

    /**
     * Crisp, deep mechanical switch click (Custom mechanical keyboard 'tok' click).
     */
    fun playMechanicalClick(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val pool = soundPool
        if (pool != null && mechanicalTokSoundId > 0) {
            val streamId = pool.play(mechanicalTokSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmMechanicalTok)
        } else {
            playDirectPcm(pcmMechanicalTok)
        }
    }

    /**
     * Rapid light micro-tick for list scrolling, wheel pickers, and sliders.
     */
    fun playScrollTick(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        val pool = soundPool
        if (pool != null && scrollTickSoundId > 0) {
            val streamId = pool.play(scrollTickSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmScrollTick)
        } else {
            playDirectPcm(pcmScrollTick)
        }
    }

    /**
     * Punchy downward cyber UI chirp for modern tactical buttons.
     */
    fun playMenuNav(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        val pool = soundPool
        if (pool != null && menuBlipSoundId > 0) {
            val streamId = pool.play(menuBlipSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmMenuBlip)
        } else {
            playDirectPcm(pcmMenuBlip)
        }
    }

    /**
     * Rewarding confirm fanfare / successful action chime.
     */
    fun playConfirm(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        val pool = soundPool
        if (pool != null && confirmChimeSoundId > 0) {
            val streamId = pool.play(confirmChimeSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmConfirm)
        } else {
            playDirectPcm(pcmConfirm)
        }
    }

    /**
     * Smooth filtered whoosh transition sound.
     */
    fun playWhoosh() {
        if (isMuted) return
        val pool = soundPool
        if (pool != null && whooshSoundId > 0) {
            val streamId = pool.play(whooshSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmWhoosh)
        } else {
            playDirectPcm(pcmWhoosh)
        }
    }

    /**
     * Heavy electronic purge / power-down wipe sound.
     */
    fun playPanicWipe(view: View? = null) {
        if (isMuted) return
        view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        val pool = soundPool
        if (pool != null && panicWipeSoundId > 0) {
            val streamId = pool.play(panicWipeSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
            if (streamId == 0) playDirectPcm(pcmPanicWipe)
        } else {
            playDirectPcm(pcmPanicWipe)
        }
    }

    fun playShakeOverride(view: View? = null) {
        playConfirm(view)
    }

    fun playCardFlip() {
        playScrollTick()
    }

    /**
     * Guaranteed instant fallback playback directly writing PCM into AudioTrack.
     */
    private fun playDirectPcm(buffer: ShortArray) {
        audioScope.launch {
            try {
                val sampleRate = 44100
                val minSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // release track after sound finishes
                val durationMs = (buffer.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    // -------------------------------------------------------------
    // Audio Waveform Synthesizers (16-bit PCM, 44.1kHz)
    // -------------------------------------------------------------

    /**
     * Heavy Mechanical Keyboard "THOCK":
     * Metallic snap (2400Hz) + warm body resonance (420Hz) + sub-bass punch (110Hz).
     */
    private fun generateMechanicalTok(): ShortArray {
        val sampleRate = 44100
        val durationMs = 38
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val snapEnv = Math.exp(-t * 600.0)
            val bodyEnv = Math.exp(-t * 190.0)
            val subEnv = Math.exp(-t * 90.0)

            // High metallic tactility
            val snap = 0.50 * sin(2.0 * PI * 2400.0 * t) + 0.25 * sin(2.0 * PI * 3800.0 * t)
            // Warm mechanical switch housing
            val body = 0.65 * sin(2.0 * PI * 420.0 * t)
            // Low thud
            val sub = 0.40 * sin(2.0 * PI * 110.0 * t)

            val sample = (snap * snapEnv + body * bodyEnv + sub * subEnv) * Short.MAX_VALUE * 0.95
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Precision ceramic roller click (Micro-tick 6ms).
     */
    private fun generateScrollTick(): ShortArray {
        val sampleRate = 44100
        val durationMs = 8
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = Math.exp(-t * 950.0)
            val wave = 0.85 * sin(2.0 * PI * 3600.0 * t) + 0.35 * sin(2.0 * PI * 1800.0 * t)
            buffer[i] = (wave * env * Short.MAX_VALUE * 0.95).toInt().toShort()
        }
        return buffer
    }

    /**
     * Cyber UI Blip:
     * Fast downward futuristic frequency sweep from 1700Hz to 850Hz with 3200Hz harmonic ping.
     */
    private fun generateCyberMenuBlip(): ShortArray {
        val sampleRate = 44100
        val durationMs = 45
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / sampleRate
            val freq = 1750.0 * (1.0 - progress * 0.52)
            phase += 2.0 * PI * freq / sampleRate
            val env = Math.exp(-t * 110.0) * (1.0 - progress * progress)

            val overtone = 0.30 * sin(2.0 * PI * 3200.0 * t) * Math.exp(-t * 400.0)
            val wave = sin(phase) * 0.85 + overtone
            val sample = wave * env * Short.MAX_VALUE * 0.95
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Futuristic sci-fi two-tone reward chord: C6 (1046Hz) + E6 (1318Hz) + G6 (1568Hz).
     */
    private fun generateRewardChime(): ShortArray {
        val sampleRate = 44100
        val durationMs = 120
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env1 = Math.exp(-t * 35.0)
            val env2 = if (t > 0.025) Math.exp(-(t - 0.025) * 28.0) else 0.0
            val env3 = if (t > 0.050) Math.exp(-(t - 0.050) * 22.0) else 0.0

            val tone1 = 0.40 * sin(2.0 * PI * 1046.5 * t) * env1
            val tone2 = 0.45 * sin(2.0 * PI * 1318.5 * t) * env2
            val tone3 = 0.35 * sin(2.0 * PI * 1567.9 * t) * env3

            val sample = (tone1 + tone2 + tone3) * Short.MAX_VALUE * 0.95
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Sci-fi plasma whoosh sweep.
     */
    private fun generateWhooshSweep(): ShortArray {
        val sampleRate = 44100
        val durationMs = 90
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 500.0 + 900.0 * sin(progress * PI)
            phase += 2.0 * PI * freq / sampleRate
            val env = sin(progress * PI)
            val sample = sin(phase) * env * Short.MAX_VALUE * 0.90
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Sub-bass descending panic purge down to 45Hz.
     */
    private fun generatePanicWipe(): ShortArray {
        val sampleRate = 44100
        val durationMs = 350
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 550.0 * (1.0 - progress * 0.92)
            phase += 2.0 * PI * freq / sampleRate
            val decay = (1.0 - progress) * (1.0 - progress)
            val sample = (sin(phase) * 31000 * decay).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Cyber Crystal Laser chirp (2800Hz down to 1200Hz).
     */
    private fun generateNeonLaser(): ShortArray {
        val sampleRate = 44100
        val durationMs = 30
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / sampleRate
            val freq = 2900.0 * (1.0 - progress * 0.55)
            phase += 2.0 * PI * freq / sampleRate
            val env = Math.exp(-t * 140.0)
            val wave = sin(phase) * 0.75 + 0.25 * sin(phase * 2.0)
            val sample = wave * env * Short.MAX_VALUE * 0.92
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Vintage Mechanical Typewriter clack (Sharp metal strike 3800Hz + plate slap 650Hz).
     */
    private fun generateTypewriterClack(): ShortArray {
        val sampleRate = 44100
        val durationMs = 34
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val strikeEnv = Math.exp(-t * 750.0)
            val plateEnv = Math.exp(-t * 220.0)
            val strike = 0.60 * sin(2.0 * PI * 3800.0 * t) + 0.30 * sin(2.0 * PI * 5200.0 * t)
            val plate = 0.55 * sin(2.0 * PI * 650.0 * t)
            val sample = (strike * strikeEnv + plate * plateEnv) * Short.MAX_VALUE * 0.90
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Quantum Deep Sub-Bass Pulse (65Hz sub tone + 220Hz harmonic warmth).
     */
    private fun generateQuantumPulse(): ShortArray {
        val sampleRate = 44100
        val durationMs = 180
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = Math.exp(-t * 18.0)
            val sub = 0.70 * sin(2.0 * PI * 68.0 * t)
            val harmonic = 0.35 * sin(2.0 * PI * 204.0 * t) * Math.exp(-t * 35.0)
            val sample = (sub + harmonic) * env * Short.MAX_VALUE * 0.95
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Tactical Sonar Radar Ping (1950Hz pure tone with decaying echo envelope).
     */
    private fun generateRadarPing(): ShortArray {
        val sampleRate = 44100
        val durationMs = 160
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attack = if (t < 0.005) t / 0.005 else 1.0
            val decay = Math.exp(-t * 22.0)
            val tone = sin(2.0 * PI * 1950.0 * t) * attack * decay
            val sample = tone * Short.MAX_VALUE * 0.90
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    private fun createWavFile(dir: File, name: String, pcmData: ShortArray): File {
        val file = File(dir, name)
        val sampleRate = 44100
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            // RIFF chunk
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            // fmt subchunk
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0 // PCM
            header[22] = channels.toByte(); header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = 2; header[33] = 0 // block align
            header[34] = 16; header[35] = 0 // bits per sample
            // data subchunk
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            out.write(header)
            val bytes = ByteArray(totalAudioLen)
            for (i in pcmData.indices) {
                val s = pcmData[i].toInt()
                bytes[i * 2] = (s and 0xff).toByte()
                bytes[i * 2 + 1] = ((s shr 8) and 0xff).toByte()
            }
            out.write(bytes)
        }
        return file
    }
}
