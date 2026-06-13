package com.example.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryGuardianScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf(-1) }
    var batteryTemperature by remember { mutableStateOf(0f) }
    var isCharging by remember { mutableStateOf(false) }
    var voltage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level != -1 && scale != -1) {
            batteryLevel = (level * 100) / scale
        }
        val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        batteryTemperature = temp / 10f
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        
        voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    }

    val lifespanScore = remember(batteryLevel, batteryTemperature) {
        var score = 100
        if (batteryTemperature > 40f) score -= 20
        if (isCharging && batteryLevel > 90) score -= 5
        score.coerceIn(0, 100)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Guardian", style = AppTypography.titleLarge) },
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
                Text("Device Lifespan Score", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().padding(top=8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("$lifespanScore / 100", style = AppTypography.displayMedium, color = if (lifespanScore > 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        Text(if (lifespanScore > 80) "Your battery health is excellent." else "Battery is strained.", style = AppTypography.bodyLarge)
                    }
                }
            }

            item {
                Text("Charging Assistant", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top=8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().padding(top=8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val chargingText = if (isCharging) "Charging at ${voltage}mV" else "Discharging"
                        Text("Current Level: $batteryLevel%", style = AppTypography.bodyMedium)
                        Text("Status: $chargingText", style = AppTypography.bodyMedium)
                        Text("Temperature: ${batteryTemperature}°C", style = AppTypography.bodyMedium)
                        
                        if (isCharging && batteryLevel < 100) {
                            Text("Estimated Full: ~${(100 - batteryLevel) * 2} minutes", style = AppTypography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
