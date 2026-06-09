package com.example.services

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class VpnToggleTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val intent = Intent(Settings.ACTION_VPN_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        launchSafeIntentAndCollapse(intent)
    }

    private fun updateState() {
        // Can't reliably detect active VPN without ConnectivityManager.NetworkCallback
        // For simplicity we show a generic state, or we could check for an active interface "tun0" or "ppp0".
        val isVpnActive = try {
            java.net.NetworkInterface.getNetworkInterfaces().toList().any { 
                it.isUp && (it.name.contains("tun") || it.name.contains("ppp") || it.name.contains("pptp")) 
            }
        } catch (e: Exception) {
            false
        }
        
        val state = if (isVpnActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        updateTileState(state, "VPN", if (isVpnActive) "Secured" else "Disconnected")
    }
}
