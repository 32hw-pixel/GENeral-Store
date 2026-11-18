package com.example.util

import android.content.Context
import android.os.Build
import android.view.Surface
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * RefreshRateConfig: Centralized configuration to enforce and monitor 120Hz display refresh mode,
 * hardware rendering acceleration, and crisp high-refresh Compose scrolling/animations.
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
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val params = window.attributes
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = params
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.display
                        ?: (context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager)
                            ?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
                } else {
                    @Suppress("DEPRECATION")
                    window.windowManager.defaultDisplay
                }
                val modes = display?.supportedModes ?: emptyArray()

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

            // Lock native window Surface frame rate to maximum supported (120Hz) on API 30+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.post {
                    try {
                        val surfaceField = window.decorView.viewRootImplCompat()
                        if (surfaceField != null && surfaceField.isValid) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                surfaceField.setFrameRate(
                                    appliedRate.coerceAtLeast(120.0f),
                                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                                    Surface.CHANGE_FRAME_RATE_ALWAYS
                                )
                            } else {
                                surfaceField.setFrameRate(
                                    appliedRate.coerceAtLeast(120.0f),
                                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                                )
                            }
                        }
                    } catch (_: Throwable) {
                    }
                }
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

    private fun android.view.View.viewRootImplCompat(): Surface? {
        return try {
            val viewRootMethod = android.view.View::class.java.getDeclaredMethod("getViewRootImpl")
            viewRootMethod.isAccessible = true
            val root = viewRootMethod.invoke(this) ?: return null
            val surfaceField = root.javaClass.getDeclaredField("mSurface")
            surfaceField.isAccessible = true
            surfaceField.get(root) as? Surface
        } catch (_: Throwable) {
            null
        }
    }
}
