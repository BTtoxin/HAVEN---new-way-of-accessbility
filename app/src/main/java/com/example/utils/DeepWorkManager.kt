package com.example.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DeepWorkManager(private val context: Context) {
    fun activateDeepWork(durationMinutes: Long = 60L) {
        val inputData = Data.Builder()
            .putLong("duration_minutes", durationMinutes)
            .build()
            
        val deepWorkRequest = OneTimeWorkRequestBuilder<DeepWorkWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "DeepWorkSession",
            ExistingWorkPolicy.REPLACE,
            deepWorkRequest
        )
    }

    fun deactivateDeepWork() {
        WorkManager.getInstance(context).cancelUniqueWork("DeepWorkSession")
        // Enqueue a cleanup worker if needed, or rely on CoroutineWorker cancellation
        val cleanupRequest = OneTimeWorkRequestBuilder<DeepWorkCleanupWorker>().build()
        WorkManager.getInstance(context).enqueue(cleanupRequest)
    }
}

