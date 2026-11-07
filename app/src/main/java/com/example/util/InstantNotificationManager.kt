package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InstantNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val iconEmoji: String = "🔔",
    val isAlert: Boolean = false
)

object InstantNotificationManager {
    private const val CHANNEL_ID = "general_chat_instant_channel"
    private const val CHANNEL_NAME = "GENeral Chat Bildirimleri"

    private val _currentNotification = MutableStateFlow<InstantNotification?>(null)
    val currentNotification = _currentNotification.asStateFlow()

    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun init(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "GENeral Chat uçtan uca şifreli mesaj ve güvenlik bildirimleri"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun show(
        title: String,
        message: String,
        iconEmoji: String = "🔔",
        isAlert: Boolean = false,
        durationMs: Long = 3500L
    ) {
        val notification = InstantNotification(
            title = title,
            message = message,
            iconEmoji = iconEmoji,
            isAlert = isAlert
        )

        // Update in-app banner
        _currentNotification.value = notification
        SoundPoolManager.playNotification()

        // Android System Notification Dispatch
        appContext?.let { ctx ->
            try {
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("$iconEmoji $title")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            } catch (_: Exception) {
                // Ignore missing system notification permissions gracefully
            }
        }

        // Auto dismiss in-app HUD
        scope.launch {
            delay(durationMs)
            if (_currentNotification.value?.id == notification.id) {
                _currentNotification.value = null
            }
        }
    }

    fun dismiss() {
        _currentNotification.value = null
    }
}
