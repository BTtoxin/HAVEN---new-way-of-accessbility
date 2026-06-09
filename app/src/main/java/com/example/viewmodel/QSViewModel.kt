package com.example.viewmodel

import android.app.Application
import android.app.NotificationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.utils.QSPreferenceManager
import com.example.utils.SystemSettingsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QSViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefManager = QSPreferenceManager(context)
    private val settingsHelper = SystemSettingsHelper

    private val _hasWriteSettingsPermission = MutableStateFlow(false)
    val hasWriteSettingsPermission = _hasWriteSettingsPermission.asStateFlow()

    private val _hasDndPermission = MutableStateFlow(false)
    val hasDndPermission = _hasDndPermission.asStateFlow()

    private val _currentSystemTimeout = MutableStateFlow(30000)
    val currentSystemTimeout = _currentSystemTimeout.asStateFlow()

    private val _customShortcutTarget = MutableStateFlow("android.settings.NOTIFICATION_HISTORY")
    val customShortcutTarget = _customShortcutTarget.asStateFlow()

    private val _customShortcutLabel = MutableStateFlow("Notification History")
    val customShortcutLabel = _customShortcutLabel.asStateFlow()

    private val _glyphBrightnessProfile = MutableStateFlow("Essential Only")
    val glyphBrightnessProfile = _glyphBrightnessProfile.asStateFlow()

    private val _essentialTimerSeconds = MutableStateFlow(0)
    val essentialTimerSeconds = _essentialTimerSeconds.asStateFlow()

    private val _isCaffeineActive = MutableStateFlow(false)
    val isCaffeineActive = _isCaffeineActive.asStateFlow()

    private val _isTheaterActive = MutableStateFlow(false)
    val isTheaterActive = _isTheaterActive.asStateFlow()

    private val _isAppAudioIsolated = MutableStateFlow(false)
    val isAppAudioIsolated = _isAppAudioIsolated.asStateFlow()

    private val _isMonochrome = MutableStateFlow(false)
    val isMonochrome = _isMonochrome.asStateFlow()

    private val _tileOrder = MutableStateFlow(emptyList<String>())
    val tileOrder = _tileOrder.asStateFlow()

    init {
        viewModelScope.launch {
            checkAllStates()
        }
    }

    fun checkAllStates() {
        _hasWriteSettingsPermission.value = settingsHelper.hasWriteSettingsPermission(context)
        _hasDndPermission.value = context.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted
        _currentSystemTimeout.value = settingsHelper.getScreenOffTimeout(context)
        _customShortcutTarget.value = prefManager.getCustomShortcutTarget()
        _customShortcutLabel.value = prefManager.getCustomShortcutLabel()
        _glyphBrightnessProfile.value = prefManager.getGlyphBrightnessProfile()
        
        viewModelScope.launch {
            val dataStore = com.example.utils.SettingsDataStore(context)
            _isCaffeineActive.value = dataStore.isCaffeineActiveFlow.first()
            _isTheaterActive.value = dataStore.isTheaterActiveFlow.first()
            _isAppAudioIsolated.value = dataStore.isAppAudioIsolatedFlow.first()
            _isMonochrome.value = dataStore.isMonochromeFlow.first()
            val orderString = dataStore.tileOrderFlow.first()
            val savedOrder = if (orderString.isNotEmpty()) orderString.split(",") else defaultTileOrder()
            _tileOrder.value = savedOrder
        }
    }
    
    fun setMonochrome(active: Boolean) {
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setMonochrome(active)
            _isMonochrome.value = active
        }
    }
    
    fun updateTileOrder(newOrder: List<String>) {
        _tileOrder.value = newOrder
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setTileOrder(newOrder.joinToString(","))
        }
    }
    
    fun resetTileOrder() {
        val order = defaultTileOrder()
        _tileOrder.value = order
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setTileOrder("")
        }
    }
    
    private fun defaultTileOrder() = listOf(
        "TIMEOUT", "CAFFEINE", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH"
    )

    fun cycleScreenTimeout() {
        val next = prefManager.getNextTimeoutPreset(_currentSystemTimeout.value)
        val actualMs = if (next == -1) Int.MAX_VALUE else next
        if (settingsHelper.setScreenOffTimeout(context, actualMs)) {
            _currentSystemTimeout.value = next
        }
    }

    fun setCustomShortcut(target: String, label: String) {
        prefManager.setCustomShortcutTarget(target)
        prefManager.setCustomShortcutLabel(label)
        _customShortcutTarget.value = target
        _customShortcutLabel.value = label
    }

    fun setGlyphBrightnessProfile(profile: String) {
        prefManager.setGlyphBrightnessProfile(profile)
        _glyphBrightnessProfile.value = profile
    }

    fun triggerEssentialTimerCountdown(secondsValue: Int) {
        viewModelScope.launch {
            for (i in secondsValue downTo 0) {
                _essentialTimerSeconds.value = i
                if (i > 0) delay(1000)
            }
        }
    }

    fun toggleCaffeine(active: Boolean) {
        viewModelScope.launch {
            val dataStore = com.example.utils.SettingsDataStore(context)
            dataStore.setCaffeineActive(active)
            _isCaffeineActive.value = active
            if (active) {
                context.startForegroundService(android.content.Intent(context, com.example.services.CaffeineWakeLockService::class.java))
            } else {
                context.stopService(android.content.Intent(context, com.example.services.CaffeineWakeLockService::class.java))
            }
        }
    }

    fun togglePrivateDns(active: Boolean) {
        viewModelScope.launch {
            val dataStore = com.example.utils.SettingsDataStore(context)
            if (!active) {
                SystemSettingsHelper.setPrivateDns(context, null)
            } else {
                val dns = dataStore.privateDnsFlow.first()
                val targetDns = if (dns == "off" || dns.isEmpty()) "dns.google" else dns
                SystemSettingsHelper.setPrivateDns(context, targetDns)
            }
        }
    }

    fun toggleTheaterMode(active: Boolean) {
        viewModelScope.launch {
            val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
            val nm = context.getSystemService(android.app.NotificationManager::class.java)
            val dataStore = com.example.utils.SettingsDataStore(context)
            if (active) {
                val brightness = dataStore.theaterBrightnessFlow.first()
                SystemSettingsHelper.setScreenBrightness(context, (brightness * 25.5f).toInt())
                
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_NONE)
                }
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_SYSTEM, 0, 0)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, 0, 0)
                prefManager.setTheaterModeActive(true)
                dataStore.setTheaterActive(true)
                _isTheaterActive.value = true
            } else {
                SystemSettingsHelper.setScreenBrightness(context, 128)
                if (nm.isNotificationPolicyAccessGranted) {
                    nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                val maxSys = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_SYSTEM)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_SYSTEM, maxSys / 2, 0)
                prefManager.setTheaterModeActive(false)
                dataStore.setTheaterActive(false)
                _isTheaterActive.value = false
            }
        }
    }

    fun toggleAudioIsolation(active: Boolean) {
        viewModelScope.launch {
            val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
            val dataStore = com.example.utils.SettingsDataStore(context)
            if (active) {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_SYSTEM, 0, 0)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_RING, 0, 0)
                dataStore.setAppAudioIsolated(true)
            } else {
                val maxSys = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_SYSTEM)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_SYSTEM, maxSys / 2, 0)
                val maxRing = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_RING)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_RING, maxRing / 2, 0)
                dataStore.setAppAudioIsolated(false)
            }
        }
    }

    fun purgeClipboard() {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
    }

    fun startFocusSandbox(durationMins: Int, allowedApps: Set<String>) {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (durationMins * 60_000L)
            com.example.utils.FocusDataStore.setTimes(context, System.currentTimeMillis(), endTime)
            com.example.utils.FocusDataStore.setAllowedApps(context, allowedApps)
            
            val intent = android.content.Intent(context, com.example.services.FocusSandboxService::class.java)
            context.startForegroundService(intent)
        }
    }
}
