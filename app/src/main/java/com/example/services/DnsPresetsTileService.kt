package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class DnsPresetsTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Private DNS", "Tap to configure")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        launchSafeIntentAndCollapse(intent)
    }
}
