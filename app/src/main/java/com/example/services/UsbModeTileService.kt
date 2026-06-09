package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class UsbModeTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "USB Mode", "Developer Options")
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
