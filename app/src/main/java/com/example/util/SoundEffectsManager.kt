package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural Copyright-Free Cyber & Mechanical Sound Effects Engine.
 * Generates custom mechanical wheel clicks (alarm clock roller tick) and
 * sci-fi menu navigation blips (arcade racing game menu beep) in-memory using AudioTrack.
 */
object SoundEffectsManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted: Boolean = false

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Mechanical Wheel / Roller Tick:
     * Damped high-frequency impulse (~2200 Hz decaying exponentially over 12ms),
     * precisely mimicking the mechanical dial / alarm clock wheel click from user's video.
     */
    fun playMechanicalClick() {
        if (isMuted) return
        SoundPoolManager.playMechanicalClick()
    }

    fun playMenuNavSound() {
        if (isMuted) return
        SoundPoolManager.playMenuNav()
    }

    fun playConfirmSound() {
        if (isMuted) return
        SoundPoolManager.playConfirm()
    }

    fun playWhooshSound() {
        if (isMuted) return
        SoundPoolManager.playWhoosh()
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }
}
