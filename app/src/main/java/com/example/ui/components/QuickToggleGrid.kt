package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.viewmodel.QSViewModel
import com.example.utils.AudioHapticEngine
import kotlin.math.roundToInt

@Composable
fun QuickToggleGrid(
    viewModel: QSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWifiActive by viewModel.isWifiActive.collectAsState()
    val isBluetoothActive by viewModel.isBluetoothActive.collectAsState()
    val isDataActive by viewModel.isDataActive.collectAsState()
    val isHotspotActive by viewModel.isHotspotActive.collectAsState()

    val toggleOrder by viewModel.quickToggleOrder.collectAsState()

    // Keep track of which tile is currently being dragged
    var activeDragId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "NETWORK CONNECTIONS",
                    style = AppTypography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Hold to drag",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Firebase Synced",
                    style = AppTypography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // We wrap the tiles inside a responsive 2x2 grid based on toggleOrder
        val orderedList = remember(toggleOrder) {
            val list = toggleOrder.toMutableList()
            val defaultToggles = listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT")
            for (tog in defaultToggles) {
                if (!list.contains(tog)) list.add(tog)
            }
            list.take(4)
        }

        // Helper to swap order
        fun swapToggles(id1: String, id2: String) {
            val index1 = orderedList.indexOf(id1)
            val index2 = orderedList.indexOf(id2)
            if (index1 != -1 && index2 != -1) {
                val newOrder = orderedList.toMutableList()
                newOrder[index1] = id2
                newOrder[index2] = id1
                viewModel.updateQuickToggleOrder(newOrder)
                AudioHapticEngine.triggerClick(context)
            }
        }

        Text(
            text = "Tip: Long-press any tile to drag & drop to reorder layout",
            style = AppTypography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
            modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 8.dp)
        )

        // 2x2 Layout Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val leftId = orderedList[0]
                val rightId = orderedList[1]

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .draggableTile(
                            id = leftId,
                            activeDragId = activeDragId,
                            onDragStart = { activeDragId = leftId },
                            onDragEnd = { activeDragId = null },
                            onDrag = { x, y ->
                                if (x > 140f) swapToggles(leftId, rightId)
                                if (y > 100f) swapToggles(leftId, orderedList[2])
                            }
                        )
                ) {
                    RenderTile(
                        id = leftId,
                        isWifiActive = isWifiActive,
                        isBluetoothActive = isBluetoothActive,
                        isDataActive = isDataActive,
                        isHotspotActive = isHotspotActive,
                        viewModel = viewModel
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .draggableTile(
                            id = rightId,
                            activeDragId = activeDragId,
                            onDragStart = { activeDragId = rightId },
                            onDragEnd = { activeDragId = null },
                            onDrag = { x, y ->
                                if (x < -140f) swapToggles(rightId, leftId)
                                if (y > 100f) swapToggles(rightId, orderedList[3])
                            }
                        )
                ) {
                    RenderTile(
                        id = rightId,
                        isWifiActive = isWifiActive,
                        isBluetoothActive = isBluetoothActive,
                        isDataActive = isDataActive,
                        isHotspotActive = isHotspotActive,
                        viewModel = viewModel
                    )
                }
            }

            // Second Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val leftId = orderedList[2]
                val rightId = orderedList[3]

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .draggableTile(
                            id = leftId,
                            activeDragId = activeDragId,
                            onDragStart = { activeDragId = leftId },
                            onDragEnd = { activeDragId = null },
                            onDrag = { x, y ->
                                if (x > 140f) swapToggles(leftId, rightId)
                                if (y < -100f) swapToggles(leftId, orderedList[0])
                            }
                        )
                ) {
                    RenderTile(
                        id = leftId,
                        isWifiActive = isWifiActive,
                        isBluetoothActive = isBluetoothActive,
                        isDataActive = isDataActive,
                        isHotspotActive = isHotspotActive,
                        viewModel = viewModel
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .draggableTile(
                            id = rightId,
                            activeDragId = activeDragId,
                            onDragStart = { activeDragId = rightId },
                            onDragEnd = { activeDragId = null },
                            onDrag = { x, y ->
                                if (x < -140f) swapToggles(rightId, leftId)
                                if (y < -100f) swapToggles(rightId, orderedList[1])
                            }
                        )
                ) {
                    RenderTile(
                        id = rightId,
                        isWifiActive = isWifiActive,
                        isBluetoothActive = isBluetoothActive,
                        isDataActive = isDataActive,
                        isHotspotActive = isHotspotActive,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// Elegant drag utility using custom modifier extension directly
fun Modifier.draggableTile(
    id: String,
    activeDragId: String?,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float, Float) -> Unit
): Modifier {
    return this.pointerInput(id) {
        var offsetX = 0f
        var offsetY = 0f
        detectDragGesturesAfterLongPress(
            onDragStart = {
                onDragStart()
                offsetX = 0f
                offsetY = 0f
            },
            onDragEnd = {
                onDragEnd()
            },
            onDragCancel = {
                onDragEnd()
            },
            onDrag = { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
                onDrag(offsetX, offsetY)
            }
        )
    }
}

@Composable
fun RenderTile(
    id: String,
    isWifiActive: Boolean,
    isBluetoothActive: Boolean,
    isDataActive: Boolean,
    isHotspotActive: Boolean,
    viewModel: QSViewModel
) {
    when (id) {
        "WIFI" -> QuickToggleTile(
            title = "Wi-Fi",
            isActive = isWifiActive,
            onToggle = { viewModel.toggleWifi() },
            activeIcon = Icons.Default.Wifi,
            inactiveIcon = Icons.Default.Wifi
        )
        "BLUETOOTH" -> QuickToggleTile(
            title = "Bluetooth",
            isActive = isBluetoothActive,
            onToggle = { viewModel.toggleBluetooth() },
            activeIcon = Icons.Default.Bluetooth,
            inactiveIcon = Icons.Default.Bluetooth
        )
        "DATA" -> QuickToggleTile(
            title = "Mobile Data",
            isActive = isDataActive,
            onToggle = { viewModel.toggleData() },
            activeIcon = Icons.Default.SignalCellularAlt,
            inactiveIcon = Icons.Default.SignalCellularAlt
        )
        "HOTSPOT" -> QuickToggleTile(
            title = "Hotspot",
            isActive = isHotspotActive,
            onToggle = { viewModel.toggleHotspot() },
            activeIcon = Icons.Default.WifiTethering,
            inactiveIcon = Icons.Default.WifiTethering
        )
    }
}

@Composable
fun QuickToggleTile(
    title: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ToggleScale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "IconScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "GlyphPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .drawBehind {
                            if (isActive) {
                                drawCircle(
                                    color = Color.White.copy(alpha = pulseAlpha * 0.15f),
                                    radius = (size.minDimension / 1.1f)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) activeIcon else inactiveIcon,
                        contentDescription = title,
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                )
            }

            Column {
                Text(
                    text = title,
                    style = AppTypography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    ),
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isActive) "ACTIVE" else "OFFLINE",
                    style = AppTypography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}
