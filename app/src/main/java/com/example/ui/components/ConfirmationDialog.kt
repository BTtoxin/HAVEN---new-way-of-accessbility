package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTypography
import com.example.ui.theme.NothingRed

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "CONFIRM",
    dismissText: String = "CANCEL",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Text(message, style = AppTypography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) NothingRed else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmText, style = AppTypography.labelSmall)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(dismissText, style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
}
