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
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class MusicTrack(val displayName: String, val assetName: String) {
    STABLE_BEAT("Stabil Arka Plan Müziği (Cyber Beat)", "bgm_electro.mp3"),
    CHILL_AMBIENT("Sakin Mod Müziği (Chill Cyber Ambient)", "bgm_chill.mp3"),
    CUSTOM("Cihazdan Seçilen Özel Müzik", "")
}

private data class SynthRecipe(
    val bpm: Int,
    val chords: List<List<Double>>,
    val bassRoots: List<Double>,
    val padWaveMix: Double,
    val arpGain: Double,
    val bassGain: Double
)

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

    private val _customMusicTitle = MutableStateFlow("Chill Cyber Ambient")
    val customMusicTitle: StateFlow<String> = _customMusicTitle.asStateFlow()

    private val _activeSongId = MutableStateFlow("chill_ambient")
    val activeSongId: StateFlow<String> = _activeSongId.asStateFlow()

    private var currentSpeed: Float = 1.0f
    private var currentPitch: Float = 1.0f

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        // Auto-start default Chill Ambient track at 50% volume
        playTrack(MusicTrack.CHILL_AMBIENT)
    }

    fun playTrack(track: MusicTrack, uri: Uri? = null, speed: Float = 1.0f, pitch: Float = 1.0f) {
        val songKey = when (track) {
            MusicTrack.STABLE_BEAT -> "stable_beat"
            MusicTrack.CHILL_AMBIENT -> "chill_ambient"
            MusicTrack.CUSTOM -> "custom_uri"
        }
        if (track != MusicTrack.CUSTOM) {
            _customMusicTitle.value = track.displayName
        }
        playInternal(track = track, songKey = songKey, uri = uri, speed = speed, pitch = pitch)
    }

    fun setCustomRoyaltyFreeTrack(title: String, isChill: Boolean, speed: Float = 1.0f, pitch: Float = 1.0f) {
        _customMusicTitle.value = title
        val slug = title.lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        playInternal(
            track = if (isChill) MusicTrack.CHILL_AMBIENT else MusicTrack.STABLE_BEAT,
            songKey = slug,
            uri = null,
            speed = speed,
            pitch = pitch,
            synthTitle = title
        )
    }

    private fun playInternal(
        track: MusicTrack,
        songKey: String,
        uri: Uri? = null,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        synthTitle: String? = null
    ) {
        val context = appContext ?: return
        try {
            currentSpeed = speed
            currentPitch = pitch

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

            if (track == MusicTrack.CUSTOM) {
                val targetUri = uri ?: _customMusicUri.value
                if (targetUri != null) {
                    player.setDataSource(context, targetUri)
                } else {
                    val wav = ensureSynthWav(context, "stable_beat")
                    player.setDataSource(wav.absolutePath)
                }
            } else if (synthTitle != null) {
                val wav = ensureSynthWav(context, songKey)
                player.setDataSource(wav.absolutePath)
            } else {
                try {
                    val afd: AssetFileDescriptor = context.assets.openFd(track.assetName)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } catch (_: Exception) {
                    val wav = ensureSynthWav(context, songKey)
                    player.setDataSource(wav.absolutePath)
                }
            }

            player.prepare()

            if (synthTitle == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                try {
                    val params = player.playbackParams
                    params.speed = speed.coerceIn(0.5f, 2.0f)
                    params.pitch = pitch.coerceIn(0.5f, 2.0f)
                    player.playbackParams = params
                } catch (_: Exception) {}
            }

            val currentVol = _volume.value
            player.setVolume(currentVol, currentVol)
            player.start()

            mediaPlayer = player
            _currentTrack.value = track
            _activeSongId.value = songKey
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
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun setCustomAudio(uri: Uri, name: String) {
        _customMusicUri.value = uri
        _customMusicTitle.value = name
        _activeSongId.value = "custom_uri"
        playInternal(MusicTrack.CUSTOM, "custom_uri", uri)
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
        } catch (_: Exception) {}
    }

    private fun recipeForKey(songKey: String): SynthRecipe = when (songKey) {
        "neon_glitch_protocol" -> SynthRecipe(
            bpm = 128,
            chords = listOf(
                listOf(220.0, 261.63, 329.63, 440.0),
                listOf(174.61, 220.0, 261.63, 349.23),
                listOf(196.0, 246.94, 293.66, 392.0),
                listOf(164.81, 207.65, 246.94, 329.63)
            ),
            bassRoots = listOf(110.0, 87.31, 98.0, 82.41),
            padWaveMix = 0.25,
            arpGain = 0.22,
            bassGain = 0.24
        )
        "tokyo_rain_lo_fi_beat" -> SynthRecipe(
            bpm = 78,
            chords = listOf(
                listOf(261.63, 311.13, 392.0, 466.16),
                listOf(233.08, 293.66, 349.23, 440.0),
                listOf(207.65, 261.63, 311.13, 392.0),
                listOf(196.0, 246.94, 293.66, 349.23)
            ),
            bassRoots = listOf(130.81, 116.54, 103.83, 98.0),
            padWaveMix = 0.45,
            arpGain = 0.14,
            bassGain = 0.18
        )
        "midnight_synthwave_drift" -> SynthRecipe(
            bpm = 108,
            chords = listOf(
                listOf(293.66, 349.23, 440.0, 523.25),
                listOf(233.08, 293.66, 349.23, 466.16),
                listOf(261.63, 329.63, 392.0, 493.88),
                listOf(220.0, 277.18, 329.63, 440.0)
            ),
            bassRoots = listOf(146.83, 116.54, 130.81, 110.0),
            padWaveMix = 0.30,
            arpGain = 0.20,
            bassGain = 0.22
        )
        "deep_night_ambient_2700k" -> SynthRecipe(
            bpm = 64,
            chords = listOf(
                listOf(196.0, 246.94, 293.66, 369.99),
                listOf(164.81, 196.0, 246.94, 329.63),
                listOf(146.83, 220.0, 261.63, 329.63),
                listOf(130.81, 196.0, 246.94, 293.66)
            ),
            bassRoots = listOf(98.0, 82.41, 73.42, 65.41),
            padWaveMix = 0.55,
            arpGain = 0.09,
            bassGain = 0.16
        )
        "neural_network_pulse" -> SynthRecipe(
            bpm = 136,
            chords = listOf(
                listOf(329.63, 392.0, 493.88, 659.25),
                listOf(293.66, 369.99, 440.0, 587.33),
                listOf(261.63, 329.63, 392.0, 523.25),
                listOf(246.94, 293.66, 369.99, 493.88)
            ),
            bassRoots = listOf(164.81, 146.83, 130.81, 123.47),
            padWaveMix = 0.22,
            arpGain = 0.24,
            bassGain = 0.23
        )
        "coffee_matte_ink" -> SynthRecipe(
            bpm = 84,
            chords = listOf(
                listOf(261.63, 329.63, 392.0, 493.88),
                listOf(220.0, 261.63, 329.63, 392.0),
                listOf(293.66, 349.23, 440.0, 523.25),
                listOf(196.0, 246.94, 293.66, 349.23)
            ),
            bassRoots = listOf(130.81, 110.0, 146.83, 98.0),
            padWaveMix = 0.42,
            arpGain = 0.15,
            bassGain = 0.17
        )
        "grid_runner_2099" -> SynthRecipe(
            bpm = 118,
            chords = listOf(
                listOf(246.94, 293.66, 369.99, 493.88),
                listOf(196.0, 246.94, 293.66, 392.0),
                listOf(220.0, 277.18, 329.63, 440.0),
                listOf(185.0, 220.0, 277.18, 369.99)
            ),
            bassRoots = listOf(123.47, 98.0, 110.0, 92.5),
            padWaveMix = 0.28,
            arpGain = 0.21,
            bassGain = 0.22
        )
        "zero_day_exploit_groove" -> SynthRecipe(
            bpm = 100,
            chords = listOf(
                listOf(207.65, 246.94, 311.13, 415.30),
                listOf(185.0, 220.0, 277.18, 369.99),
                listOf(164.81, 207.65, 246.94, 329.63),
                listOf(196.0, 233.08, 293.66, 392.0)
            ),
            bassRoots = listOf(103.83, 92.50, 82.41, 98.0),
            padWaveMix = 0.30,
            arpGain = 0.19,
            bassGain = 0.25
        )
        "safe_harbor_sleepwave" -> SynthRecipe(
            bpm = 58,
            chords = listOf(
                listOf(220.0, 261.63, 329.63, 392.0),
                listOf(174.61, 220.0, 261.63, 329.63),
                listOf(196.0, 246.94, 293.66, 392.0),
                listOf(164.81, 196.0, 246.94, 329.63)
            ),
            bassRoots = listOf(110.0, 87.31, 98.0, 82.41),
            padWaveMix = 0.60,
            arpGain = 0.08,
            bassGain = 0.15
        )
        "stable_beat" -> SynthRecipe(
            bpm = 116,
            chords = listOf(
                listOf(261.63, 311.13, 392.0, 523.25),
                listOf(233.08, 277.18, 349.23, 466.16)
            ),
            bassRoots = listOf(130.81, 116.54),
            padWaveMix = 0.30,
            arpGain = 0.18,
            bassGain = 0.22
        )
        else -> SynthRecipe(
            bpm = 74,
            chords = listOf(
                listOf(220.0, 261.63, 329.63, 392.0),
                listOf(196.0, 246.94, 293.66, 349.23)
            ),
            bassRoots = listOf(110.0, 98.0),
            padWaveMix = 0.50,
            arpGain = 0.10,
            bassGain = 0.16
        )
    }

    private fun ensureSynthWav(context: Context, songKey: String): File {
        val dir = File(context.cacheDir, "synth_bgm_v2").apply { mkdirs() }
        val file = File(dir, "$songKey.wav")
        if (file.exists() && file.length() > 1024) return file

        val recipe = recipeForKey(songKey)
        val sampleRate = 22050
        val beatSec = 60.0 / recipe.bpm
        val stepsPerChord = 4
        val totalBeats = recipe.chords.size * 2
        val durationSec = totalBeats * beatSec
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)

        val stepSec = beatSec / 2.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val beatIdx = (t / beatSec).toInt()
            val chordIdx = (beatIdx / 2) % recipe.chords.size
            val chord = recipe.chords[chordIdx]
            val bassFreq = recipe.bassRoots[chordIdx]

            val stepIdx = (t / stepSec).toInt()
            val stepLocalT = t - stepIdx * stepSec
            val arpFreq = chord[stepIdx % chord.size]
            val arpEnv = exp(-stepLocalT * 9.0)
            val arpSample = sin(2.0 * PI * arpFreq * t) * arpEnv * recipe.arpGain

            val beatLocalT = t - beatIdx * beatSec
            val bassEnv = exp(-beatLocalT * 4.5)
            val bassSample = (sin(2.0 * PI * bassFreq * t) + 0.35 * sin(4.0 * PI * bassFreq * t)) *
                bassEnv * recipe.bassGain

            var pad = 0.0
            for (f in chord) {
                pad += sin(2.0 * PI * f * t)
            }
            pad = (pad / chord.size) * recipe.padWaveMix

            // Smooth fade-in/out at loop boundaries to prevent clicks
            val edgeFade = when {
                i < 440 -> i / 440.0
                i > totalSamples - 440 -> (totalSamples - i) / 440.0
                else -> 1.0
            }

            val mixed = ((pad + arpSample + bassSample) * edgeFade).coerceIn(-0.95, 0.95)
            pcm[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
        }

        writeWavFile(file, pcm, sampleRate)
        return file
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        val dataSize = pcm.size * 2
        val header = ByteArray(44)
        val totalDataLen = dataSize + 36
        val byteRate = sampleRate * 2

        fun putInt(offset: Int, v: Int) {
            header[offset] = (v and 0xff).toByte()
            header[offset + 1] = ((v shr 8) and 0xff).toByte()
            header[offset + 2] = ((v shr 16) and 0xff).toByte()
            header[offset + 3] = ((v shr 24) and 0xff).toByte()
        }

        fun putShort(offset: Int, v: Int) {
            header[offset] = (v and 0xff).toByte()
            header[offset + 1] = ((v shr 8) and 0xff).toByte()
        }

        "RIFF".toByteArray().copyInto(header, 0)
        putInt(4, totalDataLen)
        "WAVE".toByteArray().copyInto(header, 8)
        "fmt ".toByteArray().copyInto(header, 12)
        putInt(16, 16)
        putShort(20, 1)
        putShort(22, 1)
        putInt(24, sampleRate)
        putInt(28, byteRate)
        putShort(32, 2)
        putShort(34, 16)
        "data".toByteArray().copyInto(header, 36)
        putInt(40, dataSize)

        FileOutputStream(file).use { out ->
            out.write(header)
            val buf = ByteArray(dataSize)
            for (i in pcm.indices) {
                val s = pcm[i].toInt()
                buf[i * 2] = (s and 0xff).toByte()
                buf[i * 2 + 1] = ((s shr 8) and 0xff).toByte()
            }
            out.write(buf)
        }
    }
}
