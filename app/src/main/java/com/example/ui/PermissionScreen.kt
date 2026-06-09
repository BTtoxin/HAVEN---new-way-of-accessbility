package com.example.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed
import com.example.utils.SystemSettingsHelper

data class PermissionItem(
    val title: String,
    val description: String,
    val checkStatus: (Context) -> Boolean,
    val requestAction: (Context) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // We will trigger a recompose on resume
    var refreshKey by remember { mutableIntStateOf(0) }
    
    DisposableEffect(Unit) {
        // Simple way to refresh when returning to this screen (if we were using a lifecycle observer)
        onDispose { }
    }

    val permissions = listOf(
        PermissionItem(
            title = "Write Settings",
            description = "Required to toggle system features like Brightness and Timeout.",
            checkStatus = { ctx -> SystemSettingsHelper.hasWriteSettingsPermission(ctx) },
            requestAction = { ctx ->
                ctx.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${ctx.packageName}")))
            }
        ),
        PermissionItem(
            title = "Do Not Disturb (DND)",
            description = "Required for Focus Mode and Theater Mode.",
            checkStatus = { ctx -> ctx.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted },
            requestAction = { ctx ->
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        ),
        PermissionItem(
            title = "Battery Optimization",
            description = "Allow background execution for Sandbox timers.",
            checkStatus = { ctx -> 
                val pm = ctx.getSystemService(android.os.PowerManager::class.java)
                pm.isIgnoringBatteryOptimizations(ctx.packageName)
            },
            requestAction = { ctx ->
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permission Centre", style = AppTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Manage system permissions required for Quick Settings tiles to function properly.",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(permissions) { perm ->
                val isGranted = remember(refreshKey) { perm.checkStatus(context) }
                
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { perm.requestAction(context) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(perm.title, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(perm.description, style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = if (isGranted) "Granted" else "Not Granted",
                            tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { refreshKey++ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text("Refresh Status", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
