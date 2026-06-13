package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentModeScreen(
    onBack: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToDeepWork: () -> Unit,
    onNavigateToDopamineDetox: () -> Unit,
    onNavigateToEmergencyMode: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Companion", style = AppTypography.titleLarge) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onNavigateToDeepWork, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("Deep Work")
                    }
                    Button(onClick = onNavigateToPlanner, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("Study Planner")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onNavigateToDopamineDetox, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("Dopamine Detox")
                    }
                    Button(onClick = onNavigateToEmergencyMode, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Emergency Exam")
                    }
                }
            }
            item {
                Text("Subject Tracker", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Anatomy: 2h", style = AppTypography.bodyMedium)
                        Text("Physiology: 1h", style = AppTypography.bodyMedium)
                        Text("Pathology: 3h", style = AppTypography.bodyMedium)
                    }
                }
            }

            item {
                Text("Exam Countdowns", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("UPSC Prelims", style = AppTypography.bodyLarge)
                        Text("245 days", style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
                 Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("NEET PG", style = AppTypography.bodyLarge)
                        Text("112 days", style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Text("Pomodoro Timer", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = {}) { Text("25/5") }
                    Button(onClick = {}) { Text("50/10") }
                    OutlinedButton(onClick = {}) { Text("Custom") }
                }
            }
        }
    }
}
