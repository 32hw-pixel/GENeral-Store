package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * VoiceNoteRecorder:
 * Uses the device microphone via Android MediaRecorder to capture AAC/M4A voice notes.
 */
class VoiceNoteRecorder(private val context: Context) {
    private val TAG = "VoiceNoteRecorder"
    private var recorder: MediaRecorder? = null
    var isRecording: Boolean = false
        private set
    var currentFile: File? = null
        private set

    fun start(): File? {
        val outputFile = File(context.cacheDir, "vn_${System.currentTimeMillis()}.m4a")
        return try {
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            isRecording = true
            currentFile = outputFile
            Log.d(TAG, "Recording started into: ${outputFile.absolutePath}")
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording: ${e.message}", e)
            recorder?.release()
            recorder = null
            isRecording = false
            currentFile = null
            null
        }
    }

    fun stop(): File? {
        if (!isRecording) return null
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            val file = currentFile
            Log.d(TAG, "Recording stopped, file size: ${file?.length()} bytes")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder: ${e.message}", e)
            recorder?.release()
            recorder = null
            isRecording = false
            null
        }
    }

    fun cancel() {
        if (isRecording) {
            try {
                recorder?.apply {
                    stop()
                    release()
                }
            } catch (_: Exception) {}
            recorder = null
            isRecording = false
            currentFile?.delete()
            currentFile = null
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) recorder?.maxAmplitude ?: 0 else 0
        } catch (_: Exception) {
            0
        }
    }
}
