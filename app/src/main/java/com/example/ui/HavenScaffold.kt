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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)) {
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
                            onNavigateToDeviceHealth = { currentDetailScreen = "device_health" },
                            onNavigateToAnalytics = { currentDetailScreen = "analytics" },
                            onNavigateToStudentMode = { currentDetailScreen = "student_mode" },
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
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)) {
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
                        "device_health" -> DeviceHealthScreen(
                            onBack = { currentDetailScreen = null }
                        )
                        "analytics" -> StudyAnalyticsScreen(
                            viewModel = viewModel,
                            onBack = { currentDetailScreen = null }
                        )
                        "student_mode" -> StudentModeScreen(
                            onBack = { currentDetailScreen = null },
                            onNavigateToPlanner = { currentDetailScreen = "study_planner" },
                            onNavigateToDeepWork = { currentDetailScreen = "deep_work" },
                            onNavigateToDopamineDetox = { currentDetailScreen = "dopamine_detox" },
                            onNavigateToEmergencyMode = { currentDetailScreen = "emergency_mode" }
                        )
                        "study_planner" -> StudyPlannerScreen(
                            onBack = { currentDetailScreen = "student_mode" }
                        )
                        "deep_work" -> DeepWorkScreen(
                            onBack = { currentDetailScreen = "student_mode" }
                        )
                        "dopamine_detox" -> DopamineDetoxScreen(
                            onBack = { currentDetailScreen = "student_mode" }
                        )
                        "emergency_mode" -> EmergencyExamModeScreen(
                            onBack = { currentDetailScreen = "student_mode" }
                        )
                        "ai_assistant" -> AiAssistantScreen(
                            viewModel = viewModel,
                            modifier = Modifier,
                        )
                    }
                }
                
                if (currentDetailScreen == "ai_assistant") {
                    IconButton(
                        onClick = { currentDetailScreen = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 8.dp, start = 8.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close AI")
                    }
                }
            }
        }
        
        // Universal AI FAB
        if (currentDetailScreen == null || currentDetailScreen == "student_mode") {
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { currentDetailScreen = "ai_assistant" },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 24.dp),
                    containerColor = HavenCyan,
                    contentColor = Color.Black // Assuming HavenCyan is light/bright
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Haven AI")
                }
            }
        }

        val currentToast by viewModel.toastMessage.collectAsStateWithLifecycle()
        var activeToastMessage by remember { mutableStateOf<com.example.viewmodel.ToastMessage?>(null) }
        
        LaunchedEffect(currentToast) {
            currentToast?.let {
                activeToastMessage = it
                AudioHapticEngine.triggerAchievement(context) // Sound-enabled format for milestone
                kotlinx.coroutines.delay(3000)
                if (activeToastMessage == it) {
                    activeToastMessage = null
                }
            }
        }

        AnimatedVisibility(
            visible = activeToastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
        ) {
            activeToastMessage?.let { toast ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (toast.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (toast.isError) Icons.Filled.Warning else Icons.Filled.Stars,
                            contentDescription = "Toast Icon",
                            tint = if (toast.isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = toast.message,
                            style = AppTypography.bodyMedium,
                            color = if (toast.isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
