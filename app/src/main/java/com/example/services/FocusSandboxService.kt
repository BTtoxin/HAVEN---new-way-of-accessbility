package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.FocusLockOverlayActivity
import com.example.utils.FocusDataStore
import kotlinx.coroutines.*
import kotlin.math.max

class FocusSandboxService : Service() {
    private val CHANNEL_ID = "FocusSandbox"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, "Focus Sandbox", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val endTime = FocusDataStore.getEndTime(this)
        val startTime = FocusDataStore.getStartTime(this)
        val totalSecs = max((endTime - startTime) / 1000, 1L).toInt()
        val remainingSecs = max((endTime - System.currentTimeMillis()) / 1000, 0L).toInt()
        val progress = (totalSecs - remainingSecs).coerceIn(0, totalSecs)

        val notification = buildProgressNotification("Lock Active", progress, totalSecs)
        startForeground(1337, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        timerJob?.cancel()
        timerJob = scope.launch {
            while (System.currentTimeMillis() < endTime) {
                val remaining = max((endTime - System.currentTimeMillis()) / 1000, 0L).toInt()
                val prog = totalSecs - remaining
                updateNotification("${formatTime(remaining)} remaining", prog, totalSecs)
                delay(1000)
            }
            stopSelf()
        }

        monitorJob?.cancel()
        monitorJob = scope.launch(Dispatchers.IO) {
            val usageManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val allowedApps = FocusDataStore.getAllowedApps(this@FocusSandboxService)
            while (isActive && System.currentTimeMillis() < endTime) {
                val now = System.currentTimeMillis()
                val events = usageManager.queryEvents(now - 1000, now)
                val event = UsageEvents.Event()
                var lastForeground = ""
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        lastForeground = event.packageName
                    }
                }
                if (lastForeground.isNotEmpty() && lastForeground !in allowedApps && lastForeground != packageName) {
                    withContext(Dispatchers.Main) {
                        val lockIntent = Intent(this@FocusSandboxService, FocusLockOverlayActivity::class.java)
                        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(lockIntent)
                    }
                }
                delay(1000)
            }
        }
        return START_STICKY
    }

    private fun buildProgressNotification(text: String, progress: Int, max: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Sandbox")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setProgress(max, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String, progress: Int, max: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1337, buildProgressNotification(text, progress, max))
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
