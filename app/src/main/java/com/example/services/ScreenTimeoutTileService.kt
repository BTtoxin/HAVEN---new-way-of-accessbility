package com.example.services

import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import com.example.MainActivity
import com.example.utils.QSPreferenceManager
import com.example.utils.SystemSettingsHelper

class ScreenTimeoutTileService : BaseTileService() {
    private val prefManager by lazy { QSPreferenceManager(this) }
    private val settingsHelper = SystemSettingsHelper
    private var isObserverRegistered = false
    private val timeoutObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) { syncTimeoutState() }
    }

    override fun onStartListening() { 
        super.onStartListening()
        syncTimeoutState()
        registerObserver() 
    }
    
    override fun onStopListening() { 
        super.onStopListening()
        unregisterObserver() 
    }
    
    override fun onClick() {
        triggerHapticClick()
        if (!settingsHelper.hasWriteSettingsPermission(this)) {
            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchSafeIntentAndCollapse(intent)
            return
        }
        val current = settingsHelper.getScreenOffTimeout(this)
        val next = prefManager.getNextTimeoutPreset(current)
        val actual = if (next == -1) Int.MAX_VALUE else next
        settingsHelper.setScreenOffTimeout(this, actual)
        syncTimeoutState()
    }

    private fun syncTimeoutState() {
        val current = settingsHelper.getScreenOffTimeout(this)
        val label = prefManager.formatTimeoutLabel(current)
        updateTileState(Tile.STATE_ACTIVE, "Timeout", label)
    }
    
    private fun registerObserver() { 
        if (!isObserverRegistered) { 
            contentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT), false, timeoutObserver)
            isObserverRegistered = true 
        } 
    }
    
    private fun unregisterObserver() { 
        if (isObserverRegistered) { 
            contentResolver.unregisterContentObserver(timeoutObserver)
            isObserverRegistered = false 
        } 
    }
}
