package net.ifmain.androiddummy.touch_pattern

import android.view.MotionEvent
import android.view.VelocityTracker
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class TouchPatternAnalyzer {
    private val touchEvents = mutableListOf<TouchEventData>()
    private val touchPatterns = mutableListOf<TouchPattern>()
    private var velocityTracker: VelocityTracker? = null
    private var lastTapTime = 0L
    
    fun startTracking() {
        touchEvents.clear()
        touchPatterns.clear()
        velocityTracker?.recycle()
        velocityTracker = VelocityTracker.obtain()
    }
    
    fun processEvent(event: MotionEvent) {
        velocityTracker?.addMovement(event)
        
        val touchEventData = TouchEventData(
            x = event.x,
            y = event.y,
            pressure = event.pressure,
            touchMajor = event.getTouchMajor(0),
            touchMinor = event.getTouchMinor(0),
            size = event.getSize(0),
            toolType = event.getToolType(0),
            orientation = event.getOrientation(0),
            eventTime = event.eventTime,
            downTime = event.downTime,
            action = event.action,
            pointerCount = event.pointerCount,
            edgeFlags = event.edgeFlags
        )
        
        touchEvents.add(touchEventData)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(event)
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(event)
            }
        }
    }
    
    private fun handleActionDown(event: MotionEvent) {
        val tapInterval = if (lastTapTime > 0) {
            event.downTime - lastTapTime
        } else null
        
        lastTapTime = event.downTime
    }
    
    private fun handleActionUp(event: MotionEvent) {
        val duration = event.eventTime - event.downTime
        val positions = mutableListOf<Pair<Float, Float>>()
        val pressures = mutableListOf<Float>()
        val velocities = mutableListOf<Pair<Float, Float>>()
        val touchSizes = mutableListOf<Float>()
        
        // Collect historical data
        for (i in 0 until event.historySize) {
            positions.add(Pair(event.getHistoricalX(i), event.getHistoricalY(i)))
            pressures.add(event.getHistoricalPressure(i))
            touchSizes.add(event.getHistoricalSize(i))
        }
        
        // Add current position
        positions.add(Pair(event.x, event.y))
        pressures.add(event.pressure)
        touchSizes.add(event.getSize(0))
        
        // Calculate velocities
        velocityTracker?.let { tracker ->
            tracker.computeCurrentVelocity(1000) // pixels per second
            val xVel = tracker.getXVelocity(0)
            val yVel = tracker.getYVelocity(0)
            velocities.add(Pair(xVel, yVel))
        }
        
        val tapInterval = if (touchPatterns.isNotEmpty()) {
            event.downTime - touchPatterns.last().downTime
        } else null
        
        val pattern = TouchPattern(
            downTime = event.downTime,
            eventTime = event.eventTime,
            duration = duration,
            tapInterval = tapInterval,
            positions = positions,
            pressures = pressures,
            velocities = velocities,
            touchSizes = touchSizes
        )
        
        touchPatterns.add(pattern)
    }
    
    fun analyzeCharacteristics(): TouchCharacteristics {
        if (touchPatterns.isEmpty()) {
            return TouchCharacteristics(0f, 0f, 0f, 0f, 0f, 0L, 0f, 0f, 0f, 0f, 0f)
        }
        
        val allPressures = touchPatterns.flatMap { it.pressures }
        val avgPressure = allPressures.average().toFloat()
        val pressureVariance = calculateVariance(allPressures)
        
        val allSpeeds = mutableListOf<Float>()
        touchPatterns.forEach { pattern ->
            pattern.velocities.forEach { (xVel, yVel) ->
                val speed = sqrt(xVel * xVel + yVel * yVel)
                allSpeeds.add(speed)
            }
        }
        
        val avgSpeed = if (allSpeeds.isNotEmpty()) allSpeeds.average().toFloat() else 0f
        val maxSpeed = allSpeeds.maxOrNull() ?: 0f
        
        val allTouchSizes = touchPatterns.flatMap { it.touchSizes }
        val avgTouchSize = allTouchSizes.average().toFloat()
        
        val avgTapDuration = touchPatterns.map { it.duration }.average().toLong()
        
        val tremor = calculateTremor()
        val dragSmoothness = calculateDragSmoothness()
        val (totalDistance, directDistance) = calculateDistances()
        val pathEfficiency = if (totalDistance > 0) directDistance / totalDistance else 1f
        
        return TouchCharacteristics(
            avgPressure = avgPressure,
            pressureVariance = pressureVariance,
            avgSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            avgTouchSize = avgTouchSize,
            avgTapDuration = avgTapDuration,
            tremor = tremor,
            dragSmoothness = dragSmoothness,
            totalDistance = totalDistance,
            directDistance = directDistance,
            pathEfficiency = pathEfficiency
        )
    }
    
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average().toFloat()
        return values.map { (it - mean).pow(2) }.average().toFloat()
    }
    
    private fun calculateTremor(): Float {
        if (touchPatterns.isEmpty()) return 0f
        
        var totalTremor = 0f
        var count = 0
        
        touchPatterns.forEach { pattern ->
            if (pattern.positions.size >= 3) {
                for (i in 1 until pattern.positions.size - 1) {
                    val prev = pattern.positions[i - 1]
                    val curr = pattern.positions[i]
                    val next = pattern.positions[i + 1]
                    
                    val angle1 = calculateAngle(prev, curr)
                    val angle2 = calculateAngle(curr, next)
                    val angleChange = abs(angle2 - angle1)
                    
                    totalTremor += angleChange
                    count++
                }
            }
        }
        
        return if (count > 0) totalTremor / count else 0f
    }
    
    private fun calculateAngle(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        return kotlin.math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
    }
    
    private fun calculateDragSmoothness(): Float {
        if (touchPatterns.isEmpty()) return 0f
        
        val dragPatterns = touchPatterns.filter { it.positions.size > 5 }
        if (dragPatterns.isEmpty()) return 1f
        
        var totalSmoothness = 0f
        
        dragPatterns.forEach { pattern ->
            val distances = mutableListOf<Float>()
            for (i in 1 until pattern.positions.size) {
                val dist = distance(pattern.positions[i - 1], pattern.positions[i])
                distances.add(dist)
            }
            
            val avgDistance = distances.average().toFloat()
            val variance = calculateVariance(distances)
            val smoothness = 1f / (1f + variance / avgDistance)
            totalSmoothness += smoothness
        }
        
        return totalSmoothness / dragPatterns.size
    }
    
    private fun calculateDistances(): Pair<Float, Float> {
        var totalDistance = 0f
        var directDistance = 0f
        
        touchPatterns.forEach { pattern ->
            if (pattern.positions.size >= 2) {
                val start = pattern.positions.first()
                val end = pattern.positions.last()
                directDistance += distance(start, end)
                
                for (i in 1 until pattern.positions.size) {
                    totalDistance += distance(pattern.positions[i - 1], pattern.positions[i])
                }
            }
        }
        
        return Pair(totalDistance, directDistance)
    }
    
    private fun distance(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {
        val dx = p2.first - p1.first
        val dy = p2.second - p1.second
        return sqrt(dx * dx + dy * dy)
    }
    
    fun stopTracking() {
        velocityTracker?.recycle()
        velocityTracker = null
    }
    
    fun getTouchPatterns(): List<TouchPattern> = touchPatterns.toList()
    fun getTouchEvents(): List<TouchEventData> = touchEvents.toList()
}