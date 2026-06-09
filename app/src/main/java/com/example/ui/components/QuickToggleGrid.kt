package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.Dns
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun QuickToggleGrid(
    viewModel: QSViewModel,
    isFlashlightActive: Boolean = false,
    onToggleFlashlight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isWifiActive by viewModel.isWifiActive.collectAsState()
    val isBluetoothActive by viewModel.isBluetoothActive.collectAsState()
    val isDataActive by viewModel.isDataActive.collectAsState()
    val isHotspotActive by viewModel.isHotspotActive.collectAsState()

    val isDnsActive by viewModel.isDnsActive.collectAsState()

    val toggleOrder by viewModel.quickToggleOrder.collectAsState()
    val tileSizes by viewModel.quickToggleSizes.collectAsState()

    // Keep track of which tile is currently being dragged
    var activeDragId by remember { mutableStateOf<String?>(null) }

    // Layout Saved status indicator state
    var showLayoutSaved by remember { mutableStateOf(false) }
    LaunchedEffect(showLayoutSaved) {
        if (showLayoutSaved) {
            delay(2500)
            showLayoutSaved = false
        }
    }

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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Animated Layout Saved indicator
                AnimatedVisibility(
                    visible = showLayoutSaved,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Layout Saved",
                            style = AppTypography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        )
                    }
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
                        text = "Cloud Synced",
                        style = AppTypography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // We wrap the tiles inside a responsive grid list based on toggleOrder
        val orderedList = remember(toggleOrder) {
            val list = toggleOrder.toMutableList()
            val defaultToggles = listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT", "FLASHLIGHT", "DNS")
            for (tog in defaultToggles) {
                if (!list.contains(tog)) list.add(tog)
            }
            list.take(6)
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
                showLayoutSaved = true
            }
        }

        Text(
            text = "Tip: Long-press any tile to drag & drop to reorder layout. Tap small pill index at top right to cycle sizing.",
            style = AppTypography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
            modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 8.dp)
        )

        // Dynamic layout rows packing generator
        val rows = remember(orderedList, tileSizes) {
            val list = mutableListOf<List<String>>()
            var i = 0
            while (i < orderedList.size) {
                val currentId = orderedList[i]
                val currentSize = tileSizes[currentId] ?: "STANDARD"
                
                if (currentSize == "WIDE") {
                    list.add(listOf(currentId))
                    i++
                } else {
                    // Try to pair with the next tile IF the next tile is also non-WIDE
                    if (i + 1 < orderedList.size) {
                        val nextId = orderedList[i + 1]
                        val nextSize = tileSizes[nextId] ?: "STANDARD"
                        if (nextSize != "WIDE") {
                            list.add(listOf(currentId, nextId))
                            i += 2
                        } else {
                            list.add(listOf(currentId))
                            i++
                        }
                    } else {
                        list.add(listOf(currentId))
                        i++
                    }
                }
            }
            list
        }

        // dynamic packing row outputs
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEach { rowTiles ->
                if (rowTiles.size == 1) {
                    val tileId = rowTiles[0]
                    val tileSize = tileSizes[tileId] ?: "STANDARD"
                    val tileHeight = if (tileSize == "COMPACT") 80.dp else if (tileSize == "WIDE") 92.dp else 115.dp
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tileHeight)
                            .draggableTile(
                                id = tileId,
                                activeDragId = activeDragId,
                                onDragStart = { activeDragId = tileId },
                                onDragEnd = { activeDragId = null },
                                onDrag = { x, y ->
                                    val idx = orderedList.indexOf(tileId)
                                    if (y > 100f && idx + 1 < orderedList.size) {
                                        swapToggles(tileId, orderedList[idx + 1])
                                    } else if (y < -100f && idx - 1 >= 0) {
                                        swapToggles(tileId, orderedList[idx - 1])
                                    }
                                }
                            )
                    ) {
                        RenderTile(
                            id = tileId,
                            tileSize = tileSize,
                            isWifiActive = isWifiActive,
                            isBluetoothActive = isBluetoothActive,
                            isDataActive = isDataActive,
                            isHotspotActive = isHotspotActive,
                            isFlashlightActive = isFlashlightActive,
                            isDnsActive = isDnsActive,
                            onToggleFlashlight = onToggleFlashlight,
                            viewModel = viewModel
                        )
                    }
                } else if (rowTiles.size == 2) {
                    val leftId = rowTiles[0]
                    val rightId = rowTiles[1]
                    val leftSize = tileSizes[leftId] ?: "STANDARD"
                    val rightSize = tileSizes[rightId] ?: "STANDARD"
                    val tileHeight = if (leftSize == "COMPACT" && rightSize == "COMPACT") 80.dp else 115.dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tileHeight),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .draggableTile(
                                    id = leftId,
                                    activeDragId = activeDragId,
                                    onDragStart = { activeDragId = leftId },
                                    onDragEnd = { activeDragId = null },
                                    onDrag = { x, y ->
                                        if (x > 140f) swapToggles(leftId, rightId)
                                        val idx = orderedList.indexOf(leftId)
                                        if (y > 100f && idx + 2 < orderedList.size) {
                                            swapToggles(leftId, orderedList[idx + 2])
                                        } else if (y < -100f && idx - 2 >= 0) {
                                            swapToggles(leftId, orderedList[idx - 2])
                                        }
                                    }
                                )
                        ) {
                            RenderTile(
                                id = leftId,
                                tileSize = leftSize,
                                isWifiActive = isWifiActive,
                                isBluetoothActive = isBluetoothActive,
                                isDataActive = isDataActive,
                                isHotspotActive = isHotspotActive,
                                isFlashlightActive = isFlashlightActive,
                                isDnsActive = isDnsActive,
                                onToggleFlashlight = onToggleFlashlight,
                                viewModel = viewModel
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .draggableTile(
                                    id = rightId,
                                    activeDragId = activeDragId,
                                    onDragStart = { activeDragId = rightId },
                                    onDragEnd = { activeDragId = null },
                                    onDrag = { x, y ->
                                        if (x < -140f) swapToggles(rightId, leftId)
                                        val idx = orderedList.indexOf(rightId)
                                        if (y > 100f && idx + 2 < orderedList.size) {
                                            swapToggles(rightId, orderedList[idx + 2])
                                        } else if (y < -100f && idx - 2 >= 0) {
                                            swapToggles(rightId, orderedList[idx - 2])
                                        }
                                    }
                                )
                        ) {
                            RenderTile(
                                id = rightId,
                                tileSize = rightSize,
                                isWifiActive = isWifiActive,
                                isBluetoothActive = isBluetoothActive,
                                isDataActive = isDataActive,
                                isHotspotActive = isHotspotActive,
                                isFlashlightActive = isFlashlightActive,
                                isDnsActive = isDnsActive,
                                onToggleFlashlight = onToggleFlashlight,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


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

fun Modifier.tileSwipeListener(
    id: String,
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {}
): Modifier {
    return this.pointerInput(id) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var totalDragY = 0f
            var triggered = false
            while (true) {
                val event = awaitPointerEvent()
                val anyPressed = event.changes.any { it.pressed }
                if (!anyPressed) break
                
                val change = event.changes.firstOrNull()
                if (change != null) {
                    val dragY = change.position.y - change.previousPosition.y
                    totalDragY += dragY
                    if (!triggered) {
                        if (totalDragY < -75f) {
                            triggered = true
                            onSwipeUp()
                            change.consume()
                        } else if (totalDragY > 75f) {
                            triggered = true
                            onSwipeDown()
                            change.consume()
                        }
                    }
                    if (triggered) {
                        change.consume()
                    }
                }
            }
        }
    }
}

@Composable
fun RenderTile(
    id: String,
    tileSize: String,
    isWifiActive: Boolean,
    isBluetoothActive: Boolean,
    isDataActive: Boolean,
    isHotspotActive: Boolean,
    isFlashlightActive: Boolean,
    isDnsActive: Boolean,
    onToggleFlashlight: () -> Unit,
    viewModel: QSViewModel
) {
    when (id) {
        "WIFI" -> QuickToggleTile(
            id = id,
            title = "Wi-Fi",
            tileSize = tileSize,
            isActive = isWifiActive,
            onToggle = { 
                viewModel.toggleWifi() 
                viewModel.logTileClick("WIFI")
            },
            activeIcon = Icons.Default.Wifi,
            inactiveIcon = Icons.Default.Wifi,
            viewModel = viewModel,
            modifier = Modifier.tileSwipeListener(
                id = id,
                onSwipeUp = { viewModel.toggleAirplaneMode() }
            )
        )
        "BLUETOOTH" -> QuickToggleTile(
            id = id,
            title = "Bluetooth",
            tileSize = tileSize,
            isActive = isBluetoothActive,
            onToggle = { 
                viewModel.toggleBluetooth() 
                viewModel.logTileClick("BLUETOOTH")
            },
            activeIcon = Icons.Default.Bluetooth,
            inactiveIcon = Icons.Default.Bluetooth,
            viewModel = viewModel,
            modifier = Modifier.tileSwipeListener(
                id = id,
                onSwipeUp = { viewModel.setMonochrome(!viewModel.isMonochrome.value) }
            )
        )
        "DATA" -> QuickToggleTile(
            id = id,
            title = "Mobile Data",
            tileSize = tileSize,
            isActive = isDataActive,
            onToggle = { 
                viewModel.toggleData() 
                viewModel.logTileClick("DATA")
            },
            activeIcon = Icons.Default.SignalCellularAlt,
            inactiveIcon = Icons.Default.SignalCellularAlt,
            viewModel = viewModel,
            modifier = Modifier.tileSwipeListener(
                id = id,
                onSwipeUp = { viewModel.togglePrivateDns(!viewModel.isDnsActive.value) }
            )
        )
        "HOTSPOT" -> QuickToggleTile(
            id = id,
            title = "Hotspot",
            tileSize = tileSize,
            isActive = isHotspotActive,
            onToggle = { 
                viewModel.toggleHotspot() 
                viewModel.logTileClick("HOTSPOT")
            },
            activeIcon = Icons.Default.WifiTethering,
            inactiveIcon = Icons.Default.WifiTethering,
            viewModel = viewModel,
            modifier = Modifier.tileSwipeListener(
                id = id,
                onSwipeUp = { viewModel.toggleCaffeine(!viewModel.isCaffeineActive.value) }
            )
        )
        "FLASHLIGHT" -> QuickToggleTile(
            id = id,
            title = "Flashlight",
            tileSize = tileSize,
            isActive = isFlashlightActive,
            onToggle = { 
                onToggleFlashlight()
                viewModel.logTileClick("FLASHLIGHT")
            },
            activeIcon = Icons.Default.FlashlightOn,
            inactiveIcon = Icons.Default.FlashlightOff,
            viewModel = viewModel
        )
        "DNS" -> QuickToggleTile(
            id = id,
            title = "Private DNS",
            tileSize = tileSize,
            isActive = isDnsActive,
            onToggle = { 
                viewModel.togglePrivateDns(!isDnsActive)
                viewModel.logTileClick("DNS")
            },
            activeIcon = Icons.Default.Dns,
            inactiveIcon = Icons.Default.Dns,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickToggleTile(
    id: String,
    title: String,
    tileSize: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    viewModel: QSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
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

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .combinedClickable(
                    onClick = { onToggle() },
                    onLongClick = {
                        showMenu = true
                        AudioHapticEngine.triggerClick(context)
                    }
                ),
        shape = RoundedCornerShape(20.dp),
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
        if (tileSize == "COMPACT") {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .drawBehind {
                            if (isActive) {
                                drawCircle(
                                    color = Color.White.copy(alpha = pulseAlpha * 0.15f),
                                    radius = (kotlin.math.min(this.size.width, this.size.height) / 1.1f)
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
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = AppTypography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        ),
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                    Text(
                        text = if (isActive) "ON" else "OFF",
                        style = AppTypography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }

                SizeBadge(id = id, isActive = isActive, tileSize = tileSize, viewModel = viewModel, context = context)
            }
        } else if (tileSize == "WIDE") {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.width(100.dp),
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
                                            radius = (kotlin.math.min(this.size.width, this.size.height) / 1.1f)
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

                        SizeBadge(id = id, isActive = isActive, tileSize = tileSize, viewModel = viewModel, context = context)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column {
                        Text(
                            text = title,
                            style = AppTypography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            ),
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1
                        )
                        Text(
                            text = if (isActive) "ACTIVE" else "OFFLINE",
                            style = AppTypography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(
                            if (isActive) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            }
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isActive) "REAL-TIME TELEMETRY" else "TELEMETRY UNSTABLE",
                        style = AppTypography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        ),
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    D3MicroChart(isActive = isActive, chartType = id, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
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
                                        radius = (kotlin.math.min(this.size.width, this.size.height) / 1.1f)
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                        SizeBadge(id = id, isActive = isActive, tileSize = tileSize, viewModel = viewModel, context = context)
                    }
                }

                if (id == "WIFI" && isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    D3MicroChart(isActive = isActive, chartType = id, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
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

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            when (id) {
                "WIFI" -> {
                    DropdownMenuItem(
                        text = { Text("Scan Channels", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Scanning WiFi channels... Done", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("IP Information", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Connection: 192.168.1.144 (Static IPv4)", Toast.LENGTH_LONG).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("WiFi Preferences...", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            try {
                                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open wifi settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                "BLUETOOTH" -> {
                    DropdownMenuItem(
                        text = { Text("Pair New Device", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            try {
                                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open bluetooth settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear BT Cache", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Bluetooth daemon cache cleared.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "DATA" -> {
                    DropdownMenuItem(
                        text = { Text("Network Deep Settings", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            try {
                                val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                    context.startActivity(fallbackIntent)
                                } catch (e2: Exception) {}
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Data Usage Tracker", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Standard limit: 4.8 GB of 20 GB used", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                "HOTSPOT" -> {
                    DropdownMenuItem(
                        text = { Text("Display Credentials", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "SSID: NothingQS_5G  Pass: NothingQS5G!", Toast.LENGTH_LONG).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("AP Settings...", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            try {
                                val intent = Intent("android.settings.WIFI_AP_SETTINGS")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open tethering settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                "DNS" -> {
                    DropdownMenuItem(
                        text = { Text("DNS Deep Settings", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            try {
                                val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                    context.startActivity(fallbackIntent)
                                } catch (e2: Exception) {}
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Flush DNS Cache", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "DNS cache flushed securely.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "FLASHLIGHT" -> {
                    DropdownMenuItem(
                        text = { Text("Adjust Brightness Intensity", fontWeight = FontWeight.Bold, style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Flashlight intensity control not supported on this hardware yet.", Toast.LENGTH_LONG).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("SOS Strobe Mode", style = AppTypography.bodyMedium) },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "SOS pattern initialized.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SizeBadge(
    id: String,
    isActive: Boolean,
    tileSize: String,
    viewModel: QSViewModel,
    context: android.content.Context
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) {
                    Color.White.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                }
            )
            .clickable {
                val nextSize = when (tileSize) {
                    "STANDARD" -> "COMPACT"
                    "COMPACT" -> "WIDE"
                    else -> "STANDARD"
                }
                viewModel.setQuickToggleSize(id, nextSize)
                AudioHapticEngine.triggerClick(context)
            }
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val label = when (tileSize) {
            "COMPACT" -> "XS"
            "WIDE" -> "XL"
            else -> "MD"
        }
        Text(
            text = label,
            style = AppTypography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 7.5.sp,
                letterSpacing = 0.sp
            ),
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun D3MicroChart(
    isActive: Boolean,
    chartType: String,
    modifier: Modifier = Modifier
) {
    var dataPoints by remember { mutableStateOf(listOf(40f, 35f, 50f, 45f, 65f, 52f, 85f, 70f, 95f, 110f)) }
    var currentValue by remember { mutableStateOf(110f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                delay(1200)
                val deviation = (kotlin.random.Random.nextFloat() * 20f) - 10f
                val nextVal = when (chartType) {
                    "WIFI" -> (currentValue + deviation).coerceIn(40f, 150f)
                    "DATA" -> (currentValue + deviation).coerceIn(-95f, -50f)
                    "BLUETOOTH" -> (currentValue + deviation).coerceIn(5f, 45f)
                    "HOTSPOT" -> (currentValue + (if (kotlin.random.Random.nextBoolean()) 0.5f else -0.5f)).coerceIn(1f, 4f)
                    else -> (currentValue + deviation).coerceIn(0f, 100f)
                }
                currentValue = nextVal
                dataPoints = (dataPoints + nextVal).takeLast(12)
            }
        } else {
            currentValue = 0f
            dataPoints = List(12) { 0f }
        }
    }

    if (!isActive) {
        Box(modifier = modifier.height(30.dp), contentAlignment = Alignment.CenterStart) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, size.height * 0.9f)
                path.lineTo(size.width, size.height * 0.9f)
                this.drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.15f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                if (dataPoints.size < 2) return@Canvas

                val minVal = dataPoints.minOrNull() ?: 0f
                val maxVal = dataPoints.maxOrNull() ?: 100f
                val range = if (maxVal == minVal) 1f else maxVal - minVal

                val stepX = width / (dataPoints.size - 1)
                val strokePath = androidx.compose.ui.graphics.Path()
                val areaPath = androidx.compose.ui.graphics.Path()

                val coords = dataPoints.mapIndexed { idx, valRaw ->
                    val x = idx * stepX
                    val pct = (valRaw - minVal) / range
                    val y = height - (pct * height * 0.70f) - (height * 0.15f)
                    androidx.compose.ui.geometry.Offset(x, y.toFloat())
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

                this.drawPath(
                    path = areaPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                )

                this.drawPath(
                    path = strokePath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            val formattedValue = when (chartType) {
                "WIFI" -> "${currentValue.roundToInt()}"
                "DATA" -> "${currentValue.roundToInt()}"
                "BLUETOOTH" -> "${currentValue.roundToInt()}"
                "HOTSPOT" -> String.format("%.1f", currentValue)
                else -> "${currentValue.roundToInt()}"
            }
            val unit = when (chartType) {
                "WIFI" -> "Mbps"
                "DATA" -> "dBm"
                "BLUETOOTH" -> "ms"
                "HOTSPOT" -> "cli"
                else -> ""
            }

            Text(
                text = formattedValue,
                style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = -0.2.sp),
                color = Color.White
            )
            Text(
                text = unit,
                style = AppTypography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
