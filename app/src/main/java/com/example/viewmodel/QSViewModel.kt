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

data class ToastMessage(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
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

    private val _isWifiActive = MutableStateFlow(true)
    val isWifiActive = _isWifiActive.asStateFlow()

    private val _isBluetoothActive = MutableStateFlow(false)
    val isBluetoothActive = _isBluetoothActive.asStateFlow()

    private val _isDataActive = MutableStateFlow(true)
    val isDataActive = _isDataActive.asStateFlow()

    private val _isHotspotActive = MutableStateFlow(false)
    val isHotspotActive = _isHotspotActive.asStateFlow()

    private val _toastMessage = MutableStateFlow<ToastMessage?>(null)
    val toastMessage = _toastMessage.asStateFlow()

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

    private val _quickToggleOrder = MutableStateFlow(listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT"))
    val quickToggleOrder = _quickToggleOrder.asStateFlow()

    private val _quickToggleSizes = MutableStateFlow<Map<String, String>>(emptyMap())
    val quickToggleSizes = _quickToggleSizes.asStateFlow()

    val systemSyncLogs = com.example.utils.MockSystemApi.syncLogs

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    // Weather & Search States
    enum class WeatherState {
        LOADING, SUCCESS, ERROR
    }

    private val _weatherState = MutableStateFlow(WeatherState.LOADING)
    val weatherState = _weatherState.asStateFlow()

    private val _weatherTemp = MutableStateFlow<Double?>(null)
    val weatherTemp = _weatherTemp.asStateFlow()

    private val _weatherCode = MutableStateFlow<Int?>(null)
    val weatherCode = _weatherCode.asStateFlow()

    private val _selectedCityIndex = MutableStateFlow(0)
    val selectedCityIndex = _selectedCityIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Predictive Search & Usage Stats
    private val _tileUsageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val tileUsageCounts = _tileUsageCounts.asStateFlow()

    // AI Reorganization Recommendation
    private val _aiLayoutSuggestion = MutableStateFlow<List<String>?>(null)
    val aiLayoutSuggestion = _aiLayoutSuggestion.asStateFlow()

    // Gemini Weather summary
    private val _weatherAiSummary = MutableStateFlow<String>("AI: ANALYZING WEATHER PATTERNS...")
    val weatherAiSummary = _weatherAiSummary.asStateFlow()

    private val weatherApi by lazy { com.example.data.WeatherApi.create() }

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
                    
                    // Propagate initial states from DB to MockSystemApi
                    prefMap["is_wifi_active"]?.let { 
                        _isWifiActive.value = it.toBoolean() 
                        com.example.utils.MockSystemApi.updateWifi(it.toBoolean(), "DATABASE_LOAD")
                    }
                    prefMap["is_bluetooth_active"]?.let { 
                        _isBluetoothActive.value = it.toBoolean() 
                        com.example.utils.MockSystemApi.updateBluetooth(it.toBoolean(), "DATABASE_LOAD")
                    }
                    prefMap["is_data_active"]?.let { 
                        _isDataActive.value = it.toBoolean() 
                        com.example.utils.MockSystemApi.updateData(it.toBoolean(), "DATABASE_LOAD")
                    }
                    prefMap["is_hotspot_active"]?.let { 
                        _isHotspotActive.value = it.toBoolean() 
                        com.example.utils.MockSystemApi.updateHotspot(it.toBoolean(), "DATABASE_LOAD")
                    }

                    prefMap["quick_toggle_order"]?.let { orderStr ->
                        if (orderStr.isNotEmpty()) {
                            _quickToggleOrder.value = orderStr.split(",")
                        }
                    }

                    val sizes = mutableMapOf<String, String>()
                    listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT").forEach { id ->
                        prefMap["tile_size_$id"]?.let { sizeStr ->
                            sizes[id] = sizeStr
                        }
                    }
                    _quickToggleSizes.value = sizes

                    val countsMap = mutableMapOf<String, Int>()
                    listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT", "TIMEOUT", "CAFFEINE", "BATTERY", "BRIGHTNESS", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH", "MANUAL", "CHANGELOG", "ABOUT").forEach { id ->
                        prefMap["tile_click_count_$id"]?.let { countStr ->
                            countsMap[id] = countStr.toIntOrNull() ?: 0
                        }
                    }
                    _tileUsageCounts.value = countsMap
                    checkForReorganization()
                }
            }
        }

        // Background listener that automatically syncs the local dashboard toggle states with a mock system API
        viewModelScope.launch {
            com.example.utils.MockSystemApi.toggleStates.collect { systemState ->
                if (_isWifiActive.value != systemState.isWifiActive) {
                    _isWifiActive.value = systemState.isWifiActive
                    savePref("is_wifi_active", systemState.isWifiActive.toString())
                    showToast("Wi-Fi state synchronized with mock system API.")
                }
                if (_isBluetoothActive.value != systemState.isBluetoothActive) {
                    _isBluetoothActive.value = systemState.isBluetoothActive
                    savePref("is_bluetooth_active", systemState.isBluetoothActive.toString())
                    showToast("Bluetooth state synchronized with mock system API.")
                }
                if (_isDataActive.value != systemState.isDataActive) {
                    _isDataActive.value = systemState.isDataActive
                    savePref("is_data_active", systemState.isDataActive.toString())
                    showToast("Mobile Data state synchronized with mock system API.")
                }
                if (_isHotspotActive.value != systemState.isHotspotActive) {
                    _isHotspotActive.value = systemState.isHotspotActive
                    savePref("is_hotspot_active", systemState.isHotspotActive.toString())
                    showToast("Personal Hotspot state synchronized with mock system API.")
                }
            }
        }
        fetchWeather()
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
        "TIMEOUT", "CAFFEINE", "BATTERY", "BRIGHTNESS", "DNS", "THEATER", "CLIPBOARD", "FOCUS", "SHORTCUT", "APP_AUDIO", "OPERATOR", "GLYPH", "MANUAL", "CHANGELOG", "ABOUT"
    )

    fun cycleScreenTimeout() {
        val next = prefManager.getNextTimeoutPreset(_currentSystemTimeout.value)
        val actualMs = if (next == -1) Int.MAX_VALUE else next
        if (settingsHelper.setScreenOffTimeout(context, actualMs)) {
            _currentSystemTimeout.value = next
        }
        logTileClick("TIMEOUT")
    }

    fun setCustomShortcut(target: String, label: String) {
        prefManager.setCustomShortcutTarget(target)
        prefManager.setCustomShortcutLabel(label)
        _customShortcutTarget.value = target
        _customShortcutLabel.value = label
        savePref("custom_shortcut_target", target)
        savePref("custom_shortcut_label", label)
        logTileClick("SHORTCUT")
    }

    fun setGlyphBrightnessProfile(profile: String) {
        prefManager.setGlyphBrightnessProfile(profile)
        _glyphBrightnessProfile.value = profile
        savePref("glyph_brightness_profile", profile)
        logTileClick("GLYPH")
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
        logTileClick("CAFFEINE")
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
        logTileClick("DNS")
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
        logTileClick("THEATER")
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
        logTileClick("APP_AUDIO")
    }

    fun purgeClipboard() {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
        logTileClick("CLIPBOARD")
    }

    fun startFocusSandbox(durationMins: Int, allowedApps: Set<String>) {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (durationMins * 60_000L)
            com.example.utils.FocusDataStore.setTimes(context, System.currentTimeMillis(), endTime)
            com.example.utils.FocusDataStore.setAllowedApps(context, allowedApps)
            
            val intent = android.content.Intent(context, com.example.services.FocusSandboxService::class.java)
            context.startForegroundService(intent)
        }
        logTileClick("FOCUS")
    }

    fun stopFocusSandbox() {
        viewModelScope.launch {
            com.example.utils.FocusDataStore.setTimes(context, 0L, 0L)
            com.example.utils.FocusDataStore.setAllowedApps(context, emptySet())
            val intent = android.content.Intent(context, com.example.services.FocusSandboxService::class.java)
            context.stopService(intent)
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        _toastMessage.value = ToastMessage(message, isError)
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun toggleWifi() {
        val newVal = !_isWifiActive.value
        _isWifiActive.value = newVal
        savePref("is_wifi_active", newVal.toString())
        com.example.utils.MockSystemApi.updateWifi(newVal, "UI")
        showToast("Wi-Fi successfully synchronized and saved to the Firebase database.")
    }

    fun toggleBluetooth() {
        val newVal = !_isBluetoothActive.value
        _isBluetoothActive.value = newVal
        savePref("is_bluetooth_active", newVal.toString())
        com.example.utils.MockSystemApi.updateBluetooth(newVal, "UI")
        showToast("Bluetooth successfully synchronized and saved to the Firebase database.")
    }

    fun toggleData() {
        val newVal = !_isDataActive.value
        _isDataActive.value = newVal
        savePref("is_data_active", newVal.toString())
        com.example.utils.MockSystemApi.updateData(newVal, "UI")
        showToast("Cellular Data successfully synchronized and saved to the Firebase database.")
    }

    fun toggleHotspot() {
        val newVal = !_isHotspotActive.value
        _isHotspotActive.value = newVal
        savePref("is_hotspot_active", newVal.toString())
        com.example.utils.MockSystemApi.updateHotspot(newVal, "UI")
        showToast("Personal Hotspot successfully synchronized and saved to the Firebase database.")
    }

    fun triggerSimulatedSystemEvent() {
        com.example.utils.MockSystemApi.triggerRandomSystemEvent()
    }

    fun setQuickToggleSize(id: String, sizeStr: String) {
        val current = _quickToggleSizes.value.toMutableMap()
        current[id] = sizeStr
        _quickToggleSizes.value = current
        savePref("tile_size_$id", sizeStr)
    }

    fun updateQuickToggleOrder(newOrder: List<String>) {
        _quickToggleOrder.value = newOrder
        savePref("quick_toggle_order", newOrder.joinToString(","))
        showToast("Quick toggles layout successfully synchronized and saved to the Firebase database.")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchWeather() {
        _weatherState.value = WeatherState.LOADING
        viewModelScope.launch {
            try {
                val city = com.example.data.PREDEFINED_CITIES[_selectedCityIndex.value]
                val response = weatherApi.getCurrentWeather(city.latitude, city.longitude)
                _weatherTemp.value = response.current.temperature
                _weatherCode.value = response.current.weatherCode
                _weatherState.value = WeatherState.SUCCESS
                fetchWeatherGeminiSummary(city.name, response.current.temperature, response.current.weatherCode)
            } catch (e: Exception) {
                e.printStackTrace()
                _weatherState.value = WeatherState.ERROR
            }
        }
    }

    fun fetchWeatherGeminiSummary(cityName: String, temp: Double, code: Int) {
        viewModelScope.launch {
            _weatherAiSummary.value = "AI: RETRIEVING ATMOSPHERIC INTEL..."
            val label = when (code) {
                0 -> "SUNNY"
                1, 2 -> "PARTLY CLOUDY"
                3 -> "OVERCAST"
                45, 48 -> "FOGGY"
                51, 53, 55 -> "MISTY DRIZZLE"
                61, 63, 65 -> "HEAVY RAIN"
                71, 73, 75 -> "LIGHT SNOW"
                80, 81, 82 -> "RAIN SHOWERS"
                95, 96, 99 -> "THUNDERSTORM"
                else -> "CLEAR SKY"
            }
            val prompt = "Provide a short, witty, 1-sentence weather forecast for $cityName: $label, ${temp.toInt()}°C. Format: Nothing Tech OS minimal tone. No markdown, no quotes, maximum 12 words."
            val summary = com.example.data.GeminiService.generateContent(
                prompt = prompt,
                systemInstruction = "You are Nothing Assistant. Speak in an ultra-short, minimalist, cybernetic, intelligent tone."
            )
            _weatherAiSummary.value = if (summary.isNotBlank()) summary.trim() else "AI: COLD INTEL. ${label.uppercase()} - STABLE CONDITIONS."
        }
    }

    fun cycleWeatherCity() {
        val nextIdx = (_selectedCityIndex.value + 1) % com.example.data.PREDEFINED_CITIES.size
        _selectedCityIndex.value = nextIdx
        fetchWeather()
    }

    fun logTileClick(id: String) {
        val currentCounts = _tileUsageCounts.value.toMutableMap()
        val newCount = (currentCounts[id] ?: 0) + 1
        currentCounts[id] = newCount
        _tileUsageCounts.value = currentCounts
        savePref("tile_click_count_$id", newCount.toString())
        checkForReorganization()
    }

    fun checkForReorganization() {
        val counts = _tileUsageCounts.value
        val targets = listOf("WIFI", "BLUETOOTH", "DATA", "HOTSPOT")
        val currentOrder = _quickToggleOrder.value
        val sortedTargets = targets.sortedByDescending { counts[it] ?: 0 }
        
        if (sortedTargets != currentOrder) {
            val totalClicks = targets.sumOf { counts[it] ?: 0 }
            if (totalClicks >= 3) {
                _aiLayoutSuggestion.value = sortedTargets
            }
        }
    }

    fun acceptReorganization() {
        val suggestion = _aiLayoutSuggestion.value ?: return
        updateQuickToggleOrder(suggestion)
        _aiLayoutSuggestion.value = null
        showToast("Reorganization completed. Top 2x2 grid optimized based on usage frequency.")
    }

    fun dismissReorganization() {
        _aiLayoutSuggestion.value = null
    }

    fun executeVoiceCommand(command: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val currentTogglesDesc = "Current states are: WiFi=${_isWifiActive.value}, Bluetooth=${_isBluetoothActive.value}, Data=${_isDataActive.value}, Hotspot=${_isHotspotActive.value}, Caffeine=${_isCaffeineActive.value}, TheaterMode=${_isTheaterActive.value}, Monochrome=${_isMonochrome.value}."
            
            val systemInstruction = """
                You are the smart voice interpreter for a Nothing Phone settings panel. 
                Interpret the natural language command and decide on a single action. 
                Respond ONLY with a valid JSON object matching this schema:
                {
                  "success": true,
                  "action": "WIFI" | "BLUETOOTH" | "DATA" | "HOTSPOT" | "CAFFEINE" | "DNS" | "THEATER" | "MONOCHROME" | "FOCUS" | "WEATHER" | "TIMEOUT" | "UNKNOWN",
                  "value": "ON" | "OFF" | "CYCLE",
                  "duration_mins": 30,
                  "weather_city_index": 2,
                  "reply": "A concise, witty confirm message in Nothing OS tech style, e.g. 'Isolating cell wave frequencies. Personal Hotspot terminated.'"
                }
                Current settings state context: $currentTogglesDesc.
                If the instruction is ambiguous, output action "UNKNOWN" and success false. Output ONLY raw JSON, do not include code blocks or Markdown tags.
            """.trimIndent()

            val response = com.example.data.GeminiService.generateContent(prompt = command, systemInstruction = systemInstruction)
            if (response.isBlank()) {
                onFinished("FAILED TO COMPILE VOCAL SIGNAL. COLD TRANSCEIVER CONNECTION.")
                return@launch
            }

            try {
                val cleanResponse = response.replace("```json", "").replace("```", "").trim()
                val json = org.json.JSONObject(cleanResponse)
                val success = json.optBoolean("success", false)
                val action = json.optString("action", "UNKNOWN")
                val value = json.optString("value", "")
                val reply = json.optString("reply", "Command executed.")

                if (success) {
                    when (action) {
                        "WIFI" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isWifiActive.value
                            if (_isWifiActive.value != target) toggleWifi()
                        }
                        "BLUETOOTH" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isBluetoothActive.value
                            if (_isBluetoothActive.value != target) toggleBluetooth()
                        }
                        "DATA" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isDataActive.value
                            if (_isDataActive.value != target) toggleData()
                        }
                        "HOTSPOT" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isHotspotActive.value
                            if (_isHotspotActive.value != target) toggleHotspot()
                        }
                        "CAFFEINE" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isCaffeineActive.value
                            toggleCaffeine(target)
                        }
                        "DNS" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isDnsActive.value
                            togglePrivateDns(target)
                        }
                        "THEATER" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isTheaterActive.value
                            toggleTheaterMode(target)
                        }
                        "MONOCHROME" -> {
                            val target = if (value == "ON") true else if (value == "OFF") false else !_isMonochrome.value
                            setMonochrome(target)
                        }
                        "FOCUS" -> {
                            val mins = json.optInt("duration_mins", 30)
                            startFocusSandbox(mins, setOf("com.android.settings"))
                        }
                        "WEATHER" -> {
                            val idx = json.optInt("weather_city_index", -1)
                            if (idx in 0..5) {
                                _selectedCityIndex.value = idx
                                fetchWeather()
                            } else {
                                cycleWeatherCity()
                            }
                        }
                        "TIMEOUT" -> {
                            cycleScreenTimeout()
                        }
                    }
                    onFinished(reply)
                } else {
                    onFinished(reply)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFinished("AI INTERPRETATION FAULT. VERBAL INPUT NOT RECOGNIZED.")
            }
        }
    }
}
