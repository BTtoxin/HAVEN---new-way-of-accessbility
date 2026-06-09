package com.example.services

import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import android.service.quicksettings.Tile

class NfcToggleTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        
        // Cannot toggle NFC directly without system/root permissions in newer Android versions
        // We will open NFC Settings
        val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        launchSafeIntentAndCollapse(intent)
    }

    private fun updateState() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            updateTileState(Tile.STATE_UNAVAILABLE, "NFC", "Not Supported")
        } else {
            val isEnabled = nfcAdapter.isEnabled
            val state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTileState(state, "NFC", if (isEnabled) "On" else "Off")
        }
    }
}
