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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Force Choreographer/Window display mode to prefer 120Hz peak performance
        try {
            val params = window.attributes
            val minField = params::class.java.getField("preferredMinDisplayRefreshRate")
            val maxField = params::class.java.getField("preferredMaxDisplayRefreshRate")
            minField.set(params, 120f)
            maxField.set(params, 120f)
            window.attributes = params
        } catch (e: Exception) {
            // Safe fallback for devices/system builds that do not support it
        }

        val openFocus = intent.getBooleanExtra("openFocus", false)

        setContent {
            val selectedPalette by viewModel.selectedPalette.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }
            NothingTheme(darkTheme = isDark, palette = selectedPalette) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("dashboard") }

                    if (currentScreen == "dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            initialOpenFocus = openFocus,
                            onNavigateToSettings = { currentScreen = "settings" },
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
                    } else {
                        com.example.ui.SettingsScreen(
                            onBack = { currentScreen = "dashboard" },
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
    }
}
