package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.AppTypography
import com.example.utils.DeepWorkManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepWorkScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val deepWorkManager = remember { DeepWorkManager(context) }
    var isDeepWorkActive by remember { mutableStateOf(false) }

    var showDndDialog by remember { mutableStateOf(false) }

    var timeRemaining by remember { mutableStateOf(60 * 60) } // 60 mins
    var isPaused by remember { mutableStateOf(false) }
    var selectedMusic by remember { mutableStateOf("None") }

    LaunchedEffect(isDeepWorkActive, isPaused) {
        if (isDeepWorkActive && !isPaused) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            if (timeRemaining == 0) {
                deepWorkManager.deactivateDeepWork()
                isDeepWorkActive = false
            }
        }
    }

    if (showDndDialog) {
        AlertDialog(
            onDismissRequest = { showDndDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Deep Work requires Do Not Disturb access to silence notifications.") },
            confirmButton = {
                TextButton(onClick = {
                    showDndDialog = false
                    val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }) { Text("GRANT") }
            },
            dismissButton = {
                TextButton(onClick = { showDndDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (isDeepWorkActive) {
        val view = androidx.compose.ui.platform.LocalView.current
        DisposableEffect(Unit) {
            var window: android.view.Window? = null
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is android.app.Activity) {
                    window = context.window
                    break
                }
                context = context.baseContext
            }
            
            val layoutParams = window?.attributes
            val originalBrightness = layoutParams?.screenBrightness ?: -1f
            
            if (window != null) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                layoutParams?.screenBrightness = 0.05f
                window.attributes = layoutParams
            }
            
            onDispose {
                if (window != null && layoutParams != null) {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    layoutParams.screenBrightness = originalBrightness
                    window.attributes = layoutParams
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Black
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("DEEP WORK", style = AppTypography.titleLarge, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "\"Focus is a matter of deciding what things you're not going to do.\"",
                        style = AppTypography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Timer Display
                    val minutes = timeRemaining / 60
                    val seconds = timeRemaining % 60
                    Text(
                        String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
                    )
                    
                    Spacer(Modifier.height(32.dp))

                    // Pause/Play
                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = "Toggle", tint = androidx.compose.ui.graphics.Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPaused) "RESUME" else "PAUSE", color = androidx.compose.ui.graphics.Color.White)
                    }

                    Spacer(Modifier.height(32.dp))

                    // Ambient Music Selection
                    Text("Ambient Music", style = AppTypography.titleMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("None", "Lo-Fi", "Waves", "Calm").forEach { track ->
                            FilterChip(
                                selected = selectedMusic == track,
                                onClick = { selectedMusic = track },
                                label = { Text(track, color = androidx.compose.ui.graphics.Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sessions", style = AppTypography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                            Text("3", style = AppTypography.titleMedium, color = androidx.compose.ui.graphics.Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Focus Time", style = AppTypography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                            Text("132m", style = AppTypography.titleMedium, color = androidx.compose.ui.graphics.Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Streak", style = AppTypography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                            Text("5 Days", style = AppTypography.titleMedium, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    // Emergency Exit
                    Button(
                        onClick = {
                            deepWorkManager.deactivateDeepWork()
                            isDeepWorkActive = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Emergency Exit", tint = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f))
                        Spacer(Modifier.width(8.dp))
                        Text("EMERGENCY EXIT", color = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
            
            // Optional overlay to simulate even dimmer screen
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f)))
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Deep Work Setup", style = AppTypography.titleLarge) },
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
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Immersive focus environment. Activating this will silence notifications, enable DND, turn the screen grayscale, and start a tracked session.",
                    style = AppTypography.bodyLarge
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Session Preferences", style = AppTypography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Block Distracting Apps")
                            Switch(checked = true, onCheckedChange = {})
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Enable Grayscale")
                            Switch(checked = true, onCheckedChange = {})
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Auto-DND")
                            Switch(checked = true, onCheckedChange = {})
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        if (!nm.isNotificationPolicyAccessGranted) {
                            showDndDialog = true
                            return@Button
                        }
                        timeRemaining = 60 * 60 // Reset timer
                        selectedMusic = "None"
                        deepWorkManager.activateDeepWork()
                        isDeepWorkActive = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        "ENTER DEEP WORK",
                        style = AppTypography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

