package com.example.services

import android.service.quicksettings.Tile

class SessionPasswordTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Session Lock", "Set Password")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        
        // Open Auth/Settings screen logic or trigger intent to lock device
        val intent = android.content.Intent(this, com.example.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_SESSION_PASSWORD", true)
        }
        launchSafeIntentAndCollapse(intent)
    }
}
