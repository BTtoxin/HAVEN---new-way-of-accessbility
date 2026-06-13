package com.example.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DeepWorkWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DeepWorkWorker", "Deep Work Session Started using WorkManager")
        
        try {
            setForeground(createForegroundInfo())
        } catch (e: Exception) {
            Log.e("DeepWorkWorker", "Foreground service start failed", e)
        }

        val durationMinutes = inputData.getLong("duration_minutes", 60L)
        
        enableDnd()
        enableGrayscale()
        minimizeVolume()

        // Focus timer logic - keep worker alive
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        while (System.currentTimeMillis() < endTime && !isStopped) {
            // Check app blocking or focus state
            delay(5000L) // Check every 5 seconds
        }

        // Restore state when finished or stopped
        restoreState()

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "DeepWork"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Deep Work", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Deep Work Active")
            .setContentText("Focus session in progress")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ForegroundInfo(1338, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            ForegroundInfo(1338, notification)
        }
    }

    private fun enableDnd() {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        } catch (e: Exception) {
            Log.e("DeepWorkWorker", "Failed to enable DND", e)
        }
    }

    private fun enableGrayscale() {
        try {
            Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer_enabled", 1)
            Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer", 0)
        } catch (e: Exception) {
            Log.e("DeepWorkWorker", "Grayscale permission missing", e)
        }
    }

    private fun minimizeVolume() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            Log.e("DeepWorkWorker", "Mute volume failed", e)
        }
    }

    private fun restoreState() {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer_enabled", 0)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } catch (e: Exception) {
            Log.e("DeepWorkWorker", "Failed to restore state", e)
        }
    }
}
