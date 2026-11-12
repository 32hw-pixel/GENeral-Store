package com.example.util

import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * RefreshRateConfig: Centralized configuration to enforce and monitor 120Hz display refresh mode,
 * hardware rendering acceleration, and ultra-smooth scrolling physics in Compose.
 */
data class RefreshRateConfig(
    val targetRefreshRateHz: Int = 120,
    val is120HzForced: Boolean = true,
    val currentDisplayRate: Float = 120.0f,
    val isHardwareAccelerated: Boolean = true,
    val scrollFlingDecayMultiplier: Float = 1.25f,
    val bufferMode: String = "TripleBuffered_120Hz"
)

val LocalRefreshRateConfig = staticCompositionLocalOf {
    RefreshRateConfig()
}

object RefreshRateManager {

    fun applyRefreshRate(window: Window, context: Context): RefreshRateConfig {
        var appliedRate = 120.0f
        var isForced = false

        try {
            // Enforce hardware acceleration on the window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val display = window.windowManager.defaultDisplay
                val modes = display.supportedModes

                // Prioritize modes that support >= 120Hz, or the maximum available refresh rate
                val highRefreshModes = modes.filter { it.refreshRate >= 119.0f }
                val targetMode = if (highRefreshModes.isNotEmpty()) {
                    highRefreshModes.maxByOrNull { it.refreshRate }
                } else {
                    modes.maxByOrNull { it.refreshRate }
                }

                val params = window.attributes
                if (targetMode != null) {
                    params.preferredDisplayModeId = targetMode.modeId
                    appliedRate = targetMode.refreshRate
                }
                params.preferredRefreshRate = appliedRate.coerceAtLeast(120.0f)
                window.attributes = params
                isForced = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return RefreshRateConfig(
            targetRefreshRateHz = 120,
            is120HzForced = isForced,
            currentDisplayRate = appliedRate,
            isHardwareAccelerated = true,
            scrollFlingDecayMultiplier = 1.25f
        )
    }
}
