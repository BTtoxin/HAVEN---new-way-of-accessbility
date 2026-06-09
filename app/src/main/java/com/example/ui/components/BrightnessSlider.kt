package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.utils.AudioHapticEngine
import com.example.utils.SystemSettingsHelper
import kotlin.math.roundToInt

import com.example.ui.components.NothingSlider

@Composable
fun BrightnessSlider(
    hasWriteSettingsPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var brightnessState by remember { mutableFloatStateOf(128f) }
    var volumeState by remember { mutableFloatStateOf(50f) }

    // Read current system brightness on load
    LaunchedEffect(Unit) {
        try {
            val current = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            brightnessState = current.toFloat()
        } catch (e: Exception) {}
    }

    // Keep track of interaction state to emulate bouncy Framer Motion tactile spring scales
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring scaling for the overall slider bento card
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SliderSpringScale"
    )

    BentoCard(
        title = "SYSTEM CONTROLS",
        icon = Icons.Default.BrightnessMedium,
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Brightness • ${((brightnessState / 255f) * 100).roundToInt()}%",
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = Icons.Default.BrightnessLow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NothingSlider(
                value = brightnessState,
                onValueChange = { newValue ->
                    val oldValInt = brightnessState.roundToInt()
                    val newValInt = newValue.roundToInt()
                    if (Math.abs(oldValInt - newValInt) >= 12) {
                        AudioHapticEngine.triggerClick(context)
                    }
                    
                    brightnessState = newValue
                    if (hasWriteSettingsPermission) {
                        SystemSettingsHelper.setScreenBrightness(context, newValue.toInt())
                    }
                },
                valueRange = 0f..255f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Media Volume • ${volumeState.roundToInt()}%",
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Vol",
                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            NothingSlider(
                value = volumeState,
                onValueChange = { newValue ->
                    val oldValInt = volumeState.roundToInt()
                    val newValInt = newValue.roundToInt()
                    if (Math.abs(oldValInt - newValInt) >= 5) {
                        AudioHapticEngine.triggerClick(context)
                    }
                    volumeState = newValue
                },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!hasWriteSettingsPermission) {
                Text(
                    text = "Requires system write settings authorization",
                    style = AppTypography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Custom Nothing OS styling with active haptics",
                    style = AppTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
