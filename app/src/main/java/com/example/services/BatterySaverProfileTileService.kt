package com.example.services

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class BatterySaverProfileTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "Extreme Saver", "Tap to activate")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            // 1. DND On
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
            
            // 2. Dim Brightness
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 10) // super dim
            }
            
            // 3. 60Hz Refresh Rate
            Settings.System.putFloat(contentResolver, "peak_refresh_rate", 60f)
            Settings.System.putFloat(contentResolver, "min_refresh_rate", 60f)
            
            // 4. Disable Auto-Sync
            ContentResolver.setMasterSyncAutomatically(false)
            
            // 5. Trigger Battery Saver settings
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchSafeIntentAndCollapse(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide fallback if permission missing
            val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchSafeIntentAndCollapse(intent)
        }
        
        updateTileState(Tile.STATE_ACTIVE, "Extreme Saver", "Active")
    }
}
