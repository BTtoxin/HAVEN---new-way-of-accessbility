package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NtSurface
import com.example.ui.theme.NtSurfaceVariant
import com.example.ui.theme.NtSecondary
import com.example.ui.theme.NtTextSecondary

@Composable
fun SystemMonitorPanel(onDismiss: () -> Unit) {
    var memoryUsage by remember { mutableStateOf(0L) }
    var rawFps by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        while (true) {
            val runtime = Runtime.getRuntime()
            memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            // Mock a relatively stable 60fps with occasional dips to make it look active
            rawFps = 58 + (Math.random() * 3).toInt()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .clickable(enabled = false) {}, // absorb clicks
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = "System Monitor", tint = NtSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SYSTEM STATUS", style = AppTypography.labelSmall, color = NtSecondary)
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("RAM USAGE", style = AppTypography.labelSmall, color = NtTextSecondary)
                        Text("${memoryUsage}MB", style = AppTypography.headlineMedium.copy(fontSize = 28.sp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("FRAME RATE", style = AppTypography.labelSmall, color = NtTextSecondary)
                        Text("${rawFps} FPS", style = AppTypography.headlineMedium.copy(fontSize = 28.sp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NtSurfaceVariant)
                ) {
                    Text("CLOSE", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun NotificationsOverlay(onDismiss: () -> Unit) {
    val maxOffset = 0f
    val minOffset = -1000f

    var dragOffset by remember { mutableStateOf(minOffset) }
    
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "NotificationSlide"
    )

    LaunchedEffect(Unit) {
        dragOffset = maxOffset
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (animatedOffset > -200f) MaterialTheme.colorScheme.background.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                // Background click dismiss
                dragOffset = minOffset
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .offset(y = animatedOffset.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffset < -150f) {
                                dragOffset = minOffset
                            } else {
                                dragOffset = maxOffset
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = dragOffset + dragAmount
                        if (newOffset <= 0) {
                            dragOffset = newOffset
                        }
                    }
                }
        ) {
            Column(modifier = Modifier.padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NOTIFICATIONS", style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { dragOffset = minOffset }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Notifications")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        NotificationItem("System", "Device is running optimally.", Icons.Default.Notifications)
                    }
                    item {
                        NotificationItem("Security", "App permissions updated.", Icons.Default.Warning)
                    }
                    item {
                        NotificationItem("Update", "System definition 4.2.1 installed.", Icons.Default.Notifications)
                    }
                }
            }
            
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 150.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-8).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(NtSurfaceVariant)
            )
        }
    }
    
    // Auto dismiss removal from hierarchy when animation finishes sliding up
    LaunchedEffect(dragOffset, animatedOffset) {
        if (dragOffset == minOffset && animatedOffset < -800f) {
            onDismiss()
        }
    }
}

@Composable
fun NotificationItem(title: String, message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NtSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NtSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = AppTypography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(message, style = AppTypography.labelSmall, color = NtTextSecondary)
        }
    }
}
