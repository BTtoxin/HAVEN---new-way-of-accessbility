package com.example.utils

import android.content.Context

object FocusDataStore {
    private const val PREFS_NAME = "focus_prefs"

    fun getEndTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong("end_time", 0L)
    }

    fun getStartTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong("start_time", 0L)
    }

    fun setTimes(context: Context, startTime: Long, endTime: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong("start_time", startTime)
            .putLong("end_time", endTime)
            .apply()
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
