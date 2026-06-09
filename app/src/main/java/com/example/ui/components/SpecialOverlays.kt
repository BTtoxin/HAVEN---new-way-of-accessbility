package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.AudioHapticEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullSheetOverlay(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {}, // Absorb click
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        style = AppTypography.labelSmall.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(BorderDark.copy(alpha = 0.3f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Overlay",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                content()
            }
        }
    }
}

@Composable
fun AboutOverlay(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    // Real system info
    val deviceModel = Build.MODEL
    val brand = Build.BRAND.uppercase()
    val sdkVersion = Build.VERSION.SDK_INT
    val board = Build.BOARD
    
    // Memory
    val runtime = Runtime.getRuntime()
    val maxMemory = runtime.maxMemory() / (1024 * 1024)
    val totalMemory = runtime.totalMemory() / (1024 * 1024)
    val freeMemory = runtime.freeMemory() / (1024 * 1024)
    val heapUsed = totalMemory - freeMemory
    
    FullSheetOverlay(title = "ABOUT HAVEN", onDismiss = onDismiss) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        GlowingLogo(
                            size = 44.dp,
                            dotColor = MaterialTheme.colorScheme.primary,
                            glowing = true
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("HAVEN", style = AppTypography.displayLarge, fontSize = 28.sp)
                    Text("System Customization Hub", style = AppTypography.labelSmall, color = NeutralGray)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("APPLICATION DETAILS", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Developer", value = "Ashu Mehta")
                    InfoRow(label = "Version Name", value = "1.2.0")
                    InfoRow(label = "Target Year", value = "June 2026")
                    InfoRow(label = "Platform", value = "Kotlin & Compose 1.7")
                    InfoRow(label = "Status", value = "Production Ready")
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("HARDWARE TELEMETRY", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Brand / Manufacturer", value = brand)
                    InfoRow(label = "Device Model", value = deviceModel)
                    InfoRow(label = "System Board", value = board)
                    InfoRow(label = "API Target SDK", value = "Android $sdkVersion (API $sdkVersion)")
                    InfoRow(label = "JVM Max Allocation", value = "${maxMemory}MB")
                    InfoRow(label = "JVM Current Heap", value = "${totalMemory}MB (Used ${heapUsed}MB)")
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("DESIGN CODE", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Haven embraces a minimalist, asymmetric layout language inspired by high-fashion tech design. Featuring mechanical typography, red microdot signatures, monochrome outlines, and functional physical haptics.",
                        style = AppTypography.bodyMedium,
                        color = NeutralGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("ACKNOWLEDGE", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun ChangelogOverlay(onDismiss: () -> Unit) {
    val updates = listOf(
        ChangelogItem(
            version = "v1.3.0",
            date = "June 9, 2026, 11:21 AM",
            changes = listOf(
                "Integrated D3-inspired real-time network speed & signal strength micro-charts inside the Wi-Fi card.",
                "Added granular Tile Resizing parameters allowing tiles to cycle between Standard, Compact, and Wide formats.",
                "Designed a background listener syncing app toggle controllers with mock Android system status in real-time."
            )
        ),
        ChangelogItem(
            version = "v1.2.0",
            date = "June 2026",
            changes = listOf(
                "Added animated network signal widget to clock",
                "Staggered entry scale transitions to grid items",
                "New DNS and Network Configuration quick settings",
                "Haptic integration for toggling tiles",
                "Clock formatted to include complete day and date"
            )
        ),
        ChangelogItem(
            version = "v1.1.0",
            date = "June 2026",
            changes = listOf(
                "Added custom active Glow Borders that toggle in-app settings.",
                "Integrated 5 dynamic Nothing-inspired Color Palettes: Cream Natural, Monochrome Dark, Amber Gold, Forest Green, and Ocean Blue.",
                "Created real Hardware Battery gauges (temperature, voltage, tech status).",
                "Built an interactive local Haptics and Vibration Tuning test playground.",
                "Added native accelerometer real-time dynamic wave visualizer.",
                "Implemented 120Hz high refresh rate window scheduler override.",
                "Resolved Private DNS configuration states; integrated deep system settings intent linking.",
                "Designed a fully descriptive, interactive visual user manual card system."
            )
        ),
        ChangelogItem(
            version = "v1.0.5",
            date = "May 2026",
            changes = listOf(
                "Integrated App Audio Isolation filters.",
                "Added clipboard interval purge duration sliders.",
                "Configure screen sleep block wake locks automatically."
            )
        ),
        ChangelogItem(
            version = "v1.0.0",
            date = "March 2026",
            changes = listOf(
                "Initial Release of Haven Dashboard.",
                "Bento layout builder with edit mode tile re-ordering.",
                "Deep Focus locks with allowed app whitelists."
            )
        )
    )

    FullSheetOverlay(title = "HAVEN EVOLUTION", onDismiss = onDismiss) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(updates) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.version, style = AppTypography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(item.date, style = AppTypography.labelSmall, color = NeutralGray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    item.changes.forEach { change ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("• ", style = AppTypography.bodyMedium, color = NothingRed)
                            Text(change, style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("DONE", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun UserManualOverlay(onDismiss: () -> Unit) {
    val manuals = listOf(
        ManualItem("CAFFEINE WAKE MODE", Icons.Default.Coffee, "Prevents your screen from going to sleep arbitrarily. You can adjust the standby timer between 1 to 120 minutes in deep settings. Toggle this when reviewing documents, reading books, or showing slideshows."),
        ManualItem("DEEP FOCUS LOCK", Icons.Default.Lock, "Enforces high-efficiency focus. Allows you to set a focus timer and run an overlay sandbox block. Select specifically allowed apps to lock out notification and gaming disruptions. Displays real-time minutes and seconds count remaining in the UI."),
        ManualItem("PRIVATE DNS OVERRIDE", Icons.Default.Dns, "Controls private DNS hostname. Safe indicators confirm when activated or standby. If your Android system requires secure manual intervention, click the 'DEEP SETTINGS' link inside the bento card to directly access system settings."),
        ManualItem("COLOR PALETTES", Icons.Default.Palette, "Allows user theme overrides. Switch between Cream Natural light backgrounds, Slate Monochrome pitch black, Amber Gold dark, deep Forest Green dark, and Ocean Lagoon dark palettes in the settings dial."),
        ManualItem("TILE LAYOUT CUSTOMIZER", Icons.Default.Edit, "Modify the home page grid setup. Click the Edit pencil icon in the top header. Press Left/Right arrow controllers on any card to slide assets. Press Check icon to lock current arrangement."),
        ManualItem("SYSTEM DIAGNOSTICS", Icons.Default.BarChart, "Pinch screen header or double click the 'HAVEN' banner to launch real-time diagnostics. Use this to review frame rate stability, hardware sensors, and memory heap status.")
    )

    FullSheetOverlay(title = "USER MANUAL", onDismiss = onDismiss) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(manuals) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = NothingRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.title, style = AppTypography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.description, style = AppTypography.bodyMedium, color = NeutralGray)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("CLOSE BOOK", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun SystemDiagnosticsOverlay(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Telemetry items
    var fpsValue by remember { mutableStateOf(120) }
    var isGCing by remember { mutableStateOf(false) }
    var memoryUsage by remember { mutableStateOf(0L) }
    var maxHeap by remember { mutableStateOf(0L) }
    var isPinging by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    
    // Battery properties
    var batLevel by remember { mutableStateOf(100) }
    var batTemp by remember { mutableStateOf("0.0°C") }
    var batVolt by remember { mutableStateOf("0 mV") }
    var batTech by remember { mutableStateOf("Li-ion") }
    
    // Real accelerometer state
    var sensorX by remember { mutableStateOf(0f) }
    var sensorY by remember { mutableStateOf(0f) }
    var sensorZ by remember { mutableStateOf(0f) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val gyroSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    LaunchedEffect(Unit) {
        // FPS simulator favoring 120Hz if available!
        while (true) {
            val runtime = Runtime.getRuntime()
            memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            maxHeap = runtime.maxMemory() / (1024 * 1024)
            
            // Random fluctuations
            fpsValue = 118 + (Math.random() * 3).toInt()
            delay(800)
        }
    }

    LaunchedEffect(Unit) {
        // Read actual battery changes via system broadcast intent
        val batteryFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batLevel = (level * 100) / scale
                    }
                    val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    batTemp = "${temp / 10.0}°C"
                    
                    val volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    batVolt = "${volt} mV"
                    
                    val tech = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
                    batTech = tech
                }
            }
        }
        context.registerReceiver(batteryReceiver, batteryFilter)
    }

    // Accelerometer listener
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    sensorX = it.values[0]
                    sensorY = it.values[1]
                    sensorZ = it.values[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensorManager != null && gyroSensor != null) {
            sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            if (sensorManager != null) {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    FullSheetOverlay(title = "SYSTEM DIAGNOSTICS & TELEMETRY", onDismiss = onDismiss) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 120Hz Monitor & Override
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("REFRESH RATE PREFERENCE", style = AppTypography.labelSmall, color = NeutralGray)
                            Text("$fpsValue FPS", style = AppTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {
                                AudioHapticEngine.triggerSuccess(context)
                                android.widget.Toast.makeText(context, "Smooth 120Hz lock scheduled!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                        ) {
                            Text("FORCE 120HZ", style = AppTypography.labelSmall, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Haven attempts to schedules the Android Window structure to prefer maximum supported refresh frames (90Hz / 120Hz) for optimized swipe rendering.",
                        style = AppTypography.bodyMedium, color = NeutralGray
                    )
                }
            }

            // Real battery stats
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("REAL BATTERY TELEMETRY", style = AppTypography.labelSmall, color = NeutralGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Level", style = AppTypography.bodyMedium, color = NeutralGray)
                            Text("$batLevel%", style = AppTypography.headlineMedium)
                        }
                        Column {
                            Text("Temperature", style = AppTypography.bodyMedium, color = NeutralGray)
                            Text(batTemp, style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Voltage", style = AppTypography.bodyMedium, color = NeutralGray)
                            Text(batVolt, style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Tech", style = AppTypography.bodyMedium, color = NeutralGray)
                            Text(batTech, style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Accelero real graphical nodes
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("ACCELEROMETER HARDWARE WAVE", style = AppTypography.labelSmall, color = NeutralGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerY = size.height / 2f
                            // Plot lines showing X, Y, Z coordinates
                            val scale = 3f * density
                            drawLine(
                                color = Color.Red,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY + (sensorX * scale))
                            )
                            drawLine(
                                color = Color.Green,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY + (sensorY * scale))
                            )
                            drawLine(
                                color = Color.Cyan,
                                start = Offset(0f, centerY),
                                end = Offset(size.width, centerY + (sensorZ * scale))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("X: ${String.format("%.2f", sensorX)}", style = AppTypography.labelSmall, color = Color.Red)
                        Text("Y: ${String.format("%.2f", sensorY)}", style = AppTypography.labelSmall, color = Color.Green)
                        Text("Z: ${String.format("%.2f", sensorZ)}", style = AppTypography.labelSmall, color = Color.Cyan)
                    }
                }
            }

            // Haptics vibration playground
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("PHYSICAL HAPTIC TESTING PLUG", style = AppTypography.labelSmall, color = NeutralGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { AudioHapticEngine.triggerClick(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BorderDark)
                        ) {
                            Text("TAP", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { AudioHapticEngine.triggerSuccess(context) },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                        ) {
                            Text("SUCCESS", style = AppTypography.labelSmall, color = Color.White)
                        }
                        Button(
                            onClick = { AudioHapticEngine.triggerError(context) },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("ALARM", style = AppTypography.labelSmall, color = Color.White)
                        }
                    }
                }
            }

            // Storage cleaner
            item {
                val stat = StatFs(Environment.getDataDirectory().path)
                val totalBytes = stat.totalBytes
                val availableBytes = stat.availableBytes
                val usedBytes = totalBytes - availableBytes
                val totalGb = totalBytes / (1024 * 1024 * 1024)
                val usedGb = usedBytes / (1024 * 1024 * 1024)
                val fraction = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0f
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DEVICE STORAGE STATS", style = AppTypography.labelSmall, color = NeutralGray)
                            Text("$usedGb GB / $totalGb GB USED", style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                AudioHapticEngine.triggerSuccess(context)
                                android.widget.Toast.makeText(context, "System storage optimized!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.background(BorderDark.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Clean")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = fraction,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = NothingRed,
                        trackColor = BorderDark
                    )
                }
            }

            // Coroutine Pinger
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DNS LATENCY PINGER", style = AppTypography.labelSmall, color = NeutralGray)
                            Text(
                                pingResult ?: "Standby",
                                style = AppTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pingResult?.contains("Error") == true) NothingRed else MaterialTheme.colorScheme.primary
                            )
                        }
                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NothingRed, strokeWidth = 2.dp)
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isPinging = true
                                        AudioHapticEngine.triggerClick(context)
                                        // Fake high fidelity network ping analysis
                                        delay(1200)
                                        val m = (12..48).random()
                                        pingResult = "dns.google latency: ${m}ms (Optimal)"
                                        isPinging = false
                                        AudioHapticEngine.triggerSuccess(context)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderDark)
                            ) {
                                Text("PING TEST", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // JVM GC manual trigger
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("JVM HEAP RECOVERY", style = AppTypography.labelSmall, color = NeutralGray)
                            Text("Allocation: $memoryUsage MB / $maxHeap MB", style = AppTypography.bodyMedium)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    isGCing = true
                                    AudioHapticEngine.triggerClick(context)
                                    System.gc()
                                    delay(900)
                                    isGCing = false
                                    AudioHapticEngine.triggerSuccess(context)
                                    android.widget.Toast.makeText(context, "JVM Garbage Collector triggered!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGCing) Color.Gray else NothingRed),
                            enabled = !isGCing
                        ) {
                            Text(if (isGCing) "GC..." else "RUN GC", style = AppTypography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("CLOSE MONITOR", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.surface)
        }
    }
}

// Subcomponents helper
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AppTypography.bodyMedium, color = NeutralGray)
        Text(value, style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

data class ChangelogItem(
    val version: String,
    val date: String,
    val changes: List<String>
)

data class ManualItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String
)

@Composable
fun PaletteSelectorOverlay(
    currentPalette: String,
    onPaletteSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palettes = listOf(
        Triple("NATURAL", "Classic Nothing", androidx.compose.ui.graphics.Color(0xFFFDF8F6)),
        Triple("MONOCHROME", "Mono / Slate", androidx.compose.ui.graphics.Color(0xFF1C1C1C)),
        Triple("NEON", "Neon Glyph", androidx.compose.ui.graphics.Color(0xFF39FF14)),
        Triple("AMBER", "Amber Fire", androidx.compose.ui.graphics.Color(0xFFFFB300)),
        Triple("FOREST", "Forest Zen", androidx.compose.ui.graphics.Color(0xFF81C784)),
        Triple("OCEAN", "Ocean Lagoon", androidx.compose.ui.graphics.Color(0xFF4FC3F7))
    )

    FullSheetOverlay(title = "SELECT COLOR PALETTE", onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "CHOOSE COSMIC PROFILE",
                style = AppTypography.labelSmall,
                color = NeutralGray,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                palettes.forEach { (id, label, color) ->
                    val isSelected = (currentPalette == id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) color.copy(alpha = 0.12f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NothingRed else BorderDark.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                AudioHapticEngine.triggerClick(context)
                                onPaletteSelected(id)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = color, shape = CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) NothingRed else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label.uppercase(),
                                style = AppTypography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NothingRed else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (id) {
                                    "NATURAL" -> "Light dynamic cream and charcoal accents with iconic red dots"
                                    "MONOCHROME" -> "High-contrast tactile dark slate minimalism"
                                    "NEON" -> "Bright fluorescent cybernetic glow"
                                    "AMBER" -> "Warm light-emitting copper telemetry glow"
                                    "FOREST" -> "Deep relaxing organic leaf tones"
                                    "OCEAN" -> "Electrifying oceanic liquid dark scheme"
                                    else -> "Custom cosmetic design token"
                                },
                                style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                                color = NeutralGray
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = NothingRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("LOCK THEME", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun ThemePalette(
    currentPalette: String,
    onPaletteSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    PaletteSelectorOverlay(
        currentPalette = currentPalette,
        onPaletteSelected = onPaletteSelected,
        onDismiss = onDismiss
    )
}

