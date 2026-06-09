package com.example.services

import android.app.ActivityManager
import android.content.Context
import android.service.quicksettings.Tile
import android.widget.Toast

class RamCleanerTileService : BaseTileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE, "RAM Clean", "Tap to Free")
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()

        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses
        var killedCount = 0

        if (runningApps != null) {
            val myPid = android.os.Process.myPid()
            for (processInfo in runningApps) {
                if (processInfo.pid != myPid && processInfo.processName != packageName) {
                    try {
                        am.killBackgroundProcesses(processInfo.processName)
                        killedCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        
        Toast.makeText(this, "Cleared $killedCount background processes", Toast.LENGTH_SHORT).show()
        updateTileState(Tile.STATE_INACTIVE, "RAM Clean", "Cleaned $killedCount")
        
        // Reset label after delay using coroutine or java timer not available here without scope, 
        // so we just let it stay until next start listening.
    }
}
