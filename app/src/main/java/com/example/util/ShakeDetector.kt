package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * High-Precision Accelerometer Shake Detector for Panic Mode.
 * Tracks 3-axis G-force and triggers when rapid violent shaking (>2.4G) is detected.
 */
class ShakeDetector(
    context: Context,
    private val onShakeListener: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var shakeCount = 0
    private var lastShakeTimestamp = 0L

    private val _currentGForce = MutableStateFlow(1.0f)
    val currentGForce: StateFlow<Float> = _currentGForce.asStateFlow()

    private val _accelX = MutableStateFlow(0f)
    val accelX: StateFlow<Float> = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(0f)
    val accelY: StateFlow<Float> = _accelY.asStateFlow()

    private val _accelZ = MutableStateFlow(9.8f)
    val accelZ: StateFlow<Float> = _accelZ.asStateFlow()

    var sensitivityG: Float = 2.4f
    var isEnabled: Boolean = true

    fun start() {
        shakeCount = 0
        lastShakeTimestamp = 0L
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        _accelX.value = x
        _accelY.value = y
        _accelZ.value = z

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
        _currentGForce.value = gForce

        if (gForce > sensitivityG) {
            val now = System.currentTimeMillis()
            if (lastShakeTimestamp + 500 > now) {
                return
            }

            // At least 2 rapid directional shakes within 700ms
            if (lastShakeTimestamp + 700 < now) {
                shakeCount = 0
            }

            lastShakeTimestamp = now
            shakeCount++

            if (shakeCount >= 2) {
                shakeCount = 0
                onShakeListener()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Convenient Compose DisposableEffect for auto lifecycle-managed shake detection.
 */
@androidx.compose.runtime.Composable
fun RememberShakeDetector(
    enabled: Boolean = true,
    sensitivityG: Float = 2.4f,
    onShake: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.DisposableEffect(enabled, sensitivityG) {
        if (!enabled) return@DisposableEffect onDispose {}
        val detector = ShakeDetector(context, onShake).apply {
            this.sensitivityG = sensitivityG
            this.isEnabled = true
            start()
        }
        onDispose {
            detector.stop()
        }
    }
}
