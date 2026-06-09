package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class WifiQrShareTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Wi-Fi Share", "Tap to open")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        
        // Open Wi-Fi settings as Android prevents sharing passwords directly
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        launchSafeIntentAndCollapse(intent)
    }
}
