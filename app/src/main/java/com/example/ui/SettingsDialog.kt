package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.AppTypography
import com.example.ui.theme.BorderDark
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(onDismiss: () -> Unit, onResetLayout: () -> Unit = {}, onConfirm: () -> Unit = {}) {
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val scope = rememberCoroutineScope()

    var tempCaffeineDuration by remember { mutableIntStateOf(30) }
    var theaterBrightness by remember { mutableIntStateOf(5) }
    var theaterSystemAudio by remember { mutableIntStateOf(50) }
    var theaterAppAudio by remember { mutableIntStateOf(30) }
    var tempDnd by remember { mutableStateOf(true) }
    var clipboardInterval by remember { mutableIntStateOf(0) }
    var tempDns by remember { mutableStateOf("") }
    var tempPalette by remember { mutableStateOf("NATURAL") }
    var tempThemeMode by remember { mutableStateOf("SYSTEM") }
    
    // Add custom shortcut stuff that is supposed to be here as per step 7 
    var tempShortcutLabel by remember { mutableStateOf("") }
    var tempShortcutTarget by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        tempCaffeineDuration = dataStore.caffeineDurationFlow.first().let { if(it<0) 30 else it }
        theaterBrightness = dataStore.theaterBrightnessFlow.first()
        theaterSystemAudio = dataStore.theaterSystemAudioFlow.first()
        theaterAppAudio = dataStore.theaterAppAudioFlow.first()
        tempDnd = dataStore.theaterDndFlow.first()
        clipboardInterval = dataStore.clipboardIntervalFlow.first()
        tempDns = dataStore.privateDnsFlow.first().let { if(it=="off") "" else it }
        tempPalette = dataStore.selectedPaletteFlow.first()
        tempThemeMode = dataStore.themeModeFlow.first()
        
        val pm = com.example.utils.QSPreferenceManager(context)
        tempShortcutLabel = pm.getCustomShortcutLabel()
        tempShortcutTarget = pm.getCustomShortcutTarget()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "SETTINGS",
                style = AppTypography.labelSmall,
                letterSpacing = 3.sp
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: CAFFEINE
                Text("CAFFEINE", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                GlyphSlider(
                    value = tempCaffeineDuration.toFloat(),
                    onValueChange = { tempCaffeineDuration = it.toInt() },
                    valueRange = 1f..120f,
                    label = "Duration",
                    valueDisplay = "$tempCaffeineDuration min"
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: THEATER MODE
                Text("THEATER MODE", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                GlyphSlider(
                    value = theaterBrightness.toFloat(),
                    onValueChange = { theaterBrightness = it.toInt() },
                    valueRange = 0f..10f,
                    label = "Brightness",
                    valueDisplay = "$theaterBrightness/10"
                )
                GlyphSlider(
                    value = theaterSystemAudio.toFloat(),
                    onValueChange = { theaterSystemAudio = it.toInt() },
                    valueRange = 0f..100f,
                    label = "System Volume",
                    valueDisplay = "$theaterSystemAudio%"
                )
                GlyphSlider(
                    value = theaterAppAudio.toFloat(),
                    onValueChange = { theaterAppAudio = it.toInt() },
                    valueRange = 0f..100f,
                    label = "App Volume",
                    valueDisplay = "$theaterAppAudio%"
                )
                GlyphSwitch(
                    checked = tempDnd,
                    onCheckedChange = { tempDnd = it },
                    label = "Enable DND"
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: CLIPBOARD
                Text("CLIPBOARD", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                GlyphSlider(
                    value = clipboardInterval.toFloat(),
                    onValueChange = { clipboardInterval = it.toInt() },
                    valueRange = 0f..60f,
                    label = "Auto-Purge",
                    valueDisplay = if (clipboardInterval == 0) "Off" else "$clipboardInterval min"
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 4: PRIVATE DNS
                Text("PRIVATE DNS", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempDns,
                    onValueChange = { tempDns = it },
                    label = { Text("DNS Hostname") },
                    placeholder = { Text("dns.google") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = BorderDark,
                        cursorColor = NothingRed
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 5: CUSTOM SHORTCUT
                Text("CUSTOM SHORTCUT", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempShortcutLabel,
                    onValueChange = { tempShortcutLabel = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempShortcutTarget,
                    onValueChange = { tempShortcutTarget = it },
                    label = { Text("Intent / Settings action") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 6: LAYOUT
                Text("LAYOUT", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onResetLayout()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed)
                ) {
                    Text("RESET TILE LAYOUT", style = AppTypography.labelSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 6.5: DARK THEME MODE
                Text("DARK THEME MODE", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 7: THEMING
                Text("COLOR PALETTE", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(12.dp))
                
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 8: HELP & ABOUT
                Text("ABOUT", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Haven", style = AppTypography.bodyMedium)
                Text("Version 1.0.0", style = AppTypography.labelSmall, color = NeutralGray)
                Text("Creator: Ashu Mehta", style = AppTypography.labelSmall, color = NeutralGray)
                Text("Last updated: June 2026", style = AppTypography.labelSmall, color = NeutralGray)

                Spacer(modifier = Modifier.height(16.dp))
                Text("CHANGELOG", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Added ARIA labels to Quick Settings tiles\n• Added Nothing Phone inspired active glow effects\n• Re-designed abstract logo\n• Added Monochrome Color Palette\n• Added About and Manual", style = AppTypography.labelSmall, color = NeutralGray)

                Spacer(modifier = Modifier.height(16.dp))
                Text("USER MANUAL", style = AppTypography.labelSmall, color = NeutralGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Use the Dashboard to quickly access device toggles. Drag tiles in Edit mode to rearrange. Use Focus mode to restrict apps.", style = AppTypography.labelSmall, color = NeutralGray)
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        dataStore.setCaffeineDuration(tempCaffeineDuration)
                        dataStore.setTheaterBrightness(theaterBrightness)
                        dataStore.setTheaterSystemAudio(theaterSystemAudio)
                        dataStore.setTheaterAppAudio(theaterAppAudio)
                        dataStore.setTheaterDnd(tempDnd)
                        dataStore.setClipboardInterval(clipboardInterval)
                        if (tempDns.isBlank()) {
                            dataStore.setPrivateDns("off")
                        } else {
                            dataStore.setPrivateDns(tempDns)
                        }
                        dataStore.setSelectedPalette(tempPalette)
                        dataStore.setMonochrome(tempPalette != "NATURAL")
                        dataStore.setThemeMode(tempThemeMode)
                        
                        val pm = com.example.utils.QSPreferenceManager(context)
                        pm.setCustomShortcutLabel(tempShortcutLabel)
                        pm.setCustomShortcutTarget(tempShortcutTarget)
                        onConfirm()
                        onDismiss()
                    }
                }
            ) {
                Text("APPLY")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
