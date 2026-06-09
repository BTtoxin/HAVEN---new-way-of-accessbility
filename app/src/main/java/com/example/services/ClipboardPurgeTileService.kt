package com.example.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.service.quicksettings.Tile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardPurgeTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_ACTIVE, "Purge Clipboard", "Tap to clear") 
    }

    override fun onClick() {
        triggerHapticClick()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        updateTileState(Tile.STATE_ACTIVE, "Purge Clipboard", "Cleared ✓")
        
        serviceScope.launch { 
            delay(2000)
            updateTileState(Tile.STATE_ACTIVE, "Purge Clipboard", "Tap to clear") 
        }
    }
}
