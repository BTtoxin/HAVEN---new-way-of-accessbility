package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.AppTypography
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite

@Composable
fun GlyphSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    valueDisplay: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lastRoundedValue = remember { mutableStateOf(value.toInt()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = label, style = AppTypography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = valueDisplay, style = AppTypography.labelSmall, color = NeutralGray)
        }
        
        val isDark = MaterialTheme.colorScheme.background != androidx.compose.ui.graphics.Color(0xFFFDF8F6)
        
        Slider(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                val rounded = newValue.toInt()
                if (rounded != lastRoundedValue.value) {
                    com.example.utils.AudioHapticEngine.triggerClick(context)
                    lastRoundedValue.value = rounded
                }
            },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = if (isDark) PureWhite else PitchBlack,
                activeTrackColor = NothingRed,
                inactiveTrackColor = BorderDark
            )
        )
    }
}
