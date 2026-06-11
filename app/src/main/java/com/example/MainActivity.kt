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
                    if (!hasSeenOnboarding) {
                        com.example.ui.OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
                    } else {
                        com.example.ui.HavenScaffold(
                            viewModel = viewModel,
                            initialOpenFocus = openFocus,
                            onRequestPermission = {
                                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
                            },
                            onRequestDndPermission = {
                                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                            }
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
