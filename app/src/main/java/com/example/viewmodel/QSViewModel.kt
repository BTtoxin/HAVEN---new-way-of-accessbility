package com.example.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.utils.QSPreferenceManager
import com.example.utils.SystemSettingsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.data.UserPrefDatabase
import com.example.data.UserRepository
import com.example.data.UserEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class BatteryInfo(
    val percentage: Int = 100,
    val status: String = "Unknown",
    val health: String = "Good",
    val isCharging: Boolean = false,
    val remainingTimeString: String = "Calculating..."
)

class QSViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefManager = QSPreferenceManager(context)
    private val settingsHelper = SystemSettingsHelper

    private val db = UserPrefDatabase.getDatabase(context)
    val userRepository = UserRepository(db)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _activeUserId = MutableStateFlow<Int>(-1)
    val activeUserId = _activeUserId.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    val allUsers = userRepository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    private val _isDnsActive = MutableStateFlow(false)
    val isDnsActive = _isDnsActive.asStateFlow()

    private val _isTheaterActive = MutableStateFlow(false)
    val isTheaterActive = _isTheaterActive.asStateFlow()

    private val _isAppAudioIsolated = MutableStateFlow(false)
    val isAppAudioIsolated = _isAppAudioIsolated.asStateFlow()

    private val _isMonochrome = MutableStateFlow(false)
    val isMonochrome = _isMonochrome.asStateFlow()

    private val _selectedPalette = MutableStateFlow("NATURAL")
    val selectedPalette = _selectedPalette.asStateFlow()

    private val _tileOrder = MutableStateFlow(emptyList<String>())
    val tileOrder = _tileOrder.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode = _themeMode.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

            val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING || statusInt == BatteryManager.BATTERY_STATUS_FULL
            val statusStr = when (statusInt) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Discharging"
            }

            val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthStr = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Normal"
            }

            val remTime = if (isCharging) {
                if (pct >= 100) "Fully Charged"
                else {
                    val mins = ((100 - pct) * 1.5).toInt()
                    if (mins >= 60) "~${mins / 60}h ${mins % 60}m to full" else "~${mins}m to full"
                }
            } else {
                val mins = (pct * 9)
                "~${mins / 60}h ${mins % 60}m remaining"
            }

            _batteryInfo.value = BatteryInfo(
                percentage = pct,
                status = statusStr,
                health = healthStr,
                isCharging = isCharging,
                remainingTimeString = remTime
            )
        }
    }

    init {
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        viewModelScope.launch {
            checkAllStates()
        }
        viewModelScope.launch {
            _activeUserId.collectLatest { userId ->
                userRepository.getPreferencesForUser(userId).collect { prefMap ->
                    prefMap["theme_mode"]?.let { _themeMode.value = it }
                    prefMap["is_monochrome"]?.let { _isMonochrome.value = it.toBoolean() }
                    prefMap["selected_palette"]?.let { _selectedPalette.value = it }
                    prefMap["tile_order"]?.let { orderStr ->
                        val savedOrder = if (orderStr.isNotEmpty()) orderStr.split(",") else defaultTileOrder()
                        _tileOrder.value = savedOrder
                    }
                    prefMap["is_dns_active"]?.let { _isDnsActive.value = it.toBoolean() }
                    prefMap["is_caffeine_active"]?.let { _isCaffeineActive.value = it.toBoolean() }
                    prefMap["is_theater_active"]?.let { _isTheaterActive.value = it.toBoolean() }
                    prefMap["is_app_audio_isolated"]?.let { _isAppAudioIsolated.value = it.toBoolean() }
                    prefMap["glyph_brightness_profile"]?.let { _glyphBrightnessProfile.value = it }
                    prefMap["custom_shortcut_target"]?.let { _customShortcutTarget.value = it }
                    prefMap["custom_shortcut_label"]?.let { _customShortcutLabel.value = it }
                }
            }
        }
    }

    fun register(username: String, pin: String, nickname: String, avatarColorHex: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.registerUser(username, pin, nickname, avatarColorHex)
            if (user != null) {
                _currentUser.value = user
                _activeUserId.value = user.id
                _authError.value = null
                onSuccess()
            } else {
                _authError.value = "Username already exists or details are invalid"
            }
        }
    }

    fun login(username: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.authenticateUser(username, pin)
            if (user != null) {
                _currentUser.value = user
                _activeUserId.value = user.id
                _authError.value = null
                onSuccess()
            } else {
                _authError.value = "Invalid username or PIN code"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _activeUserId.value = -1
        _authError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}
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
            _isDnsActive.value = dataStore.isDnsActiveFlow.first()
            _isTheaterActive.value = dataStore.isTheaterActiveFlow.first()
            _isAppAudioIsolated.value = dataStore.isAppAudioIsolatedFlow.first()
            _isMonochrome.value = dataStore.isMonochromeFlow.first()
            _selectedPalette.value = dataStore.selectedPaletteFlow.first()
            _themeMode.value = dataStore.themeModeFlow.first()
            val orderString = dataStore.tileOrderFlow.first()
            val savedOrder = if (orderString.isNotEmpty()) orderString.split(",") else defaultTileOrder()
            _tileOrder.value = savedOrder
        }
    }
    
    private fun savePref(key: String, value: String) {
        viewModelScope.launch {
            userRepository.savePreference(_activeUserId.value, key, value)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setThemeMode(mode)
            _themeMode.value = mode
            savePref("theme_mode", mode)
        }
    }
    
    fun setMonochrome(active: Boolean) {
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setMonochrome(active)
            _isMonochrome.value = active
            val palette = if (active) "MONOCHROME" else "NATURAL"
            com.example.utils.SettingsDataStore(context).setSelectedPalette(palette)
            _selectedPalette.value = palette
            savePref("is_monochrome", active.toString())
            savePref("selected_palette", palette)
        }
    }

    fun setSelectedPalette(palette: String) {
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setSelectedPalette(palette)
            _selectedPalette.value = palette
            val isMono = (palette != "NATURAL")
            _isMonochrome.value = isMono
            com.example.utils.SettingsDataStore(context).setMonochrome(isMono)
            savePref("selected_palette", palette)
            savePref("is_monochrome", isMono.toString())
        }
    }
    
    fun updateTileOrder(newOrder: List<String>) {
        _tileOrder.value = newOrder
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setTileOrder(newOrder.joinToString(","))
            savePref("tile_order", newOrder.joinToString(","))
        }
    }
    
    fun resetTileOrder() {
        val order = defaultTileOrder()
        _tileOrder.value = order
        viewModelScope.launch {
            com.example.utils.SettingsDataStore(context).setTileOrder("")
            savePref("tile_order", "")
        }
    }
    
    private fun defaultTileOrder() = listOf(
        "TIMEOUT", "CAFFEINE", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH", "MANUAL", "CHANGELOG", "ABOUT"
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
        savePref("custom_shortcut_target", target)
        savePref("custom_shortcut_label", label)
    }

    fun setGlyphBrightnessProfile(profile: String) {
        prefManager.setGlyphBrightnessProfile(profile)
        _glyphBrightnessProfile.value = profile
        savePref("glyph_brightness_profile", profile)
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
            savePref("is_caffeine_active", active.toString())
        }
    }

    fun togglePrivateDns(active: Boolean) {
        viewModelScope.launch {
            val dataStore = com.example.utils.SettingsDataStore(context)
            dataStore.setDnsActive(active)
            _isDnsActive.value = active
            if (!active) {
                SystemSettingsHelper.setPrivateDns(context, null)
            } else {
                val dns = dataStore.privateDnsFlow.first()
                val targetDns = if (dns == "off" || dns.isEmpty()) "dns.google" else dns
                SystemSettingsHelper.setPrivateDns(context, targetDns)
            }
            savePref("is_dns_active", active.toString())
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
            savePref("is_theater_active", active.toString())
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
            savePref("is_app_audio_isolated", active.toString())
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

    fun stopFocusSandbox() {
        viewModelScope.launch {
            com.example.utils.FocusDataStore.setTimes(context, 0L, 0L)
            com.example.utils.FocusDataStore.setAllowedApps(context, emptySet())
            val intent = android.content.Intent(context, com.example.services.FocusSandboxService::class.java)
            context.stopService(intent)
        }
    }
}
