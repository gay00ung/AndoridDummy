package net.ifmain.androiddummy.touch_pattern

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TouchPatternCollectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val touchPatternAnalyzer = TouchPatternAnalyzer()
    private val elderlyTouchAnalyzer = ElderlyTouchAnalyzer()
    
    private val touchPath = Path()
    private val touchPoints = mutableListOf<Pair<Float, Float>>()
    
    private val pathPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    
    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        isAntiAlias = true
    }
    
    private var isCollecting = false
    private var analysisResult: String = "Touch the screen to start analysis"
    
    var onAnalysisComplete: ((ElderlyTouchProfile) -> Unit)? = null
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isCollecting && event.action == MotionEvent.ACTION_DOWN) {
            startCollection()
        }
        
        if (isCollecting) {
            touchPatternAnalyzer.processEvent(event)
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchPath.moveTo(event.x, event.y)
                    touchPoints.clear()
                    touchPoints.add(Pair(event.x, event.y))
                }
                MotionEvent.ACTION_MOVE -> {
                    touchPath.lineTo(event.x, event.y)
                    touchPoints.add(Pair(event.x, event.y))
                }
                MotionEvent.ACTION_UP -> {
                    // Don't stop collection on single touch up - collect multiple touches
                    touchPath.reset()
                }
            }
            
            invalidate()
        }
        
        return true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw current touch path
        canvas.drawPath(touchPath, pathPaint)
        
        // Draw touch points
        touchPoints.forEach { (x, y) ->
            canvas.drawCircle(x, y, 10f, pointPaint)
        }
        
        // Draw analysis result
        val lines = analysisResult.split("\n")
        var yOffset = 50f
        lines.forEach { line ->
            canvas.drawText(line, 20f, yOffset, textPaint)
            yOffset += 45f
        }
    }
    
    fun startCollection() {
        isCollecting = true
        touchPatternAnalyzer.startTracking()
        touchPath.reset()
        touchPoints.clear()
        analysisResult = "Collecting touch data...\nPerform various touches and gestures"
        invalidate()
        
        // Auto-stop collection after 10 seconds
        postDelayed({
            if (isCollecting) {
                stopCollectionAndAnalyze()
            }
        }, 10000)
    }
    
    fun stopCollectionAndAnalyze() {
        if (!isCollecting) return
        
        isCollecting = false
        val characteristics = touchPatternAnalyzer.analyzeCharacteristics()
        val elderlyProfile = elderlyTouchAnalyzer.analyzeElderlyProfile(characteristics)
        
        touchPatternAnalyzer.stopTracking()
        
        // Update display with summary
        analysisResult = buildString {
            appendLine("Analysis Complete!")
            appendLine("Elderly User: ${if (elderlyProfile.isLikelyElderly) "Yes" else "No"}")
            appendLine("Confidence: ${(elderlyProfile.confidenceScore * 100).toInt()}%")
            appendLine("")
            appendLine("Key Metrics:")
            appendLine("Speed: ${characteristics.avgSpeed.toInt()} px/s")
            appendLine("Tap Duration: ${characteristics.avgTapDuration} ms")
            appendLine("Tremor: ${String.format("%.2f", characteristics.tremor)}")
            appendLine("")
            appendLine("Touch again to restart")
        }
        
        invalidate()
        
        // Notify listener
        onAnalysisComplete?.invoke(elderlyProfile)
    }
    
    fun reset() {
        isCollecting = false
        touchPatternAnalyzer.stopTracking()
        touchPath.reset()
        touchPoints.clear()
        analysisResult = "Touch the screen to start analysis"
        invalidate()
    }
    
    fun getDetailedReport(): String? {
        if (isCollecting) return null
        
        val characteristics = touchPatternAnalyzer.analyzeCharacteristics()
        val elderlyProfile = elderlyTouchAnalyzer.analyzeElderlyProfile(characteristics)
        return elderlyTouchAnalyzer.generateDetailedReport(elderlyProfile)
    }
}