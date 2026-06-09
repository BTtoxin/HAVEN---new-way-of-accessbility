package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class DevOptionsTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Dev Options", "Tap to open")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        launchSafeIntentAndCollapse(intent)
    }
}
