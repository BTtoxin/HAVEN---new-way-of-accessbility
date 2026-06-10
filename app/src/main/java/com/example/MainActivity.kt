package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.example.ui.DashboardScreen
import com.example.ui.theme.NothingTheme
import com.example.viewmodel.QSViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QSViewModel by viewModels()

    private var sensorManager: android.hardware.SensorManager? = null
    private var accelSensor: android.hardware.Sensor? = null
    private var lastUpdate: Long = 0
    private var last_x = 0f
    private var last_y = 0f
    private var last_z = 0f
    private val SHAKE_THRESHOLD = 800

    private val shakeListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000
                if (speed > SHAKE_THRESHOLD) {
                    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
                    android.widget.Toast.makeText(this@MainActivity, "Clipboard Purged (Shake)", android.widget.Toast.LENGTH_SHORT).show()
                }
                last_x = x
                last_y = y
                last_z = z
            }
        }
        override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
    }

    private fun setupShakeToPurgeClipboard() {
        sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        accelSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupShakeToPurgeClipboard()

        // Force Choreographer/Window display mode to prefer 120Hz peak performance
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                val display = window.context.display
                val modes = display?.supportedModes
                // Find a mode with high refresh rate (e.g. 120Hz or close to it)
                val highMode = modes?.find { it.refreshRate >= 119.0f }
                if (highMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = highMode.modeId
                    window.attributes = params
                }
            } catch (e: Throwable) {
                // Safe fallback for devices/system builds that do not support it
            }
        }

        val openFocus = intent.getBooleanExtra("openFocus", false)
        var initialScreen = "dashboard"

        if (intent.action == android.service.quicksettings.TileService.ACTION_QS_TILE_PREFERENCES) {
            val component = intent.getParcelableExtra<android.content.ComponentName>(android.content.Intent.EXTRA_COMPONENT_NAME)
            if (component != null) {
                // Determine screen based on component, for now just open app
                initialScreen = "dashboard"
            }
        }

        setContent {
            val selectedPalette by viewModel.selectedPalette.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }
            NothingTheme(darkTheme = isDark, palette = selectedPalette) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialScreen) }

                    if (!hasSeenOnboarding) {
                        com.example.ui.OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
                    } else if (currentScreen == "dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            initialOpenFocus = openFocus,
                            onNavigateToSettings = { currentScreen = "settings" },
                            onNavigateToAutomation = { currentScreen = "automation" },
                            onNavigateToClipboard = { currentScreen = "clipboard" },
                            onNavigateToSensors = { currentScreen = "sensors" },
                            onNavigateToFocusHistory = { currentScreen = "focus_history" },
                            onRequestPermission = {
                                startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                        Uri.parse("package:$packageName")
                                    )
                                )
                            },
                            onRequestDndPermission = {
                                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                            }
                        )
                    } else if (currentScreen == "permissions") {
                        com.example.ui.PermissionScreen(
                            onBack = { currentScreen = "dashboard" }
                        )
                    } else if (currentScreen == "automation") {
                        com.example.ui.AutomationScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    } else if (currentScreen == "clipboard") {
                        com.example.ui.ClipboardScreen(
                            onBack = { currentScreen = "dashboard" }
                        )
                    } else if (currentScreen == "sensors") {
                        com.example.ui.SensorScreen(
                            onBack = { currentScreen = "dashboard" }
                        )
                    } else if (currentScreen == "focus_history") {
                        com.example.ui.FocusHistoryScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "dashboard" }
                        )
                    } else {
                        com.example.ui.SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "dashboard" },
                            onNavigateToPermissions = { currentScreen = "permissions" },
                            onResetLayout = { viewModel.resetTileOrder() },
                            onConfirm = { viewModel.checkAllStates() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAllStates()
        accelSensor?.let { sensorManager?.registerListener(shakeListener, it, android.hardware.SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeListener)
    }
}
