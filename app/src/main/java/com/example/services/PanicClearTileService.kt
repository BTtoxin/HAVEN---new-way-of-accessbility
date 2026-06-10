package com.example.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.service.quicksettings.Tile
import android.widget.Toast

class PanicClearTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Panic Clear", "Tap to secure")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        // 1. Wipe clipboard
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.clearPrimaryClip()
        } catch (e: Exception) {}

        // 2. Enable DND
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        } catch (e: Exception) {}

        // 3. Go to Home (closes apps conceptually without root)
        try {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            launchSafeIntentAndCollapse(startMain)
        } catch (e: Exception) {}

        Toast.makeText(this, "Panic Mode Enabled", Toast.LENGTH_SHORT).show()
    }
}
