package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var sensorData by remember { mutableStateOf(mapOf<Int, String>()) }
    var availableSensors by remember { mutableStateOf<List<Sensor>>(emptyList()) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val type = event.sensor.type
                    val values = event.values.joinToString(", ") { String.format("%.2f", it) }
                    sensorData = sensorData.toMutableMap().apply { put(type, values) }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        availableSensors.forEach { sensor ->
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Dashboard", style = AppTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(availableSensors) { sensor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = "Sensor", tint = NothingRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sensor.name, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Type: ${sensor.stringType}\nVendor: ${sensor.vendor}",
                            style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "Live Values: ${sensorData[sensor.type] ?: "Waiting..."}",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
