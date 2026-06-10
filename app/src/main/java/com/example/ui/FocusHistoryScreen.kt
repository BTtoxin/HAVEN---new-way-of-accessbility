package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NeutralGray
import com.example.viewmodel.QSViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusHistoryScreen(viewModel: QSViewModel, onBack: () -> Unit) {
    val history by viewModel.focusSessionHistory.collectAsStateWithLifecycle()

    val totalMinutes = history.filter { it.completed }.sumOf { (it.endTime - it.startTime) / 60000 }
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    var selectedTab by remember { mutableStateOf("DAILY") }

    // Calculate last 7 days daily totals
    val dailyTotals = remember(history) {
        val totals = mutableMapOf<String, Long>()
        val cal = Calendar.getInstance()
        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        
        // Initialize last 7 days with 0
        for (i in 6 downTo 0) {
            val dayName = dayFormatter.format(cal.time)
            totals[dayName] = 0L
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        // Populate totals
        history.filter { it.completed }.forEach { session ->
            val sessionCal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            val dayName = dayFormatter.format(sessionCal.time)
            if (totals.containsKey(dayName)) {
                totals[dayName] = totals[dayName]!! + ((session.endTime - session.startTime) / 60000)
            }
        }
        totals.toList().reversed() // Oldest to newest
    }

    val weeklyTotals = remember(history) {
        val totals = mutableListOf("4W", "3W", "2W", "1W", "TW").associateWith { 0L }.toMutableMap()
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        
        history.filter { it.completed }.forEach { session ->
            val sessionCal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            val sessionWeek = sessionCal.get(Calendar.WEEK_OF_YEAR)
            val diff = currentWeek - sessionWeek
            
            val key = when(diff) {
                0 -> "TW"
                1 -> "1W"
                2 -> "2W"
                3 -> "3W"
                4 -> "4W"
                else -> null
            }
            if (key != null) {
                totals[key] = totals[key]!! + ((session.endTime - session.startTime) / 60000)
            }
        }
        totals.toList()
    }
    
    val currentTotals = if (selectedTab == "DAILY") dailyTotals else weeklyTotals
    val maxRecord = currentTotals.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus History", style = AppTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val prodScore = minOf(100, (totalMinutes / 60) * 10 + history.count { it.completed } * 5)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL FOCUS TIME", style = AppTypography.labelSmall, color = NeutralGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${totalMinutes}m", style = AppTypography.displayLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PRODUCTIVITY SCORE", style = AppTypography.labelSmall, color = NeutralGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$prodScore", style = AppTypography.displayLarge.copy(fontWeight = FontWeight.Bold), color = if (prodScore >= 50) androidx.compose.ui.graphics.Color(0xFF10B981) else NothingRed)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TabButton("DAILY", selectedTab == "DAILY") { selectedTab = "DAILY" }
                        Spacer(modifier = Modifier.width(16.dp))
                        TabButton("WEEKLY", selectedTab == "WEEKLY") { selectedTab = "WEEKLY" }
                    }

                    // Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        currentTotals.forEach { (label, minutes) ->
                            val heightFraction = (minutes.toFloat() / maxRecord.toFloat()).coerceIn(0f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (minutes > 0) {
                                    Text("${minutes}m", style = AppTypography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(heightFraction.coerceAtLeast(0.05f))
                                        .background(
                                            color = if (minutes > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(label, style = AppTypography.labelSmall, color = NeutralGray)
                            }
                        }
                    }
                }
            }
            
            Text(
                "RECENT SESSIONS",
                style = AppTypography.labelSmall,
                color = NeutralGray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No focus sessions yet", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(history) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val dateStr = formatter.format(Date(session.startTime))
                                    Text(dateStr, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val duration = (session.endTime - session.startTime) / 60000
                                    Text("Duration: ${duration}m", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Icon(
                                    if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = if (session.completed) "Completed" else "Cancelled",
                                    tint = if (session.completed) Color(0xFF10B981) else NothingRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) MaterialTheme.colorScheme.primary else NeutralGray
        )
    }
}
