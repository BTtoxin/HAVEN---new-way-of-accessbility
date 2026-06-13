package com.example.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseTileService : TileService() {
    protected val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    protected fun updateTileState(state: Int, label: String, subtitle: String? = null) {
        qsTile?.apply {
            this.state = state
            this.label = label
            if (Build.VERSION.SDK_INT >= 29 && subtitle != null) {
                this.subtitle = subtitle
            }
            updateTile()
        }
    }

    protected fun triggerHapticClick() {
        (getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                it.vibrate(30)
            }
        }
    }

    protected fun launchSafeIntentAndCollapse(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
                startActivityAndCollapse(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
