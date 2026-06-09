package com.example.services

import android.content.Context
import android.media.AudioManager
import android.service.quicksettings.Tile

class SoundProfileCycleTileService : BaseTileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentRingerMode = audioManager.ringerMode
        
        val nextMode = when (currentRingerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        
        try {
            audioManager.ringerMode = nextMode
        } catch (e: Exception) {
            // Might need Do Not Disturb permission to set silent mode
            e.printStackTrace()
        }
        updateState()
    }

    private fun updateState() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentRingerMode = audioManager.ringerMode
        val state = if (currentRingerMode == AudioManager.RINGER_MODE_NORMAL) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        
        val subtitle = when (currentRingerMode) {
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            else -> "Unknown"
        }
        updateTileState(state, "Sound Profile", subtitle)
    }
}
