package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.api.GeminiApi
import kotlinx.coroutines.launch
import com.example.ui.theme.AppTypography

import com.example.viewmodel.QSViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class ChatMessage(val text: String, val isUser: Boolean, val isLoading: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: QSViewModel, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Good Evening. I am your Haven AI. How can I assist with your focus and productivity today?", isUser = false)
    )) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val focusHistory by viewModel.focusSessionHistory.collectAsStateWithLifecycle()
    val aiVoiceProfile by viewModel.aiVoiceProfile.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    var systemInstruction = "You are a ${aiVoiceProfile.lowercase()} AI productivity coach. Analyze the user's queries and suggest focus or study plans. They have ${focusHistory.size} total sessions recorded. Use a $aiVoiceProfile tone."

    fun sendMessage() {
        if (query.isBlank()) return
        val currentQuery = query
        query = ""
        messages = messages + ChatMessage(currentQuery, isUser = true)
        
        val q = currentQuery.lowercase()
        if (q == "start focus") {
            messages = messages + ChatMessage("Starting deep focus mode.", isUser = false)
            return
        } else if (q == "enable battery saver") {
            messages = messages + ChatMessage("Enabling battery saver profile.", isUser = false)
            return
        }

        messages = messages + ChatMessage("...", isUser = false, isLoading = true)

        scope.launch {
            try {
                // Determine model based on task. Use flash for quick chat.
                val prompt = "User says: $currentQuery"
                val response = GeminiApi.generateContent(
                    prompt = prompt,
                    model = "gemini-3.5-flash",
                    systemInstruction = systemInstruction
                )
                messages = messages.dropLast(1) + ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                messages = messages.dropLast(1) + ChatMessage("I encountered an error connecting to my neural network. Please try again.", isUser = false)
            }
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("AI Assistant Settings") },
            text = {
                Column {
                    Text("Voice Profile", style = AppTypography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Calm", "Professional", "Motivational").forEach { profile ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAiVoiceProfile(profile)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (aiVoiceProfile == profile),
                                onClick = { viewModel.setAiVoiceProfile(profile) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(profile, style = AppTypography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("CLOSE")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Haven AI", style = AppTypography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "AI Settings")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Ask anything or launch a feature...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = { sendMessage() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (query.isBlank()) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice Input")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!msg.isUser) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .align(Alignment.Bottom)
            )
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (msg.isUser) 16.dp else 4.dp, 
                bottomEnd = if (msg.isUser) 4.dp else 16.dp
            ),
            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(16.dp),
                style = AppTypography.bodyLarge
            )
        }
    }
}
