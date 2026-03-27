package net.ifmain.androiddummy.sensor_ui

import kotlinx.coroutines.flow.StateFlow

interface TiltSensorDataSource {
    val sensorData: StateFlow<SensorData>

    fun startListening()

    fun stopListening()
}

interface HapticFeedbackController {
    fun lightTap()

    fun mediumTap()

    fun strongTap()
}
