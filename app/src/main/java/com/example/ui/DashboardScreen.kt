package com.example.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BentoCard
import com.example.ui.components.QuickControlTile
import com.example.ui.components.GlyphSwitch
import com.example.ui.components.SystemMonitorPanel
import com.example.ui.components.NotificationsOverlay
import com.example.ui.theme.*
import com.example.utils.SystemSettingsHelper
import com.example.viewmodel.QSViewModel
import android.hardware.camera2.CameraManager

@Composable
fun DashboardScreen(
    viewModel: QSViewModel,
    initialOpenFocus: Boolean = false,
    onRequestPermission: () -> Unit,
    onRequestDndPermission: () -> Unit
) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(android.content.Context.CAMERA_SERVICE) as? CameraManager }
    var isFlashlightOn by remember { mutableStateOf(false) }

    val hasWriteSettingsPermission by viewModel.hasWriteSettingsPermission.collectAsStateWithLifecycle()
    val hasDndPermission by viewModel.hasDndPermission.collectAsStateWithLifecycle()
    val systemTimeout by viewModel.currentSystemTimeout.collectAsStateWithLifecycle()
    val customShortcutTarget by viewModel.customShortcutTarget.collectAsStateWithLifecycle()
    val customShortcutLabel by viewModel.customShortcutLabel.collectAsStateWithLifecycle()
    val glyphProfile by viewModel.glyphBrightnessProfile.collectAsStateWithLifecycle()
    val isCaffeineActive by viewModel.isCaffeineActive.collectAsStateWithLifecycle()
    val isTheaterActive by viewModel.isTheaterActive.collectAsStateWithLifecycle()
    
    // We'll also collect private DNS from DataStore manually here since it wasn't in _isDnsActive in VM completely.
    val isDnsActive = SystemSettingsHelper.getPrivateDnsMode(context) == "hostname"
    val isAudioIsolated by viewModel.isAppAudioIsolated.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showSystemMonitor by remember { mutableStateOf(false) }

    val tileOrderList by viewModel.tileOrder.collectAsStateWithLifecycle()
    val availableOrder = if (tileOrderList.isEmpty()) listOf("TIMEOUT", "CAFFEINE", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH") else tileOrderList

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            // HEADER
            item(span = StaggeredGridItemSpan.FullLine) {
                var currentTime by remember { mutableStateOf(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }
                var currentDate by remember { mutableStateOf(java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())) }

                LaunchedEffect(Unit) {
                    while(true) {
                        currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        currentDate = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())
                        kotlinx.coroutines.delay(1000)
                    }
                }

                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var tapCount by remember { mutableStateOf(0) }
                    var lastTapTime by remember { mutableStateOf(0L) }
                    Column(
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    val now = System.currentTimeMillis()
                                    if (now - lastTapTime < 500) {
                                        tapCount++
                                        if (tapCount >= 2) {
                                            showSystemMonitor = !showSystemMonitor
                                            tapCount = 0
                                            lastTapTime = 0L
                                        } else {
                                            lastTapTime = now
                                        }
                                    } else {
                                        tapCount = 1
                                        lastTapTime = now
                                    }
                                }
                            )
                        }
                    ) {
                        Text(
                            text = "HAVEN",
                            style = AppTypography.displayLarge,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "by ashu mehta",
                            style = AppTypography.labelSmall,
                            color = NtSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currentTime,
                            style = AppTypography.displayLarge.copy(fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = currentDate,
                            style = AppTypography.labelSmall,
                            color = NeutralGray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val isMonochrome by viewModel.isMonochrome.collectAsStateWithLifecycle()
                    IconButton(onClick = { viewModel.setMonochrome(!isMonochrome) }) {
                        Icon(
                            if (isMonochrome) Icons.Default.Contrast else Icons.Default.Palette,
                            contentDescription = "Toggle Theme",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(if (isEditMode) Icons.Default.Done else Icons.Default.Edit, contentDescription = "Edit Layout", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp))
                    }
                }
            }

            // QUICK CONTROLS SECTION
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quick Controls", style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text("Swipe for more", style = AppTypography.labelSmall, color = NtSecondary)
                    }
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            QuickControlTile(
                                title = "Wi-Fi",
                                subtitle = "Network Options",
                                icon = Icons.Default.Wifi,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
                                    } catch(e: Exception) {
                                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                    }
                                }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Bluetooth",
                                subtitle = "Devices",
                                icon = Icons.Default.Bluetooth,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Do Not Disturb",
                                subtitle = if (hasDndPermission) "Active" else "Off",
                                icon = Icons.Default.DoNotDisturbOn,
                                containerColor = NtGreen,
                                iconColor = NtGreenText,
                                subtitleColor = NtGreenText,
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    if (!hasDndPermission) onRequestDndPermission()
                                    else {
                                        val intent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
                                        try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                    }
                                }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Flashlight",
                                subtitle = if (isFlashlightOn) "On" else "Off",
                                icon = Icons.Default.FlashlightOn,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    try {
                                        val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                                        if (cameraId != null) {
                                            cameraManager.setTorchMode(cameraId, !isFlashlightOn)
                                            isFlashlightOn = !isFlashlightOn
                                        }
                                    } catch (e: Exception) { }
                                }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Display",
                                subtitle = "Settings",
                                icon = Icons.Default.DisplaySettings,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Sound",
                                subtitle = "Volume levels",
                                icon = Icons.Default.VolumeUp,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Location",
                                subtitle = "GPS",
                                icon = Icons.Default.LocationOn,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Battery",
                                subtitle = "Usage",
                                icon = Icons.Default.BatteryFull,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Data Saver",
                                subtitle = "Network",
                                icon = Icons.Default.DataUsage,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Security",
                                subtitle = "Lock screen",
                                icon = Icons.Default.Security,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Apps",
                                subtitle = "Manage",
                                icon = Icons.Default.Apps,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Storage",
                                subtitle = "Space",
                                icon = Icons.Default.Storage,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Language",
                                subtitle = "Locale",
                                icon = Icons.Default.Translate,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Date & Time",
                                subtitle = "Clock",
                                icon = Icons.Default.Schedule,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DATE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "NFC",
                                subtitle = "Connections",
                                icon = Icons.Default.Nfc,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Print",
                                subtitle = "Services",
                                icon = Icons.Default.Print,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_PRINT_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } }
                            )
                        }
                    }
                }
            }

            // PERMISSION BANNER
            if (!hasWriteSettingsPermission) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    BentoCard(
                        title = "PERMISSION",
                        icon = Icons.Default.Warning,
                        tint = NothingRed,
                        onClick = onRequestPermission
                    ) {
                        Text("WRITE_SETTINGS required for Screen Timeout control", style = AppTypography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("GRANT ACCESS")
                        }
                    }
                }
            }

            items(
                count = availableOrder.size,
                key = { index -> availableOrder[index] },
                span = { index ->
                    val id = availableOrder[index]
                    if (id in listOf("CAFFEINE", "THEATER", "FOCUS", "GLYPH")) StaggeredGridItemSpan.FullLine
                    else StaggeredGridItemSpan.SingleLane
                }
            ) { index ->
                val id = availableOrder[index]
                Box {
                    when (id) {
                        "TIMEOUT" -> {
                            BentoCard(title = "SCREEN TIMEOUT", icon = Icons.Default.Timer) {
                                val pm = com.example.utils.QSPreferenceManager(context)
                                val label = remember(systemTimeout) { pm.formatTimeoutLabel(systemTimeout) }
                                Text(label, style = AppTypography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.cycleScreenTimeout() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("CYCLE →", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "CAFFEINE" -> {
                            BentoCard(title = "CAFFEINE", icon = Icons.Default.Coffee) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (isCaffeineActive) "SCREEN ON" else "STANDBY", style = AppTypography.displayLarge)
                                        Text(if (isCaffeineActive) "Wake lock active" else "Tap to activate", style = AppTypography.bodyMedium, color = NeutralGray)
                                    }
                                    Switch(
                                        checked = isCaffeineActive,
                                        onCheckedChange = { viewModel.toggleCaffeine(it) }
                                    )
                                }
                                AnimatedVisibility(visible = isCaffeineActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NothingRed)
                                    )
                                }
                            }
                        }
                        "DNS" -> {
                            BentoCard(title = "PRIVATE DNS", icon = Icons.Default.Dns) {
                                Text(if (isDnsActive) "ACTIVE" else "OFF", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                GlyphSwitch(
                                    checked = isDnsActive,
                                    onCheckedChange = { viewModel.togglePrivateDns(it) },
                                    label = "Private DNS"
                                )
                            }
                        }
                        "THEATER" -> {
                            BentoCard(title = "THEATER MODE", icon = Icons.Default.Theaters) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (isTheaterActive) "ACTIVE" else "INACTIVE", style = AppTypography.displayLarge)
                                        if (isTheaterActive) Text("DND · DIM · MUTE", style = AppTypography.labelSmall, color = NothingRed)
                                    }
                                    Switch(
                                        checked = isTheaterActive,
                                        onCheckedChange = { viewModel.toggleTheaterMode(it) },
                                        modifier = Modifier.then(
                                            if (isTheaterActive) Modifier.shadow(
                                                elevation = 12.dp,
                                                spotColor = MaterialTheme.colorScheme.onSurface,
                                                ambientColor = MaterialTheme.colorScheme.onSurface,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ) else Modifier
                                        )
                                    )
                                }
                            }
                        }
                        "CLIPBOARD" -> {
                            BentoCard(title = "CLIPBOARD", icon = Icons.Default.ContentPaste) {
                                Text("PURGE", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.purgeClipboard() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear Clipboard", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CLEAR NOW", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "FOCUS" -> {
                            BentoCard(title = "DEEP FOCUS", icon = Icons.Default.Lock) {
                                var selectedDuration by remember { mutableIntStateOf(25) }
                                var showAppSelector by remember { mutableStateOf(false) }
                                var allowedApps by remember { mutableStateOf(setOf<String>()) }
                                
                                val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                                val usageMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                                } else {
                                    appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                                }
                                val hasUsageStats = usageMode == android.app.AppOpsManager.MODE_ALLOWED
                                val hasOverlay = android.provider.Settings.canDrawOverlays(context)
                                
                                if (!hasUsageStats || !hasOverlay) {
                                    if (!hasUsageStats) {
                                        Button(onClick = {
                                            context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NothingRed)) { Text("GRANT USAGE ACCESS") }
                                    }
                                    if (!hasOverlay) {
                                        Spacer(Modifier.height(8.dp))
                                        Button(onClick = {
                                            context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NothingRed)) { Text("GRANT OVERLAY PERMISSION") }
                                    }
                                } else {
                                    val isDark = MaterialTheme.colorScheme.background == PitchBlack

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedDuration.toString(), style = AppTypography.displayLarge)
                                        Text(" MIN", style = AppTypography.labelSmall, color = NeutralGray)
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { if (selectedDuration > 5) selectedDuration -= 5 }) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                        }
                                        IconButton(onClick = { if (selectedDuration < 120) selectedDuration += 5 }) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(onClick = { showAppSelector = true }, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.Apps, contentDescription = "Select Apps", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${allowedApps.size} APPS ALLOWED", style = AppTypography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.startFocusSandbox(selectedDuration, allowedApps) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) PureWhite else PitchBlack)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Focus", tint = if (isDark) PitchBlack else PureWhite)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("START FOCUS", style = AppTypography.labelSmall, color = if (isDark) PitchBlack else PureWhite)
                                    }

                                    if (showAppSelector) {
                                        AppSelectorDialog(
                                            preselectedApps = allowedApps,
                                            onDismiss = { showAppSelector = false },
                                            onConfirm = { apps ->
                                                allowedApps = apps
                                                showAppSelector = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        "SHORTCUT" -> {
                            BentoCard(title = "SHORTCUT", icon = Icons.Default.OpenInNew) {
                                Text(customShortcutLabel.uppercase(), style = AppTypography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    customShortcutTarget,
                                    style = AppTypography.labelSmall,
                                    color = NeutralGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { showSettingsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("CONFIGURE", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "APP_AUDIO" -> {
                            BentoCard(title = "APP AUDIO", icon = Icons.Default.VolumeOff) {
                                Text(if (isAudioIsolated) "ISOLATED" else "NORMAL", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                GlyphSwitch(
                                    checked = isAudioIsolated,
                                    onCheckedChange = { viewModel.toggleAudioIsolation(it) },
                                    label = "Isolate"
                                )
                            }
                        }
                        "GLYPH" -> {
                            BentoCard(title = "GLYPH PROFILE", icon = Icons.Default.Brightness6) {
                                val profiles = listOf("Essential Only", "Full Glyph", "Silent")
                                profiles.forEach { profile ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setGlyphBrightnessProfile(profile) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(profile.uppercase(), style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
                                        if (glyphProfile == profile) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = NothingRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    if (profile != profiles.last()) {
                                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                        "OPERATOR" -> {
                            BentoCard(
                                title = "NETWORK",
                                icon = Icons.Default.NetworkCell,
                                onClick = {
                                    val intent = Intent("android.settings.NETWORK_OPERATOR_SETTINGS").apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // handle error
                                    }
                                }
                            ) {
                                Text("OPERATOR", style = AppTypography.displayLarge)
                                Text("4G · 5G · LTE", style = AppTypography.labelSmall, color = NeutralGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Chevron Right", tint = NeutralGray, modifier = Modifier.size(16.dp))
                                    Text("Opens carrier settings", style = AppTypography.labelSmall, color = NeutralGray)
                                }
                            }
                        }
                    }
                    if (isEditMode) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                                .clickable { /* Consume clicks */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val newOrder = availableOrder.toMutableList()
                                            java.util.Collections.swap(newOrder, index, index - 1)
                                            viewModel.updateTileOrder(newOrder)
                                        }
                                    },
                                    enabled = index > 0,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Move Left/Up")
                                }
                                IconButton(
                                    onClick = {
                                        if (index < availableOrder.size - 1) {
                                            val newOrder = availableOrder.toMutableList()
                                            java.util.Collections.swap(newOrder, index, index + 1)
                                            viewModel.updateTileOrder(newOrder)
                                        }
                                    },
                                    enabled = index < availableOrder.size - 1,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Move Right/Down")
                                }
                            }
                        }
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                var brightnessState by remember { mutableFloatStateOf(128f) }
                LaunchedEffect(Unit) {
                    try {
                        val current = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                        brightnessState = current.toFloat()
                    } catch (e: Exception) {}
                }
                
                BentoCard(title = "BRIGHTNESS", icon = Icons.Default.BrightnessMedium) {
                    Slider(
                        value = brightnessState,
                        onValueChange = { 
                            brightnessState = it
                            if (hasWriteSettingsPermission) {
                                SystemSettingsHelper.setScreenBrightness(context, it.toInt())
                            }
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 20) {
                            showNotifications = true
                        }
                    }
                }
        )
    }

    if (showNotifications) {
        NotificationsOverlay(onDismiss = { showNotifications = false })
    }

    if (showSystemMonitor) {
        SystemMonitorPanel(onDismiss = { showSystemMonitor = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onResetLayout = { viewModel.resetTileOrder() }
        )
    }
}
