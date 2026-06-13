package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.viewmodel.QSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAnalyticsScreen(viewModel: QSViewModel, onBack: () -> Unit) {
    val totalWeeklyFocus by viewModel.totalWeeklyFocus.collectAsState()
    val allSessions by viewModel.allStudySessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Analytics Center", style = AppTypography.titleLarge) },
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
                Text("Weekly Overview", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Total Focus Time", style = AppTypography.bodyMedium)
                        val hours = totalWeeklyFocus / (1000 * 60 * 60)
                        val mins = (totalWeeklyFocus / (1000 * 60)) % 60
                        Text("${hours}h ${mins}m", style = AppTypography.displayLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            item {
                Text("Recent Sessions", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            }

            items(allSessions.size) { index ->
                val session = allSessions[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(session.subject ?: "Deep Focus", style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            val date = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(session.startTime))
                            Text(date, style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val minDur = session.durationMillis / (1000 * 60)
                        Text("${minDur}m", style = AppTypography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
