package net.ifmain.androiddummy.touch_pattern

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.MotionEvent
import android.view.View
import android.view.VelocityTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.PI
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchMonitoringScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val touchDataFlow = remember { MutableStateFlow<TouchMonitorData?>(null) }
    val touchData by touchDataFlow.asStateFlow().collectAsState()
    
    var touchPath by remember { mutableStateOf(listOf<Offset>()) }
    var isCollecting by remember { mutableStateOf(true) }
    var velocityTracker by remember { mutableStateOf<VelocityTracker?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("터치 데이터 실시간 모니터링") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 터치 캔버스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        object : View(ctx) {
                            override fun onTouchEvent(event: MotionEvent): Boolean {
                                if (isCollecting) {
                                    // VelocityTracker 초기화 및 업데이트
                                    when (event.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            velocityTracker?.recycle()
                                            velocityTracker = VelocityTracker.obtain()
                                            velocityTracker?.addMovement(event)
                                            touchPath = listOf(Offset(event.x, event.y))
                                        }
                                        MotionEvent.ACTION_MOVE -> {
                                            velocityTracker?.addMovement(event)
                                            touchPath = touchPath + Offset(event.x, event.y)
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            velocityTracker?.addMovement(event)
                                            velocityTracker?.recycle()
                                            velocityTracker = null
                                        }
                                    }
                                    
                                    // 속도 계산
                                    velocityTracker?.computeCurrentVelocity(1000) // pixels per second
                                    val vx = velocityTracker?.xVelocity ?: 0f
                                    val vy = velocityTracker?.yVelocity ?: 0f
                                    
                                    val data = extractTouchData(event, vx, vy)
                                    touchDataFlow.value = data
                                }
                                return true
                            }
                        }.apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 터치 경로 그리기
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawTouchPath(touchPath)
                    
                    // 현재 터치 위치 표시
                    touchData?.let { data ->
                        drawCircle(
                            color = when (data.toolType) {
                                "스타일러스" -> Color.Green
                                "지우개" -> Color.Red
                                else -> Color.Cyan
                            },
                            radius = (data.touchSize * 100).dp.toPx(),
                            center = Offset(data.x, data.y)
                        )
                    }
                }
                
                // 엣지 터치 인디케이터
                touchData?.let { data ->
                    if (data.edgeTouch.isNotEmpty()) {
                        Text(
                            text = data.edgeTouch,
                            color = Color.Yellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )
                    }
                }
            }
            
            // 실시간 데이터 표시
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                touchData?.let { data ->
                    DataSection(title = "기본 정보") {
                        DataRow("위치", "X: ${data.x.roundToInt()}, Y: ${data.y.roundToInt()}")
                        DataRow("도구 타입", data.toolType)
                        DataRow("액션", data.action)
                        DataRow("포인터 수", data.pointerCount.toString())
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DataSection(title = "압력 및 크기") {
                        DataRow("압력 (정규화)", "%.5f".format(data.pressure))
                        DataRow("압력 (Raw)", "%.5f".format(data.rawPressure))
                        DataRow("압력 (event.pressure)", "%.5f".format(data.eventPressure))
                        DataRow("압력 범위", "%.3f - %.3f".format(data.pressureMin, data.pressureMax))
                        DataRow("압력 지원", if (data.pressureSupported) "지원됨" else "미지원")
                        DataRow("기기명", data.deviceName)
                        DataRow("터치 크기", "%.3f".format(data.touchSize))
                        DataRow("터치 영역", "Major: %.1f, Minor: %.1f".format(data.touchMajor, data.touchMinor))
                        DataRow("추정 압력", "%.3f".format(data.estimatedPressure))
                        DataRow("접촉 면적", "%.1f px²".format(data.contactArea))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DataSection(title = "스타일러스 데이터") {
                        DataRow("기울기", "%.2f°".format(data.tilt))
                        DataRow("방향", "%.2f rad".format(data.orientation))
                        DataRow("거리 (호버링)", "%.2f".format(data.distance))
                    }
                    
                    if (data.edgeTouch.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DataSection(title = "엣지 터치") {
                            DataRow("감지된 엣지", data.edgeTouch)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DataSection(title = "속도 정보") {
                        DataRow("X 속도", "%.1f px/s".format(data.velocityX))
                        DataRow("Y 속도", "%.1f px/s".format(data.velocityY))
                        val totalVelocity = sqrt(data.velocityX * data.velocityX + data.velocityY * data.velocityY)
                        DataRow("총 속도", "%.1f px/s".format(totalVelocity))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DataSection(title = "시간 정보") {
                        DataRow("다운 시간", data.downTime.toString())
                        DataRow("이벤트 시간", data.eventTime.toString())
                        DataRow("지속 시간", "${data.eventTime - data.downTime} ms")
                    }
                }
            }
        }
    }
}

@Composable
private fun DataSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawTouchPath(path: List<Offset>) {
    if (path.size < 2) return
    
    for (i in 1 until path.size) {
        drawLine(
            color = Color.Cyan.copy(alpha = 0.7f),
            start = path[i - 1],
            end = path[i],
            strokeWidth = 3.dp.toPx()
        )
    }
    
    // 터치 포인트 표시
    path.forEach { point ->
        drawCircle(
            color = Color.Red,
            radius = 3.dp.toPx(),
            center = point
        )
    }
}

private fun extractTouchData(event: MotionEvent, velocityX: Float, velocityY: Float): TouchMonitorData {
    val toolType = when (event.getToolType(0)) {
        MotionEvent.TOOL_TYPE_FINGER -> "손가락"
        MotionEvent.TOOL_TYPE_STYLUS -> "스타일러스"
        MotionEvent.TOOL_TYPE_MOUSE -> "마우스"
        MotionEvent.TOOL_TYPE_ERASER -> "지우개"
        else -> "알 수 없음"
    }
    
    val action = when (event.action) {
        MotionEvent.ACTION_DOWN -> "DOWN"
        MotionEvent.ACTION_MOVE -> "MOVE"
        MotionEvent.ACTION_UP -> "UP"
        MotionEvent.ACTION_CANCEL -> "CANCEL"
        else -> "OTHER"
    }
    
    val edgeFlags = event.edgeFlags
    val edgeTouch = buildString {
        if (edgeFlags and MotionEvent.EDGE_TOP != 0) append("상단 ")
        if (edgeFlags and MotionEvent.EDGE_BOTTOM != 0) append("하단 ")
        if (edgeFlags and MotionEvent.EDGE_LEFT != 0) append("왼쪽 ")
        if (edgeFlags and MotionEvent.EDGE_RIGHT != 0) append("오른쪽 ")
    }.trim()
    
    // 스타일러스 관련 데이터
    val tilt = try {
        event.getAxisValue(MotionEvent.AXIS_TILT, 0) * 180 / Math.PI
    } catch (e: Exception) { 0.0 }
    
    val distance = try {
        event.getAxisValue(MotionEvent.AXIS_DISTANCE, 0)
    } catch (e: Exception) { 0f }
    
    // 기기 정보
    val deviceName = event.device?.name ?: "Unknown"
    
    // 압력 감지 지원 여부 및 범위 확인
    val pressureRange = event.device?.getMotionRange(MotionEvent.AXIS_PRESSURE)
    val pressureSupported = pressureRange != null
    val pressureMin = pressureRange?.min ?: 0f
    val pressureMax = pressureRange?.max ?: 1f
    
    // 다양한 방법으로 압력 값 가져오기
    val eventPressure = event.pressure // 기본 압력 값
    val rawPressure = try {
        event.getAxisValue(MotionEvent.AXIS_PRESSURE, 0)
    } catch (e: Exception) { 
        eventPressure 
    }
    
    // 압력 값을 0-1 범위로 정규화 (일부 기기는 다른 범위 사용)
    val normalizedPressure = if (pressureSupported && pressureMax > pressureMin && pressureMax != 1f) {
        (rawPressure - pressureMin) / (pressureMax - pressureMin)
    } else {
        rawPressure
    }
    
    // 디버그: 압력 관련 모든 축 정보 출력
    println("=== 압력 디버그 정보 ===")
    println("Device: $deviceName")
    println("Pressure supported: $pressureSupported")
    println("Pressure range: $pressureMin - $pressureMax")
    println("event.pressure: $eventPressure")
    println("AXIS_PRESSURE: $rawPressure")
    println("Normalized: $normalizedPressure")
    
    // 포인터별 압력도 확인
    if (event.pointerCount > 0) {
        for (i in 0 until event.pointerCount) {
            println("Pointer $i pressure: ${event.getPressure(i)}")
        }
    }
    
    // 터치 영역을 기반으로 압력 추정
    // 일반적으로 세게 누를수록 접촉 면적이 넓어짐
    val contactArea = (event.touchMajor * event.touchMinor * PI / 4).toFloat()
    
    // 실제 데이터 기반으로 범위 재조정
    // 살짝: ~5px², 보통: ~20px², 세게: ~50px² 이상
    val minArea = 3f    // 최소 면적 (아주 살짝)
    val maxArea = 50f   // 최대 면적 (세게 누름)
    
    val estimatedPressure = when {
        contactArea <= minArea -> 0.1f
        contactArea >= maxArea -> 1.0f
        else -> {
            // 더 부드러운 곡선으로 매핑
            val normalized = (contactArea - minArea) / (maxArea - minArea)
            // 제곱근을 사용해 중간 값들을 더 높게 조정
            0.1f + kotlin.math.sqrt(normalized) * 0.9f
        }
    }
    
    println("Touch area - Major: ${event.touchMajor}, Minor: ${event.touchMinor}")
    println("Contact area: $contactArea px²")
    println("Estimated pressure: $estimatedPressure")
    
    return TouchMonitorData(
        x = event.x,
        y = event.y,
        pressure = normalizedPressure,
        rawPressure = rawPressure,
        eventPressure = eventPressure,
        pressureMin = pressureMin,
        pressureMax = pressureMax,
        pressureSupported = pressureSupported,
        deviceName = deviceName,
        touchSize = event.size,
        touchMajor = event.touchMajor,
        touchMinor = event.touchMinor,
        toolType = toolType,
        orientation = event.orientation,
        tilt = tilt,
        distance = distance,
        action = action,
        pointerCount = event.pointerCount,
        edgeTouch = edgeTouch,
        velocityX = velocityX,
        velocityY = velocityY,
        downTime = event.downTime,
        eventTime = event.eventTime,
        estimatedPressure = estimatedPressure,
        contactArea = contactArea.toDouble()
    )
}

data class TouchMonitorData(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val rawPressure: Float,
    val eventPressure: Float,
    val pressureMin: Float,
    val pressureMax: Float,
    val pressureSupported: Boolean,
    val deviceName: String,
    val touchSize: Float,
    val touchMajor: Float,
    val touchMinor: Float,
    val toolType: String,
    val orientation: Float,
    val tilt: Double,
    val distance: Float,
    val action: String,
    val pointerCount: Int,
    val edgeTouch: String,
    val velocityX: Float,
    val velocityY: Float,
    val downTime: Long,
    val eventTime: Long,
    val estimatedPressure: Float,
    val contactArea: Double
)