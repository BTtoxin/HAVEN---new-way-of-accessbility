package com.example.services

import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile

class RingerModeTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val currentMode = am.ringerMode
        
        try {
            val nextMode = when (currentMode) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            am.ringerMode = nextMode
        } catch (e: Exception) {
            // Might need Notification Policy Access constraint
        }
        updateState()
    }

    private fun updateState() {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val mode = am.ringerMode
        val (state, label, subtitle) = when (mode) {
           AudioManager.RINGER_MODE_NORMAL -> Triple(Tile.STATE_ACTIVE, "Ringer", "Ring")
           AudioManager.RINGER_MODE_VIBRATE -> Triple(Tile.STATE_INACTIVE, "Ringer", "Vibrate")
           else -> Triple(Tile.STATE_INACTIVE, "Ringer", "Silent")
        }
        updateTileState(state, label, subtitle)
    }
}
