package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceHealthScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var batteryLevel by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(-1) }
    var batteryTemperature by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    var isCharging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var voltage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: android.content.Intent? = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level != -1 && scale != -1) {
            batteryLevel = (level * 100) / scale
        }
        val temp = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        batteryTemperature = temp / 10f
        
        val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os.BatteryManager.BATTERY_STATUS_FULL
        
        voltage = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    }

    val lifespanScore = androidx.compose.runtime.remember(batteryLevel, batteryTemperature) {
        var score = 100
        if (batteryTemperature > 40f) score -= 20
        if (isCharging && batteryLevel > 90) score -= 5
        score.coerceIn(0, 100)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Health Center", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Battery Guardian & Charging Assistant", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Lifespan Score: $lifespanScore / 100", style = AppTypography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val chargingText = if (isCharging) "Charging at ${voltage}mV" else "Discharging"
                        Text("Level: $batteryLevel%", style = AppTypography.bodyMedium)
                        Text("Status: $chargingText", style = AppTypography.bodyMedium)
                        Text("Temperature: ${batteryTemperature}°C", style = AppTypography.bodyMedium)
                        
                        if (isCharging && batteryLevel < 100) {
                            Spacer(Modifier.height(4.dp))
                            Text("Estimated Full: ~${(100 - batteryLevel) * 2} minutes", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item {
                Text("System Resources", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Memory (RAM)", style = AppTypography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Analyzing services...", style = AppTypography.bodySmall)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Storage Analytics", style = AppTypography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("60% Used", style = AppTypography.bodySmall)
                    }
                }
            }
        }
    }
}
