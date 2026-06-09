package com.example.services

import android.content.Intent
import android.service.quicksettings.Tile
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CaffeineTileService : BaseTileService() {
    private val dataStore by lazy { SettingsDataStore(this) }

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            val isOn = dataStore.isCaffeineActiveFlow.first()
            val dur = dataStore.caffeineDurationFlow.first()
            updateTileState(
                if(isOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, 
                "Caffeine", 
                if(isOn) "${dur}min active" else "Off"
            )
        }
    }

    override fun onClick() {
        triggerHapticClick()
        serviceScope.launch {
            val isOn = dataStore.isCaffeineActiveFlow.first()
            val dur = dataStore.caffeineDurationFlow.first()
            if (!isOn) {
                dataStore.setCaffeineActive(true)
                startForegroundService(Intent(this@CaffeineTileService, CaffeineWakeLockService::class.java))
                updateTileState(Tile.STATE_ACTIVE, "Caffeine", "${dur}min")
            } else {
                dataStore.setCaffeineActive(false)
                stopService(Intent(this@CaffeineTileService, CaffeineWakeLockService::class.java))
                updateTileState(Tile.STATE_INACTIVE, "Caffeine", "Off")
            }
        }
    }
}
