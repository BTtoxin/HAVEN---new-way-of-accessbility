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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed
import com.example.viewmodel.QSViewModel
import com.example.data.AutomationRuleEntity
import org.json.JSONArray
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(viewModel: QSViewModel, onBack: () -> Unit) {
    val rules by viewModel.automationRules.collectAsStateWithLifecycle()
    var showBuilder by remember { mutableStateOf(false) }

    if (showBuilder) {
        MacroBuilderDialog(
            onDismiss = { showBuilder = false },
            onSave = { name, trigger, actions ->
                val newRule = AutomationRuleEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    triggerType = trigger,
                    actionCount = actions.size,
                    enabled = true,
                    actions = JSONArray(actions).toString()
                )
                viewModel.insertAutomationRule(newRule)
                showBuilder = false
            }
        )
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
                onClick = { showBuilder = true },
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
                        modifier = Modifier.fillMaxWidth().clickable { /* Could show details */ },
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
                            IconButton(onClick = { viewModel.deleteAutomationRule(rule.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = rule.enabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleAutomationRule(rule.id, isChecked)
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

@Composable
fun MacroBuilderDialog(onDismiss: () -> Unit, onSave: (String, String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("Time (08:00)") }
    var actions by remember { mutableStateOf(listOf<String>()) }
    
    val availableActions = listOf("Toggle Wi-Fi", "Turn on Bluetooth", "Start Focus Mode", "Enable DND", "Set Brightness 50%")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Build Automation", style = AppTypography.titleMedium) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Macro Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Trigger Condition") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Action Sequence:", style = AppTypography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                actions.forEachIndexed { index, action ->
                    Text("${index + 1}. $action", style = AppTypography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text("+ Add Action")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableActions.forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act) },
                                onClick = { 
                                    actions = actions + act
                                    expanded = false 
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.ifEmpty { "New Macro" }, trigger, actions) },
                colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                enabled = actions.isNotEmpty()
            ) { Text("Save Macro") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
