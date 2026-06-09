package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.ui.theme.*
import com.example.viewmodel.QSViewModel

@Composable
fun AuthModal(
    viewModel: QSViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var showRegisterScreen by remember { mutableStateOf(false) }
    var selectedUserForLogin by remember { mutableStateOf<UserEntity?>(null) }

    FullSheetOverlay(
        title = if (currentUser != null) "User Profile" else "Account & Auth",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            if (currentUser != null) {
                // Logged In Status Screen
                LoggedInUserContent(
                    user = currentUser!!,
                    onLogout = {
                        viewModel.logout()
                    },
                    onDismiss = onDismiss
                )
            } else if (showRegisterScreen) {
                // Registration Screen
                RegisterForm(
                    onRegister = { username, pin, nickname, color ->
                        viewModel.register(username, pin, nickname, color) {
                            showRegisterScreen = false
                        }
                    },
                    onBack = {
                        showRegisterScreen = false
                    },
                    authError = authError
                )
            } else if (selectedUserForLogin != null) {
                // PIN Prompt Screen for selected user
                PinVerificationScreen(
                    user = selectedUserForLogin!!,
                    onVerify = { pin ->
                        viewModel.login(selectedUserForLogin!!.username, pin) {
                            selectedUserForLogin = null
                            onDismiss()
                        }
                    },
                    onBack = {
                        selectedUserForLogin = null
                    },
                    authError = authError
                )
            } else {
                // Main Entrance Screen (Account Switcher or Guest Panel)
                AuthLandingPage(
                    allUsers = allUsers,
                    onSelectUser = { selectedUserForLogin = it },
                    onRegisterClick = { showRegisterScreen = true },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun ColumnScope.LoggedInUserContent(
    user: UserEntity,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(user.avatarColorHex))
    } catch (e: Exception) {
        Color(0xFFFF5722)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(parsedColor, CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (user.nickname.firstOrNull() ?: '?').uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.nickname,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Synchronized SQLite Storage",
                        style = AppTypography.headlineSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your custom quick tile ordering, active colors, theme style, and Private DNS options are bound securely under your local account.",
                        style = AppTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Options
        Button(
            onClick = {
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Profile / Log Out", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Text("Keep Exploring", style = AppTypography.bodySmall)
        }
    }
}

@Composable
fun ColumnScope.AuthLandingPage(
    allUsers: List<UserEntity>,
    onSelectUser: (UserEntity) -> Unit,
    onRegisterClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        // Header Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GUEST PREFERENCE MODE",
                    style = AppTypography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.5.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Welcome to your temporary Guest Dashboard. All settings choices are saved, but logging in lets you create personalized profiles with isolated setting layouts.",
                    style = AppTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SWITCH PROFILE / LOG IN",
            style = AppTypography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (allUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, BorderDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No profiles found yet",
                        style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create the first profile containing your personalized config!",
                        style = AppTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // User List / Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allUsers) { user ->
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(user.avatarColorHex))
                    } catch (e: Exception) {
                        Color(0xFFFF4500)
                    }

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectUser(user) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(parsedColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.nickname.firstOrNull() ?: '?').uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.nickname,
                            style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "@${user.username}",
                            style = AppTypography.bodyMedium.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sticky Bottom Actions
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Account", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Text("Continue as Guest", style = AppTypography.bodySmall)
        }
    }
}

@Composable
fun ColumnScope.RegisterForm(
    onRegister: (username: String, pin: String, nickname: String, colorHex: String) -> Unit,
    onBack: () -> Unit,
    authError: String?
) {
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF5722") }

    val colorOptions = listOf(
        "#FF5722", // Terracotta/Orange
        "#4CAF50", // Green Forest
        "#0288D1", // Ocean Blue
        "#E91E63", // Pink Rose
        "#FF9800", // Amber
        "#9C27B0"  // Purple
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Landing", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CREATE LOCAL ACCOUNT",
            style = AppTypography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        TextField(
            value = username,
            onValueChange = { username = it.take(15).trim().lowercase() },
            label = { Text("Unique Username (no spaces)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nickname
        TextField(
            value = nickname,
            onValueChange = { nickname = it.take(20) },
            label = { Text("Display Name / Nickname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4-Digit PIN
        TextField(
            value = pin,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 4) {
                    pin = input
                }
            },
            label = { Text("4-Digit Security PIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Choose Theme Color
        Text(
            text = "AVATAR THEME COLOR",
            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colorOptions.forEach { hex ->
                val col = Color(android.graphics.Color.parseColor(hex))
                val isSelected = selectedColor == hex
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(col, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = hex }
                )
            }
        }

        if (authError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = authError,
                color = MaterialTheme.colorScheme.error,
                style = AppTypography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onRegister(username, pin, nickname, selectedColor)
            },
            enabled = username.isNotEmpty() && nickname.isNotEmpty() && pin.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Register and Authenticate", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun ColumnScope.PinVerificationScreen(
    user: UserEntity,
    onVerify: (pin: String) -> Unit,
    onBack: () -> Unit,
    authError: String?
) {
    var pin by remember { mutableStateOf("") }
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(user.avatarColorHex))
    } catch (e: Exception) {
        Color(0xFFFF5722)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Switcher", style = AppTypography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(parsedColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (user.nickname.firstOrNull() ?: '?').uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Verify Security PIN",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Enter 4-character PIN for ${user.nickname}",
            style = AppTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // PIN TextField
        TextField(
            value = pin,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 4) {
                    pin = input
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(180.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        if (authError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = authError,
                color = MaterialTheme.colorScheme.error,
                style = AppTypography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onVerify(pin)
            },
            enabled = pin.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Unlock Session", style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}
