package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlyphSlider
import com.example.ui.components.GlyphSwitch
import com.example.ui.theme.*
import com.example.utils.SettingsDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.QSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QSViewModel,
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToChangelog: () -> Unit = {},
    onResetLayout: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialCaffeine by viewModel.caffeineDuration.collectAsStateWithLifecycle()
    val initialBrightness by viewModel.theaterBrightness.collectAsStateWithLifecycle()
    val initialSysAudio by viewModel.theaterSystemAudio.collectAsStateWithLifecycle()
    val initialAppAudio by viewModel.theaterAppAudio.collectAsStateWithLifecycle()
    val initialDnd by viewModel.theaterDnd.collectAsStateWithLifecycle()
    val initialClipInterval by viewModel.clipboardInterval.collectAsStateWithLifecycle()
    val initialDns by viewModel.privateDns.collectAsStateWithLifecycle()
    val initialPalette by viewModel.selectedPalette.collectAsStateWithLifecycle()
    val initialThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val initialShortcutLabel by viewModel.customShortcutLabel.collectAsStateWithLifecycle()
    val initialShortcutTarget by viewModel.customShortcutTarget.collectAsStateWithLifecycle()

    var tempCaffeineDuration by remember { mutableIntStateOf(30) }
    var theaterBrightness by remember { mutableIntStateOf(5) }
    var theaterSystemAudio by remember { mutableIntStateOf(50) }
    var theaterAppAudio by remember { mutableIntStateOf(30) }
    var tempDnd by remember { mutableStateOf(true) }
    var clipboardInterval by remember { mutableIntStateOf(0) }
    var tempDns by remember { mutableStateOf("") }
    var tempPalette by remember { mutableStateOf("NATURAL") }
    var tempThemeMode by remember { mutableStateOf("SYSTEM") }
    
    var tempShortcutLabel by remember { mutableStateOf("") }
    var tempShortcutTarget by remember { mutableStateOf("") }

    LaunchedEffect(
        initialCaffeine, initialBrightness, initialSysAudio, initialAppAudio, initialDnd,
        initialClipInterval, initialDns, initialPalette, initialThemeMode, 
        initialShortcutLabel, initialShortcutTarget
    ) {
        tempCaffeineDuration = initialCaffeine.let { if(it<0) 30 else it }
        theaterBrightness = initialBrightness
        theaterSystemAudio = initialSysAudio
        theaterAppAudio = initialAppAudio
        tempDnd = initialDnd
        clipboardInterval = initialClipInterval
        tempDns = initialDns.let { if(it=="off") "" else it }
        tempPalette = initialPalette
        tempThemeMode = initialThemeMode
        tempShortcutLabel = initialShortcutLabel
        tempShortcutTarget = initialShortcutTarget
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        style = AppTypography.labelSmall.copy(fontSize = 14.sp, letterSpacing = 3.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            onBack()
                        },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", style = AppTypography.labelSmall)
                    }
                    Button(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            viewModel.submitSettings(
                                caffeineDur = tempCaffeineDuration,
                                tBrightness = theaterBrightness,
                                tSystemAudio = theaterSystemAudio,
                                tAppAudio = theaterAppAudio,
                                tDnd = tempDnd,
                                clipInterval = clipboardInterval,
                                pDns = tempDns,
                                palette = tempPalette,
                                tMode = tempThemeMode,
                                sLabel = tempShortcutLabel,
                                sTarget = tempShortcutTarget
                            )
                            onConfirm()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE & APPLY", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // HAVEN LOGO HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HAVEN",
                    style = AppTypography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        letterSpacing = 8.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SETTINGS & CONFIGURATION",
                    style = AppTypography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 2.sp),
                    color = NeutralGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: CAFFEINE
            SettingsCard(title = "CAFFEINE KEEPALIVE") {
                GlyphSlider(
                    value = tempCaffeineDuration.toFloat(),
                    onValueChange = { tempCaffeineDuration = it.toInt() },
                    valueRange = 1f..120f,
                    label = "Duration",
                    valueDisplay = "$tempCaffeineDuration min"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: THEATER MODE
            SettingsCard(title = "THEATER MODE DEFAULT PRESETS") {
                GlyphSlider(
                    value = theaterBrightness.toFloat(),
                    onValueChange = { theaterBrightness = it.toInt() },
                    valueRange = 1f..10f,
                    label = "Brightness Level",
                    valueDisplay = "${theaterBrightness * 10}%"
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlyphSlider(
                    value = theaterSystemAudio.toFloat(),
                    onValueChange = { theaterSystemAudio = it.toInt() },
                    valueRange = 0f..100f,
                    label = "System Speaker Volume",
                    valueDisplay = "$theaterSystemAudio%"
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlyphSlider(
                    value = theaterAppAudio.toFloat(),
                    onValueChange = { theaterAppAudio = it.toInt() },
                    valueRange = 0f..100f,
                    label = "Media Playback Volume",
                    valueDisplay = "$theaterAppAudio%"
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlyphSwitch(
                    checked = tempDnd,
                    onCheckedChange = { tempDnd = it },
                    label = "Force Mute All Notifications (DND)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: SECURITY & SHORTCUTS
            SettingsCard(title = "SECURITY & SHORTCUTS") {
                Text("Clipboard Auto-Purging", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 30, 60, 300).forEach { sec ->
                        val label = when(sec) {
                            0 -> "OFF"
                            30 -> "30s"
                            60 -> "1m"
                            else -> "5m"
                        }
                        val isSelected = (clipboardInterval == sec)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NothingRed else BorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    clipboardInterval = sec 
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isSelected) NothingRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Custom Quick Launch Action", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempShortcutLabel,
                    onValueChange = { tempShortcutLabel = it },
                    label = { Text("Display Label") },
                    textStyle = AppTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = NothingRed
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempShortcutTarget,
                    onValueChange = { tempShortcutTarget = it },
                    label = { Text("Android Intent Package/Action") },
                    textStyle = AppTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = NothingRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 5: NETWORKING & PROTOCOLS
            SettingsCard(title = "NETWORKING & PROTOCOLS") {
                Text("Private DNS Provider", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempDns,
                    onValueChange = { tempDns = it },
                    placeholder = { Text("dns.google or dns.adguard.com") },
                    textStyle = AppTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = BorderDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 6.5: DARK THEME MODE
            SettingsCard(title = "DARK THEME SYSTEM OVERRIDE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                        val isSelected = (tempThemeMode == mode)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NothingRed else BorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    color = if (isSelected) NothingRed.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    tempThemeMode = mode
                                    com.example.utils.AudioHapticEngine.triggerClick(context)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isSelected) NothingRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 7: THEMING
            SettingsCard(title = "NOTHING COLOR SCHEME PALETTE") {
                val paletteList = listOf(
                    Triple("NATURAL", "Cream", androidx.compose.ui.graphics.Color(0xFFFDF8F6)),
                    Triple("MONOCHROME", "Mono", androidx.compose.ui.graphics.Color(0xFF1C1C1C)),
                    Triple("NEON", "Neon", androidx.compose.ui.graphics.Color(0xFF39FF14)),
                    Triple("AMBER", "Amber", androidx.compose.ui.graphics.Color(0xFFFFB300)),
                    Triple("FOREST", "Forest", androidx.compose.ui.graphics.Color(0xFF81C784)),
                    Triple("OCEAN", "Ocean", androidx.compose.ui.graphics.Color(0xFF4FC3F7))
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paletteList.chunked(3).forEach { rowList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowList.forEach { (id, label, previewColor) ->
                                val isSelected = (tempPalette == id)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            tempPalette = id
                                            com.example.utils.AudioHapticEngine.triggerClick(context)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = previewColor,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) NothingRed else BorderDark,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (id == "NATURAL" || id == "AMBER" || id == "FOREST" || id == "OCEAN" || id == "NEON") androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                                        color = if (isSelected) NothingRed else NeutralGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 8: ABOUT
            val appVersion = remember { com.example.utils.VersionManager.getAppVersion(context).first }
            val hasDiscrepancy = remember { com.example.utils.VersionManager.checkVersionDiscrepancy(context) }
            
            SettingsCard(title = "ABOUT RELEASES") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Software Version", style = AppTypography.labelSmall, color = NeutralGray)
                    Text(
                        appVersion, 
                        style = AppTypography.labelSmall, 
                        color = if (hasDiscrepancy) com.example.ui.theme.NothingRed else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (hasDiscrepancy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version mismatch detected between metadata.json (v${com.example.BuildConfig.METADATA_VERSION}) and changelog.json ($appVersion).",
                        style = AppTypography.bodySmall,
                        color = com.example.ui.theme.NothingRed
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            onNavigateToChangelog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VIEW CHANGELOG", style = AppTypography.labelSmall)
                    }
                    Button(
                        onClick = {
                            com.example.utils.AudioHapticEngine.triggerClick(context)
                            com.example.utils.GitHubUpdater.checkForUpdates(context, downloadIfAvailable = true, notifyUpToDate = true)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CHECK UPDATE", style = AppTypography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PERMISSIONS
            Button(
                onClick = { onNavigateToPermissions() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PERMISSION CENTRE", style = AppTypography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESET LAYOUT
            var showResetDialog by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    com.example.utils.AudioHapticEngine.triggerClick(context)
                    showResetDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RESET TO DEFAULT GRID LAYOUT", style = AppTypography.labelSmall)
            }

            if (showResetDialog) {
                com.example.ui.components.ConfirmationDialog(
                    title = "Reset Layout",
                    message = "Are you sure you want to reset the quick toggles layout to default?",
                    confirmText = "RESET",
                    isDestructive = true,
                    onConfirm = {
                        onResetLayout()
                    },
                    onDismiss = {
                        showResetDialog = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.labelSmall.copy(fontSize = 11.sp),
                color = NothingRed,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
