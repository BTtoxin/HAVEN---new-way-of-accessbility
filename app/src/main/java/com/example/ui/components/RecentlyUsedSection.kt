package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NeutralGray
import com.example.viewmodel.QSViewModel

@Composable
fun RecentlyUsedSection(
    recentlyUsed: List<String>,
    viewModel: QSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe active states for highlighting recently used tiles
    val isWifi by viewModel.isWifiActive.collectAsStateWithLifecycle()
    val isBluetooth by viewModel.isBluetoothActive.collectAsStateWithLifecycle()
    val isData by viewModel.isDataActive.collectAsStateWithLifecycle()
    val isHotspot by viewModel.isHotspotActive.collectAsStateWithLifecycle()
    val isAirplane by viewModel.isAirplaneMode.collectAsStateWithLifecycle()
    val isCaffeine by viewModel.isCaffeineActive.collectAsStateWithLifecycle()
    val isDns by viewModel.isDnsActive.collectAsStateWithLifecycle()
    val isTheater by viewModel.isTheaterActive.collectAsStateWithLifecycle()
    val isAudioIsolated by viewModel.isAppAudioIsolated.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Signature active Red Dot for Nothing OS aesthetic
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFE53935), shape = CircleShape)
                )
                Text(
                    text = "RECENTLY USED TILES",
                    style = AppTypography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = NeutralGray
                )
            }

            // Real-time Firebase Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF10B981), shape = CircleShape) // Emerald Green
                )
                Text(
                    text = "FIREBASE LIVE SYNCED",
                    style = AppTypography.bodySmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                )
            }
        }

        // Horizontal Carousel of last 5 used tiles
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentlyUsed, key = { it }) { tileId ->
                // Gather details for rendering
                val tileInfo = getTilePresentationInfo(
                    id = tileId,
                    isWifi = isWifi,
                    isBluetooth = isBluetooth,
                    isData = isData,
                    isHotspot = isHotspot,
                    isAirplane = isAirplane,
                    isCaffeine = isCaffeine,
                    isDns = isDns,
                    isTheater = isTheater,
                    isAudioIsolated = isAudioIsolated
                )

                // Render dynamic tactile interactive button
                RecentTileItem(
                    info = tileInfo,
                    onClick = {
                        viewModel.triggerTileAction(tileId)
                    }
                )
            }
        }
    }
}

@Composable
fun RecentTileItem(
    info: TilePresentationInfo,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale-down tactile response matching Framer Motion's dynamic physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "tileScale"
    )

    // Dynamic coloring based on active state
    val containerColor = animateColorAsState(
        targetValue = if (info.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "containerColor"
    )
    
    val borderColor = animateColorAsState(
        targetValue = if (info.isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColor"
    )

    val iconColor = animateColorAsState(
        targetValue = if (info.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        label = "iconColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor.value)
            .border(
                border = BorderStroke(if (info.isActive) 1.5.dp else 0.dp, borderColor.value),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Rounded Mini Card representing Dot Matrix and Glyph structure
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (info.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.label,
                tint = iconColor.value,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = info.label,
            style = AppTypography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (info.isActive) 1.0f else 0.7f)
        )
    }
}

// Presentation mapping schema
data class TilePresentationInfo(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isActive: Boolean
)

fun getTilePresentationInfo(
    id: String,
    isWifi: Boolean,
    isBluetooth: Boolean,
    isData: Boolean,
    isHotspot: Boolean,
    isAirplane: Boolean,
    isCaffeine: Boolean,
    isDns: Boolean,
    isTheater: Boolean,
    isAudioIsolated: Boolean
): TilePresentationInfo {
    return when (id) {
        "WIFI" -> TilePresentationInfo(id, "WI-FI", Icons.Default.Wifi, isWifi)
        "BLUETOOTH" -> TilePresentationInfo(id, "BLUETOOTH", Icons.Default.Bluetooth, isBluetooth)
        "DATA" -> TilePresentationInfo(id, "MOBILE DATA", Icons.Default.SignalCellularAlt, isData)
        "HOTSPOT" -> TilePresentationInfo(id, "HOTSPOT", Icons.Default.WifiTethering, isHotspot)
        "AIRPLANE" -> TilePresentationInfo(id, "AIRPLANE", Icons.Default.AirplanemodeActive, isAirplane)
        "TIMEOUT" -> TilePresentationInfo(id, "SCREEN TIME", Icons.Default.Timer, false)
        "CAFFEINE" -> TilePresentationInfo(id, "CAFFEINE", Icons.Default.Coffee, isCaffeine)
        "WEATHER" -> TilePresentationInfo(id, "WEATHER", Icons.Default.Cloud, false)
        "BATTERY" -> TilePresentationInfo(id, "BATTERY", Icons.Default.BatteryStd, false)
        "BRIGHTNESS" -> TilePresentationInfo(id, "BRIGHTNESS", Icons.Default.Brightness5, false)
        "DNS" -> TilePresentationInfo(id, "PRIVATE DNS", Icons.Default.Dns, isDns)
        "THEATER" -> TilePresentationInfo(id, "THEATER", Icons.Default.Theaters, isTheater)
        "CLIPBOARD" -> TilePresentationInfo(id, "CLIPBOARD", Icons.Default.ContentPaste, false)
        "FOCUS" -> TilePresentationInfo(id, "DEEP FOCUS", Icons.Default.Lock, false)
        "SHORTCUT" -> TilePresentationInfo(id, "SHORTCUT", Icons.Default.OpenInNew, false)
        "APP_AUDIO" -> TilePresentationInfo(id, "APP AUDIO", Icons.Default.VolumeOff, isAudioIsolated)
        "GLYPH" -> TilePresentationInfo(id, "GLYPH LIGHTS", Icons.Default.Brightness6, false)
        "CHANGELOG" -> TilePresentationInfo(id, "CHANGELOG", Icons.Default.Label, false)
        "MANUAL" -> TilePresentationInfo(id, "MANUAL", Icons.Default.MenuBook, false)
        "ABOUT" -> TilePresentationInfo(id, "ABOUT", Icons.Default.Info, false)
        else -> TilePresentationInfo(id, id, Icons.Default.ToggleOn, false)
    }
}
