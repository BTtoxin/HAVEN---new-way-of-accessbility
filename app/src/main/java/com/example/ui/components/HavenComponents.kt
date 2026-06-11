package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.HavenCyan
import com.example.utils.AudioHapticEngine

import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HavenCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    accentColor: Color = HavenCyan,
    isActive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null || onLongClick != null)
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,  // Remove default ripple completely for cleaner look or use LocalIndication
                        onClick = {
                            AudioHapticEngine.triggerClick(context)
                            onClick?.invoke()
                        },
                        onLongClick = onLongClick
                    )
                else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) accentColor.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isActive) BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f)) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun HavenSectionHeader(title: String, actionLabel: String = "", onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (actionLabel.isNotEmpty()) {
            TextButton(onClick = onAction) {
                Text("$actionLabel ›", style = AppTypography.labelMedium, color = HavenCyan)
            }
        }
    }
}

@Composable
fun HavenPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, isPrimary: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) HavenCyan else MaterialTheme.colorScheme.surface,
            contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, style = AppTypography.labelLarge.copy(fontWeight = FontWeight.ExtraBold))
    }
}

@Composable
fun HavenChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        color = if (isSelected) HavenCyan else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(40.dp)
    ) {
        Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
