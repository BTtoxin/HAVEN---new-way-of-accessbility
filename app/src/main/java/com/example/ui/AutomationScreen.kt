package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed

data class AutomationRule(val id: String, val name: String, val triggerType: String, val actionCount: Int, var enabled: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(onBack: () -> Unit) {
    var rules by remember { 
        mutableStateOf(listOf(
            AutomationRule("1", "Cinema Mode", "Time (20:00)", 3, true),
            AutomationRule("2", "Low Battery Saver", "Battery < 20%", 2, true),
            AutomationRule("3", "Driving Mode", "Bluetooth Connected", 4, false)
        )) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automation Rules", style = AppTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Placeholder for adding new rule
                    rules = rules + AutomationRule("temp", "New Macro", "Trigger", 1, true)
                },
                containerColor = NothingRed,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        if (rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No automation rules yet", style = AppTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rules) { rule ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { /* Edit */ },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = "Rule", tint = NothingRed)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.name, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${rule.triggerType} • ${rule.actionCount} Actions",
                                    style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = rule.enabled,
                                onCheckedChange = { isChecked ->
                                    rules = rules.map { if (it.id == rule.id) it.copy(enabled = isChecked) else it }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = NothingRed, checkedTrackColor = NothingRed.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}
