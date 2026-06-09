package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite

@Composable
fun GlyphSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        
        val isDark = MaterialTheme.colorScheme.background == PitchBlack
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.then(
                if (checked) Modifier.shadow(
                    elevation = 12.dp,
                    spotColor = if (isDark) PureWhite else PitchBlack,
                    ambientColor = if (isDark) PureWhite else PitchBlack,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) else Modifier
            ),
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isDark) PitchBlack else PureWhite,
                checkedTrackColor = if (isDark) PureWhite else PitchBlack
            )
        )
    }
}
