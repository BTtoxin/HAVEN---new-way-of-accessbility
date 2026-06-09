package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BatteryInfo
import com.example.ui.theme.AppTypography
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular progress gauge (Left side)
        Box(
            modifier = Modifier
                .size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp)
            ) {
                val width = size.width
                val height = size.height
                val center = size / 2f
                val radius = (width - 12.dp.toPx()) / 2f

                // 1. Draw outer SVG concentric dotted guide line
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius + 4.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 2. Draw outer SVG precision tick marks
                val tickCount = 40
                for (i in 0 until tickCount) {
                    val angleInDegrees = (i * (360f / tickCount))
                    val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
                    
                    val innerRadius = radius + 2.dp.toPx()
                    val outerRadius = radius + 4.dp.toPx()

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
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Draw dynamic charging sweep arc (representing the SVG path)
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
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // 5. If charging, draw a rotating concentric loading orbit
                if (batteryInfo.isCharging) {
                    rotate(degrees = chargingRotation) {
                        drawCircle(
                          color = primaryColor.copy(alpha = 0.25f),
                            radius = radius - 6.dp.toPx(),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(center.width, center.height - radius + 6.dp.toPx())
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
                    style = AppTypography.displayLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
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
                        fontSize = 7.5.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (batteryInfo.isCharging) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // D3-inspired battery drain line chart of last 24 hours (Right side)
        BatteryDrainChart(
            percentage = batteryInfo.percentage,
            isCharging = batteryInfo.isCharging,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun BatteryDrainChart(
    percentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var canvasWidth by remember { mutableStateOf(300f) }
    var touchX by remember { mutableStateOf<Float?>(null) }

    // Generates a nice SVG transition-like D3 series profile matching current status
    val points = remember(percentage, isCharging) {
        val list = mutableListOf<Float>()
        val current = percentage.toFloat()
        if (isCharging) {
            val valley = (current - 25f).coerceAtLeast(15f)
            val startVal = (current + 15f).coerceAtMost(100f)
            for (i in 0..11) {
                when {
                    i < 7 -> {
                        val t = i / 7f
                        list.add(startVal - (startVal - valley) * t)
                    }
                    else -> {
                        val t = (i - 7) / 4f
                        list.add(valley + (current - valley) * t)
                    }
                }
            }
        } else {
            val startVal = (current + 35f).coerceAtMost(100f)
            for (i in 0..11) {
                val t = i / 11f
                val wave = kotlin.math.sin(t * Math.PI).toFloat() * 4f
                list.add((startVal - (startVal - current) * t + wave).coerceIn(current, 100f))
            }
        }
        list[list.lastIndex] = current
        list
    }

    val selectedIndex = remember(touchX, canvasWidth) {
        touchX?.let { x ->
            val stepSize = canvasWidth / 11f
            val idx = (x / stepSize).roundToInt().coerceIn(0, 11)
            idx
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("100%", "50%", "0%").forEach { tick ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tick,
                        style = AppTypography.bodySmall.copy(fontSize = 7.5.sp, color = Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.width(30.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, bottom = 14.dp, end = 8.dp)
                .onSizeChanged { canvasWidth = it.width.toFloat() }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                touchX = change.position.x
                                change.consume()
                            } else {
                                touchX = null
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            if (points.size < 2) return@Canvas

            val stepX = width / 11f
            val strokePath = Path()
            val areaPath = Path()

            val coords = points.mapIndexed { idx, value ->
                val x = idx * stepX
                val pct = value / 100f
                val y = height - (pct * height)
                androidx.compose.ui.geometry.Offset(x, y)
            }

            strokePath.moveTo(coords[0].x, coords[0].y)
            areaPath.moveTo(0f, height)
            areaPath.lineTo(coords[0].x, coords[0].y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y

                strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            areaPath.lineTo(width, height)
            areaPath.lineTo(0f, height)
            areaPath.close()

            // Area Gradient Underneath Line
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.18f),
                        primaryColor.copy(alpha = 0.01f)
                    )
                )
            )

            // D3 Stroke Path
            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // Interactive Ticks & Guides
            points.forEachIndexed { idx, value ->
                val coord = coords[idx]
                val isSelected = idx == selectedIndex

                drawCircle(
                    color = if (isSelected) Color.White else primaryColor,
                    radius = if (isSelected) 4.5.dp.toPx() else 2.dp.toPx(),
                    center = coord
                )

                if (isSelected) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = coord
                    )
                }
            }

            // Draw vertical scrubbing line
            if (selectedIndex != null) {
                val sCoord = coords[selectedIndex]
                val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = androidx.compose.ui.geometry.Offset(sCoord.x, 0f),
                    end = androidx.compose.ui.geometry.Offset(sCoord.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
            }
        }

        // Timeline labels (At extreme bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("-24h", style = AppTypography.bodySmall.copy(fontSize = 7.5.sp, color = Color.White.copy(alpha = 0.4f)))
            Text("-12h", style = AppTypography.bodySmall.copy(fontSize = 7.5.sp, color = Color.White.copy(alpha = 0.4f)))
            Text("now", style = AppTypography.bodySmall.copy(fontSize = 7.5.sp, color = Color.White.copy(alpha = 0.4f)))
        }

        // Tooltip bubble overlay
        if (selectedIndex != null) {
            val selectedValue = points[selectedIndex]
            val timeLabel = when (selectedIndex) {
                11 -> "Now"
                else -> "-${(11 - selectedIndex) * 2}h ago"
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Text(
                        text = "$timeLabel: ${selectedValue.roundToInt()}%",
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.5.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
