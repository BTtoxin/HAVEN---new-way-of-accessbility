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

/**
 * GlyphLogo represents the iconic four-dot brand identity arranged in quadrants
 * with elegant atmospheric pulsing glows that emulate early light-emitting displays.
 */
@Composable
fun GlyphLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    glowing: Boolean = true
) {
    // Elegant pulsing animation imitating neon / phosphor luminescent glows
    val infiniteTransition = rememberInfiniteTransition(label = "GlyphGlowTransition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlyphPulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlyphPulseAlpha"
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

            centers.forEachIndexed { index, center ->
                if (glowing) {
                    // Outer diffuse atmospheric glow
                    drawCircle(
                        color = dotColor.copy(alpha = pulseAlpha * 0.22f),
                        radius = baseRadius * pulseScale * 2.4f,
                        center = center
                    )
                    // Inner dense glow halo
                    drawCircle(
                        color = dotColor.copy(alpha = pulseAlpha * 0.60f),
                        radius = baseRadius * pulseScale * 1.5f,
                        center = center
                    )
                }
                
                // Solid main core circle
                drawCircle(
                    color = dotColor,
                    radius = baseRadius,
                    center = center
                )
                
                // High-intensity white spark highlight at the center of each quadrant dot to sell the glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = baseRadius * 0.35f,
                    center = center
                )
            }
        }
    }
}
