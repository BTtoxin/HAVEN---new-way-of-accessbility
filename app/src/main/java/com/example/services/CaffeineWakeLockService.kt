package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CaffeineWakeLockService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "GlyphQS:Caffeine")

        val dataStore = SettingsDataStore(this)
        scope.launch {
            val durationMin = dataStore.caffeineDurationFlow.first().let { if (it <= 0) 30 else it }
            val durationMs = durationMin * 60_000L
            wakeLock?.acquire(durationMs)

            val notification = NotificationCompat.Builder(this@CaffeineWakeLockService, createChannel())
                .setContentTitle("Caffeine Active")
                .setContentText("Screen will stay on for $durationMin minutes")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build()
                
            startForeground(1338, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

            delay(durationMs)
            dataStore.setCaffeineActive(false)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun createChannel(): String {
        val channel = NotificationChannel("caffeine", "Caffeine", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return "caffeine"
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
