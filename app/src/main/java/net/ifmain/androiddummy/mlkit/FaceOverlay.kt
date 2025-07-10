package net.ifmain.androiddummy.mlkit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark

@Composable
fun FaceOverlay(
    faces: List<Face>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // For front camera in portrait mode, image is rotated 90 degrees
        // So we swap width and height for correct scaling
        val widthScaleFactor = size.width / imageHeight.toFloat()
        val heightScaleFactor = size.height / imageWidth.toFloat()
        
        faces.forEach { face ->
            drawFaceContours(face, widthScaleFactor, heightScaleFactor)
            drawFaceLandmarks(face, widthScaleFactor, heightScaleFactor)
        }
    }
}

private fun DrawScope.drawFaceContours(
    face: Face,
    widthScaleFactor: Float,
    heightScaleFactor: Float
) {
    val contourTypes = listOf(
        FaceContour.FACE,
        FaceContour.LEFT_EYEBROW_TOP,
        FaceContour.LEFT_EYEBROW_BOTTOM,
        FaceContour.RIGHT_EYEBROW_TOP,
        FaceContour.RIGHT_EYEBROW_BOTTOM,
        FaceContour.LEFT_EYE,
        FaceContour.RIGHT_EYE,
        FaceContour.UPPER_LIP_TOP,
        FaceContour.UPPER_LIP_BOTTOM,
        FaceContour.LOWER_LIP_TOP,
        FaceContour.LOWER_LIP_BOTTOM,
        FaceContour.NOSE_BRIDGE,
        FaceContour.NOSE_BOTTOM
    )
    
    contourTypes.forEach { contourType ->
        val contour = face.getContour(contourType)
        contour?.let { faceContour ->
            val points = faceContour.points
            if (points.isNotEmpty()) {
                val path = Path()
                
                points.forEachIndexed { index, point ->
                    val x = translateX(point.x, widthScaleFactor, size.width)
                    val y = translateY(point.y, heightScaleFactor)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                val color = when (contourType) {
                    FaceContour.FACE -> Color.Green
                    FaceContour.LEFT_EYE, FaceContour.RIGHT_EYE -> Color.Cyan
                    FaceContour.LEFT_EYEBROW_TOP, FaceContour.LEFT_EYEBROW_BOTTOM,
                    FaceContour.RIGHT_EYEBROW_TOP, FaceContour.RIGHT_EYEBROW_BOTTOM -> Color.Yellow
                    FaceContour.UPPER_LIP_TOP, FaceContour.UPPER_LIP_BOTTOM,
                    FaceContour.LOWER_LIP_TOP, FaceContour.LOWER_LIP_BOTTOM -> Color.Magenta
                    FaceContour.NOSE_BRIDGE, FaceContour.NOSE_BOTTOM -> Color.White
                    else -> Color.Green
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

private fun DrawScope.drawFaceLandmarks(
    face: Face,
    widthScaleFactor: Float,
    heightScaleFactor: Float
) {
    val landmarkTypes = listOf(
        FaceLandmark.MOUTH_BOTTOM,
        FaceLandmark.MOUTH_RIGHT,
        FaceLandmark.MOUTH_LEFT,
        FaceLandmark.LEFT_EYE,
        FaceLandmark.RIGHT_EYE,
        FaceLandmark.LEFT_CHEEK,
        FaceLandmark.RIGHT_CHEEK,
        FaceLandmark.NOSE_BASE
    )
    
    landmarkTypes.forEach { landmarkType ->
        val landmark = face.getLandmark(landmarkType)
        landmark?.let { faceLandmark ->
            val position = faceLandmark.position
            val x = translateX(position.x, widthScaleFactor, size.width)
            val y = translateY(position.y, heightScaleFactor)
            
            drawCircle(
                color = Color.Red,
                radius = 10f,
                center = Offset(x, y)
            )
        }
    }
}

// Front camera mirror adjustment and rotation handling
private fun translateX(x: Float, scaleFactor: Float, canvasWidth: Float): Float {
    // For front camera, we need to mirror the X coordinate
    // Also, because image is rotated 90 degrees, we use Y coordinate as X
    return canvasWidth - (x * scaleFactor)
}

private fun translateY(y: Float, scaleFactor: Float): Float {
    // Because image is rotated 90 degrees, we use X coordinate as Y
    return y * scaleFactor
}