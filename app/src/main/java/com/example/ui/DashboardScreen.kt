package com.example.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.components.TactileButton as Button
import com.example.ui.components.TactileOutlinedButton as OutlinedButton
import com.example.ui.components.TactileIconButton as IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.utils.SystemSettingsHelper
import com.example.viewmodel.QSViewModel
import android.hardware.camera2.CameraManager

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeaderActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    activeColor: androidx.compose.ui.graphics.Color = NothingRed,
    tooltipText: String = "",
    extraModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showTooltip by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isHovered) 1.05f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "HeaderButtonScale"
    )

    LaunchedEffect(isHovered) {
        if (isHovered) {
            showTooltip = true
        } else {
            showTooltip = false
        }
    }

    LaunchedEffect(showTooltip) {
        if (showTooltip) {
            kotlinx.coroutines.delay(2000)
            showTooltip = false
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.wrapContentSize()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    color = if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) activeColor else BorderDark.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        com.example.utils.AudioHapticEngine.triggerClick(context)
                        onClick()
                    },
                    onLongClick = {
                        com.example.utils.AudioHapticEngine.triggerClick(context)
                        showTooltip = true
                    }
                )
                .then(extraModifier),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    val springSpec = androidx.compose.animation.core.spring<kotlin.Float>(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                    (androidx.compose.animation.scaleIn(animationSpec = springSpec) + androidx.compose.animation.fadeIn()) togetherWith (androidx.compose.animation.scaleOut(animationSpec = springSpec) + androidx.compose.animation.fadeOut())
                },
                label = "HeaderActionIcon"
            ) { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = contentDescription,
                    tint = if (isActive) activeColor else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // TOOLTIP WINDOW
        AnimatedVisibility(
            visible = showTooltip && tooltipText.isNotEmpty(),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(expandFrom = Alignment.Top),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier.absoluteOffset(y = 54.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black, shape = RoundedCornerShape(6.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tooltipText.uppercase(),
                    style = AppTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: QSViewModel,
    initialOpenFocus: Boolean = false,
    onNavigateToSettings: () -> Unit = {},
    onRequestPermission: () -> Unit,
    onRequestDndPermission: () -> Unit
) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(android.content.Context.CAMERA_SERVICE) as? CameraManager }
    var isFlashlightOn by remember { mutableStateOf(false) }

    var isBooted by remember { mutableStateOf(true) }
    val entranceTranslationY by animateFloatAsState(
        targetValue = if (isBooted) 0f else 80f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "dashboardEntranceTranslationY"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isBooted) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 800,
            easing = androidx.compose.animation.core.EaseOutQuad
        ),
        label = "dashboardEntranceAlpha"
    )

    val hasWriteSettingsPermission by viewModel.hasWriteSettingsPermission.collectAsStateWithLifecycle()
    val hasDndPermission by viewModel.hasDndPermission.collectAsStateWithLifecycle()
    val systemTimeout by viewModel.currentSystemTimeout.collectAsStateWithLifecycle()
    val customShortcutTarget by viewModel.customShortcutTarget.collectAsStateWithLifecycle()
    val customShortcutLabel by viewModel.customShortcutLabel.collectAsStateWithLifecycle()
    val glyphProfile by viewModel.glyphBrightnessProfile.collectAsStateWithLifecycle()
    val isCaffeineActive by viewModel.isCaffeineActive.collectAsStateWithLifecycle()
    val isTheaterActive by viewModel.isTheaterActive.collectAsStateWithLifecycle()
    
    // We'll collect private DNS from VM StateFlow.
    val isDnsActive by viewModel.isDnsActive.collectAsStateWithLifecycle()
    val isAudioIsolated by viewModel.isAppAudioIsolated.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val editRotation by animateFloatAsState(
        targetValue = if (isEditing) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "EditRotationAnimation"
    )
    val otherScale by animateFloatAsState(
        targetValue = if (isEditing) 0.85f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "SecondaryButtonScale"
    )

    var showNotifications by remember { mutableStateOf(false) }
    var showSystemMonitor by remember { mutableStateOf(false) }

    var showSpecialAbout by remember { mutableStateOf(false) }
    var showSpecialChangelog by remember { mutableStateOf(false) }
    var showSpecialManual by remember { mutableStateOf(false) }
    var showSpecialPaletteSelector by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var activeTileSettings by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = toastMessage!!.message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToast()
        }
    }

    val tileOrderList by viewModel.tileOrder.collectAsStateWithLifecycle()
    val availableOrder = remember(tileOrderList) {
        if (tileOrderList.isEmpty() || !tileOrderList.contains("MANUAL")) {
            listOf("TIMEOUT", "CAFFEINE", "WEATHER", "BATTERY", "BRIGHTNESS", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH", "MANUAL", "CHANGELOG", "ABOUT")
        } else {
            tileOrderList
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val aiLayoutSuggestion by viewModel.aiLayoutSuggestion.collectAsStateWithLifecycle()
    val filteredOrder = remember(availableOrder, searchQuery) {
        if (searchQuery.isBlank()) {
            availableOrder
        } else {
            val q = searchQuery.trim().lowercase()
            availableOrder.filter { id ->
                val keywords = when (id) {
                    "TIMEOUT" -> listOf("timeout", "screen", "display", "time", "sleep", "lock")
                    "CAFFEINE" -> listOf("caffeine", "screen on", "wake", "lock", "standby", "keep awake")
                    "WEATHER" -> listOf("weather", "forecast", "temperature", "rain", "meteorology", "climate", "sun", "clouds")
                    "BATTERY" -> listOf("battery", "power", "charging", "statistics", "saver", "gauge", "percentage")
                    "BRIGHTNESS" -> listOf("brightness", "backlight", "screen", "dim", "slider")
                    "DNS" -> listOf("dns", "private dns", "network", "domain", "internet", "private")
                    "THEATER" -> listOf("theater", "theatre", "dnd", "mute", "dim", "quiet", "cinema")
                    "CLIPBOARD" -> listOf("clipboard", "copy", "paste", "purge", "clear", "clean")
                    "FOCUS" -> listOf("focus", "deep focus", "sandbox", "timer", "minutes", "lock", "restrict")
                    "SHORTCUT" -> listOf("shortcut", "configure", "open", "custom", "launch", "quick")
                    "APP_AUDIO" -> listOf("app audio", "audio", "isolate", "volume", "sound", "mute")
                    "OPERATOR" -> listOf("operator", "carrier", "network", "cell", "sim", "mobile", "carrier settings")
                    "GLYPH" -> listOf("glyph", "led", "brightness", "profile", "backlight", "lights", "ring")
                    "MANUAL" -> listOf("manual", "help", "guide", "user", "instructions", "faq")
                    "CHANGELOG" -> listOf("changelog", "update", "history", "version", "june", "new", "release")
                    "ABOUT" -> listOf("about", "metadata", "creator", "system", "ashu", "info", "developer")
                    else -> emptyList()
                }
                keywords.any { it.contains(q) } || id.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = entranceTranslationY
                    alpha = entranceAlpha
                }
        ) {
            // DIGITAL CLOCK WIDGET
            item(span = StaggeredGridItemSpan.FullLine) {
                var currentTime by remember { mutableStateOf(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }
                var currentDate by remember { mutableStateOf(java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())) }
                var networkType by remember { mutableStateOf("4G LTE") }
                var signalStrength by remember { mutableIntStateOf(4) }

                LaunchedEffect(Unit) {
                    while(true) {
                        currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        currentDate = java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        // Simulate window.navigator.connection updates
                        signalStrength = listOf(2, 3, 4).random()
                        networkType = listOf("5G", "4G LTE", "Wi-Fi").random()
                        kotlinx.coroutines.delay(5000)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    // Small Network Signal Strength Indicator
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(signalStrength) {
                                4 -> Icons.Default.SignalCellular4Bar
                                3 -> Icons.Default.NetworkCell
                                else -> Icons.Default.NetworkCell
                            },
                            contentDescription = "Signal Strength",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = networkType,
                            style = AppTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTime,
                            style = AppTypography.displayLarge.copy(fontSize = 64.sp, letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDate.uppercase(),
                            style = AppTypography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 4.sp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // AI REORGANIZATION SUGGESTION BANNER
            if (false && aiLayoutSuggestion != null) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, NothingRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Layout Check",
                                    tint = NothingRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NOTHING OS AI • GRID OPTIMIZER",
                                    style = AppTypography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 11.sp,
                                        color = NothingRed
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Based on your frequent click habits, we found a more efficient arrangement. Move active toggles to the primary 2x2 grid positions?",
                                style = AppTypography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { 
                                        com.example.utils.AudioHapticEngine.triggerClick(context)
                                        viewModel.dismissReorganization() 
                                    }
                                ) {
                                    Text(
                                        "DISMISS",
                                        style = AppTypography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { 
                                        com.example.utils.AudioHapticEngine.triggerClick(context)
                                        viewModel.acceptReorganization() 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "REORGANIZE",
                                        style = AppTypography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // HEADER
            item(span = StaggeredGridItemSpan.FullLine) {
                var currentTime by remember { mutableStateOf(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }
                var currentDate by remember { mutableStateOf(java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())) }

                LaunchedEffect(Unit) {
                    while(true) {
                        currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        currentDate = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())
                        kotlinx.coroutines.delay(60000)
                    }
                }

                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var tapCount by remember { mutableStateOf(0) }
                    var lastTapTime by remember { mutableStateOf(0L) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    val now = System.currentTimeMillis()
                                    if (now - lastTapTime < 500) {
                                        tapCount++
                                        if (tapCount >= 3) {
                                            showSystemMonitor = !showSystemMonitor
                                            tapCount = 0
                                            lastTapTime = 0L
                                        } else {
                                            lastTapTime = now
                                        }
                                    } else {
                                        tapCount = 1
                                        lastTapTime = now
                                    }
                                }
                            )
                        }
                    ) {
                        // Animated quadrant glowing logo (representing modern art and tech identity)
                        GlowingLogo(
                            size = 36.dp,
                            modifier = Modifier.padding(end = 12.dp),
                            dotColor = MaterialTheme.colorScheme.primary,
                            glowing = true
                        )
                        Column {
                            Text(
                                text = "HAVEN",
                                style = AppTypography.displayLarge,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "by ashu mehta",
                                style = AppTypography.labelSmall,
                                color = NtSecondary,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val isMonochrome by viewModel.isMonochrome.collectAsStateWithLifecycle()
                    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                    val isDark = when (themeMode) {
                        "DARK" -> true
                        "LIGHT" -> false
                        else -> systemDark
                    }

                    HeaderActionButton(
                        icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tooltipText = if (isDark) "Light Mode" else "Dark Mode",
                        extraModifier = Modifier.graphicsLayer {
                            scaleX = otherScale
                            scaleY = otherScale
                        },
                        onClick = {
                            viewModel.setThemeMode(if (isDark) "LIGHT" else "DARK")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderActionButton(
                        icon = Icons.Default.Palette,
                        contentDescription = "Toggle Theme Selection",
                        tooltipText = "Theme Palette",
                        extraModifier = Modifier.graphicsLayer {
                            scaleX = otherScale
                            scaleY = otherScale
                        },
                        onClick = {
                            showSpecialPaletteSelector = true
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderActionButton(
                        icon = if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = "Edit Layout",
                        isActive = isEditing,
                        tooltipText = if (isEditing) "Finish Editing" else "Edit Layout",
                        extraModifier = Modifier.graphicsLayer {
                            rotationZ = editRotation
                        },
                        onClick = {
                            isEditing = !isEditing
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                    HeaderActionButton(
                        icon = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.ManageAccounts,
                        contentDescription = "User Profile",
                        isActive = currentUser != null,
                        tooltipText = if (currentUser != null) "Profile: ${currentUser!!.nickname}" else "Sign In & Auth",
                        extraModifier = Modifier.graphicsLayer {
                            scaleX = otherScale
                            scaleY = otherScale
                        },
                        onClick = {
                            showAuthModal = true
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderActionButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tooltipText = "App Settings",
                        extraModifier = Modifier.graphicsLayer {
                            scaleX = otherScale
                            scaleY = otherScale
                        },
                        onClick = {
                            onNavigateToSettings()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    var showHelpDialog by remember { mutableStateOf(false) }
                    HeaderActionButton(
                        icon = Icons.Default.HelpOutline,
                        contentDescription = "Quick Add Guide",
                        tooltipText = "How to add Quick Tiles",
                        onClick = {
                            showHelpDialog = true
                        }
                    )
                    if (showHelpDialog) {
                        com.example.ui.QuickAddGuideDialog(onDismiss = { showHelpDialog = false })
                    }
                }
            }

            // RECENTLY USED SECTION
            item(span = StaggeredGridItemSpan.FullLine) {
                val recentlyUsed by viewModel.recentlyUsedTiles.collectAsStateWithLifecycle()
                if (recentlyUsed.isNotEmpty()) {
                    com.example.ui.components.RecentlyUsedSection(
                        recentlyUsed = recentlyUsed,
                        viewModel = viewModel,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
            }

            // 2X2 QUICK TOGGLES GRID COMPONENT
            item(span = StaggeredGridItemSpan.FullLine) {
                com.example.ui.components.QuickToggleGrid(
                    viewModel = viewModel,
                    isFlashlightActive = isFlashlightOn,
                    onToggleFlashlight = {
                        try {
                            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                            if (cameraId != null) {
                                cameraManager.setTorchMode(cameraId, !isFlashlightOn)
                                isFlashlightOn = !isFlashlightOn
                            }
                        } catch (e: Exception) { }
                    },
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            // QUICK CONTROLS SECTION
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quick Controls", style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text("Swipe for more", style = AppTypography.labelSmall, color = NtSecondary)
                    }
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            QuickControlTile(
                                title = "Wi-Fi",
                                subtitle = "Network Options",
                                icon = Icons.Default.Wifi,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 0,
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
                                    } catch(e: Exception) {
                                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                    }
                                },
                                onLongClick = { activeTileSettings = "Wi-Fi" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Network",
                                subtitle = "Configuration",
                                icon = Icons.Default.CellTower,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 1,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                                    try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                },
                                onLongClick = { activeTileSettings = "Network" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "DNS Settings",
                                subtitle = "Private DNS",
                                icon = Icons.Default.Dns,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 2,
                                onClick = {
                                    val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
                                    try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                },
                                onLongClick = { activeTileSettings = "DNS Settings" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Bluetooth",
                                subtitle = "Devices",
                                icon = Icons.Default.Bluetooth,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 3,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                },
                                onLongClick = { activeTileSettings = "Bluetooth" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Do Not Disturb",
                                subtitle = if (hasDndPermission) "Active" else "Off",
                                icon = if (hasDndPermission) Icons.Default.DoNotDisturbOn else Icons.Default.DoNotDisturbOff,
                                containerColor = NtGreen,
                                iconColor = NtGreenText,
                                subtitleColor = NtGreenText,
                                modifier = Modifier.width(140.dp),
                                index = 4,
                                onClick = {
                                    if (!hasDndPermission) onRequestDndPermission()
                                    else {
                                        val intent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
                                        try { context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { }
                                    }
                                },
                                onLongClick = { activeTileSettings = "Do Not Disturb" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Flashlight",
                                subtitle = if (isFlashlightOn) "On" else "Off",
                                icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 5,
                                onClick = {
                                    try {
                                        val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                                        if (cameraId != null) {
                                            cameraManager.setTorchMode(cameraId, !isFlashlightOn)
                                            isFlashlightOn = !isFlashlightOn
                                        }
                                    } catch (e: Exception) { }
                                },
                                onLongClick = { activeTileSettings = "Flashlight" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Display",
                                subtitle = "Settings",
                                icon = Icons.Default.DisplaySettings,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 6,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Display" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Sound",
                                subtitle = "Volume levels",
                                icon = Icons.Default.VolumeUp,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 7,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Sound" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Location",
                                subtitle = "GPS",
                                icon = Icons.Default.LocationOn,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 8,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Location" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Battery",
                                subtitle = "Usage",
                                icon = Icons.Default.BatteryFull,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 9,
                                onClick = { try { context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Battery" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Data Saver",
                                subtitle = "Network",
                                icon = Icons.Default.DataUsage,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 10,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Data Saver" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Security",
                                subtitle = "Lock screen",
                                icon = Icons.Default.Security,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 11,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Security" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Apps",
                                subtitle = "Manage",
                                icon = Icons.Default.Apps,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 12,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Apps" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Storage",
                                subtitle = "Space",
                                icon = Icons.Default.Storage,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 13,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Storage" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Language",
                                subtitle = "Locale",
                                icon = Icons.Default.Translate,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 14,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Language" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Date & Time",
                                subtitle = "Clock",
                                icon = Icons.Default.Schedule,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 15,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_DATE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Date & Time" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "NFC",
                                subtitle = "Connections",
                                icon = Icons.Default.Nfc,
                                containerColor = NtSurfaceVariant,
                                iconColor = NtSecondary,
                                subtitleColor = NtTextTertiary,
                                modifier = Modifier.width(140.dp),
                                index = 16,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "NFC" }
                            )
                        }
                        item {
                            QuickControlTile(
                                title = "Print",
                                subtitle = "Services",
                                icon = Icons.Default.Print,
                                containerColor = NtSurface,
                                iconColor = NtTextSecondary,
                                subtitleColor = NtTextSecondary,
                                modifier = Modifier.width(140.dp),
                                index = 17,
                                onClick = { try { context.startActivity(Intent(Settings.ACTION_PRINT_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (e: Exception) { } },
                                onLongClick = { activeTileSettings = "Print" }
                            )
                        }
                    }
                }
            }

            // PERMISSION BANNER
            if (!hasWriteSettingsPermission) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    BentoCard(
                        title = "PERMISSION",
                        icon = Icons.Default.Warning,
                        tint = NothingRed,
                        onClick = onRequestPermission
                    ) {
                        Text("WRITE_SETTINGS required for Screen Timeout control", style = AppTypography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("GRANT ACCESS")
                        }
                    }
                }
            }

            if (filteredOrder.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No results",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "NO SETTINGS MATCH",
                                style = AppTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Try searching for caffeine, timeout, battery, dns, app audio, or glyph profiles.",
                                style = AppTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredOrder,
                    key = { id -> id },
                    span = { id ->
                        if (id in listOf("CAFFEINE", "BATTERY", "BRIGHTNESS", "THEATER", "FOCUS", "GLYPH", "WEATHER")) StaggeredGridItemSpan.FullLine
                        else StaggeredGridItemSpan.SingleLane
                    }
                ) { id ->
                    val index = remember(id, availableOrder) { availableOrder.indexOf(id) }
                Box(modifier = Modifier.animateItem()) {
                    when (id) {
                        "TIMEOUT" -> {
                            BentoCard(title = "SCREEN TIMEOUT", icon = Icons.Default.Timer) {
                                val pm = com.example.utils.QSPreferenceManager(context)
                                val label = remember(systemTimeout) { pm.formatTimeoutLabel(systemTimeout) }
                                Text(label, style = AppTypography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.cycleScreenTimeout() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("CYCLE →", style = AppTypography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                                        } catch (e: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("DEEP SETTINGS", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "WEATHER" -> {
                            com.example.ui.components.MinimalistPlaceholder(type = "WEATHER")
                        }
                        "CAFFEINE" -> {
                            BentoCard(title = "CAFFEINE", icon = Icons.Default.Coffee) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (isCaffeineActive) "SCREEN ON" else "STANDBY", style = AppTypography.displayLarge)
                                        Text(if (isCaffeineActive) "Wake lock active" else "Tap to activate", style = AppTypography.bodyMedium, color = NeutralGray)
                                    }
                                    Switch(
                                        checked = isCaffeineActive,
                                        onCheckedChange = { viewModel.toggleCaffeine(it) }
                                    )
                                }
                                AnimatedVisibility(visible = isCaffeineActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NothingRed)
                                    )
                                }
                            }
                        }
                        "BATTERY" -> {
                            val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
                            val prediction by viewModel.batteryPrediction.collectAsStateWithLifecycle()
                            val isAnalyzing by viewModel.isAnalyzingBattery.collectAsStateWithLifecycle()
                            
                            if (batteryInfo.percentage <= 0) {
                                com.example.ui.components.MinimalistPlaceholder(type = "BATTERY", message = "ACCUMULATOR TELEMETRY DISCONNECTED")
                            } else {
                                BentoCard(
                                    title = "BATTERY STATISTICS",
                                    icon = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                    onClick = {
                                        viewModel.fetchBatteryPrediction(batteryInfo.percentage, batteryInfo.isCharging)
                                    }
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BatteryGauge(batteryInfo = batteryInfo)
                                        
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.fetchBatteryPrediction(batteryInfo.percentage, batteryInfo.isCharging)
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            if (isAnalyzing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = NothingRed
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = "Recalibrate",
                                                    tint = NothingRed,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "GEMINI INTELLIGENCE PREDICTION",
                                                    style = AppTypography.labelSmall.copy(
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp,
                                                        color = NothingRed
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = prediction,
                                                    style = AppTypography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        lineHeight = 15.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "BRIGHTNESS" -> {
                            BrightnessSlider(
                                hasWriteSettingsPermission = hasWriteSettingsPermission,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "DNS" -> {
                            BentoCard(title = "PRIVATE DNS", icon = Icons.Default.Dns) {
                                Text(if (isDnsActive) "ACTIVE" else "OFF", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                GlyphSwitch(
                                    checked = isDnsActive,
                                    onCheckedChange = { viewModel.togglePrivateDns(it) },
                                    label = "Private DNS"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        com.example.utils.AudioHapticEngine.triggerClick(context)
                                        SystemSettingsHelper.openPrivateDnsSettings(context)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.SettingsInputComponent, contentDescription = "Deep Settings", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DEEP SETTINGS", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "THEATER" -> {
                            BentoCard(title = "THEATER MODE", icon = Icons.Default.Theaters) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (isTheaterActive) "ACTIVE" else "INACTIVE", style = AppTypography.displayLarge)
                                        if (isTheaterActive) Text("DND · DIM · MUTE", style = AppTypography.labelSmall, color = NothingRed)
                                    }
                                    Switch(
                                        checked = isTheaterActive,
                                        onCheckedChange = { viewModel.toggleTheaterMode(it) },
                                        modifier = Modifier.then(
                                            if (isTheaterActive) Modifier.shadow(
                                                elevation = 12.dp,
                                                spotColor = MaterialTheme.colorScheme.onSurface,
                                                ambientColor = MaterialTheme.colorScheme.onSurface,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ) else Modifier
                                        )
                                    )
                                }
                            }
                        }
                        "CLIPBOARD" -> {
                            BentoCard(title = "CLIPBOARD", icon = Icons.Default.ContentPaste) {
                                Text("PURGE", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.purgeClipboard() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear Clipboard", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CLEAR NOW", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "FOCUS" -> {
                            BentoCard(title = "DEEP FOCUS", icon = Icons.Default.Lock) {
                                var selectedDuration by remember { mutableIntStateOf(25) }
                                var showAppSelector by remember { mutableStateOf(false) }
                                val allowedApps by viewModel.focusWhitelist.collectAsStateWithLifecycle()
                                
                                val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                                val usageMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                                } else {
                                    appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                                }
                                val hasUsageStats = usageMode == android.app.AppOpsManager.MODE_ALLOWED
                                val hasOverlay = android.provider.Settings.canDrawOverlays(context)
                                
                                if (!hasUsageStats || !hasOverlay) {
                                    if (!hasUsageStats) {
                                        Button(onClick = {
                                            context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NothingRed)) { Text("GRANT USAGE ACCESS") }
                                    }
                                    if (!hasOverlay) {
                                        Spacer(Modifier.height(8.dp))
                                        Button(onClick = {
                                            context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NothingRed)) { Text("GRANT OVERLAY PERMISSION") }
                                    }
                                } else {
                                    val isDark = MaterialTheme.colorScheme.background != androidx.compose.ui.graphics.Color(0xFFFDF8F6)
                                    var isSessionActive by remember { mutableStateOf(com.example.utils.FocusDataStore.isSandboxActive(context)) }
                                    var remainingSeconds by remember { mutableLongStateOf(0L) }
                                    
                                    LaunchedEffect(isSessionActive) {
                                        if (isSessionActive) {
                                            while (true) {
                                                val endTime = com.example.utils.FocusDataStore.getEndTime(context)
                                                val now = System.currentTimeMillis()
                                                val diff = (endTime - now) / 1000
                                                if (diff <= 0) {
                                                    isSessionActive = false
                                                    break
                                                }
                                                remainingSeconds = diff
                                                kotlinx.coroutines.delay(1000)
                                            }
                                        }
                                    }

                                    if (isSessionActive) {
                                        val mins = remainingSeconds / 60
                                        val secs = remainingSeconds % 60
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Text("SESSION TIME REMAINING", style = AppTypography.labelSmall, color = NothingRed)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = String.format("%02d:%02d", mins, secs),
                                                style = AppTypography.displayLarge.copy(fontSize = 32.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            LinearProgressIndicator(
                                                progress = (remainingSeconds.toFloat() / (selectedDuration * 60f)).coerceIn(0f, 1f),
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                color = NothingRed,
                                                trackColor = BorderDark
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                                    viewModel.stopFocusSandbox()
                                                    isSessionActive = false
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                                            ) {
                                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("STOP FOCUS", style = AppTypography.labelSmall, color = Color.White)
                                            }
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(selectedDuration.toString(), style = AppTypography.displayLarge)
                                            Text(" MIN", style = AppTypography.labelSmall, color = NeutralGray)
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(onClick = { if (selectedDuration > 5) selectedDuration -= 5 }) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                            }
                                            IconButton(onClick = { if (selectedDuration < 120) selectedDuration += 5 }) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val allApps = remember { com.example.ui.loadInstalledApps(context) }
                                        val whitelistedDashboardApps = remember(allowedApps, allApps) {
                                            allApps.filter { it.packageName in allowedApps }
                                        }

                                        if (whitelistedDashboardApps.isNotEmpty()) {
                                            val scrollState = rememberScrollState()
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(bottom = 8.dp)
                                            ) {
                                                whitelistedDashboardApps.forEach { app ->
                                                    coil.compose.AsyncImage(
                                                        model = app.icon,
                                                        contentDescription = app.label,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .border(1.5.dp, androidx.compose.ui.graphics.Color(0xFF10B981), CircleShape)
                                                            .clickable {
                                                                if (isSessionActive) {
                                                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                                    if (launchIntent != null) {
                                                                        try {
                                                                            context.startActivity(launchIntent)
                                                                        } catch (e: Exception) {
                                                                            android.widget.Toast.makeText(context, "Could not launch app", android.widget.Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedButton(onClick = { showAppSelector = true }, modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Default.Apps, contentDescription = "Select Apps", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${allowedApps.size} APPS ALLOWED", style = AppTypography.labelSmall)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                com.example.utils.AudioHapticEngine.triggerClick(context)
                                                viewModel.startFocusSandbox(selectedDuration, allowedApps)
                                                isSessionActive = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) PureWhite else PitchBlack)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Focus", tint = if (isDark) PitchBlack else PureWhite)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("START FOCUS", style = AppTypography.labelSmall, color = if (isDark) PitchBlack else PureWhite)
                                        }
                                    }

                                    if (showAppSelector) {
                                        AppSelectorDialog(
                                            preselectedApps = allowedApps,
                                            onDismiss = { showAppSelector = false },
                                            onConfirm = { apps ->
                                                viewModel.updateFocusWhitelist(apps)
                                                showAppSelector = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        "SHORTCUT" -> {
                            BentoCard(title = "SHORTCUT", icon = Icons.Default.OpenInNew) {
                                Text(customShortcutLabel.uppercase(), style = AppTypography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    customShortcutTarget,
                                    style = AppTypography.labelSmall,
                                    color = NeutralGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { showSettingsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("CONFIGURE", style = AppTypography.labelSmall)
                                }
                            }
                        }
                        "APP_AUDIO" -> {
                            BentoCard(title = "APP AUDIO", icon = Icons.Default.VolumeOff) {
                                Text(if (isAudioIsolated) "ISOLATED" else "NORMAL", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                GlyphSwitch(
                                    checked = isAudioIsolated,
                                    onCheckedChange = { viewModel.toggleAudioIsolation(it) },
                                    label = "Isolate"
                                )
                            }
                        }
                        "GLYPH" -> {
                            BentoCard(title = "GLYPH PROFILE", icon = Icons.Default.Brightness6) {
                                val profiles = listOf("Essential Only", "Full Glyph", "Silent")
                                profiles.forEach { profile ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setGlyphBrightnessProfile(profile) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(profile.uppercase(), style = AppTypography.bodyMedium, modifier = Modifier.weight(1f))
                                        if (glyphProfile == profile) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = NothingRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    if (profile != profiles.last()) {
                                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                        "OPERATOR" -> {
                            BentoCard(
                                title = "NETWORK",
                                icon = Icons.Default.NetworkCell,
                                onClick = {
                                    val intent = Intent("android.settings.NETWORK_OPERATOR_SETTINGS").apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // handle error
                                    }
                                }
                            ) {
                                Text("OPERATOR", style = AppTypography.displayLarge)
                                Text("4G · 5G · LTE", style = AppTypography.labelSmall, color = NeutralGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Chevron Right", tint = NeutralGray, modifier = Modifier.size(16.dp))
                                    Text("Opens carrier settings", style = AppTypography.labelSmall, color = NeutralGray)
                                }
                            }
                        }
                        "MANUAL" -> {
                            BentoCard(
                                title = "USER MANUAL",
                                icon = Icons.Default.HelpOutline,
                                onClick = {
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                    showSpecialManual = true
                                }
                            ) {
                                Text("MANUAL", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("TAP FOR USER MANUAL GUIDE", style = AppTypography.labelSmall, color = NeutralGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp))
                                    Text("Explore instructions", style = AppTypography.labelSmall, color = NeutralGray)
                                }
                            }
                        }
                        "CHANGELOG" -> {
                            BentoCard(
                                title = "CHANGELOGS",
                                icon = Icons.Default.History,
                                onClick = {
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                    showSpecialChangelog = true
                                }
                            ) {
                                Text("UPDATES", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("VERSION 1.2.0 (JUNE 2026)", style = AppTypography.labelSmall, color = NeutralGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp))
                                    Text("Added animations, network widget, settings intents", style = AppTypography.labelSmall, color = NeutralGray)
                                }
                            }
                        }
                        "ABOUT" -> {
                            BentoCard(
                                title = "ABOUT APP",
                                icon = Icons.Default.Info,
                                onClick = {
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                    showSpecialAbout = true
                                }
                            ) {
                                Text("HAVEN", style = AppTypography.displayLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("CREATOR: ASHU MEHTA", style = AppTypography.labelSmall, color = NeutralGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp))
                                    Text("Tap for system metadata", style = AppTypography.labelSmall, color = NeutralGray)
                                }
                            }
                        }
                    }
                    if (isEditing) {
                        var dragDistanceX by remember { mutableFloatStateOf(0f) }
                        var dragDistanceY by remember { mutableFloatStateOf(0f) }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                                .pointerInput(index, availableOrder) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            dragDistanceX = 0f
                                            dragDistanceY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragDistanceX += dragAmount.x
                                            dragDistanceY += dragAmount.y
                                        },
                                        onDragEnd = {
                                            val threshold = 120f
                                            if (dragDistanceX < -threshold || dragDistanceY < -threshold) {
                                                if (index > 0) {
                                                    val newOrder = availableOrder.toMutableList()
                                                    java.util.Collections.swap(newOrder, index, index - 1)
                                                    viewModel.updateTileOrder(newOrder)
                                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                                }
                                            } else if (dragDistanceX > threshold || dragDistanceY > threshold) {
                                                if (index < availableOrder.size - 1) {
                                                    val newOrder = availableOrder.toMutableList()
                                                    java.util.Collections.swap(newOrder, index, index + 1)
                                                    viewModel.updateTileOrder(newOrder)
                                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                                }
                                            }
                                        }
                                    )
                                }
                                .clickable { /* Consume clicks */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = NothingRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "DRAG / SWIPE TO MOVE",
                                    style = AppTypography.labelSmall.copy(fontSize = 9.sp),
                                    color = NeutralGray
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val newOrder = availableOrder.toMutableList()
                                                java.util.Collections.swap(newOrder, index, index - 1)
                                                viewModel.updateTileOrder(newOrder)
                                                com.example.utils.AudioHapticEngine.triggerClick(context)
                                            }
                                        },
                                        enabled = index > 0,
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Move Left/Up")
                                    }
                                    IconButton(
                                        onClick = {
                                            if (index < availableOrder.size - 1) {
                                                val newOrder = availableOrder.toMutableList()
                                                java.util.Collections.swap(newOrder, index, index + 1)
                                                viewModel.updateTileOrder(newOrder)
                                                com.example.utils.AudioHapticEngine.triggerClick(context)
                                            }
                                        },
                                        enabled = index < availableOrder.size - 1,
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = "Move Right/Down")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }


        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 20) {
                            showNotifications = true
                        }
                    }
                }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .zIndex(100f)
        )

        // FLOATING SEARCH BAR AT THE BOTTOM OF THE DASHBOARD
        val isDarkTheme = MaterialTheme.colorScheme.background != Color(0xFFFDF8F6)
        val searchBarBg = if (isDarkTheme) Color(0xFF161616) else Color(0xFFF3ECE9)
        val searchBarBorder = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
        val textCol = if (isDarkTheme) Color.White else Color.Black

        val tileUsageCounts by viewModel.tileUsageCounts.collectAsStateWithLifecycle()
        val searchSuggestions = remember(tileUsageCounts, searchQuery) {
            val allItems = listOf(
                "WIFI" to "Wi-Fi",
                "BLUETOOTH" to "Bluetooth",
                "DATA" to "Data",
                "HOTSPOT" to "Hotspot",
                "TIMEOUT" to "Timeout",
                "CAFFEINE" to "Caffeine",
                "DNS" to "DNS",
                "THEATER" to "Theater",
                "CLIPBOARD" to "Clipboard",
                "FOCUS" to "Focus",
                "SHORTCUT" to "Shortcut",
                "APP_AUDIO" to "App Audio",
                "GLYPH" to "Glyph"
            )
            if (searchQuery.isBlank()) {
                allItems.sortedByDescending { tileUsageCounts[it.first] ?: 0 }.take(3)
            } else {
                allItems.filter { it.second.contains(searchQuery, ignoreCase = true) }
                    .sortedByDescending { tileUsageCounts[it.first] ?: 0 }
                    .take(4)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .zIndex(90f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (searchSuggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUGGESTED:",
                            style = AppTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 8.sp,
                                color = textCol.copy(alpha = 0.5f)
                            )
                        )
                        searchSuggestions.forEach { (id, label) ->
                            val count = tileUsageCounts[id] ?: 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(textCol.copy(alpha = 0.08f))
                                    .border(1.dp, textCol.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.setSearchQuery(label)
                                        com.example.utils.AudioHapticEngine.triggerClick(context)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = label.uppercase(),
                                        style = AppTypography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp,
                                            color = textCol
                                        )
                                    )
                                    if (count > 0) {
                                        Text(
                                            text = "($count)",
                                            style = AppTypography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                color = textCol.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "FILTER SETTINGS TOGGLES...",
                            style = AppTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = textCol.copy(alpha = 0.4f)
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textCol.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            modifier = Modifier.padding(end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.setSearchQuery("")
                                        com.example.utils.AudioHapticEngine.triggerClick(context)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = textCol.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                    showVoiceDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice assistant",
                                    tint = textCol.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = searchBarBg,
                        unfocusedContainerColor = searchBarBg,
                        focusedBorderColor = textCol.copy(alpha = 0.4f),
                        unfocusedBorderColor = searchBarBorder,
                        focusedTextColor = textCol,
                        unfocusedTextColor = textCol
                    ),
                    textStyle = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

@Composable
fun SoundWaveVisualizer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "SoundWave")
    val pulseOffsets: List<State<Float>> = List(10) { index ->
        transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 350 + (index * 90),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Bar_$index"
        )
    }

    Row(
        modifier = modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pulseOffsets.forEach { heightScale ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightScale.value)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(NothingRed)
            )
        }
    }
}

    if (showVoiceDialog) {
        var voiceInputText by remember { mutableStateOf("") }
        var isListeningState by remember { mutableStateOf(false) }
        var aiResponseFeedback by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = NothingRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "OS VOICE CONTROLLER",
                        style = AppTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Command Nothing OS with natural voice suggestions.",
                        style = AppTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = { voiceInputText = it },
                        placeholder = {
                            Text(
                                "e.g. 'Turn caffeine on and disable mobile data'",
                                style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = AppTypography.bodyMedium,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (isListeningState) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SoundWaveVisualizer()
                            Text(
                                "REC • TRANSCEIVING AUDIO SPECTRUM...",
                                style = AppTypography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = NothingRed
                                )
                            )
                        }
                    }
                    
                    if (aiResponseFeedback.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingRed.copy(alpha = 0.1f))
                                .border(1.dp, NothingRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = aiResponseFeedback,
                                style = AppTypography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp,
                                    color = NothingRed
                                )
                            )
                        }
                    }
                    
                    Text(
                        "SUGGESTIONS:",
                        style = AppTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp
                        )
                    )
                    
                    listOf(
                        "Turn on bluetooth",
                        "Deactivate mobile data",
                        "Activate caffeine",
                        "Start focus sandbox for 45 minutes",
                        "Cycle weather selection",
                        "Turn on theater mode",
                        "Set screen monochrome"
                    ).forEach { phrase ->
                        Text(
                            text = "• \"$phrase\"",
                            style = AppTypography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    voiceInputText = phrase
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (voiceInputText.isNotBlank()) {
                            isListeningState = true
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            viewModel.executeVoiceCommand(voiceInputText) { feedback ->
                                isListeningState = false
                                aiResponseFeedback = feedback
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "COMPILE COMMAND",
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showVoiceDialog = false
                    }
                ) {
                    Text(
                        "CLOSE",
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showNotifications) {
        NotificationsOverlay(onDismiss = { showNotifications = false })
    }

    if (showSystemMonitor) {
        SystemDiagnosticsOverlay(onDismiss = { showSystemMonitor = false })
    }

    if (showSpecialAbout) {
        AboutOverlay(onDismiss = { showSpecialAbout = false })
    }

    if (showSpecialChangelog) {
        ChangelogOverlay(onDismiss = { showSpecialChangelog = false })
    }

    if (showSpecialManual) {
        UserManualOverlay(onDismiss = { showSpecialManual = false })
    }

    if (showSpecialPaletteSelector) {
        val selectedPalette by viewModel.selectedPalette.collectAsStateWithLifecycle()
        ThemePalette(
            currentPalette = selectedPalette,
            onPaletteSelected = { id ->
                viewModel.setSelectedPalette(id)
                showSpecialPaletteSelector = false
            },
            onDismiss = { showSpecialPaletteSelector = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onResetLayout = { viewModel.resetTileOrder() },
            onConfirm = { viewModel.checkAllStates() }
        )
    }

    if (showAuthModal) {
        com.example.ui.components.AuthModal(
            viewModel = viewModel,
            onDismiss = { showAuthModal = false }
        )
    }

    activeTileSettings?.let { tile -> 
        com.example.ui.components.TileSettingsModal(tile = tile, onDismiss = { activeTileSettings = null })
    }

    if (!isBooted) {
        com.example.ui.components.DotMatrixBootScreen(
            onComplete = { isBooted = true }
        )
    }
}
