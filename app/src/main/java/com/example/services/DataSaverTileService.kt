package com.example.services

import android.content.Context
import android.service.quicksettings.Tile

class DataSaverTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val prefs = getSharedPreferences("tile_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("data_saver_enabled", false)
        prefs.edit().putBoolean("data_saver_enabled", !isEnabled).apply()
        updateState()
    }

    private fun updateState() {
        val prefs = getSharedPreferences("tile_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("data_saver_enabled", false)
        val state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val subtitle = if (isEnabled) "Saving data..." else "Off"
        updateTileState(state, "Data Saver", subtitle)
    }
}
