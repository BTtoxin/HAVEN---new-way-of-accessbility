package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class AdbWirelessTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        try {
            val enabled = Settings.Global.getInt(contentResolver, "adb_wifi_enabled", 0) == 1
            Settings.Global.putInt(contentResolver, "adb_wifi_enabled", if (enabled) 0 else 1)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchSafeIntentAndCollapse(intent)
        }
        updateState()
    }

    private fun updateState() {
        try {
            val enabled = Settings.Global.getInt(contentResolver, "adb_wifi_enabled", 0) == 1
            updateTileState(if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, "Wireless ADB", if (enabled) "On" else "Off")
        } catch (e: Exception) {
             updateTileState(Tile.STATE_INACTIVE, "Wireless ADB", "Off")
        }
    }
}
