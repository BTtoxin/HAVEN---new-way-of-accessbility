package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast

class AirplaneBtComboTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            // Check if airplane mode is ON
            val isAirplaneMode = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
            
            // Adjust the radios that airplane mode affects
            val radios = Settings.Global.getString(contentResolver, Settings.Global.AIRPLANE_MODE_RADIOS)
            if (radios != null && radios.contains("bluetooth")) {
                val newRadios = radios.replace(",bluetooth", "").replace("bluetooth,", "").replace("bluetooth", "")
                Settings.Global.putString(contentResolver, Settings.Global.AIRPLANE_MODE_RADIOS, newRadios)
            }
            
            // Toggle
            val newState = if (isAirplaneMode) 0 else 1
            Settings.Global.putInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, newState)
            
            // Broadcast intent so system updates UI
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", newState == 1)
            }
            sendBroadcast(intent)
            
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchSafeIntentAndCollapse(intent)
        }
        updateState()
    }

    private fun updateState() {
        try {
            val isAirplaneMode = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
            val state = if (isAirplaneMode) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTileState(state, "Flight + BT", if (isAirplaneMode) "Active" else "Off")
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Flight + BT", "Unknown")
        }
    }
}
