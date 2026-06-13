package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.AppTypography
import com.example.utils.DeepWorkManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepWorkScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val deepWorkManager = remember { DeepWorkManager(context) }
    var isDeepWorkActive by remember { mutableStateOf(false) }

    var showDndDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep Work", style = AppTypography.titleLarge) },
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
                    if (!isDeepWorkActive && !nm.isNotificationPolicyAccessGranted) {
                        showDndDialog = true
                        return@Button
                    }
                    if (isDeepWorkActive) {
                        deepWorkManager.deactivateDeepWork()
                    } else {
                        deepWorkManager.activateDeepWork()
                    }
                    isDeepWorkActive = !isDeepWorkActive
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isDeepWorkActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (isDeepWorkActive) "END DEEP WORK" else "ENTER DEEP WORK",
                    style = AppTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
