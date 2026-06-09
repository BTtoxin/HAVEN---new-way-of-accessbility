package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography

@Composable
fun MinimalistPlaceholder(
    type: String, // "BATTERY" or "WEATHER"
    message: String = "TELEMETRY UNLINKED",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlaceholderAnim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFFDF8F6)
    val cardBg = if (isDark) Color(0xFF121212) else Color(0xFFF5F0EE)
    val textCol = if (isDark) Color.White else Color.Black
    val edgeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, edgeColor, RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dotted circular outline drawing representing generic offline telemetry
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                color = textCol.copy(alpha = pulseAlpha * 0.15f),
                radius = size.minDimension / 2,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (type == "BATTERY") Icons.Default.BatteryAlert else Icons.Default.CloudOff,
                contentDescription = "No data",
                tint = textCol.copy(alpha = pulseAlpha),
                modifier = Modifier.size(24.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (type == "BATTERY") "BATTERY STATISTICS" else "WEATHER SERVICE",
                    style = AppTypography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = textCol.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.uppercase(),
                    style = AppTypography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = textCol.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}
