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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = label, style = AppTypography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = valueDisplay, style = AppTypography.labelSmall, color = NeutralGray)
        }
        
        val isDark = MaterialTheme.colorScheme.background == PitchBlack
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = if (isDark) PureWhite else PitchBlack,
                activeTrackColor = NothingRed,
                inactiveTrackColor = BorderDark
            )
        )
    }
}
