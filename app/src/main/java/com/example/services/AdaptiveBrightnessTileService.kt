package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class AdaptiveBrightnessTileService : BaseTileService() {
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
            val currentMode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            val newMode = if (currentMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            } else {
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, newMode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateState()
    }

    private fun updateState() {
        try {
            val mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            val isAuto = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            val state = if (isAuto) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            
            val subtitle = if (isAuto) "Adaptive" else {
                val manualLevel = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
                val percent = ((manualLevel / 255f) * 100).toInt()
                "$percent%"
            }
            updateTileState(state, "Brightness", subtitle)
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Brightness", "Off")
        }
    }
}
