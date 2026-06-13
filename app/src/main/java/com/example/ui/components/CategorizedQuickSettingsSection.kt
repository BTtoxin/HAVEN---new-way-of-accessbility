package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import java.util.Collections
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppTypography
import com.example.viewmodel.QSViewModel
import kotlinx.coroutines.launch
import android.hardware.camera2.CameraManager

data class ConfigInfo(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val hasStatus: Boolean = false,
    val isActiveStatus: Boolean = false,
    val intentAction: String? = null,
    val customAction: (() -> Unit)? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorizedQuickSettingsSection(
    viewModel: QSViewModel,
    hasDndPermission: Boolean,
    onRequestDndPermission: () -> Unit,
    isFlashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    onLongClickTile: (String) -> Unit
) {
    val context = LocalContext.current
    val tileCategories by viewModel.tileCategories.collectAsStateWithLifecycle()
    
    val allConfigs = listOf(
        ConfigInfo("Wi-Fi", "Network Options", Icons.Default.Wifi, intentAction = Settings.ACTION_WIFI_SETTINGS),
        ConfigInfo("Network", "Configuration", Icons.Default.CellTower, intentAction = Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
        ConfigInfo("DNS Settings", "Private DNS", Icons.Default.Dns, intentAction = "android.settings.PRIVATE_DNS_SETTINGS"),
        ConfigInfo("Bluetooth", "Devices", Icons.Default.Bluetooth, intentAction = Settings.ACTION_BLUETOOTH_SETTINGS),
        ConfigInfo("Do Not Disturb", if (hasDndPermission) "Active" else "Off", if (hasDndPermission) Icons.Default.DoNotDisturbOn else Icons.Default.DoNotDisturbOff, hasStatus = true, isActiveStatus = hasDndPermission, customAction = {
            if (!hasDndPermission) onRequestDndPermission() else {
                try { context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch(e:Exception){}
            }
        }),
        ConfigInfo("Flashlight", if (isFlashlightOn) "On" else "Off", if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff, hasStatus = true, isActiveStatus = isFlashlightOn, customAction = onToggleFlashlight),
        ConfigInfo("Display", "Settings", Icons.Default.DisplaySettings, intentAction = Settings.ACTION_DISPLAY_SETTINGS),
        ConfigInfo("Sound", "Volume levels", Icons.Default.VolumeUp, intentAction = Settings.ACTION_SOUND_SETTINGS),
        ConfigInfo("Location", "GPS", Icons.Default.LocationOn, intentAction = Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        ConfigInfo("Battery", "Usage", Icons.Default.BatteryFull, intentAction = Intent.ACTION_POWER_USAGE_SUMMARY),
        ConfigInfo("Data Saver", "Network", Icons.Default.DataUsage, intentAction = Settings.ACTION_DATA_ROAMING_SETTINGS),
        ConfigInfo("Security", "Lock screen", Icons.Default.Security, intentAction = Settings.ACTION_SECURITY_SETTINGS),
        ConfigInfo("Apps", "Manage", Icons.Default.Apps, intentAction = Settings.ACTION_APPLICATION_SETTINGS),
        ConfigInfo("Storage", "Space", Icons.Default.Storage, intentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        ConfigInfo("Language", "Locale", Icons.Default.Translate, intentAction = Settings.ACTION_LOCALE_SETTINGS),
        ConfigInfo("Date & Time", "Clock", Icons.Default.Schedule, intentAction = Settings.ACTION_DATE_SETTINGS),
        ConfigInfo("NFC", "Connections", Icons.Default.Nfc, intentAction = Settings.ACTION_NFC_SETTINGS),
        ConfigInfo("Print", "Services", Icons.Default.Print, intentAction = Settings.ACTION_PRINT_SETTINGS)
    )

    val categories = listOf("Connectivity", "Display", "System")
    val defaultCatMap = mapOf(
        "Wi-Fi" to "Connectivity",
        "Network" to "Connectivity",
        "DNS Settings" to "Connectivity",
        "Bluetooth" to "Connectivity",
        "NFC" to "Connectivity",
        "Data Saver" to "Connectivity",
        "Flashlight" to "Display",
        "Display" to "Display"
    ) // default fallback

    val tileOrderList by viewModel.tileOrder.collectAsStateWithLifecycle()
    var localOrder by remember(tileOrderList) { mutableStateOf(tileOrderList) }

    val groupedTiles = remember(tileCategories, allConfigs, localOrder) {
        val groups = mutableMapOf<String, MutableList<ConfigInfo>>()
        categories.forEach { groups[it] = mutableListOf() }
        
        allConfigs.forEach { config ->
            val cat = tileCategories[config.title] ?: defaultCatMap[config.title] ?: "System"
            groups[cat]?.add(config)
        }
        
        groups.forEach { (_, list) ->
            list.sortBy { config ->
                val idx = localOrder.indexOf(config.title)
                if (idx == -1) Int.MAX_VALUE else idx
            }
        }
        groups
    }

    val pagerState = rememberPagerState(pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()

    val gridLayoutColumns by viewModel.gridLayoutColumns.collectAsStateWithLifecycle()
    val categoryNamesMap by viewModel.categoryNames.collectAsStateWithLifecycle()

    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var renameValue by remember { mutableStateOf("") }

    if (categoryToRename != null) {
        AlertDialog(
            onDismissRequest = { categoryToRename = null },
            title = { Text("Rename Category") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameValue.isNotBlank()) {
                            viewModel.updateCategoryNames(categoryToRename!!, renameValue)
                        }
                        categoryToRename = null
                    }
                ) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { categoryToRename = null }) { Text("CANCEL") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Device Management", style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 4.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            categories.forEachIndexed { index, category ->
                val selected = pagerState.currentPage == index
                val color by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                val displayCat = categoryNamesMap[category] ?: category
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.pointerInput(category) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { },
                            onDragEnd = {
                                renameValue = displayCat
                                categoryToRename = category
                                com.example.utils.AudioHapticEngine.triggerClick(context)
                            },
                            onDrag = { change, _ -> change.consume() }
                        )
                    },
                    text = {
                        Text(
                            text = displayCat,
                            style = AppTypography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = color
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val items = groupedTiles[categories[page]] ?: emptyList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridLayoutColumns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(if (gridLayoutColumns > 2) 200.dp else 300.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                items(items) { config ->
                    var totalDragX by remember { mutableStateOf(0f) }
                    var totalDragY by remember { mutableStateOf(0f) }

                    QuickControlTile(
                        title = config.title,
                        subtitle = config.subtitle,
                        icon = config.icon,
                        containerColor = if (config.hasStatus && config.isActiveStatus) MaterialTheme.colorScheme.primary.copy(alpha=0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        iconColor = if (config.hasStatus && config.isActiveStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        subtitleColor = if (config.hasStatus && config.isActiveStatus) MaterialTheme.colorScheme.primary.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f),
                        modifier = Modifier.fillMaxWidth().pointerInput(items) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    totalDragX = 0f
                                    totalDragY = 0f
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                },
                                onDragEnd = {
                                    if (Math.abs(totalDragX) < 10f && Math.abs(totalDragY) < 10f) {
                                        onLongClickTile(config.title)
                                    } else {
                                        viewModel.updateTileOrder(localOrder)
                                    }
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                    
                                    val idx = items.indexOf(config)
                                    val mut = localOrder.toMutableList()
                                    
                                    if (totalDragX > 150f && idx + 1 < items.size) { // swap right
                                        val id1 = mut.indexOf(config.title)
                                        val id2 = mut.indexOf(items[idx+1].title)
                                        if(id1 != -1 && id2 != -1) {
                                            Collections.swap(mut, id1, id2)
                                            localOrder = mut
                                            totalDragX = 0f
                                        }
                                    } else if (totalDragX < -150f && idx - 1 >= 0) { // swap left
                                        val id1 = mut.indexOf(config.title)
                                        val id2 = mut.indexOf(items[idx-1].title)
                                        if(id1 != -1 && id2 != -1) {
                                            Collections.swap(mut, id1, id2)
                                            localOrder = mut
                                            totalDragX = 0f
                                        }
                                    } else if (totalDragY > 150f && idx + 2 < items.size) { // swap down
                                        val id1 = mut.indexOf(config.title)
                                        val id2 = mut.indexOf(items[idx+2].title)
                                        if(id1 != -1 && id2 != -1) {
                                            Collections.swap(mut, id1, id2)
                                            localOrder = mut
                                            totalDragY = 0f
                                        }
                                    } else if (totalDragY < -150f && idx - 2 >= 0) { // swap up
                                        val id1 = mut.indexOf(config.title)
                                        val id2 = mut.indexOf(items[idx-2].title)
                                        if(id1 != -1 && id2 != -1) {
                                            Collections.swap(mut, id1, id2)
                                            localOrder = mut
                                            totalDragY = 0f
                                        }
                                    }
                                }
                            )
                        },
                        index = 0,
                        onClick = {
                            if (config.customAction != null) {
                                config.customAction.invoke()
                            } else if (config.intentAction != null) {
                                try {
                                    context.startActivity(Intent(config.intentAction).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                } catch (e: Exception) { }
                            }
                        },
                        onLongClick = null
                    )
                }
            }
        }
    }
}
