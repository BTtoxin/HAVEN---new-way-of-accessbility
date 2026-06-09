package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.widget.Toast

class GrayscaleTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        try {
            val enabled = Settings.Secure.getInt(contentResolver, "accessibility_display_daltonizer_enabled", 0) == 1
            // 0 = MONOCHROMACY for daltonizer
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", 0)
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", if (enabled) 0 else 1)
        } catch (e: Exception) {
            Toast.makeText(this, "Requires WRITE_SECURE_SETTINGS permission via ADB", Toast.LENGTH_LONG).show()
        }
        updateState()
    }

    private fun updateState() {
        try {
            val isGrayscale = Settings.Secure.getInt(contentResolver, "accessibility_display_daltonizer_enabled", 0) == 1 &&
                              Settings.Secure.getInt(contentResolver, "accessibility_display_daltonizer", -1) == 0
            val state = if (isGrayscale) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTileState(state, "Grayscale", if (isGrayscale) "On" else "Off")
        } catch (e: Exception) {
            updateTileState(Tile.STATE_INACTIVE, "Grayscale", "Off")
        }
    }
}
