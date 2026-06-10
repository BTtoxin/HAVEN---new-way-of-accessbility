package com.example.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object FocusDataStore {
    private const val PREFS_NAME = "focus_prefs"

    fun getEndTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong("end_time", 0L)
    }

    fun getStartTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong("start_time", 0L)
    }

    fun setTimes(context: Context, startTime: Long, endTime: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val oldStart = prefs.getLong("start_time", 0L)
        val oldEnd = prefs.getLong("end_time", 0L)
        
        // Log previous session if it just ended or was cancelled
        if (oldEnd > 0 && endTime == 0L) {
            val actualEnd = System.currentTimeMillis().coerceAtMost(oldEnd)
            if (actualEnd > oldStart) {
                logCompletedSession(context, oldStart, actualEnd, actualEnd >= oldEnd)
            }
        }

        prefs.edit()
            .putLong("start_time", startTime)
            .putLong("end_time", endTime)
            .apply()
    }

    private fun logCompletedSession(context: Context, start: Long, end: Long, completed: Boolean) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val db = com.example.data.UserPrefDatabase.getDatabase(context)
            db.focusSessionDao().insertSession(
                com.example.data.FocusSessionEntity(
                    startTime = start,
                    endTime = end,
                    completed = completed
                )
            )
        }
    }

    fun getSessionHistory(context: Context): List<FocusSession> {
        val historyStr = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("session_history", "[]") ?: "[]"
        val list = mutableListOf<FocusSession>()
        try {
            val array = JSONArray(historyStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(FocusSession(obj.getLong("start"), obj.getLong("end"), obj.getBoolean("completed")))
            }
        } catch (e: Exception) {}
        return list.reversed()
    }

    fun getAllowedApps(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getStringSet("allowed_apps", emptySet()) ?: emptySet()
    }

    fun setAllowedApps(context: Context, apps: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putStringSet("allowed_apps", apps)
            .apply()
    }

    fun isSandboxActive(context: Context): Boolean {
        return getEndTime(context) > System.currentTimeMillis()
    }
}

data class FocusSession(val start: Long, val end: Long, val completed: Boolean)

