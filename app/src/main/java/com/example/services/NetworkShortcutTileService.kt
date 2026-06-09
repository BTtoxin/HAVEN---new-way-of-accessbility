package com.example.services

import android.content.Intent
import android.service.quicksettings.Tile

class NetworkShortcutTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_ACTIVE, "Network Prefs", "Toggle 4G/5G")
    }

    override fun onClick() {
        triggerHapticClick()
        launchSafeIntentAndCollapse(Intent("android.settings.NETWORK_OPERATOR_SETTINGS"))
    }
}
