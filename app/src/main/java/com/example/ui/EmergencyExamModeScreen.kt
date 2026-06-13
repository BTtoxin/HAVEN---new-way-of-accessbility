package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import com.example.utils.DeepWorkManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyExamModeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val deepWorkManager = remember { DeepWorkManager(context) }
    var duration by remember { mutableStateOf(1f) } // hours
    var isActivated by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Exam Mode", style = AppTypography.titleLarge) },
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
                "Instantly activate Focus Lock, Deep Work, DND, and App Blocking for crucial crunch sessions.",
                style = AppTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            Text("Duration: ${duration.toInt()} Hours", style = AppTypography.titleMedium)
            Slider(
                value = duration,
                onValueChange = { duration = it },
                valueRange = 1f..12f,
                steps = 11
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (isActivated) {
                        deepWorkManager.deactivateDeepWork()
                    } else {
                        deepWorkManager.activateDeepWork()
                    }
                    isActivated = !isActivated
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isActivated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isActivated) "CANCEL EMERGENCY MODE" else "ACTIVATE EMERGENCY MODE", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
