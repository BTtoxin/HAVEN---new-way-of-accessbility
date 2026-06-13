package com.example.utils

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DeepWorkCleanupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer_enabled", 0)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } catch (e: Exception) {
            Log.e("DeepWorkCleanupWorker", "Failed to restore state", e)
        }
        return Result.success()
    }
}
