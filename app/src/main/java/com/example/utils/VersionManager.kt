package com.example.utils

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

object VersionManager {
    fun getAppVersion(context: Context): Pair<String, String> {
        return try {
            val inputStream = context.assets.open("changelog.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonText)
            val currentVersion = jsonObject.getString("currentVersion")
            val lastUpdated = jsonObject.getString("lastUpdated")
            Pair(currentVersion, lastUpdated)
        } catch (e: Exception) {
            Pair("v1.4.0", "Unknown")
        }
    }

    fun checkVersionDiscrepancy(context: Context): Boolean {
        val appVersionStr = "v${com.example.BuildConfig.METADATA_VERSION}"
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
            // fallback
            entries.add(
                ChangelogEntry(
                    version = "v1.4.0",
                    date = "June 10, 2026, 13:24 PM",
                    changes = listOf(
                        ChangeItem("Added daily focus-streak badge on dashboard utilizing Room database.", "Feature", "Local persistence setup for daily focus streak tracking.")
                    )
                )
            )
        }
        
        // Check for remote latest version in preferences
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val remoteVersion = prefs.getString("latest_remote_version", "")
        val remoteChangelog = prefs.getString("latest_remote_changelog", "")
        
        if (!remoteVersion.isNullOrEmpty() && !remoteChangelog.isNullOrEmpty()) {
            val isNewer = entries.isEmpty() || remoteVersion != entries.first().version
            if (isNewer) {
                // Parse markdown-like bullet points to list of ChangeItems
                val changeItems = mutableListOf<ChangeItem>()
                val lines = remoteChangelog.split("\n")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                        changeItems.add(ChangeItem(trimmed.substring(1).trim(), "Update", "From GitHub Release"))
                    }
                }
                if (changeItems.isEmpty()) {
                    changeItems.add(ChangeItem(remoteChangelog, "Update", "From GitHub Release"))
                }
                
                // Add at the top
                entries.add(0, ChangelogEntry(remoteVersion, "Latest Remote Release", changeItems))
            }
        }
        
        return entries
    }
}

data class ChangeItem(val text: String, val tag: String, val details: String = "")
data class ChangelogEntry(val version: String, val date: String, val changes: List<ChangeItem>)
