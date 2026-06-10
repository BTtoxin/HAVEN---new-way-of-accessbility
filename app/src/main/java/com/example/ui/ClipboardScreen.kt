package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed

import org.json.JSONArray
import org.json.JSONObject

data class ClipboardEntry(val id: Int, val text: String, var isVaulted: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("clipboard_vault", Context.MODE_PRIVATE) }
    
    var entries by remember { mutableStateOf(listOf<ClipboardEntry>()) }

    LaunchedEffect(Unit) {
        val savedEntriesStr = prefs.getString("entries", "[]") ?: "[]"
        val savedList = mutableListOf<ClipboardEntry>()
        try {
            val array = JSONArray(savedEntriesStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                savedList.add(ClipboardEntry(obj.getInt("id"), obj.getString("text"), obj.getBoolean("isVaulted")))
            }
        } catch (e: Exception) {}
        
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var newText: String? = null
        if (clipboard.hasPrimaryClip()) {
            newText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        }
        
        if (newText != null && savedList.none { it.text == newText }) {
            val newId = (savedList.maxOfOrNull { it.id } ?: 0) + 1
            savedList.add(0, ClipboardEntry(newId, newText, false))
        }
        
        entries = savedList
    }
    
    LaunchedEffect(entries) {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("text", entry.text)
                put("isVaulted", entry.isVaulted)
            }
            array.put(obj)
        }
        prefs.edit().putString("entries", array.toString()).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clipboard Vault", style = AppTypography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (entry.isVaulted) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Vault Status",
                                tint = if (entry.isVaulted) NothingRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(if (entry.isVaulted) "VAULTED" else "RECENT", style = AppTypography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = entry.text,
                            style = AppTypography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = {
                                entries = entries.map { if (it.id == entry.id) it.copy(isVaulted = !it.isVaulted) else it }
                            }) {
                                Icon(Icons.Default.Lock, contentDescription = "Toggle Vault", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", entry.text))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = {
                                entries = entries.filterNot { it.id == entry.id }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
