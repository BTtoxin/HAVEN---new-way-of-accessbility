package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.AppTypography
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed

data class AppInfo(val packageName: String, val label: String, val icon: Drawable)

fun loadInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { AppInfo(it.activityInfo.packageName, it.loadLabel(pm).toString(), it.loadIcon(pm)) }
        .sortedBy { it.label }
        .distinctBy { it.packageName }
        .filter { it.packageName != context.packageName } // exclude self
}

@Composable
fun AppSelectorDialog(
    preselectedApps: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedApps by remember { mutableStateOf(preselectedApps) }
    val allApps by remember { mutableStateOf(loadInstalledApps(context)) }
    val filtered = allApps.filter { it.label.contains(searchQuery, ignoreCase = true) }

    val recentAppPackages = remember {
        val attributionContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context
        } else {
            context
        }
        val usageStatsManager = attributionContext.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 * 7 // 7 days window for recent
        val stats = usageStatsManager.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        stats?.filter { it.lastTimeUsed > 0 }?.sortedByDescending { it.lastTimeUsed }?.map { it.packageName }?.distinct() ?: emptyList()
    }
    
    val recentApps = remember(allApps, recentAppPackages) {
        recentAppPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }.take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("SELECT ALLOWED APPS", style = AppTypography.labelSmall)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Apps") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${selectedApps.size} selected", style = AppTypography.labelSmall, color = NothingRed)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    if (searchQuery.isEmpty() && recentApps.isNotEmpty()) {
                        item {
                            Text("RECENTLY USED", style = AppTypography.labelSmall, modifier = Modifier.padding(12.dp))
                        }
                        items(recentApps) { app ->
                            AppItemRow(
                                app = app,
                                isSelected = app.packageName in selectedApps,
                                onCheckedChange = { checked ->
                                    selectedApps = if (checked) {
                                        selectedApps + app.packageName
                                    } else {
                                        selectedApps - app.packageName
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BorderDark, thickness = 1.dp)
                            Text("ALL APPS", style = AppTypography.labelSmall, modifier = Modifier.padding(12.dp))
                        }
                    }
                    items(filtered) { app ->
                        AppItemRow(
                            app = app,
                            isSelected = app.packageName in selectedApps,
                            onCheckedChange = { checked ->
                                selectedApps = if (checked) {
                                    selectedApps + app.packageName
                                } else {
                                    selectedApps - app.packageName
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedApps) }) {
                Text("CONFIRM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun AppItemRow(
    app: AppInfo,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isSelected) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = app.icon,
                contentDescription = app.label,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = AppTypography.bodyMedium)
                Text(app.packageName, style = AppTypography.labelSmall, color = NeutralGray)
            }
            Switch(
                checked = isSelected,
                onCheckedChange = onCheckedChange
            )
        }
        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
    }
}
