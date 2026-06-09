package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.utils.AudioHapticEngine

@Composable
fun NothingSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.15f)
) {
    val context = LocalContext.current
    var isDragging by remember { mutableStateOf(false) }

    val trackScale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TrackScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = trackScale
                scaleY = trackScale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val percent = (offset.x / size.width).coerceIn(0f, 1f)
                        val newValue = valueRange.start + percent * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                        AudioHapticEngine.triggerClick(context)
                        tryAwaitRelease()
                        isDragging = false
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    change.consume()
                    val percent = (change.position.x / size.width).coerceIn(0f, 1f)
                    val newValue = valueRange.start + percent * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cornerRadius = CornerRadius(16.dp.toPx())
            
            // Background track
            drawRoundRect(
                color = inactiveColor,
                size = size,
                cornerRadius = cornerRadius
            )
            
            // Foreground fill
            val percent = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            val fillWidth = size.width * percent
            
            drawRoundRect(
                color = activeColor,
                size = Size(width = fillWidth, height = size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}
