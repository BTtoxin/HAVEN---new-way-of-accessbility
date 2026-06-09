package com.example.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class SystemToggleState(
    val isWifiActive: Boolean = true,
    val isBluetoothActive: Boolean = false,
    val isDataActive: Boolean = true,
    val isHotspotActive: Boolean = false
)

data class SystemSyncLog(
    val id: Long,
    val message: String,
    val timestamp: String,
    val source: String // "UI" or "SYSTEM"
)

object MockSystemApi {
    private val _toggleStates = MutableStateFlow(SystemToggleState())
    val toggleStates = _toggleStates.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<SystemSyncLog>>(emptyList())
    val syncLogs = _syncLogs.asStateFlow()

    private val apiScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var logIndex = 0L

    init {
        addLog("Mock System API initialized & listening", "SYSTEM")
        startSystemSimulation()
    }

    private fun addLog(message: String, source: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeString = sdf.format(Date())
        val log = SystemSyncLog(logIndex++, message, timeString, source)
        val currentList = _syncLogs.value.toMutableList()
        currentList.add(0, log) // Add to top
        _syncLogs.value = currentList.take(50) // Keep last 50 logs
    }

    fun updateWifi(isActive: Boolean, source: String = "UI") {
        if (_toggleStates.value.isWifiActive != isActive) {
            _toggleStates.value = _toggleStates.value.copy(isWifiActive = isActive)
            addLog("Wi-Fi status synchronized to mock device API: ${if (isActive) "ENABLED" else "DISABLED"}", source)
        }
    }

    fun updateBluetooth(isActive: Boolean, source: String = "UI") {
        if (_toggleStates.value.isBluetoothActive != isActive) {
            _toggleStates.value = _toggleStates.value.copy(isBluetoothActive = isActive)
            addLog("Bluetooth status synchronized to mock device API: ${if (isActive) "ENABLED" else "DISABLED"}", source)
        }
    }

    fun updateData(isActive: Boolean, source: String = "UI") {
        if (_toggleStates.value.isDataActive != isActive) {
            _toggleStates.value = _toggleStates.value.copy(isDataActive = isActive)
            addLog("Mobile Data status synchronized to mock device API: ${if (isActive) "ENABLED" else "DISABLED"}", source)
        }
    }

    fun updateHotspot(isActive: Boolean, source: String = "UI") {
        if (_toggleStates.value.isHotspotActive != isActive) {
            _toggleStates.value = _toggleStates.value.copy(isHotspotActive = isActive)
            addLog("Personal Hotspot status synchronized to mock device API: ${if (isActive) "ENABLED" else "DISABLED"}", source)
        }
    }

    /**
     * Start a background listener simulation that emits random realistic hardware changes (e.g., router disconnected, power-saving triggered)
     */
    private fun startSystemSimulation() {
        apiScope.launch {
            while (isActive) {
                delay(30000) // Trigger every 30 seconds
                triggerRandomSystemEvent()
            }
        }
    }

    fun triggerRandomSystemEvent() {
        val currentState = _toggleStates.value
        val eventType = Random.nextInt(5)
        when (eventType) {
            0 -> {
                // Out of range Wifi disconnects
                if (currentState.isWifiActive) {
                    updateWifi(false, "SYSTEM")
                    addLog("System Alert: Router disconnected or out of range. Wi-Fi deactivated.", "SYSTEM")
                } else {
                    updateWifi(true, "SYSTEM")
                    addLog("System Alert: Known high-speed Wi-Fi router detected. Wi-Fi auto-connected.", "SYSTEM")
                }
            }
            1 -> {
                // Bluetooth headset connected
                if (!currentState.isBluetoothActive) {
                    updateBluetooth(true, "SYSTEM")
                    addLog("System Alert: Companion smartwatch detected. Bluetooth activated.", "SYSTEM")
                } else {
                    updateBluetooth(false, "SYSTEM")
                    addLog("System Alert: Wearable accessory disconnected. Bluetooth auto-deactivated to save power.", "SYSTEM")
                }
            }
            2 -> {
                // Hotspot auto timeout
                if (currentState.isHotspotActive) {
                    updateHotspot(false, "SYSTEM")
                    addLog("System Alert: No clients connected to sharing portal. Hotspot auto-timeout triggered.", "SYSTEM")
                } else {
                    // Turn on Hotspot
                    updateHotspot(true, "SYSTEM")
                    addLog("System Alert: Simulated remote tethering request received. Hotspot enabled.", "SYSTEM")
                }
            }
            3 -> {
                // Cellular data saver
                if (currentState.isDataActive) {
                    updateData(false, "SYSTEM")
                    addLog("System Alert: Data roaming limit exceeded. Cellular data deactivated.", "SYSTEM")
                } else {
                    updateData(true, "SYSTEM")
                    addLog("System Alert: Home carrier detected. Cellular data re-enabled.", "SYSTEM")
                }
            }
            4 -> {
                // Double sync confirmation
                addLog("System heartbeat check: Hardware registers verified fully aligned and accurate.", "SYSTEM")
            }
        }
    }
}
