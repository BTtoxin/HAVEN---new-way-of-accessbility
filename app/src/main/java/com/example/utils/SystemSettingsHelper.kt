package com.example.utils

import android.content.Context
import android.provider.Settings

object SystemSettingsHelper {
    fun hasWriteSettingsPermission(context: Context): Boolean = Settings.System.canWrite(context)

    fun getSystemInt(context: Context, key: String, default: Int): Int {
        return try {
            Settings.System.getInt(context.contentResolver, key, default)
        } catch (e: Exception) {
            default
        }
    }

    fun setSystemInt(context: Context, key: String, value: Int): Boolean {
        return try {
            Settings.System.putInt(context.contentResolver, key, value)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getScreenOffTimeout(context: Context): Int = getSystemInt(context, Settings.System.SCREEN_OFF_TIMEOUT, 30000)

    fun setScreenOffTimeout(context: Context, ms: Int): Boolean = setSystemInt(context, Settings.System.SCREEN_OFF_TIMEOUT, ms)

    fun setScreenBrightness(context: Context, value: Int): Boolean = setSystemInt(context, Settings.System.SCREEN_BRIGHTNESS, value)

    fun getPrivateDnsMode(context: Context): String {
        return Settings.Global.getString(context.contentResolver, "private_dns_mode") ?: "off"
    }

    fun setPrivateDns(context: Context, hostname: String?): Boolean {
        return try {
            if (hostname == null) {
                Settings.Global.putString(context.contentResolver, "private_dns_mode", "off")
            } else {
                Settings.Global.putString(context.contentResolver, "private_dns_mode", "hostname")
                Settings.Global.putString(context.contentResolver, "private_dns_specifier", hostname)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openPrivateDnsSettings(context: Context) {
        try {
            val intent = android.content.Intent("android.settings.PRIVATE_DNS_SETTINGS").apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = android.content.Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                // Ignore fallback
            }
        }
    }
}
