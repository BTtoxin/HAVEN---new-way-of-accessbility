package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HavenCard
import com.example.ui.components.HavenPillButton
import com.example.ui.components.HavenSectionHeader
import com.example.ui.theme.AppTypography
import com.example.ui.theme.HavenCyan
import com.example.viewmodel.QSViewModel

@Composable
fun ProfileTab(
    viewModel: QSViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToChangelog: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showAuthModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text("Profile", style = AppTypography.displayMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
            }
        }

        item {
            Box(Modifier.padding(horizontal = 20.dp)) {
                HavenCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(HavenCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUser != null) {
                                val color = try { android.graphics.Color.parseColor(currentUser!!.avatarColorHex) } catch(e: Exception) { android.graphics.Color.GRAY }
                                Box(modifier = Modifier.fillMaxSize().background(Color(color)))
                                Text(currentUser!!.nickname.take(1).uppercase(), style = AppTypography.titleLarge, color = Color.White)
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HavenCyan, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(currentUser?.nickname ?: "Guest User", style = AppTypography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                            Text(if (currentUser != null) "Signed in" else "Tap to sign in", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (currentUser == null) {
                        HavenPillButton(text = "Sign In or Register", modifier = Modifier.fillMaxWidth(), onClick = { showAuthModal = true })
                    } else {
                        OutlinedButton(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50.dp)) {
                            Text("Sign Out", style = AppTypography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            HavenSectionHeader("Your Week")
            Box(Modifier.padding(horizontal = 20.dp)) {
                HavenCard {
                    // Simple placeholder for the chart
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Mood history will appear here", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = HavenCyan)
                            Spacer(Modifier.width(12.dp))
                            Text("\"Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure.\"", style = AppTypography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            HavenSectionHeader("Settings & Preferences")
            Column(Modifier.padding(horizontal = 20.dp)) {
                val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
                if (isCheckingUpdate) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), color = HavenCyan)
                }
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val settingsItems = listOf(
                    Triple(Icons.Default.Settings, "Settings", onNavigateToSettings),
                    Triple(Icons.Default.Palette, "Theme", {}),
                    Triple(Icons.Default.Language, "Language", {}),
                    Triple(Icons.Default.Notifications, "Notifications", {}),
                    Triple(Icons.Default.Lock, "Permissions", onNavigateToPermissions),
                    Triple(Icons.Default.History, "Changelog", onNavigateToChangelog),
                    Triple(Icons.Default.SystemUpdate, "Check for Updates", { viewModel.checkForUpdatesExplicit(context) }),
                    Triple(Icons.Default.Help, "FAQ / Manual", {}),
                    Triple(Icons.Default.Info, "About", {})
                )

                settingsItems.forEach { (icon, title, action) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = action)
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(HavenCyan.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = HavenCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(title, style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = com.example.ui.theme.HavenBorder)
                }
            }
        }
    }

    if (showAuthModal) {
        com.example.ui.components.AuthModal(
            viewModel = viewModel,
            onDismiss = { showAuthModal = false }
        )
    }
}
