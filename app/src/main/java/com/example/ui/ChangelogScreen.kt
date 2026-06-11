package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed
import com.example.utils.VersionManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val entries = remember { VersionManager.getChangelogEntries(context) }
    val (currentVersion, lastUpdated) = remember { VersionManager.getAppVersion(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }

    val tags = listOf("All", "Feature", "Refactor", "Bug Fix", "Optimization", "Release")

    val filteredEntries = remember(searchQuery, selectedTag, entries) {
        entries.mapNotNull { entry ->
            val matchingChanges = entry.changes.filter { change ->
                val matchesTag = selectedTag == "All" || change.tag == selectedTag
                val matchesSearch = searchQuery.isEmpty() ||
                        change.text.contains(searchQuery, ignoreCase = true) ||
                        entry.version.contains(searchQuery, ignoreCase = true) ||
                        entry.date.contains(searchQuery, ignoreCase = true) ||
                        change.details.contains(searchQuery, ignoreCase = true)
                matchesTag && matchesSearch
            }
            if (matchingChanges.isNotEmpty()) {
                entry.copy(changes = matchingChanges)
            } else {
                null
            }
        }
    }

    fun exportChangelogAsText(ctx: Context, data: List<com.example.utils.ChangelogEntry>) {
        val exportText = StringBuilder()
        exportText.append("Haven Changelog\n\n")
        data.forEach { entry ->
            exportText.append("Version: ${entry.version} (${entry.date})\n")
            entry.changes.forEach { change ->
                exportText.append("- [${change.tag}] ${change.text}\n")
                if (change.details.isNotEmpty()) {
                    exportText.append("  Details: ${change.details}\n")
                }
            }
            exportText.append("\n")
        }
        
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, exportText.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Changelog")
        ctx.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CHANGELOG",
                        style = AppTypography.labelSmall.copy(fontSize = 14.sp, letterSpacing = 3.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            exportChangelogAsText(context, filteredEntries)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Changelog",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CURRENT BUILD", style = AppTypography.labelSmall, color = NeutralGray)
                    Text(remember { VersionManager.getFullVersionString(context) }, style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search version, date, or changes...", style = AppTypography.bodyMedium, color = NeutralGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag, style = AppTypography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            itemsIndexed(filteredEntries) { index, entry ->
                var isVisible by remember { mutableStateOf(false) }
                
                LaunchedEffect(entry.version, selectedTag, searchQuery) {
                    delay(index * 50L)
                    isVisible = true
                }
                
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500), initialOffsetY = { 50 })
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.version,
                                        style = AppTypography.bodySmall.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (entry.date.contains("Remote") || (entries.isNotEmpty() && entry == entries.first() && entry.date != entries.getOrNull(1)?.date)) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NothingRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("REMOTE", style = AppTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp), color = NothingRed)
                                        }
                                    }
                                }
                                Text(
                                    text = entry.date,
                                    style = AppTypography.labelSmall,
                                    color = NeutralGray
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            entry.changes.forEach { change ->
                                var isExpanded by remember { mutableStateOf(false) }

                                Column(
                                    modifier = Modifier
                                        .padding(bottom = 12.dp)
                                        .fillMaxWidth()
                                        .clickable { 
                                            if(change.details.isNotEmpty()) {
                                                com.example.utils.AudioHapticEngine.triggerClick(context)
                                                isExpanded = !isExpanded 
                                            }
                                        }
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            "•",
                                            color = NothingRed,
                                            style = AppTypography.bodyMedium,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            change.text,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            style = AppTypography.bodyMedium,
                                            lineHeight = 20.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (change.details.isNotEmpty()) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expand details",
                                                tint = NeutralGray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    AnimatedVisibility(visible = isExpanded) {
                                        Text(
                                            text = change.details,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            style = AppTypography.bodySmall,
                                            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Box(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                        Text(
                                            text = change.tag.uppercase(),
                                            style = AppTypography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
