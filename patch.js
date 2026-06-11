const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/DashboardScreen.kt', 'utf8');

const startTag = '            // HAVEN BRANDING HEADER\n';
const endTag = '            // RECENTLY USED SECTION';

let startIdx = code.indexOf(startTag);
let endIdx = code.indexOf(endTag);

if (startIdx === -1 || endIdx === -1) {
    console.error("Tags not found!");
    process.exit(1);
}

const replacement = `            // COMBINED TOP HEADER CARD
            item {
                var currentTime by remember { mutableStateOf(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }
                var currentDate by remember { mutableStateOf(java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())) }
                var networkType by remember { mutableStateOf("4G LTE") }
                var signalStrength by remember { mutableIntStateOf(4) }

                LaunchedEffect(Unit) {
                    while(true) {
                        currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        currentDate = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())
                        signalStrength = listOf(2, 3, 4).random()
                        networkType = listOf("5G", "4G LTE", "Wi-Fi").random()
                        kotlinx.coroutines.delay(5000)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GlowingLogo(size = 32.dp, modifier = Modifier.padding(end = 10.dp), dotColor = MaterialTheme.colorScheme.primary, glowing = true)
                                Column {
                                    Text("HAVEN", style = AppTypography.displayLarge, fontFamily = FontFamily.SansSerif, color = MaterialTheme.colorScheme.onBackground)
                                    Text("by ashu mehta", style = AppTypography.labelSmall, color = NtSecondary, letterSpacing = 1.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when(signalStrength) {
                                            4 -> Icons.Default.SignalCellular4Bar
                                            3 -> Icons.Default.NetworkCell
                                            else -> Icons.Default.NetworkCell
                                        },
                                        contentDescription = "Signal Strength",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(networkType, style = AppTypography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                                }
                                Text(currentTime, style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onBackground)
                                Text(currentDate.uppercase(), style = AppTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val streak by viewModel.focusStreak.collectAsStateWithLifecycle()
                        if (streak > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NothingRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Whatshot, contentDescription = "Streak", tint = NothingRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$streak Day Streak", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NothingRed)
                            }
                            Spacer(Modifier.height(14.dp))
                        } else {
                            Spacer(Modifier.height(2.dp))
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(Modifier.height(12.dp))

                        val isMonochrome by viewModel.isMonochrome.collectAsStateWithLifecycle()
                        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                        val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                        val isDark = when (themeMode) { "DARK" -> true; "LIGHT" -> false; else -> systemDark }
                        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                        var showHelpDialog by remember { mutableStateOf(false) }

                        if (showHelpDialog) {
                            com.example.ui.QuickAddGuideDialog(onDismiss = { showHelpDialog = false })
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .androidx.compose.foundation.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderActionButton(icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle Theme", tooltipText = if (isDark) "Light Mode" else "Dark Mode", onClick = { viewModel.setThemeMode(if (isDark) "LIGHT" else "DARK") })
                            HeaderActionButton(icon = Icons.Default.Palette, contentDescription = "Toggle Theme Selection", tooltipText = "Theme Palette", onClick = { showSpecialPaletteSelector = true })
                            HeaderActionButton(icon = if (gridLayoutColumns == 2) Icons.Default.ViewAgenda else Icons.Default.GridView, contentDescription = "Toggle Grid Layout", tooltipText = if (gridLayoutColumns == 2) "List Layout" else "Grid Layout", onClick = { viewModel.setGridLayoutColumns(if (gridLayoutColumns == 2) 1 else 2) })
                            HeaderActionButton(icon = if (isEditing) Icons.Default.Done else Icons.Default.Edit, contentDescription = "Edit Layout", isActive = isEditing, tooltipText = if (isEditing) "Finish Editing" else "Edit Layout", onClick = { isEditing = !isEditing })
                            HeaderActionButton(icon = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.ManageAccounts, contentDescription = "User Profile", isActive = currentUser != null, tooltipText = if (currentUser != null) "Profile: \${currentUser!!.nickname}" else "Sign In & Auth", onClick = { showAuthModal = true })
                            HeaderActionButton(icon = Icons.Default.Settings, contentDescription = "Settings", tooltipText = "App Settings", onClick = { onNavigateToSettings() })
                            HeaderActionButton(icon = Icons.Default.Bolt, contentDescription = "Automations", tooltipText = "Macro Rules", onClick = { onNavigateToAutomation() })
                            HeaderActionButton(icon = Icons.AutoMirrored.Filled.Assignment, contentDescription = "Clipboard Manager", tooltipText = "Clipboard", onClick = { onNavigateToClipboard() })
                            HeaderActionButton(icon = Icons.Default.Sensors, contentDescription = "Sensor Dashboard", tooltipText = "Sensors", onClick = { onNavigateToSensors() })
                            HeaderActionButton(icon = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Quick Add Guide", tooltipText = "How to add Quick Tiles", onClick = { showHelpDialog = true })
                        }
                    }
                }
            }

            // DEVICE STATUS ROW
            item {
                var batteryPct by remember { mutableFloatStateOf(0f) }
                var batteryTemp by remember { mutableFloatStateOf(0f) }
                var batteryVoltage by remember { mutableIntStateOf(0) }
                var batteryHealth by remember { mutableStateOf("Unknown") }
                var isCharging by remember { mutableStateOf(false) }
                var timeToFull by remember { mutableLongStateOf(-1L) }
                var chargeRateW by remember { mutableFloatStateOf(0f) }
                var availRam by remember { mutableLongStateOf(0L) }
                var totalRam by remember { mutableLongStateOf(0L) }
                var totalSpace by remember { mutableLongStateOf(1L) }
                var freeSpace by remember { mutableLongStateOf(1L) }
                var expandedChip by remember { mutableStateOf<String?>(null) } // "BATTERY", "RAM", "STORAGE", or null

                LaunchedEffect(Unit) {
                    val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
                    while (true) {
                        val intent = context.registerReceiver(null, filter)
                        if (intent != null) {
                            val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                            val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                            if (level != -1 && scale != -1) batteryPct = level * 100 / scale.toFloat()
                            batteryTemp = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                            batteryVoltage = intent.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0)
                            
                            val healthInt = intent.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, 0)
                            batteryHealth = when (healthInt) {
                                android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                                android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                                android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                                android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                                android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                                android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                                else -> "Unknown"
                            }
                            val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                            isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os.BatteryManager.BATTERY_STATUS_FULL
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                val bm = context.getSystemService(android.os.BatteryManager::class.java)
                                timeToFull = bm.computeChargeTimeRemaining()
                            }
                            
                            val bm = context.getSystemService(android.os.BatteryManager::class.java)
                            val currentMicroAmps = bm.getLongProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                            if (currentMicroAmps > 0) {
                                chargeRateW = (batteryVoltage.toFloat() * currentMicroAmps.toFloat()) / 1_000_000_000f
                            } else {
                                chargeRateW = 0f
                            }
                        }

                        try {
                            val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                            val memoryInfo = android.app.ActivityManager.MemoryInfo()
                            am.getMemoryInfo(memoryInfo)
                            availRam = memoryInfo.availMem / (1024 * 1024)
                            totalRam = memoryInfo.totalMem / (1024 * 1024)
                        } catch (e: Exception) {}

                        try {
                            val stat = android.os.StatFs(android.os.Environment.getExternalStorageDirectory().path)
                            totalSpace = stat.totalBytes / (1024 * 1024 * 1024)
                            freeSpace = stat.availableBytes / (1024 * 1024 * 1024)
                        } catch (e: Exception) {}

                        kotlinx.coroutines.delay(10000)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DeviceStatusChip(label = "BATTERY", value = "\${batteryPct.toInt()}%", icon = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd, isExpanded = expandedChip == "BATTERY", modifier = Modifier.weight(1f), onClick = { expandedChip = if (expandedChip == "BATTERY") null else "BATTERY" })
                            DeviceStatusChip(label = "RAM", value = "\${availRam}M", icon = Icons.Default.Memory, isExpanded = expandedChip == "RAM", modifier = Modifier.weight(1f), onClick = { expandedChip = if (expandedChip == "RAM") null else "RAM" })
                            DeviceStatusChip(label = "STORAGE", value = "\${freeSpace}G", icon = Icons.Default.Storage, isExpanded = expandedChip == "STORAGE", modifier = Modifier.weight(1f), onClick = { expandedChip = if (expandedChip == "STORAGE") null else "STORAGE" })
                        }

                        AnimatedVisibility(visible = expandedChip != null) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(Modifier.height(12.dp))
                                when (expandedChip) {
                                    "BATTERY" -> {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column { Text("Level", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)); Text("\${batteryPct.toInt()}%", style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                                            Column { Text("Temp", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)); Text("\${batteryTemp}°C", style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                                            Column { Text("Voltage", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)); Text("\${batteryVoltage}mV", style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                                            Column { Text("Health", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)); Text(batteryHealth, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                                        }
                                        if (isCharging) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Bolt, contentDescription = "Charging", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (timeToFull > 0) "Est \${timeToFull / 60000} mins to full • \${String.format(\"%.1f\", chargeRateW)}W" else "Charging • \${String.format(\"%.1f\", chargeRateW)}W", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                }
                                            }
                                        }
                                    }
                                    "RAM" -> {
                                        Text("Available RAM: \${availRam}MB / \${totalRam}MB", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        Spacer(Modifier.height(8.dp))
                                        LinearProgressIndicator(progress = { if (totalRam > 0) 1f - (availRam.toFloat() / totalRam.toFloat()) else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = NothingRed, trackColor = MaterialTheme.colorScheme.surface, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    }
                                    "STORAGE" -> {
                                        Text("Free Space: \${freeSpace}GB / \${totalSpace}GB", style = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        Spacer(Modifier.height(8.dp))
                                        LinearProgressIndicator(progress = { if (totalSpace > 0) 1f - (freeSpace.toFloat() / totalSpace.toFloat()) else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surface, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    }
                                }
                            }
                        }
                    }
                }
            }

`;

code = code.slice(0, startIdx) + replacement + code.slice(endIdx);

const bottomCode = `    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()

    updateInfo?.let { info ->
        com.example.ui.components.UpdateAvailableDialog(
            updateInfo = info,
            onDownload = { viewModel.downloadAndInstallUpdate(context) },
            onDismiss = { viewModel.dismissUpdateInfo() }
        )
    }

`;

code = code.replace(/    if \(showAuthModal\) {/, bottomCode + '    if (showAuthModal) {');

const deviceStatusChipCode = `
@Composable
private fun DeviceStatusChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isExpanded) NothingRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isExpanded) 1.dp else 0.5.dp,
            color = if (isExpanded) NothingRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = if (isExpanded) NothingRed else NeutralGray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = AppTypography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 0.5.sp), color = NeutralGray)
        }
    }
}
`;

code = code + deviceStatusChipCode;
fs.writeFileSync('app/src/main/java/com/example/ui/DashboardScreen.kt', code);
