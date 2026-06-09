package com.example.services

import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast

class BrightnessLockTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            val isAuto = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            if (isAuto) {
                // Turn off auto brightness (lock it)
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            } else {
                // Unlock (turn auto back on)
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            }
        } catch (e: Exception) {
            val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchSafeIntentAndCollapse(intent)
        }
        updateState()
    }

    private fun updateState() {
        try {
            val isAuto = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            val state = if (!isAuto) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            
            val manualLevel = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
            val percent = ((manualLevel / 255f) * 100).toInt()
            
            updateTileState(state, "Brightness Lock", if (!isAuto) "Locked at $percent%" else "Unlocked")
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Brightness Lock", "Unknown")
        }
    }
}
