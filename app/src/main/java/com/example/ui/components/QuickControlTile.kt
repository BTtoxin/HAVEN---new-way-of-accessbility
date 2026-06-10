package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
    index: Int = 0,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 50).toLong())
        isVisible = true
    }
    
    val popScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "popScale"
    )
    val popAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "popAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isHovered) 1.05f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "tileScale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (isHovered || isPressed) 0.8f else 1f,
        label = "tileOpacity"
    )

    Box(
        modifier = modifier
            .alpha(popAlpha)
            .scale(popScale * scale)
            .alpha(opacity)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .hoverable(interactionSource = interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = MaterialTheme.colorScheme.onBackground),
                onClick = {
                    com.example.utils.AudioHapticEngine.triggerClick(context)
                    onClick()
                },
                onLongClick = onLongClick
            )
            .padding(16.dp)
            .height(110.dp) // Giving some fixed height to match HTML design roughly
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    val springSpec = androidx.compose.animation.core.spring<kotlin.Float>(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                    (androidx.compose.animation.scaleIn(animationSpec = springSpec) + androidx.compose.animation.fadeIn()) togetherWith (androidx.compose.animation.scaleOut(animationSpec = springSpec) + androidx.compose.animation.fadeOut())
                },
                label = "TileIconAnim"
            ) { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title.uppercase(),
                    style = AppTypography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = AppTypography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                    color = subtitleColor
                )
            }
        }
    }
}
