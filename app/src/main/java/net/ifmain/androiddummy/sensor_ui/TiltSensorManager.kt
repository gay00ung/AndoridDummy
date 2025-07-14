package net.ifmain.androiddummy.sensor_ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class TiltSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    // 성능 최적화를 위한 필터링
    private var lastUpdateTime = 0L
    private val updateInterval = 16L // ~60fps
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val threshold = 0.1f // 최소 변화량

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val currentTime = System.currentTimeMillis()

                // 업데이트 빈도 제한
                if (currentTime - lastUpdateTime < updateInterval) return

                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // 노이즈 필터링 - 최소 변화량 이상일 때만 업데이트
                if (abs(x - lastX) > threshold ||
                    abs(y - lastY) > threshold ||
                    abs(z - lastZ) > threshold) {

                    // 저역 통과 필터 적용 (부드러운 움직임)
                    val alpha = 0.8f
                    val filteredX = alpha * lastX + (1 - alpha) * x
                    val filteredY = alpha * lastY + (1 - alpha) * y
                    val filteredZ = alpha * lastZ + (1 - alpha) * z

                    _sensorData.value = SensorData(
                        x = filteredX,
                        y = filteredY,
                        z = filteredZ
                    )

                    lastX = filteredX
                    lastY = filteredY
                    lastZ = filteredZ
                    lastUpdateTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}