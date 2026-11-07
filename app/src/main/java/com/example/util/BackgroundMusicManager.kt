package com.example.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

enum class MusicTrack(val displayName: String, val assetName: String) {
    STABLE_BEAT("Stabil Arka Plan Müziği (Cyber Beat)", "bgm_electro.mp3"),
    CHILL_AMBIENT("Sakin Mod Müziği (Chill Cyber Ambient)", "bgm_chill.mp3"),
    CUSTOM("Cihazdan Seçilen Özel Müzik", "")
}

object BackgroundMusicManager {

    private var appContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow(MusicTrack.CHILL_AMBIENT)
    val currentTrack: StateFlow<MusicTrack> = _currentTrack.asStateFlow()

    private val _volume = MutableStateFlow(0.50f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _customMusicUri = MutableStateFlow<Uri?>(null)
    val customMusicUri: StateFlow<Uri?> = _customMusicUri.asStateFlow()

    private val _customMusicTitle = MutableStateFlow<String>("Chill Cyber Ambient")
    val customMusicTitle: StateFlow<String> = _customMusicTitle.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        // Auto-start default Chill Ambient track at 50% volume
        playTrack(MusicTrack.CHILL_AMBIENT)
    }

    fun playTrack(track: MusicTrack, uri: Uri? = null) {
        val context = appContext ?: return
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
            }

            when (track) {
                MusicTrack.STABLE_BEAT, MusicTrack.CHILL_AMBIENT -> {
                    val afd: AssetFileDescriptor = context.assets.openFd(track.assetName)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }
                MusicTrack.CUSTOM -> {
                    val targetUri = uri ?: _customMusicUri.value
                    if (targetUri != null) {
                        player.setDataSource(context, targetUri)
                    } else {
                        // Fallback to stable beat
                        val afd = context.assets.openFd(MusicTrack.STABLE_BEAT.assetName)
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    }
                }
            }

            player.prepare()
            val currentVol = _volume.value
            player.setVolume(currentVol, currentVol)
            player.start()

            mediaPlayer = player
            _currentTrack.value = track
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                }
            }
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    val vol = _volume.value
                    it.setVolume(vol, vol)
                    it.start()
                    _isPlaying.value = true
                }
            }
        } catch (_: Exception) {}
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null && player.isPlaying) {
            pause()
        } else if (player != null) {
            resume()
        } else {
            playTrack(_currentTrack.value)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        mediaPlayer?.let { player ->
            player.setVolume(clamped, clamped)
            if (clamped == 0f && player.isPlaying) {
                // Muted
            }
        }
    }

    fun setCustomAudio(uri: Uri, name: String) {
        _customMusicUri.value = uri
        _customMusicTitle.value = name
        playTrack(MusicTrack.CUSTOM, uri)
    }

    fun setCustomRoyaltyFreeTrack(title: String, isChill: Boolean) {
        _customMusicTitle.value = title
        playTrack(if (isChill) MusicTrack.CHILL_AMBIENT else MusicTrack.STABLE_BEAT)
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
        } catch (_: Exception) {}
    }
}
