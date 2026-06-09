package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed
import kotlinx.coroutines.delay

@Composable
fun DotMatrixBootScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Standard booting phase tracking
    var phase by remember { mutableStateOf(0) } // 0: Init matrix, 1: Constructing glyph symbols, 2: Final flare, 3: Completed
    
    // Animate sweep line wave
    val sweepProgress = remember { Animatable(0f) }
    
    // General opacity for fading out entire boot screen when finished
    val systemAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Phase 0: Ambient matrix initialization (subtle sweeps)
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearEasing)
        )
        
        // Phase 1: High-intensity Construct Glyph assembly
        phase = 1
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        // Phase 2: Core flare (crimson dot highlight)
        phase = 2
        delay(600)
        
        // Phase 3: Transition out
        phase = 3
        systemAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        
        onComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(systemAlpha.value)
            .background(Color(0xFF070708)), // Deep void canvas
        contentAlignment = Alignment.Center
    ) {
        // High frequency micro dot-grid background
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            
            // Sizing parameters
            val dotSpacing = 14.dp
            val density = androidx.compose.ui.platform.LocalDensity.current
            val spacingPx = with(density) { dotSpacing.toPx() }
            
            val cols = (widthPx / spacingPx).toInt()
            val rows = (heightPx / spacingPx).toInt()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val waveY = sweepProgress.value * heightPx
                val waveRadius = 320f // Glow footprint range

                for (c in 0 until cols) {
                    for (r in 0 until rows) {
                        val cx = (c * spacingPx) + (spacingPx / 2f)
                        val cy = (r * spacingPx) + (spacingPx / 2f)
                        
                        // Default passive state
                        var isCentralGlyphPoint = false
                        var isBrandPoint = false
                        var isRedCentroid = false
                        
                        // Define custom Nothing/Haven Glyph curves (Mathematical coordinates)
                        val dx = cx - (widthPx / 2f)
                        val dy = cy - (heightPx / 2f)
                        
                        // 1. Center Vertical Stripe
                        if (Math.abs(dx) < 6f && dy > -300f && dy < 150f) {
                            isCentralGlyphPoint = true
                        }
                        // 2. Circular ring dots around centre
                        val ringRadius = 180f
                        val distToCenter = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        if (Math.abs(distToCenter - ringRadius) < 8f && dy < 60f) {
                            isCentralGlyphPoint = true
                        }
                        // 3. Diagonal brand flare line at top-right
                        if (dx > 80f && dx < 220f && Math.abs(dx - dy * 1.5f - 180f) < 8f && dy < -100f) {
                            isCentralGlyphPoint = true
                        }
                        
                        // 4. Crimson Red Centroid Dot at bottom-right (The Nothing brand signature)
                        if (Math.abs(dx - 120f) < 14f && Math.abs(dy - 120f) < 14f) {
                            isRedCentroid = true
                        }

                        // Determine illumination levels
                        val distanceToSweep = Math.abs(cy - waveY)
                        val sweepGlow = if (distanceToSweep < waveRadius) {
                            (1f - (distanceToSweep / waveRadius)).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        val finalColor = when {
                            // Signature Red Centroid
                            isRedCentroid -> {
                                if (phase >= 2) {
                                    Color(0xFFE53935).copy(alpha = 0.95f)
                                } else {
                                    Color(0xFFE53935).copy(alpha = 0.25f + (sweepGlow * 0.7f))
                                }
                            }
                            // Central Glyph Core Pattern
                            isCentralGlyphPoint -> {
                                if (phase >= 1) {
                                    Color.White.copy(alpha = 0.90f * (0.4f + sweepGlow * 0.6f))
                                } else {
                                    Color.White.copy(alpha = 0.08f + (sweepGlow * 0.4f))
                                }
                            }
                            // Ambient Background Dot Density
                            else -> {
                                Color.White.copy(alpha = 0.04f + (sweepGlow * 0.12f))
                            }
                        }

                        val dotRadius = when {
                            isRedCentroid -> {
                                if (phase >= 2) 4.5f else 3f
                            }
                            isCentralGlyphPoint -> {
                                if (phase >= 1) 3f else 2f
                            }
                            else -> 1.5f
                        }

                        drawCircle(
                            color = finalColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
        }

        // Foreground Tech Info HUD overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SYSTEM BOOT SEQUENCE",
                style = AppTypography.labelSmall.copy(
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "H A V E N  O S  I N I T I A L I Z I N G",
                style = AppTypography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.35f)
                )
            )
        }

        // Elegant minimal SKIP affordance to respect user convenience
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .clickable {
                    onComplete()
                }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "SKIP SEQUENCE",
                style = AppTypography.labelSmall.copy(
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
