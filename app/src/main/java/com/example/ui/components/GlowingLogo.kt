package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowingLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    glowing: Boolean = true
) {
    // Infinite pulsing glow animations mimicking active cathode light bulbs
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val width = size.toPx()
            val height = size.toPx()
            val baseRadius = 4.dp.toPx()
            
            // Four quadrants offsets
            val topLeft = Offset(width * 0.28f, height * 0.28f)
            val topRight = Offset(width * 0.72f, height * 0.28f)
            val bottomLeft = Offset(width * 0.28f, height * 0.72f)
            val bottomRight = Offset(width * 0.72f, height * 0.72f)
            val centers = listOf(topLeft, topRight, bottomLeft, bottomRight)

            centers.forEach { center ->
                if (glowing) {
                    // Outer atmospheric glow
                    drawCircle(
                        color = dotColor.copy(alpha = pulseAlpha * 0.25f),
                        radius = baseRadius * pulseScale * 2.2f,
                        center = center
                    )
                    // Secondary inner halo
                    drawCircle(
                        color = dotColor.copy(alpha = pulseAlpha * 0.55f),
                        radius = baseRadius * pulseScale * 1.4f,
                        center = center
                    )
                }
                
                // Solid Core Dot
                drawCircle(
                    color = dotColor,
                    radius = baseRadius,
                    center = center
                )
                
                // High-contrast delicate white core highlight to maximize glowing illusion
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = baseRadius * 0.35f,
                    center = center
                )
            }
        }
    }
}
