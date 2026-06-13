package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(onBack: () -> Unit) {
    var examType by remember { mutableStateOf("NEET PG") }
    var studyHours by remember { mutableStateOf(4f) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedPlan by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Study Planner", style = AppTypography.titleLarge) },
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
                Text("Generate an adaptive schedule based on your target exam.", style = AppTypography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = examType,
                    onValueChange = { examType = it },
                    label = { Text("Target Exam (e.g. UPSC, JEE)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Daily Study Hours: ${studyHours.toInt()}h")
                Slider(
                    value = studyHours,
                    onValueChange = { studyHours = it },
                    valueRange = 1f..14f,
                    steps = 13
                )
            }

            item {
                Button(
                    onClick = {
                        isGenerating = true
                        scope.launch {
                            kotlinx.coroutines.delay(1500) // Simulate AI generation delay
                            generatedPlan = "Recommended Plan for $examType:\n\nMorning (2h): High-Focus revisions.\nAfternoon (1h): Practice Questions.\nEvening (1h): Deep Dive into weak areas.\n\nSuggested cycle: 4 days study, 1 day revision, 1 day mock test."
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Generate Plan", style = AppTypography.labelLarge)
                    }
                }
            }

            if (generatedPlan != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Your AI Plan", style = AppTypography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(generatedPlan!!, style = AppTypography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
