package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class FontSizeTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchSafeIntentAndCollapse(intent)
            return
        }

        try {
            val currentScale = Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE, 1.0f)
            val nextScale = when {
                currentScale < 1.0f -> 1.0f // Small -> Normal
                currentScale < 1.15f -> 1.15f // Normal -> Large
                currentScale < 1.3f -> 1.3f // Large -> XL
                else -> 0.85f // XL -> Small
            }
            Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, nextScale)
        } catch (e: Exception) {}
        updateState()
    }

    private fun updateState() {
        try {
            val currentScale = Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE, 1.0f)
            val subtitle = when {
                currentScale < 1.0f -> "Small"
                currentScale < 1.15f -> "Normal"
                currentScale < 1.3f -> "Large"
                else -> "Extra Large"
            }
            // Will set to active if it's not normal
            val state = if (currentScale == 1.0f) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            updateTileState(state, "Font Size", subtitle)
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Font Size", "Unknown")
        }
    }
}
