package com.example.services

import android.content.Context
import android.media.AudioManager
import android.service.quicksettings.Tile
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppAudioIsolationTileService : BaseTileService() {
    private val dataStore by lazy { SettingsDataStore(this) }

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            val isIsolated = dataStore.isAppAudioIsolatedFlow.first()
            updateTileState(
                if(isIsolated) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, 
                "App Audio", 
                if(isIsolated) "Isolated" else "Normal"
            )
        }
    }

    override fun onClick() {
        triggerHapticClick()
        serviceScope.launch {
            val isIsolated = dataStore.isAppAudioIsolatedFlow.first()
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (!isIsolated) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                dataStore.setAppAudioIsolated(true)
                updateTileState(Tile.STATE_ACTIVE, "App Audio", "Isolated")
            } else {
                val maxSys = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSys / 2, 0)
                val maxRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                audioManager.setStreamVolume(AudioManager.STREAM_RING, maxRing / 2, 0)
                dataStore.setAppAudioIsolated(false)
                updateTileState(Tile.STATE_INACTIVE, "App Audio", "Normal")
            }
        }
    }
}
