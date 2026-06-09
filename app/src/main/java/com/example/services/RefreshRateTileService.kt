package com.example.services

import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast

class RefreshRateTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            val currentRate = Settings.System.getFloat(contentResolver, "peak_refresh_rate", 60f)
            val nextRate = when {
                currentRate < 90f -> 90f
                currentRate < 120f -> 120f
                else -> 60f
            }
            Settings.System.putFloat(contentResolver, "peak_refresh_rate", nextRate)
            Settings.System.putFloat(contentResolver, "min_refresh_rate", nextRate)
        } catch (e: Exception) {
            val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchSafeIntentAndCollapse(intent)
        }
        updateState()
    }

    private fun updateState() {
        try {
            val currentRate = Settings.System.getFloat(contentResolver, "peak_refresh_rate", 60f)
            val state = if (currentRate > 60f) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTileState(state, "Refresh Rate", "${currentRate.toInt()}Hz")
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Refresh Rate", "60Hz")
        }
    }
}
