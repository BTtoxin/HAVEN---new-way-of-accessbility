package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.example.ui.theme.AppTypography
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingTheme
import com.example.ui.theme.PureWhite
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.utils.FocusDataStore
import kotlinx.coroutines.delay

class FocusLockOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NothingTheme(darkTheme = true) {
                FocusLockScreen(onEmergencyExit = { 
                    val intent = android.content.Intent(this@FocusLockOverlayActivity, com.example.services.FocusSandboxService::class.java)
                    stopService(intent)
                    lifecycleScope.launch {
                        com.example.utils.FocusDataStore.setTimes(this@FocusLockOverlayActivity, 0, 0)
                        finish()
                    }
                })
            }
        }
    }
}

@Composable
fun FocusLockScreen(onEmergencyExit: () -> Unit) {
    val context = LocalContext.current
    var showExitConfirm by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }
    val endTime = FocusDataStore.getEndTime(context)
    var remaining by remember { mutableLongStateOf(endTime - System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining = endTime - System.currentTimeMillis()
        }
        onEmergencyExit() // Auto exit when finished
    }

    BackHandler { /* consume back — do nothing */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val pulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Icon(
                Icons.Default.Lock,
                contentDescription = "Lock Icon",
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulse),
                tint = PureWhite
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("FOCUS MODE", style = AppTypography.labelSmall, color = NeutralGray, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            val mins = (remaining / 1000 / 60).coerceAtLeast(0)
            val secs = (remaining / 1000 % 60).coerceAtLeast(0)
            Text(
                "%02d:%02d".format(mins, secs),
                style = AppTypography.displayLarge,
                color = PureWhite,
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Stay focused.", style = AppTypography.bodyMedium, color = NeutralGray)

            val allowedPackages = remember { FocusDataStore.getAllowedApps(context) }
            val whitelistedApps = remember {
                com.example.ui.loadInstalledApps(context).filter { it.packageName in allowedPackages }
            }

            if (whitelistedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("ALLOWED APPS", style = AppTypography.labelSmall, color = NeutralGray, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(whitelistedApps) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launchIntent != null) {
                                        try {
                                            context.startActivity(launchIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not launch app", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, androidx.compose.ui.graphics.Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.label,
                                style = AppTypography.labelSmall,
                                color = PureWhite,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            TextButton(
                onClick = {
                    tapCount++
                    if (tapCount >= 3) {
                        showExitConfirm = true
                    } else {
                        Toast.makeText(context, "Tap ${3 - tapCount} more times to exit", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("emergency exit", style = AppTypography.labelSmall, color = BorderDark)
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = {
                showExitConfirm = false
                tapCount = 0
            },
            title = { Text("EXIT FOCUS?") },
            text = { Text("This will end your Focus session early.") },
            confirmButton = {
                Button(
                    onClick = onEmergencyExit,
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                ) {
                    Text("EXIT")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    tapCount = 0
                }) {
                    Text("STAY")
                }
            }
        )
    }
}
