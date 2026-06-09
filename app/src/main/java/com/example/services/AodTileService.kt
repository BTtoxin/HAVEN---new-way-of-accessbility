package com.example.services

import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast

class AodTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            val enabled = Settings.Secure.getInt(contentResolver, "doze_always_on", 0) == 1
            Settings.Secure.putInt(contentResolver, "doze_always_on", if (enabled) 0 else 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Requires WRITE_SECURE_SETTINGS permission via ADB", Toast.LENGTH_LONG).show()
        }
        updateState()
    }

    private fun updateState() {
        try {
            val enabled = Settings.Secure.getInt(contentResolver, "doze_always_on", 0) == 1
            val state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTileState(state, "AOD", if (enabled) "On" else "Off")
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "AOD", "Off")
        }
    }
}
