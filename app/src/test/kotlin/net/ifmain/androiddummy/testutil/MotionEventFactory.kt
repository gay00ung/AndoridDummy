package net.ifmain.androiddummy.testutil

import android.content.Context
import android.view.MotionEvent

data class HistoricalTouchPoint(
    val eventTime: Long,
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val size: Float = 1f,
    val touchMajor: Float = size,
    val touchMinor: Float = size,
)

fun configureDisplayMetrics(
    context: Context,
    widthPixels: Int = 1080,
    heightPixels: Int = 2400,
) {
    context.resources.displayMetrics.widthPixels = widthPixels
    context.resources.displayMetrics.heightPixels = heightPixels
}

fun singlePointerMotionEvent(
    action: Int,
    x: Float,
    y: Float,
    downTime: Long,
    eventTime: Long,
    pressure: Float = 1f,
    size: Float = 1f,
    touchMajor: Float = size,
    touchMinor: Float = size,
    edgeFlags: Int = 0,
    toolType: Int = MotionEvent.TOOL_TYPE_FINGER,
    history: List<HistoricalTouchPoint> = emptyList(),
): MotionEvent {
    val pointerProperties = arrayOf(
        MotionEvent.PointerProperties().apply {
            id = 0
            this.toolType = toolType
        }
    )
    val pointerCoords = arrayOf(
        pointerCoords(
            x = x,
            y = y,
            pressure = pressure,
            size = size,
            touchMajor = touchMajor,
            touchMinor = touchMinor,
        )
    )

    return MotionEvent.obtain(
        downTime,
        eventTime,
        action,
        1,
        pointerProperties,
        pointerCoords,
        0,
        0,
        1f,
        1f,
        0,
        edgeFlags,
        0,
        0,
    ).apply {
        history.forEach { point ->
            addBatch(
                point.eventTime,
                arrayOf(
                    pointerCoords(
                        x = point.x,
                        y = point.y,
                        pressure = point.pressure,
                        size = point.size,
                        touchMajor = point.touchMajor,
                        touchMinor = point.touchMinor,
                    )
                ),
                0,
            )
        }
    }
}

private fun pointerCoords(
    x: Float,
    y: Float,
    pressure: Float,
    size: Float,
    touchMajor: Float,
    touchMinor: Float,
): MotionEvent.PointerCoords {
    return MotionEvent.PointerCoords().apply {
        this.x = x
        this.y = y
        this.pressure = pressure
        this.size = size
        this.touchMajor = touchMajor
        this.touchMinor = touchMinor
    }
}
