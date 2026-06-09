package com.example.services

import android.content.Context
import android.service.quicksettings.Tile

class MobileHotspotTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val prefs = getSharedPreferences("tile_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("hotspot_enabled", false)
        prefs.edit().putBoolean("hotspot_enabled", !isEnabled).apply()
        updateState()
    }

    private fun updateState() {
        val prefs = getSharedPreferences("tile_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("hotspot_enabled", false)
        val state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val subtitle = if (isEnabled) "1 device connected" else "Off"
        updateTileState(state, "Hotspot", subtitle)
    }
}
