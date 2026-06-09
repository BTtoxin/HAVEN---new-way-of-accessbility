package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class QSPreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qs_prefs", Context.MODE_PRIVATE)

    fun getCustomShortcutTarget(): String = prefs.getString("custom_shortcut_target", "android.settings.NOTIFICATION_HISTORY") ?: "android.settings.NOTIFICATION_HISTORY"
    fun setCustomShortcutTarget(target: String) = prefs.edit().putString("custom_shortcut_target", target).apply()

    fun getCustomShortcutLabel(): String = prefs.getString("custom_shortcut_label", "Notification History") ?: "Notification History"
    fun setCustomShortcutLabel(label: String) = prefs.edit().putString("custom_shortcut_label", label).apply()

    fun getGlyphBrightnessProfile(): String = prefs.getString("glyph_brightness_profile", "Essential Only") ?: "Essential Only"
    fun setGlyphBrightnessProfile(profile: String) = prefs.edit().putString("glyph_brightness_profile", profile).apply()

    fun isTheaterModeActive(): Boolean = prefs.getBoolean("theater_mode_active", false)
    fun setTheaterModeActive(active: Boolean) = prefs.edit().putBoolean("theater_mode_active", active).apply()

    fun getScreenTimeoutPresets(): List<Int> = listOf(15000, 30000, 60000, 120000, 300000, 600000, -1)

    fun getNextTimeoutPreset(currentMs: Int): Int {
        val presets = getScreenTimeoutPresets()
        val index = presets.indexOf(currentMs)
        return if (index != -1 && index + 1 < presets.size) {
            presets[index + 1]
        } else {
            presets[0]
        }
    }

    fun formatTimeoutLabel(ms: Int): String {
        return when (ms) {
            15000 -> "15s"
            30000 -> "30s"
            60000 -> "1 min"
            120000 -> "2 min"
            300000 -> "5 min"
            600000 -> "10 min"
            -1, Int.MAX_VALUE -> "Never"
            else -> "${ms / 1000}s"
        }
    }
}
