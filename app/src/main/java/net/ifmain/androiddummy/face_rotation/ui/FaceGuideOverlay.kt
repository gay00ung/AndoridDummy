package net.ifmain.androiddummy.face_rotation.ui

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import net.ifmain.androiddummy.face_rotation.FaceVerificationAnalyzer
import net.ifmain.androiddummy.face_rotation.viewmodel.FaceVerificationViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

/**
 *
 * @author gayoung.
 * @since 2025. 7. 21.
 */
@Composable
fun FaceGuideOverlay(
    modifier: Modifier = Modifier,
    faceData: FaceVerificationAnalyzer.FaceData?,
    currentStep: FaceVerificationViewModel.VerificationStep
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val frameRadius = size.width.coerceAtMost(size.height) * 0.4f
        
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = frameRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
        
        faceData?.let { face ->
            // 3D 회전 효과를 위한 변환값 계산
            val rotationY = face.rotY / 45f // -1 ~ 1 범위로 정규화
            val rotationZ = face.rotZ / 45f
            val rotationX = face.rotX / 45f
            
            drawTossCrosshair(
                center = Offset(centerX, centerY),
                radius = frameRadius * 0.9f,
                rotationX = rotationX,
                rotationY = rotationY,
                rotationZ = rotationZ,
                color = when {
                    currentStep.validation(face) -> Color.Green
                    else -> Color.White
                },
                strokeWidth = 3.dp.toPx()
            )
            
            when (currentStep) {
                FaceVerificationViewModel.VerificationStep.TURN_LEFT,
                FaceVerificationViewModel.VerificationStep.TURN_RIGHT -> {
                    drawRotationGuideArrow(
                        center = Offset(centerX, centerY),
                        radius = frameRadius,
                        currentAngle = face.rotY,
                        targetAngle = currentStep.targetAngle,
                        isValid = currentStep.validation(face)
                    )
                }
                FaceVerificationViewModel.VerificationStep.LOOK_STRAIGHT -> {
                    if (!currentStep.validation(face)) {
                        drawCenterArrow(
                            center = Offset(centerX, centerY),
                            radius = frameRadius,
                            currentRotation = face.rotY
                        )
                    }
                }
                FaceVerificationViewModel.VerificationStep.SMILE -> {
                    drawSmileIndicator(
                        center = Offset(centerX, centerY),
                        radius = frameRadius,
                        smileProbability = face.smilingProbability ?: 0f,
                        isValid = currentStep.validation(face)
                    )
                }
                FaceVerificationViewModel.VerificationStep.BLINK -> {
                    drawBlinkIndicator(
                        center = Offset(centerX, centerY),
                        radius = frameRadius,
                        leftEyeOpen = face.leftEyeOpenProbability ?: 1f,
                        rightEyeOpen = face.rightEyeOpenProbability ?: 1f,
                        isValid = currentStep.validation(face)
                    )
                } else -> {
                    // 다른 단계에서는 눈 깜빡임이나 웃음 가이드를 그리지 않음
                }
            }
        } ?: run {
            // 얼굴이 감지되지 않았을 때 기본 십자가
            drawTossCrosshair(
                center = Offset(centerX, centerY),
                radius = frameRadius * 0.9f,
                rotationX = 0f,
                rotationY = 0f,
                rotationZ = 0f,
                color = Color.White.copy(alpha = 0.5f),
                strokeWidth = 3.dp.toPx()
            )
        }
        
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

private fun DrawScope.drawTossCrosshair(
    center: Offset,
    radius: Float,
    rotationX: Float,
    rotationY: Float,
    rotationZ: Float,
    color: Color,
    strokeWidth: Float
) {
    // 3D 회전 효과를 위한 스케일 계산
    val scaleX = 1f - abs(rotationY) * 0.3f // Y축 회전시 좌우 압축
    val scaleY = 1f - abs(rotationX) * 0.3f // X축 회전시 상하 압축
    
    // Z축 회전 (얼굴 기울기)
    rotate(rotationZ * 45f, center) {
        // 각 호의 길이와 간격
        val arcLength = 80f // 각 호의 각도
        val gapLength = 10f // 호 사이의 간격
        
        // 4개의 호 그리기
        listOf(
            -45f to 45f,    // 상단 호 (12시)
            45f to 135f,    // 우측 호 (3시)
            135f to 225f,   // 하단 호 (6시)
            225f to 315f    // 좌측 호 (9시)
        ).forEach { (startAngle, endAngle) ->
            // 3D 효과를 위한 변형
            val adjustedRadius = when {
                // Y축 회전 (좌우)
                rotationY > 0 && (startAngle == 45f || startAngle == 135f) -> radius * (1f + rotationY * 0.2f) // 오른쪽 호들이 커짐
                rotationY < 0 && (startAngle == 225f || startAngle == -45f) -> radius * (1f - rotationY * 0.2f) // 왼쪽 호들이 커짐
                // X축 회전 (상하)
                rotationX > 0 && (startAngle == 135f || startAngle == 225f) -> radius * (1f + rotationX * 0.2f) // 하단 호들이 커짐
                rotationX < 0 && (startAngle == -45f || startAngle == 45f) -> radius * (1f - rotationX * 0.2f) // 상단 호들이 커짐
                else -> radius
            }
            
            scale(scaleX, scaleY, center) {
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = arcLength,
                    useCenter = false,
                    topLeft = Offset(center.x - adjustedRadius, center.y - adjustedRadius),
                    size = Size(adjustedRadius * 2, adjustedRadius * 2),
                    style = Stroke(
                        width = strokeWidth * (0.8f + 0.2f * (adjustedRadius / radius)) // 가까운 호는 두껍게
                    )
                )
            }
        }
    }
}

// 얼굴 중심 십자선 (회전 가능)
private fun DrawScope.drawFaceCrosshair(
    center: Offset,
    faceRadius: Float,
    rotation: Float,
    color: Color,
    strokeWidth: Float
) {
    // 회전 변환 적용
    val rotationRadians = Math.toRadians(rotation.toDouble()).toFloat()
    val cosR = cos(rotationRadians)
    val sinR = sin(rotationRadians)
    
    val lineLength = faceRadius * 1.5f
    
    // 수평선 (회전 적용)
    val horizontalStart = Offset(
        center.x - lineLength * cosR,
        center.y - lineLength * sinR
    )
    val horizontalEnd = Offset(
        center.x + lineLength * cosR,
        center.y + lineLength * sinR
    )
    
    // 수직선 (90도 추가 회전)
    val verticalStart = Offset(
        center.x + lineLength * sinR,
        center.y - lineLength * cosR
    )
    val verticalEnd = Offset(
        center.x - lineLength * sinR,
        center.y + lineLength * cosR
    )
    
    // 십자선 그리기
    drawLine(
        color = color,
        start = horizontalStart,
        end = horizontalEnd,
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = verticalStart,
        end = verticalEnd,
        strokeWidth = strokeWidth
    )
    
    // 중심점 강조
    drawCircle(
        color = color,
        radius = 5.dp.toPx(),
        center = center
    )
}

// 타겟 위치 점선 십자선
private fun DrawScope.drawTargetCrosshair(
    currentCenter: Offset,
    targetAngle: Float,
    faceRadius: Float,
    color: Color,
    strokeWidth: Float
) {
    // 목표 각도에 따른 위치 계산
    val targetX = currentCenter.x + faceRadius * sin(Math.toRadians(targetAngle.toDouble())).toFloat()
    val targetCenter = Offset(targetX, currentCenter.y)
    
    val lineLength = faceRadius * 0.8f
    
    // 점선 효과
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
    
    // 수평선
    drawLine(
        color = color,
        start = Offset(targetCenter.x - lineLength, targetCenter.y),
        end = Offset(targetCenter.x + lineLength, targetCenter.y),
        strokeWidth = strokeWidth,
        pathEffect = pathEffect
    )
    
    // 수직선
    drawLine(
        color = color,
        start = Offset(targetCenter.x, targetCenter.y - lineLength),
        end = Offset(targetCenter.x, targetCenter.y + lineLength),
        strokeWidth = strokeWidth,
        pathEffect = pathEffect
    )
}

// 회전 방향 화살표
private fun DrawScope.drawRotationArrow(
    center: Offset,
    currentAngle: Float,
    targetAngle: Float,
    radius: Float,
    isValid: Boolean
) {
    if (!isValid) {
        // 화살표 호
        val startAngle = kotlin.math.min(currentAngle, targetAngle)
        val sweepAngle = kotlin.math.abs(targetAngle - currentAngle).coerceAtMost(60f)
        
        val arcPath = Path().apply {
            addArc(
                androidx.compose.ui.geometry.Rect(
                    offset = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                ),
                startAngleDegrees = startAngle - 90f, // Canvas 좌표계 보정
                sweepAngleDegrees = if (targetAngle > currentAngle) sweepAngle else -sweepAngle
            )
        }
        
        drawPath(
            path = arcPath,
            color = Color.Yellow,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
            )
        )
        
        // 화살표 머리
        val arrowAngle = if (targetAngle > currentAngle) 
            Math.toRadians((targetAngle - 90).toDouble()) 
        else 
            Math.toRadians((targetAngle - 90).toDouble())
            
        val arrowTipX = center.x + radius * cos(arrowAngle).toFloat()
        val arrowTipY = center.y + radius * sin(arrowAngle).toFloat()
        
        val arrowPath = Path().apply {
            moveTo(arrowTipX, arrowTipY)
            val arrowSize = 20f
            val arrowAngle1 = arrowAngle + PI / 6
            val arrowAngle2 = arrowAngle - PI / 6
            lineTo(
                arrowTipX - arrowSize * cos(arrowAngle1).toFloat(),
                arrowTipY - arrowSize * sin(arrowAngle1).toFloat()
            )
            moveTo(arrowTipX, arrowTipY)
            lineTo(
                arrowTipX - arrowSize * cos(arrowAngle2).toFloat(),
                arrowTipY - arrowSize * sin(arrowAngle2).toFloat()
            )
        }
        
        drawPath(
            path = arrowPath,
            color = Color.Yellow,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

// 정면 복귀 가이드
private fun DrawScope.drawCenteringGuide(
    center: Offset,
    currentRotation: Float,
    radius: Float
) {
    if (kotlin.math.abs(currentRotation) > 10f) {
        // 중앙으로 향하는 화살표
        val direction = if (currentRotation > 0) -1 else 1
        val arrowLength = radius * 0.8f
        
        val arrowPath = Path().apply {
            val startX = center.x + arrowLength * direction
            moveTo(startX, center.y)
            lineTo(center.x, center.y)
            
            // 화살표 머리
            val arrowHeadSize = 20f
            lineTo(center.x + arrowHeadSize * direction * 0.7f, center.y - arrowHeadSize * 0.7f)
            moveTo(center.x, center.y)
            lineTo(center.x + arrowHeadSize * direction * 0.7f, center.y + arrowHeadSize * 0.7f)
        }
        
        drawPath(
            path = arrowPath,
            color = Color.Yellow.copy(alpha = 0.8f),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // "CENTER" 텍스트 표시를 위한 작은 원
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = center
        )
    }
}

// 웃음 가이드
private fun DrawScope.drawSmileGuide(
    center: Offset,
    faceRadius: Float,
    smileProbability: Float,
    isValid: Boolean
) {
    // 입 모양 가이드 (스마일 곡선)
    val smilePath = Path().apply {
        val smileWidth = faceRadius * 0.8f
        val smileHeight = faceRadius * 0.3f * (0.5f + smileProbability) // 웃을수록 곡선이 깊어짐
        
        moveTo(center.x - smileWidth / 2, center.y + faceRadius * 0.5f)
        quadraticBezierTo(
            center.x, center.y + faceRadius * 0.5f + smileHeight,
            center.x + smileWidth / 2, center.y + faceRadius * 0.5f
        )
    }
    
    drawPath(
        path = smilePath,
        color = if (isValid) Color.Green else Color.Yellow,
        style = Stroke(
            width = 4.dp.toPx(),
            pathEffect = if (!isValid) PathEffect.dashPathEffect(floatArrayOf(10f, 5f)) else null
        )
    )
    
    // 웃음 강도 표시 (막대 그래프)
    val barWidth = 100.dp.toPx()
    val barHeight = 10.dp.toPx()
    val barX = center.x - barWidth / 2
    val barY = center.y + faceRadius * 1.5f
    
    // 배경
    drawRect(
        color = Color.Gray.copy(alpha = 0.3f),
        topLeft = Offset(barX, barY),
        size = Size(barWidth, barHeight)
    )
    
    // 현재 웃음 정도
    drawRect(
        color = when {
            smileProbability > 0.15f -> Color.Green
            smileProbability > 0.1f -> Color.Yellow
            else -> Color.Red
        },
        topLeft = Offset(barX, barY),
        size = Size(barWidth * smileProbability.coerceIn(0f, 1f), barHeight)
    )
    
    // 임계값 표시선
    val thresholdX = barX + barWidth * 0.15f
    drawLine(
        color = Color.White,
        start = Offset(thresholdX, barY - 5),
        end = Offset(thresholdX, barY + barHeight + 5),
        strokeWidth = 2.dp.toPx()
    )
}

// 눈 깜빡임 가이드
private fun DrawScope.drawBlinkGuide(
    faceBounds: Rect,
    leftEyeOpen: Float,
    rightEyeOpen: Float,
    isValid: Boolean
) {
    val eyeY = faceBounds.top + faceBounds.height() * 0.35f
    val eyeSpacing = faceBounds.width() * 0.25f
    val leftEyeX = faceBounds.centerX() - eyeSpacing
    val rightEyeX = faceBounds.centerX() + eyeSpacing
    val eyeSize = faceBounds.width() * 0.15f
    
    // 왼쪽 눈
    drawEye(
        center = Offset(leftEyeX.toFloat(), eyeY),
        size = eyeSize,
        openness = leftEyeOpen,
        isValid = isValid
    )
    
    // 오른쪽 눈
    drawEye(
        center = Offset(rightEyeX.toFloat(), eyeY),
        size = eyeSize,
        openness = rightEyeOpen,
        isValid = isValid
    )
}

// 개별 눈 그리기
private fun DrawScope.drawEye(
    center: Offset,
    size: Float,
    openness: Float,
    isValid: Boolean
) {
    val eyeHeight = size * openness.coerceIn(0f, 1f)
    
    if (openness > 0.3f) {
        // 눈 뜬 상태
        drawOval(
            color = if (isValid) Color.Red else Color.White,
            topLeft = Offset(center.x - size / 2, center.y - eyeHeight / 2),
            size = Size(size, eyeHeight),
            style = Stroke(width = 3.dp.toPx())
        )
    } else {
        // 눈 감은 상태 (선으로 표시)
        drawLine(
            color = Color.Green,
            start = Offset(center.x - size / 2, center.y),
            end = Offset(center.x + size / 2, center.y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

private fun DrawScope.drawRotationGuideArrow(
    center: Offset,
    radius: Float,
    currentAngle: Float,
    targetAngle: Float,
    isValid: Boolean
) {
    if (!isValid) {
        val direction = if (targetAngle > currentAngle) 1 else -1
        val arrowRadius = radius * 1.2f
        
        // 회전 방향 호
        drawArc(
            color = Color.Yellow.copy(alpha = 0.7f),
            startAngle = if (direction > 0) 0f else 180f,
            sweepAngle = 60f * direction,
            useCenter = false,
            topLeft = Offset(center.x - arrowRadius, center.y - arrowRadius),
            size = Size(arrowRadius * 2, arrowRadius * 2),
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
            )
        )
        
        // 화살표 끝
        val arrowTipAngle = if (direction > 0) 60f else 180f - 60f
        val arrowTipRad = Math.toRadians(arrowTipAngle.toDouble())
        val tipX = center.x + arrowRadius * cos(arrowTipRad).toFloat()
        val tipY = center.y + arrowRadius * sin(arrowTipRad).toFloat()
        
        val arrowPath = Path().apply {
            moveTo(tipX, tipY)
            val arrowSize = 20f
            lineTo(
                tipX - arrowSize * cos(arrowTipRad - PI / 6).toFloat(),
                tipY - arrowSize * sin(arrowTipRad - PI / 6).toFloat()
            )
            moveTo(tipX, tipY)
            lineTo(
                tipX - arrowSize * cos(arrowTipRad + PI / 6).toFloat(),
                tipY - arrowSize * sin(arrowTipRad + PI / 6).toFloat()
            )
        }
        
        drawPath(
            path = arrowPath,
            color = Color.Yellow,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun DrawScope.drawCenterArrow(
    center: Offset,
    radius: Float,
    currentRotation: Float
) {
    if (abs(currentRotation) > 10f) {
        val direction = if (currentRotation > 0) -1 else 1
        val arrowX = center.x + radius * 0.7f * direction
        
        drawLine(
            color = Color.Yellow.copy(alpha = 0.7f),
            start = Offset(arrowX, center.y),
            end = Offset(center.x, center.y),
            strokeWidth = 3.dp.toPx()
        )
        
        // 화살표 머리
        val arrowPath = Path().apply {
            moveTo(center.x, center.y)
            lineTo(center.x + 15f * direction, center.y - 10f)
            moveTo(center.x, center.y)
            lineTo(center.x + 15f * direction, center.y + 10f)
        }
        
        drawPath(
            path = arrowPath,
            color = Color.Yellow,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun DrawScope.drawSmileIndicator(
    center: Offset,
    radius: Float,
    smileProbability: Float,
    isValid: Boolean
) {
    // 원형 진행바
    val progressRadius = radius * 0.8f
    
    // 배경 원
    drawCircle(
        color = Color.Gray.copy(alpha = 0.3f),
        radius = progressRadius,
        center = center,
        style = Stroke(width = 8.dp.toPx())
    )
    
    // 진행 상태
    drawArc(
        color = if (isValid) Color.Green else Color.Yellow,
        startAngle = -90f,
        sweepAngle = 360f * smileProbability.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = Offset(center.x - progressRadius, center.y - progressRadius),
        size = Size(progressRadius * 2, progressRadius * 2),
        style = Stroke(width = 8.dp.toPx())
    )
    
    // 웃는 얼굴 아이콘
    val iconRadius = 30.dp.toPx()
    drawCircle(
        color = Color.White,
        radius = iconRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // 눈
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(center.x - 10.dp.toPx(), center.y - 5.dp.toPx())
    )
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(center.x + 10.dp.toPx(), center.y - 5.dp.toPx())
    )
    
    // 입 (스마일)
    drawArc(
        color = Color.White,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - 15.dp.toPx(), center.y),
        size = Size(30.dp.toPx(), 20.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawBlinkIndicator(
    center: Offset,
    radius: Float,
    leftEyeOpen: Float,
    rightEyeOpen: Float,
    isValid: Boolean
) {
    // 눈 아이콘
    val eyeY = center.y - 10.dp.toPx()
    val eyeSpacing = 20.dp.toPx()
    
    // 왼쪽 눈 (카메라 미러링으로 인해 오른쪽에 표시)
    drawBlinkEye(
        center = Offset(center.x + eyeSpacing, eyeY),
        isOpen = leftEyeOpen > 0.3f,
        color = if (isValid) Color.Green else Color.White
    )
    
    // 오른쪽 눈 (카메라 미러링으로 인해 왼쪽에 표시)
    drawBlinkEye(
        center = Offset(center.x - eyeSpacing, eyeY),
        isOpen = rightEyeOpen > 0.3f,
        color = if (isValid) Color.Green else Color.White
    )
    
    // 안내 텍스트 위치 표시 (원으로)
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius * 0.3f,
        center = Offset(center.x, center.y + radius * 0.5f),
        style = Stroke(width = 1.dp.toPx())
    )
    
    // 디버그용 L/R 표시
    if (leftEyeOpen < 0.3f || rightEyeOpen < 0.3f) {
        // 왼쪽 눈 상태 표시 (오른쪽에)
        if (leftEyeOpen < 0.3f) {
            drawCircle(
                color = Color.Yellow,
                radius = 5.dp.toPx(),
                center = Offset(center.x + eyeSpacing, eyeY - 30.dp.toPx())
            )
        }
        // 오른쪽 눈 상태 표시 (왼쪽에)
        if (rightEyeOpen < 0.3f) {
            drawCircle(
                color = Color.Cyan,
                radius = 5.dp.toPx(),
                center = Offset(center.x - eyeSpacing, eyeY - 30.dp.toPx())
            )
        }
    }
}

// 개별 눈 그리기
private fun DrawScope.drawBlinkEye(
    center: Offset,
    isOpen: Boolean,
    color: Color
) {
    if (isOpen) {
        // 열린 눈
        drawOval(
            color = color,
            topLeft = Offset(center.x - 15.dp.toPx(), center.y - 10.dp.toPx()),
            size = Size(30.dp.toPx(), 20.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
        // 눈동자
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = center
        )
    } else {
        // 닫힌 눈 (선)
        drawLine(
            color = color,
            start = Offset(center.x - 15.dp.toPx(), center.y),
            end = Offset(center.x + 15.dp.toPx(), center.y),
            strokeWidth = 3.dp.toPx()
        )
    }
}