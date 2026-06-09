package com.example.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile

class NotifFilterTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            val filter = nm.currentInterruptionFilter
            val nextFilter = when (filter) {
                NotificationManager.INTERRUPTION_FILTER_ALL -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                NotificationManager.INTERRUPTION_FILTER_PRIORITY -> NotificationManager.INTERRUPTION_FILTER_NONE
                else -> NotificationManager.INTERRUPTION_FILTER_ALL
            }
            nm.setInterruptionFilter(nextFilter)
        } else {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchSafeIntentAndCollapse(intent)
        }
        updateState()
    }

    private fun updateState() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            val filter = nm.currentInterruptionFilter
            when (filter) {
                NotificationManager.INTERRUPTION_FILTER_ALL -> updateTileState(Tile.STATE_INACTIVE, "Notif Filter", "All")
                NotificationManager.INTERRUPTION_FILTER_PRIORITY -> updateTileState(Tile.STATE_ACTIVE, "Notif Filter", "Priority")
                NotificationManager.INTERRUPTION_FILTER_NONE -> updateTileState(Tile.STATE_ACTIVE, "Notif Filter", "None")
                else -> updateTileState(Tile.STATE_INACTIVE, "Notif Filter", "Unknown")
            }
        } else {
            updateTileState(Tile.STATE_INACTIVE, "Notif Filter", "Requires Prm")
        }
    }
}
