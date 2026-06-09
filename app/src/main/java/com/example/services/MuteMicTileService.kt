package com.example.services

import android.content.Context
import android.media.AudioManager
import android.service.quicksettings.Tile

class MuteMicTileService : BaseTileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentMute = audioManager.isMicrophoneMute
        audioManager.isMicrophoneMute = !currentMute
        updateState()
    }

    private fun updateState() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isMuted = audioManager.isMicrophoneMute
        val state = if (isMuted) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val subtitle = if (isMuted) "Muted" else "Listening"
        updateTileState(state, "Mic", subtitle)
    }
}
