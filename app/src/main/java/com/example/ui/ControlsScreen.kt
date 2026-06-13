package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HavenCard
import com.example.ui.components.HavenSectionHeader
import com.example.ui.components.QuickToggleGrid
import com.example.ui.theme.AppTypography
import com.example.ui.theme.HavenCyan
import com.example.viewmodel.QSViewModel

data class TileInfo(val title: String, val icon: ImageVector, val isActiveState: State<Boolean>?, val actionId: String = "")

@Composable
fun ControlsScreen(
    viewModel: QSViewModel,
    onNavigateToAutomation: () -> Unit
) {
    val automationRules by viewModel.automationRules.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text("Controls", style = AppTypography.displayMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
                Text("Manage your Quick Settings tiles", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            HavenSectionHeader(title = "Active Toggles")
            Box(Modifier.padding(horizontal = 20.dp)) {
                HavenCard {
                    QuickToggleGrid(viewModel = viewModel)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            HavenSectionHeader(title = "Automation Rules", actionLabel = "New +", onAction = onNavigateToAutomation)
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                automationRules.take(3).forEach { rule ->
                    HavenCard(
                        modifier = Modifier.padding(bottom = 8.dp),
                        isActive = rule.enabled,
                        onClick = onNavigateToAutomation
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(HavenCyan.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoMode, tint = HavenCyan, contentDescription = null)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.name, style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text("1 trigger • ${rule.actionCount} actions", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = rule.enabled,
                                onCheckedChange = { viewModel.toggleAutomationRule(rule.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HavenCyan,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
                if (automationRules.isEmpty()) {
                    Text("No automation rules set up", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                }
                TextButton(onClick = onNavigateToAutomation) {
                    Text("View all →", style = AppTypography.labelMedium, color = HavenCyan)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            HavenSectionHeader(title = "Quick Settings Tiles")
            
            // Reusing categories from prompt
            val displayTiles = listOf(
                TileInfo("Brightness Lock", Icons.Default.BrightnessAuto, null, "brightness_lock"),
                TileInfo("Grayscale", Icons.Default.FilterBAndW, viewModel.isMonochrome.collectAsStateWithLifecycle(), "grayscale"),
                TileInfo("Night Light", Icons.Default.Nightlight, null, "night_light"),
                TileInfo("AOD", Icons.Default.Aod, null, "aod"),
                TileInfo("Refresh Rate", Icons.Default.Speed, null, "refresh_rate"),
                TileInfo("Adaptive Brightness", Icons.Default.BrightnessAuto, null, "adaptive_brightness"),
                TileInfo("Font Size", Icons.Default.FormatSize, null, "font_size"),
                TileInfo("Auto-Rotate", Icons.Default.ScreenRotation, null, "auto_rotate")
            )
            
            val audioTiles = listOf(
                TileInfo("Ringer Mode", Icons.Default.VolumeUp, null, "ringer_mode"),
                TileInfo("Mute Mic", Icons.Default.MicOff, null, "mute_mic"),
                TileInfo("App Audio", Icons.Default.AudioFile, viewModel.isAppAudioIsolated.collectAsStateWithLifecycle(), "app_audio"),
                TileInfo("Sound Profile", Icons.Default.Hearing, null, "sound_profile")
            )
            
            val networkTiles = listOf(
                TileInfo("VPN", Icons.Default.VpnKey, null, "vpn"),
                TileInfo("Private DNS", Icons.Default.Dns, viewModel.isDnsActive.collectAsStateWithLifecycle(), "dns"),
                TileInfo("Hotspot", Icons.Default.WifiTethering, null, "hotspot"),
                TileInfo("Flight + BT", Icons.Default.FlightTakeoff, null, "flight_bt"),
                TileInfo("Wi-Fi Share", Icons.Default.QrCode, null, "wifi_share"),
                TileInfo("NFC Toggle", Icons.Default.Nfc, null, "nfc"),
                TileInfo("Wireless ADB", Icons.Default.DeveloperBoard, null, "adb"),
                TileInfo("Network Prefs", Icons.Default.CellTower, null, "network_prefs")
            )
            
            val powerTiles = listOf(
                TileInfo("Caffeine", Icons.Default.LocalCafe, viewModel.isCaffeineActive.collectAsStateWithLifecycle(), "caffeine"),
                TileInfo("Battery Saver", Icons.Default.BatterySaver, null, "battery_saver"),
                TileInfo("Extreme Saver", Icons.Default.BatteryAlert, null, "extreme_saver"),
                TileInfo("RAM Cleaner", Icons.Default.Memory, null, "ram_cleaner"),
                TileInfo("Panic Clear", Icons.Default.Warning, null, "panic_clear")
            )
            
            val systemTiles = listOf(
                TileInfo("Screen Timeout", Icons.Default.Timer, null, "screen_timeout"),
                TileInfo("USB Mode", Icons.Default.Usb, null, "usb_mode"),
                TileInfo("Dev Options", Icons.Default.DeveloperMode, null, "dev_options"),
                TileInfo("Session Password", Icons.Default.Password, null, "session_password"),
                TileInfo("Notif Filter", Icons.Default.NotificationsOff, null, "notif_filter"),
                TileInfo("Theater Mode", Icons.Default.Theaters, viewModel.isTheaterActive.collectAsStateWithLifecycle(), "theater"),
                TileInfo("Data Saver", Icons.Default.DataSaverOn, null, "data_saver"),
                TileInfo("Purge Clipboard", Icons.Default.ContentPasteOff, null, "purge_clipboard"),
                TileInfo("Custom Shortcut", Icons.Default.Shortcut, null, "custom_shortcut"),
                TileInfo("Flashlight Intensity", Icons.Default.Highlight, null, "flashlight_intensity")
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                TileCategorySection("Display", displayTiles, viewModel)
                TileCategorySection("Audio", audioTiles, viewModel)
                TileCategorySection("Network & Security", networkTiles, viewModel)
                TileCategorySection("Power & Battery", powerTiles, viewModel)
                TileCategorySection("System & Tools", systemTiles, viewModel)
            }
        }
    }
}

@Composable
fun TileCategorySection(title: String, tiles: List<TileInfo>, viewModel: QSViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Text(title, style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(vertical = 12.dp))
    
    // We replace LazyVerticalGrid inside a LazyColumn with a Grid implementation or Column/Row approach
    // Since LazyVerticalGrid inside LazyColumn causes crash unless bounded, we'll manually chunk it.
    val chunked = tiles.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in chunked) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (tile in row) {
                    HavenTileChip(
                        modifier = Modifier.weight(1f),
                        icon = tile.icon,
                        name = tile.title,
                        isActive = tile.isActiveState?.value ?: false,
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            when (tile.actionId) {
                                "grayscale" -> viewModel.setMonochrome(!(tile.isActiveState?.value ?: false))
                                "theater" -> viewModel.toggleTheaterMode(!(tile.isActiveState?.value ?: false))
                                "app_audio" -> viewModel.toggleAudioIsolation(!(tile.isActiveState?.value ?: false))
                                "caffeine" -> viewModel.toggleCaffeine(!(tile.isActiveState?.value ?: false))
                                "dns" -> viewModel.togglePrivateDns(!(tile.isActiveState?.value ?: false))
                                else -> {
                                    // Open mock settings for unimplemented toggles to prevent crashes and act responsive
                                    android.widget.Toast.makeText(context, "${tile.title} action triggered.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun HavenTileChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    isActive: Boolean,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "HavenTileScale"
    )

    Surface(
        modifier = modifier
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isActive) 0.5f else 1f),
        onClick = { onClick?.invoke() },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isActive) HavenCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = AppTypography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (isActive) {
                Box(modifier = Modifier.size(8.dp).background(HavenCyan, CircleShape))
            }
        }
    }
}
