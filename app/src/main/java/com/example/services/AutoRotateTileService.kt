package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class AutoRotateTileService : BaseTileService() {
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
            val current = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
            Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (current == 1) 0 else 1)
        } catch (e: Exception) {}
        updateState()
    }

    private fun updateState() {
        try {
            val isAuto = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
            val state = if (isAuto) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            val subtitle = if (isAuto) "Auto-rotate" else "Portrait Locked"
            updateTileState(state, "Auto-rotate", subtitle)
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Auto-rotate", "Unknown")
        }
    }
}
