package com.example.services

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.service.quicksettings.Tile
import com.example.utils.QSPreferenceManager
import com.example.utils.SettingsDataStore
import com.example.utils.SystemSettingsHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TheaterModeTileService : BaseTileService() {
    private val prefManager by lazy { QSPreferenceManager(this) }
    private val dataStore by lazy { SettingsDataStore(this) }

    override fun onStartListening() {
        super.onStartListening()
        val isActive = prefManager.isTheaterModeActive()
        updateTileState(
            if(isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, 
            "Theater Mode", 
            if(isActive) "Active" else "Inactive"
        )
    }

    override fun onClick() {
        triggerHapticClick()
        serviceScope.launch {
            val isActive = prefManager.isTheaterModeActive()
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val nm = getSystemService(NotificationManager::class.java)
            if (!isActive) {
                val brightness = dataStore.theaterBrightnessFlow.first()
                SystemSettingsHelper.setScreenBrightness(this@TheaterModeTileService, (brightness * 25.5f).toInt())
                
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                }
                
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                prefManager.setTheaterModeActive(true)
                dataStore.setTheaterActive(true)
                updateTileState(Tile.STATE_ACTIVE, "Theater Mode", "Active")
            } else {
                SystemSettingsHelper.setScreenBrightness(this@TheaterModeTileService, 128)
                
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                
                val maxSys = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSys / 2, 0)
                prefManager.setTheaterModeActive(false)
                dataStore.setTheaterActive(false)
                updateTileState(Tile.STATE_INACTIVE, "Theater Mode", "Inactive")
            }
        }
    }
}
