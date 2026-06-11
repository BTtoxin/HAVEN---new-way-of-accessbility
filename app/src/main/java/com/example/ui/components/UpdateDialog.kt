package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NeutralGray
import com.example.ui.theme.NothingRed
import com.example.viewmodel.UpdateInfo

@Composable
fun UpdateAvailableDialog(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = NothingRed, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (updateInfo.isNewer) "UPDATE AVAILABLE" else "YOU'RE UP TO DATE",
                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "GitHub version: ${updateInfo.version}\nInstalled: v${com.example.BuildConfig.APP_VERSION_NAME}",
                    style = AppTypography.bodySmall,
                    color = NeutralGray
                )
                if (updateInfo.changelog.isNotBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text("RELEASE NOTES:", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp), color = NothingRed)
                    // Show first 300 chars of changelog
                    Text(
                        updateInfo.changelog.take(300) + if (updateInfo.changelog.length > 300) "..." else "",
                        style = AppTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            if (updateInfo.isNewer && updateInfo.apkUrl.isNotEmpty()) {
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = NothingRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("DOWNLOAD UPDATE", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            }
        }
    )
}
