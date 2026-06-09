package com.example.services

import android.provider.Settings
import android.service.quicksettings.Tile
import com.example.utils.SettingsDataStore
import com.example.utils.SystemSettingsHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DnsShortcutTileService : BaseTileService() {
    private var isPrivateDnsActive = false

    override fun onStartListening() {
        super.onStartListening()
        val mode = Settings.Global.getString(contentResolver, "private_dns_mode") ?: "off"
        isPrivateDnsActive = (mode == "hostname")
        updateTileState(
            if(isPrivateDnsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, 
            "Private DNS", 
            if(isPrivateDnsActive) "Enabled" else "Off"
        )
    }

    override fun onClick() {
        triggerHapticClick()
        serviceScope.launch {
            val dataStore = SettingsDataStore(this@DnsShortcutTileService)
            if (isPrivateDnsActive) {
                SystemSettingsHelper.setPrivateDns(this@DnsShortcutTileService, null)
            } else {
                val dns = dataStore.privateDnsFlow.first()
                val targetDns = if (dns == "off" || dns.isEmpty()) "dns.google" else dns
                SystemSettingsHelper.setPrivateDns(this@DnsShortcutTileService, targetDns)
            }
            isPrivateDnsActive = !isPrivateDnsActive
            updateTileState(
                if(isPrivateDnsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE, 
                "Private DNS", 
                if(isPrivateDnsActive) "Enabled" else "Off"
            )
        }
    }
}
