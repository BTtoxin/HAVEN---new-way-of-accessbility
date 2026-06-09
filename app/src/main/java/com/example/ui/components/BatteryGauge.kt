package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BatteryInfo
import com.example.ui.theme.AppTypography
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BatteryGauge(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val targetPercentage = batteryInfo.percentage.toFloat()
    
    // Dynamic transition sweep representing D3 SVG transitions
    val animatedPercentage by animateFloatAsState(
        targetValue = targetPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BatteryArc"
    )

    // Charging state subtle rotation/pulse transition to simulate interactive SVG gauges
    val infiniteTransition = rememberInfiniteTransition(label = "GaugeRotation")
    val chargingPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val chargingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotate"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        // Compose Canvas rendering D3-like SVG structure
        Canvas(
            modifier = Modifier
                .size(110.dp)
                .padding(4.dp)
        ) {
            val width = size.width
            val height = size.height
            val center = size / 2f
            val radius = (width - 16.dp.toPx()) / 2f

            // 1. Draw outer SVG concentric dotted guide line
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius + 6.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Draw outer SVG precision tick marks
            val tickCount = 40
            for (i in 0 until tickCount) {
                val angleInDegrees = (i * (360f / tickCount))
                val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
                
                val innerRadius = radius + 3.dp.toPx()
                val outerRadius = radius + 6.dp.toPx()

                val startX = center.width + innerRadius * cos(angleInRadians).toFloat()
                val startY = center.height + innerRadius * sin(angleInRadians).toFloat()
                val endX = center.width + outerRadius * cos(angleInRadians).toFloat()
                val endY = center.height + outerRadius * sin(angleInRadians).toFloat()

                // Highlight tick marks depending on charging state or charge percentage
                val limitAngleValue = (animatedPercentage / 100f) * 360f
                val isTickHot = angleInDegrees <= limitAngleValue
                
                val tickColor = if (isTickHot) {
                    primaryColor.copy(alpha = if (batteryInfo.isCharging) chargingPulseAlpha else 0.7f)
                } else {
                    Color.White.copy(alpha = 0.12f)
                }

                drawLine(
                    color = tickColor,
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // 3. Draw background gauge track arc
            drawArc(
                color = Color.White.copy(alpha = 0.06f),
                startAngle = -225f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Draw dynamic charging sweep arc (representing the SVG path)
            // Sweep range of 270 degrees mapping 0 to 100 percentage
            val sweepAngle = (animatedPercentage / 100f) * 270f
            
            drawArc(
                color = if (batteryInfo.percentage <= 20 && !batteryInfo.isCharging) {
                    Color(0xFFE53935) // Urgent red for low battery
                } else {
                    primaryColor
                },
                startAngle = -225f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. If charging, draw a rotating concentric loading orbit representing SVG dynamic gauges
            if (batteryInfo.isCharging) {
                rotate(degrees = chargingRotation) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = radius - 8.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(center.width, center.height - radius + 8.dp.toPx())
                    )
                }
            }
        }

        // Inner Gauge Info Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${batteryInfo.percentage}%",
                style = AppTypography.displayLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                color = if (batteryInfo.percentage <= 20 && !batteryInfo.isCharging) {
                    Color(0xFFE53935)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = if (batteryInfo.isCharging) "CHARGING" else "DISCHARGING",
                style = AppTypography.labelSmall.copy(
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = if (batteryInfo.isCharging) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
