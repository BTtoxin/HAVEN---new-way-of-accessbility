package com.example.utils

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

object VersionManager {

    /**
     * Returns the definitive current version of the app.
     * Priority: BuildConfig.APP_VERSION_NAME (from build.gradle) → changelog.json fallback
     */
    fun getAppVersion(context: Context): Pair<String, String> {
        return try {
            val versionName = "v${com.example.BuildConfig.APP_VERSION_NAME}"
            val inputStream = context.assets.open("changelog.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonText)
            val lastUpdated = jsonObject.optString("lastUpdated", "Unknown")
            Pair(versionName, lastUpdated)
        } catch (e: Exception) {
            try {
                val inputStream = context.assets.open("changelog.json")
                val jsonText = InputStreamReader(inputStream).readText()
                val jsonObject = JSONObject(jsonText)
                Pair(
                    jsonObject.optString("currentVersion", "v1.4.0"),
                    jsonObject.optString("lastUpdated", "Unknown")
                )
            } catch (e2: Exception) {
                Pair("v${com.example.BuildConfig.APP_VERSION_NAME}", "Unknown")
            }
        }
    }

    /**
     * Returns the full version string including build code for display.
     * e.g. "v1.4.0 (build 4)"
     */
    fun getFullVersionString(context: Context): String {
        return try {
            "v${com.example.BuildConfig.APP_VERSION_NAME} (build ${com.example.BuildConfig.APP_VERSION_CODE})"
        } catch (e: Exception) {
            getAppVersion(context).first
        }
    }

    fun checkVersionDiscrepancy(context: Context): Boolean {
        val appVersionStr = try { "v${com.example.BuildConfig.APP_VERSION_NAME}" } catch (e: Exception) { "v1.4.0" }
        val (changelogVersion, _) = getAppVersion(context)
        return appVersionStr != changelogVersion
    }

    fun checkAndNotifyNewVersion(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lastSeenVersion = prefs.getString("last_seen_version", "")
        val currentVersion = getAppVersion(context).first

        if (lastSeenVersion != currentVersion) {
            android.widget.Toast.makeText(
                context,
                "New version detected ($currentVersion)! Check the Changelog for details.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            prefs.edit().putString("last_seen_version", currentVersion).apply()
        }
    }

    fun getChangelogEntries(context: Context): List<ChangelogEntry> {
        val entries = mutableListOf<ChangelogEntry>()

        // 1. Load local changelog.json history
        try {
            val inputStream = context.assets.open("changelog.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonText)
            val historyArray = jsonObject.getJSONArray("history")
            for (i in 0 until historyArray.length()) {
                val entryObj = historyArray.getJSONObject(i)
                val version = entryObj.getString("version")
                val date = entryObj.getString("date")
                val changesArray = entryObj.getJSONArray("changes")
                val changes = mutableListOf<ChangeItem>()
                for (j in 0 until changesArray.length()) {
                    val changeObj = changesArray.getJSONObject(j)
                    val text = changeObj.getString("text")
                    val tag = changeObj.getString("tag")
                    val details = if (changeObj.has("details")) changeObj.getString("details") else ""
                    changes.add(ChangeItem(text, tag, details))
                }
                entries.add(ChangelogEntry(version, date, changes))
            }
        } catch (e: Exception) {
            // minimal fallback
            entries.add(
                ChangelogEntry(
                    version = "v${com.example.BuildConfig.APP_VERSION_NAME}",
                    date = "Unknown",
                    changes = listOf(ChangeItem("App launched.", "Release", ""))
                )
            )
        }

        // 2. Check for cached remote release
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val remoteVersion = prefs.getString("latest_remote_version", "") ?: ""
        val remoteChangelog = prefs.getString("latest_remote_changelog", "") ?: ""
        val remotePublishedAt = prefs.getString("latest_remote_published_at", "") ?: ""

        if (remoteVersion.isNotEmpty() && remoteChangelog.isNotEmpty()) {
            // Only add remote entry if it's newer than or equal to what we already have
            val alreadyExists = entries.any { it.version == remoteVersion }
            if (!alreadyExists) {
                val changeItems = parseMarkdownBullets(remoteChangelog)
                val displayDate = if (remotePublishedAt.isNotEmpty()) {
                    try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                        val date = sdf.parse(remotePublishedAt)
                        java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(date!!)
                    } catch (e: Exception) { "Remote Release" }
                } else "Remote Release"

                entries.add(0, ChangelogEntry(remoteVersion, displayDate, changeItems))
            }
        }

        return entries
    }

    private fun parseMarkdownBullets(markdown: String): List<ChangeItem> {
        val items = mutableListOf<ChangeItem>()
        val lines = markdown.split("\n")
        var currentHeading = "Update"

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("##") -> {
                    currentHeading = trimmed.replace("#", "").trim()
                        .let { h ->
                            when {
                                h.contains("feat", ignoreCase = true) || h.contains("new", ignoreCase = true) -> "Feature"
                                h.contains("fix", ignoreCase = true) || h.contains("bug", ignoreCase = true) -> "Bug Fix"
                                h.contains("optim", ignoreCase = true) || h.contains("perf", ignoreCase = true) -> "Optimization"
                                h.contains("refact", ignoreCase = true) -> "Refactor"
                                else -> h.take(12)
                            }
                        }
                }
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val text = trimmed.substring(1).trim()
                    if (text.isNotBlank()) {
                        items.add(ChangeItem(text, currentHeading, "From GitHub Release"))
                    }
                }
            }
        }

        if (items.isEmpty() && markdown.isNotBlank()) {
            items.add(ChangeItem(markdown.take(200), "Update", "From GitHub Release"))
        }

        return items
    }
}

data class ChangeItem(val text: String, val tag: String, val details: String = "")
data class ChangelogEntry(val version: String, val date: String, val changes: List<ChangeItem>)

data class ManualSection(
    val sectionTitle: String,
    val icon: String,
    val features: List<ManualFeature>
)

data class ManualFeature(
    val title: String,
    val description: String,
    val tip: String = "",
    val isNew: Boolean = false
)

object ManualContent {
    fun getSections(context: Context): List<ManualSection> {
        val sections = mutableListOf<ManualSection>()

        sections.add(ManualSection(
            sectionTitle = "GETTING STARTED",
            icon = "home",
            features = listOf(
                ManualFeature("Dashboard Home", "The main screen shows your device stats, quick toggles, and feature tiles. Scroll down to access all features.", "Triple-tap the HAVEN logo to open System Diagnostics overlay."),
                ManualFeature("Bottom Search Bar", "Use the search bar at the bottom to instantly filter and jump to any feature tile.", "Voice icon opens the AI voice command interface."),
                ManualFeature("Edit Layout", "Tap the pencil icon in the header to enter edit mode. Drag or use arrow buttons to reorder tiles.", "Your layout is saved per user profile in the local database."),
                ManualFeature("Grid vs List", "Toggle between 2-column grid view and single-column list view using the grid icon in the header.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "QUICK TOGGLES",
            icon = "grid_view",
            features = listOf(
                ManualFeature("Wi-Fi", "Toggle Wi-Fi connectivity. Long-press a tile to pin it.", "Tap the tile in the 2×2 grid to toggle on/off."),
                ManualFeature("Bluetooth", "Toggle Bluetooth. Long-press for device settings.", ""),
                ManualFeature("Mobile Data", "Toggle mobile data on/off.", "Long-press to access network operator settings."),
                ManualFeature("Hotspot", "Enable/disable personal hotspot.", ""),
                ManualFeature("Airplane + BT Combo", "Quick Tiles panel tile to toggle airplane mode while keeping Bluetooth active.", "Add from Android Quick Tiles panel."),
                ManualFeature("Flashlight", "Toggle device flashlight directly from the Quick Toggle grid.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "FEATURE TILES",
            icon = "dashboard",
            features = listOf(
                ManualFeature("Screen Timeout", "Cycle through preset screen timeout values (15s → 30s → 1m → 5m → 10m → 30m).", "Requires WRITE_SETTINGS permission."),
                ManualFeature("Caffeine Mode", "Keeps the screen awake indefinitely using a wake lock. Set a duration or disable anytime.", "Active when the pulsing red dot is visible."),
                ManualFeature("Battery Statistics", "Tap the BATTERY tile for detailed stats. Tap again to request an AI-powered battery life prediction via Gemini.", ""),
                ManualFeature("Brightness Control", "Adjust screen brightness with the slider. Lock brightness to prevent auto-adjustment.", "Requires WRITE_SETTINGS permission."),
                ManualFeature("Private DNS", "Toggle your custom Private DNS preset on or off. Tap 'DEEP SETTINGS' to configure the preset.", ""),
                ManualFeature("Theater Mode", "Activates DND + dims screen + lowers audio — ideal for movie watching.", "Customize brightness/audio levels in Settings."),
                ManualFeature("Clipboard Manager", "Purge your clipboard contents instantly. Go to the Clipboard screen for history and auto-clear settings.", ""),
                ManualFeature("Deep Focus", "Start a timed focus session that locks out distracting apps. Supports Pomodoro mode (25 min work + 5 min break).", "Requires Usage Access and Overlay permissions."),
                ManualFeature("Custom Shortcut", "Configure a custom deep-link or settings action to launch with one tap.", "Configure in Settings → Custom Shortcut."),
                ManualFeature("App Audio Isolation", "Route all app audio through a system audio focus so background apps are muted.", ""),
                ManualFeature("Network (Operator)", "Opens carrier/network settings for SIM management.", ""),
                ManualFeature("Glyph Profile", "Select Glyph LED brightness profile: Essential Only, Full Glyph, or Silent.", "Nothing Phone only.")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "FOCUS & PRODUCTIVITY",
            icon = "self_improvement",
            features = listOf(
                ManualFeature("Focus Sessions", "Start a timed Deep Focus session from the Focus tab or the Dashboard tile. Set duration (5–120 min), configure allowed apps.", ""),
                ManualFeature("Pomodoro Mode", "Enables automatic 5-minute breaks after each focus session. Toggle inside the DEEP FOCUS tile.", ""),
                ManualFeature("Focus History", "Access detailed session history with duration charts. Tap the arrow in the DEEP FOCUS tile's recent sessions row.", ""),
                ManualFeature("Focus Streak", "Your daily focus streak (consecutive days with at least one completed session) is shown on the main clock card.", "Keep your streak alive!"),
                ManualFeature("Whitelist Apps", "Select which apps are accessible during a Focus session. All other apps are blocked by the system overlay.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "AUTOMATIONS",
            icon = "bolt",
            features = listOf(
                ManualFeature("Automation Rules", "Create trigger-based rules (time, battery level, charging, network) to automatically toggle features.", "Access via the ⚡ button in the header or Navigation → Settings."),
                ManualFeature("Rule Conditions", "Each rule can have multiple conditions (AND logic). e.g. 'When battery < 20% AND not charging → enable Battery Saver Tile'.", ""),
                ManualFeature("Enable/Disable Rules", "Toggle individual rules on/off without deleting them.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "QUICK TILES (ANDROID QS PANEL)",
            icon = "tune",
            features = listOf(
                ManualFeature("Adding Tiles", "Swipe down twice to open the full Quick Settings panel → tap the pencil/edit icon → drag HAVEN tiles into your active tiles.", "Tap 'How to add Quick Tiles' (? button) in the header for a visual guide."),
                ManualFeature("Available QS Tiles", "Caffeine, Theater Mode, Private DNS, Clipboard Purge, Screen Timeout, Battery Saver, ADB Wireless, Grayscale, NFC Toggle, Mute Mic, RAM Cleaner, and more.", ""),
                ManualFeature("Tile State Sync", "QS tile states stay in sync with in-app toggles.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "WIDGETS",
            icon = "widgets",
            features = listOf(
                ManualFeature("Caffeine Widget", "Shows Caffeine status. Tap to toggle from your home screen.", "Long-press home screen → Widgets → HAVEN."),
                ManualFeature("Battery Status Widget", "Displays current battery percentage and status.", ""),
                ManualFeature("Clock Pill Widget", "Minimal clock widget in Nothing OS pill style.", ""),
                ManualFeature("Tile Shortcut Widget", "Launches a configured HAVEN shortcut from the home screen.", ""),
                ManualFeature("Focus Launcher Widget", "Shows active Focus session countdown on home screen.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "SYSTEM TOOLS",
            icon = "sensors",
            features = listOf(
                ManualFeature("Sensor Dashboard", "View live readings from device sensors: accelerometer, gyroscope, light, proximity, etc.", "Access via 📡 Sensors button in the header."),
                ManualFeature("Clipboard Screen", "Full clipboard history, auto-clear interval settings, and manual purge.", "Access via 📋 icon in header or Navigation."),
                ManualFeature("System Diagnostics", "Triple-tap the HAVEN logo to open a live diagnostics overlay showing CPU, memory, and system logs.", ""),
                ManualFeature("Notifications Overlay", "Drag down from the top of the Dashboard to reveal the in-app notifications panel.", "")
            )
        ))

        sections.add(ManualSection(
            sectionTitle = "PROFILE & SETTINGS",
            icon = "person",
            features = listOf(
                ManualFeature("User Profiles", "Create local user profiles to save your layout, theme, and preferences per user.", "Data is stored locally in Room database — no cloud upload."),
                ManualFeature("Theme Palette", "Choose from multiple color palettes: HAVEN (default), Ocean, Forest, Sunset, etc.", ""),
                ManualFeature("Dark / Light Mode", "Toggle between Dark, Light, or System-default theme via the 🌙 button in the header.", ""),
                ManualFeature("Voice Commands", "Use the 🎤 icon in the search bar to issue natural language commands like 'Turn on caffeine' or 'Start focus for 30 minutes'.", ""),
                ManualFeature("Changelog", "View the full version history of Haven. New GitHub releases are automatically fetched and appear here.", ""),
                ManualFeature("Updates", "Haven checks for new APK releases on GitHub automatically in the background. Go to Settings → Check for Updates for a manual check.", "")
            )
        ))

        try {
            val entries = VersionManager.getChangelogEntries(context)
            val currentVersionEntry = entries.firstOrNull()
            if (currentVersionEntry != null) {
                val newFeatures = currentVersionEntry.changes
                    .filter { it.tag == "Feature" }
                    .map { ManualFeature(it.text, it.details.ifBlank { "New in ${currentVersionEntry.version}" }, "", isNew = true) }

                if (newFeatures.isNotEmpty()) {
                    sections.add(0, ManualSection(
                        sectionTitle = "NEW IN ${currentVersionEntry.version.uppercase()}",
                        icon = "new_releases",
                        features = newFeatures
                    ))
                }
            }
        } catch (e: Exception) {
            // skip dynamic section on error
        }

        return sections
    }
}
