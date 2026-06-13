package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DopamineDetoxScreen(onBack: () -> Unit) {
    var isDetoxActive by remember { mutableStateOf(false) }
    
    val appsToBlock = listOf(
        "Instagram" to true,
        "YouTube" to true,
        "TikTok" to true,
        "Snapchat" to false,
        "Games" to true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dopamine Detox Mode", style = AppTypography.titleLarge) },
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
                Text(
                    "Block high-dopamine apps like short-form video and endless scrolling feeds to retrain your attention span.",
                    style = AppTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detox Statistics", style = AppTypography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Current Streak: 3 Days", style = AppTypography.bodyMedium)
                        Text("Time Recovered: 4h 20m", style = AppTypography.bodyMedium)
                    }
                }
            }

            item {
                Text("Select Apps to Block", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }

            items(appsToBlock) { app ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)), modifier = Modifier.fillMaxWidth()) {
                   Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                       Text(app.first, style = AppTypography.bodyLarge)
                       Switch(checked = app.second, onCheckedChange = {})
                   } 
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { isDetoxActive = !isDetoxActive },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(isDetoxActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isDetoxActive) "END DETOX PROTOCOL" else "START DETOX PROTOCOL", style = AppTypography.labelLarge)
                }
            }
        }
    }
}
