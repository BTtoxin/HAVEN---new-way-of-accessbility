package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.utils.ClipboardWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("glyph_settings", Context.MODE_PRIVATE)
            val interval = prefs.getInt("clipboard_interval", 0)
            if (interval > 0) {
                val request = PeriodicWorkRequestBuilder<ClipboardWorker>(interval.toLong(), TimeUnit.MINUTES).build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "clipboard_purge",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
            }
        }
    }
}
