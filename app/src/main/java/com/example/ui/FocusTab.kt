package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FocusSessionEntity
import com.example.ui.components.HavenCard
import com.example.ui.components.HavenSectionHeader
import com.example.ui.components.HavenPillButton
import com.example.ui.theme.AppTypography
import com.example.ui.theme.HavenCyan
import com.example.ui.theme.HavenGreen
import com.example.viewmodel.QSViewModel

@Composable
fun FocusTab(
    viewModel: QSViewModel,
    onNavigateToFocusHistory: () -> Unit
) {
    val focusSessionHistory by viewModel.focusSessionHistory.collectAsStateWithLifecycle()
    val isSandboxActive by viewModel.isFocusSandboxActive.collectAsStateWithLifecycle()
    val focusTimeLimitStr by viewModel.focusTimeLimit.collectAsStateWithLifecycle()

    var selectedDuration by remember { mutableIntStateOf(25) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text("Focus", style = AppTypography.displayMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
                Text("Stay deep in work", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (isSandboxActive) {
            item {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = HavenCyan)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SESSION ACTIVE", style = AppTypography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black), color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                focusTimeLimitStr.ifEmpty { "00:00" }, 
                                style = AppTypography.displayLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Spacer(Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { 0.5f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.stopFocusSandbox() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = com.example.ui.theme.HavenRed),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Stop Session", style = AppTypography.labelLarge.copy(fontWeight = FontWeight.ExtraBold))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HavenCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (selectedDuration > 5) selectedDuration -= 5 }) {
                                    Text("-", style = AppTypography.titleLarge, color = HavenCyan)
                                }
                                Spacer(Modifier.width(24.dp))
                                Text("$selectedDuration MIN", style = AppTypography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
                                Spacer(Modifier.width(24.dp))
                                IconButton(onClick = { if (selectedDuration < 120) selectedDuration += 5 }) {
                                    Text("+", style = AppTypography.titleLarge, color = HavenCyan)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = { /* open AppSelectorDialog */ },
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Apps Allowed", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(Modifier.height(24.dp))
                            HavenPillButton(
                                text = "Start Focus",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.startFocusSandbox(selectedDuration, emptySet()) }
                            )
                        }
                    }
                }
            }
        }

        item {
            HavenSectionHeader("This Week")
            Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Pair("Total time", "3h 40m"),
                    Pair("Sessions", "8"),
                    Pair("Streak", "🔥 4 days")
                ).forEach { stat ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stat.second, style = AppTypography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
                            Text(stat.first, style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            HavenSectionHeader("Recent Sessions", "See all", onAction = onNavigateToFocusHistory)
            Column(Modifier.padding(horizontal = 20.dp)) {
                focusSessionHistory.take(5).forEach { session ->
                    HavenCard(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = HavenGreen)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                Text(sdf.format(java.util.Date(session.startTime)), style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                val durationMins = ((session.endTime - session.startTime) / 60000).toInt()
                                Text("$durationMins minutes", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.padding(horizontal = 20.dp)) {
                HavenCard {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pomodoro Mode", style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("25 min work / 5 min break", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(checkedTrackColor = HavenCyan, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
