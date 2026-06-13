package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.HavenCyan
import com.example.utils.AudioHapticEngine
import com.example.viewmodel.QSViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun HavenScaffold(
    viewModel: QSViewModel,
    initialOpenFocus: Boolean = false,
    onRequestPermission: () -> Unit,
    onRequestDndPermission: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(if (initialOpenFocus) 2 else 0) }
    var currentDetailScreen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialOpenFocus) {
        if (initialOpenFocus) {
            selectedTab = 2
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentDetailScreen == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.navigationBarsPadding()) {
                            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tabs = listOf(
                                    Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                                    Triple("Controls", Icons.Filled.Tune, Icons.Outlined.Tune),
                                    Triple("Focus", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
                                    Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
                                )

                                tabs.forEachIndexed { index, tab ->
                                    val isSelected = selectedTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            onClick = {
                                                AudioHapticEngine.triggerClick(context)
                                                selectedTab = index
                                            },
                                            shape = RoundedCornerShape(50),
                                            color = if (isSelected) HavenCyan.copy(alpha = 0.12f) else Color.Transparent,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) tab.second else tab.third,
                                                    contentDescription = tab.first,
                                                    tint = if (isSelected) HavenCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = tab.first,
                                                        style = AppTypography.labelMedium,
                                                        color = HavenCyan
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
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        initialOpenFocus = initialOpenFocus,
                        onNavigateToSettings = { currentDetailScreen = "settings" },
                        onNavigateToAutomation = { currentDetailScreen = "automation" },
                        onNavigateToClipboard = { currentDetailScreen = "clipboard" },
                        onNavigateToSensors = { currentDetailScreen = "sensors" },
                        onNavigateToFocus = { selectedTab = 2 },
                        onNavigateToFocusHistory = { currentDetailScreen = "focus_history" },
                        onRequestPermission = onRequestPermission,
                        onRequestDndPermission = onRequestDndPermission
                    )
                    1 -> ControlsScreen(
                        viewModel = viewModel,
                        onNavigateToAutomation = { currentDetailScreen = "automation" }
                    )
                    2 -> FocusTab(
                        viewModel = viewModel,
                        onNavigateToFocusHistory = { currentDetailScreen = "focus_history" }
                    )
                    3 -> ProfileTab(
                        viewModel = viewModel,
                        onNavigateToSettings = { currentDetailScreen = "settings" },
                        onNavigateToPermissions = { currentDetailScreen = "permissions" },
                        onNavigateToChangelog = { currentDetailScreen = "changelog" }
                    )
                }
            }
        }

        // Overlay Detail Screens
        AnimatedVisibility(
            visible = currentDetailScreen != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentDetailScreen) {
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentDetailScreen = null },
                        onNavigateToPermissions = { currentDetailScreen = "permissions" },
                        onNavigateToChangelog = { currentDetailScreen = "changelog" },
                        onResetLayout = { viewModel.resetTileOrder() },
                        onConfirm = { viewModel.checkAllStates() }
                    )
                    "automation" -> AutomationScreen(
                        viewModel = viewModel,
                        onBack = { currentDetailScreen = null }
                    )
                    "clipboard" -> ClipboardScreen(
                        onBack = { currentDetailScreen = null }
                    )
                    "sensors" -> SensorScreen(
                        onBack = { currentDetailScreen = null }
                    )
                    "focus_history" -> FocusHistoryScreen(
                        viewModel = viewModel,
                        onBack = { currentDetailScreen = null }
                    )
                    "permissions" -> PermissionScreen(
                        onBack = { currentDetailScreen = null }
                    )
                    "changelog" -> ChangelogScreen(
                        onBack = { currentDetailScreen = "settings" }
                    )
                }
            }
        }
    }
}
