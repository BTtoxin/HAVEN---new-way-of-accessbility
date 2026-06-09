package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PREDEFINED_CITIES
import com.example.ui.theme.AppTypography
import com.example.viewmodel.QSViewModel
import com.example.viewmodel.QSViewModel.WeatherState
import com.example.utils.AudioHapticEngine
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherWidget(
    viewModel: QSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val weatherTemp by viewModel.weatherTemp.collectAsStateWithLifecycle()
    val weatherCode by viewModel.weatherCode.collectAsStateWithLifecycle()
    val selectedCityIndex by viewModel.selectedCityIndex.collectAsStateWithLifecycle()

    val city = remember(selectedCityIndex) {
        PREDEFINED_CITIES.getOrNull(selectedCityIndex) ?: PREDEFINED_CITIES[0]
    }

    // Material 3 container colors
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFFDF8F6)
    val cardBackground = if (isDark) Color(0xFF101010) else Color(0xFFF0EBE9)
    val borderStrokeColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val contentColor = if (isDark) Color.White else Color.Black

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 215.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, borderStrokeColor, RoundedCornerShape(24.dp))
            .clickable {
                AudioHapticEngine.triggerClick(context)
                viewModel.cycleWeatherCity()
            },
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw delicate Nothing dots background
                    val dotRadius = 0.8.dp.toPx()
                    val gap = 14.dp.toPx()
                    val columns = (size.width / gap).toInt()
                    val rows = (size.height / gap).toInt()
                    for (c in 0..columns + 1) {
                        for (r in 0..rows + 1) {
                            drawCircle(
                                color = contentColor.copy(alpha = 0.035f),
                                radius = dotRadius,
                                center = androidx.compose.ui.geometry.Offset(c * gap, r * gap)
                            )
                        }
                    }
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (City Label + Refresh Indicators)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = city.name,
                            style = AppTypography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                fontSize = 11.sp
                            ),
                            color = contentColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = city.region.uppercase(),
                            style = AppTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 8.sp
                            ),
                            color = contentColor.copy(alpha = 0.4f)
                        )
                    }

                    // Cycle hint / small icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(contentColor.copy(alpha = 0.06f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Cycle City",
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Body content based on network/loading state
                Crossfade(
                    targetState = weatherState,
                    animationSpec = tween(300),
                    label = "WeatherContentChange"
                ) { state ->
                    when (state) {
                        WeatherState.LOADING -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = contentColor,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        WeatherState.ERROR -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = contentColor.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "OFFLINE STATUS - TAP TO RETRY",
                                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 9.sp),
                                    color = contentColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                        WeatherState.SUCCESS -> {
                            val temp = weatherTemp ?: 0.0
                            val code = weatherCode ?: 0
                            val weatherLabel = remember(code) {
                                when (code) {
                                    0 -> "SUNNY"
                                    1, 2 -> "PARTLY CLOUDY"
                                    3 -> "OVERCAST"
                                    45, 48 -> "FOGGY"
                                    51, 53, 55 -> "MISTY DRIZZLE"
                                    61, 63, 65 -> "HEAVY RAIN"
                                    71, 73, 75 -> "LIGHT SNOW"
                                    80, 81, 82 -> "RAIN SHOWERS"
                                    95, 96, 99 -> "THUNDERSTORM"
                                    else -> "CLEAR SKY"
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(top = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(verticalArrangement = Arrangement.Bottom) {
                                        Text(
                                            text = "${temp.toInt()}°C",
                                            style = AppTypography.displayLarge.copy(
                                                fontSize = 44.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-1).sp
                                            ),
                                            color = contentColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = weatherLabel,
                                            style = AppTypography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.5.sp,
                                                fontSize = 9.sp
                                            ),
                                            color = contentColor.copy(alpha = 0.9f)
                                        )
                                    }

                                    RunningWeatherIcon(
                                        weatherCode = code,
                                        color = contentColor,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .padding(bottom = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val aiSummary by viewModel.weatherAiSummary.collectAsStateWithLifecycle()
                                Text(
                                    text = aiSummary,
                                    style = AppTypography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        lineHeight = 12.sp
                                    ),
                                    color = contentColor.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(contentColor.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                        .border(0.5.dp, contentColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RunningWeatherIcon(
    weatherCode: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherIconAnim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )
    val rainYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainOffset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val strokeWidth = 1.5.dp.toPx()

        when (weatherCode) {
            0, 1 -> { // SUNNY or CLEAR
                drawCircle(
                    color = color,
                    radius = width * 0.22f,
                    style = Stroke(width = strokeWidth)
                )
                rotate(rotation) {
                    for (i in 0 until 8) {
                        val angle = (i * 45) * Math.PI / 180.0
                        val rayStartRadius = width * 0.3f
                        val rayEndRadius = width * 0.42f
                        val startX = cx + (rayStartRadius * cos(angle)).toFloat()
                        val startY = cy + (rayStartRadius * sin(angle)).toFloat()
                        val endX = cx + (rayEndRadius * cos(angle)).toFloat()
                        val endY = cy + (rayEndRadius * sin(angle)).toFloat()

                        drawLine(
                            color = color.copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(startX, startY),
                            end = androidx.compose.ui.geometry.Offset(endX, endY),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            2, 3 -> { // OVERCAST or CLOUDY
                val scaleX = width / 100f
                val scaleY = height / 100f
                val cloudPath = Path().apply {
                    moveTo(25f * scaleX, 65f * scaleY)
                    cubicTo(10f * scaleX, 65f * scaleY, 10f * scaleX, 45f * scaleY, 25f * scaleX, 45f * scaleY)
                    cubicTo(25f * scaleX, 22f * scaleY, 55f * scaleX, 18f * scaleY, 65f * scaleX, 32f * scaleY)
                    cubicTo(82f * scaleX, 28f * scaleY, 88f * scaleX, 50f * scaleY, 75f * scaleX, 65f * scaleY)
                    close()
                }
                drawPath(path = cloudPath, color = color.copy(alpha = 0.08f))
                drawPath(path = cloudPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                
                // Draw decorative minimal grid lines intersecting behind cloud to display Nothing OS technical layout
                drawLine(
                    color = color.copy(alpha = 0.15f),
                    start = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.5f),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
            45, 48 -> { // FOGGY
                val scaleY = height / 10f
                listOf(3.5f, 5.0f, 6.5f).forEachIndexed { index, yPos ->
                    val opacity = if (index == 1) 0.9f else 0.4f
                    drawLine(
                        color = color.copy(alpha = opacity),
                        start = androidx.compose.ui.geometry.Offset(width * 0.15f, yPos * scaleY),
                        end = androidx.compose.ui.geometry.Offset(width * 0.85f, yPos * scaleY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> { // RAIN SHOWERS / DRIZZLE
                val scaleX = width / 100f
                val scaleY = height / 100f
                val cloudPath = Path().apply {
                    moveTo(25f * scaleX, 55f * scaleY)
                    cubicTo(10f * scaleX, 55f * scaleY, 10f * scaleX, 35f * scaleY, 25f * scaleX, 35f * scaleY)
                    cubicTo(25f * scaleX, 12f * scaleY, 55f * scaleX, 8f * scaleY, 65f * scaleX, 22f * scaleY)
                    cubicTo(82f * scaleX, 18f * scaleY, 88f * scaleX, 40f * scaleY, 75f * scaleX, 55f * scaleY)
                    close()
                }
                drawPath(path = cloudPath, color = color.copy(alpha = 0.08f))
                drawPath(path = cloudPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                val dropGridX = listOf(30f, 45f, 60f)
                dropGridX.forEachIndexed { idx, xPercent ->
                    val x = xPercent * scaleX
                    val localYOffset = (rainYOffset + idx * 6f) % 20f
                    val yStart = 60f * scaleY + localYOffset
                    if (yStart < height * 0.9f) {
                        drawLine(
                            color = color.copy(alpha = 0.5f),
                            start = androidx.compose.ui.geometry.Offset(x, yStart),
                            end = androidx.compose.ui.geometry.Offset(x - 2.dp.toPx(), yStart + 8.dp.toPx()),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            95, 96, 99 -> { // THUNDERSTORM
                val scaleX = width / 100f
                val scaleY = height / 100f
                val cloudPath = Path().apply {
                    moveTo(25f * scaleX, 52f * scaleY)
                    cubicTo(10f * scaleX, 52f * scaleY, 10f * scaleX, 32f * scaleY, 25f * scaleX, 32f * scaleY)
                    cubicTo(25f * scaleX, 9f * scaleY, 55f * scaleX, 5f * scaleY, 65f * scaleX, 19f * scaleY)
                    cubicTo(82f * scaleX, 15f * scaleY, 88f * scaleX, 37f * scaleY, 75f * scaleX, 52f * scaleY)
                    close()
                }
                drawPath(path = cloudPath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Lightning spike
                val boltPath = Path().apply {
                    moveTo(48f * scaleX, 55f * scaleY)
                    lineTo(35f * scaleX, 70f * scaleY)
                    lineTo(45f * scaleX, 70f * scaleY)
                    lineTo(38f * scaleX, 88f * scaleY)
                    lineTo(58f * scaleX, 66f * scaleY)
                    lineTo(48f * scaleX, 66f * scaleY)
                    close()
                }
                drawPath(path = boltPath, color = color)
            }
            else -> { // DEFAULT / CLEAR SUN/MOON
                val scale = width / 10f
                drawCircle(color = color.copy(alpha = 0.3f), radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(3.5f * scale, 4.5f * scale))
                drawCircle(color = color.copy(alpha = 0.7f), radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(5f * scale, 6f * scale))
                drawCircle(color = color.copy(alpha = 0.4f), radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(6.5f * scale, 4.2f * scale))
            }
        }
    }
}
