package com.example.services

import android.content.Intent
import android.net.Uri
import android.service.quicksettings.Tile
import android.widget.Toast
import com.example.utils.QSPreferenceManager

class CustomShortcutTileService : BaseTileService() {
    private val prefManager by lazy { QSPreferenceManager(this) }

    override fun onStartListening() {
        super.onStartListening()
        val label = prefManager.getCustomShortcutLabel()
        updateTileState(Tile.STATE_ACTIVE, label, "Custom shortcut")
    }

    override fun onClick() {
        triggerHapticClick()
        val target = prefManager.getCustomShortcutTarget()
        try {
            launchSafeIntentAndCollapse(Intent(target))
        } catch(e: Exception) {
            try { 
                launchSafeIntentAndCollapse(Intent(Intent.ACTION_VIEW, Uri.parse(target))) 
            } catch(e2: Exception) { 
                Toast.makeText(this, "Cannot launch shortcut", Toast.LENGTH_SHORT).show() 
            }
        }
    }
}
