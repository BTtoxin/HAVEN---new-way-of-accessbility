package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdater {
    // Repository format: owner/repo
    private const val GITHUB_REPO = "ashumehta2004/Haven"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    @OptIn(DelicateCoroutinesApi::class)
    fun checkForUpdates(context: Context, downloadIfAvailable: Boolean = true, notifyUpToDate: Boolean = true) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(responseText)
                    val tagName = json.getString("tag_name")
                    val currentVersion = VersionManager.getAppVersion(context).first

                    // Simplified version parsing (assumes vX.Y.Z)
                    val cleanTag = tagName.replace("v", "").replace(".", "").toIntOrNull() ?: 0
                    val cleanCurrent = currentVersion.replace("v", "").replace(".", "").toIntOrNull() ?: 0

                    if (cleanTag > cleanCurrent || (tagName != currentVersion && cleanTag == 0)) {
                        // Found new version
                        val assets = json.getJSONArray("assets")
                        var downloadUrl = ""
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.getString("name").endsWith(".apk")) {
                                downloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                        
                        // Save latest changelog to SharedPreferences
                        val body = json.optString("body", "Bug fixes and improvements.")
                        val prefs = context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("latest_remote_version", tagName)
                                    .putString("latest_remote_changelog", body)
                                    .apply()
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "New version $tagName found on GitHub!", Toast.LENGTH_SHORT).show()
                        }

                        if (downloadIfAvailable && downloadUrl.isNotEmpty()) {
                            downloadAndInstallApk(context, downloadUrl, tagName)
                        } else if (downloadUrl.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "No APK found in the latest release.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        if (notifyUpToDate) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Haven is up to date ($currentVersion).", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Could not check for updates (HTTP ${connection.responseCode}). No release found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update check failed (API might be unavailable).", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadAndInstallApk(context: Context, url: String, versionTag: String) {
        with(context.applicationContext) {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
                .setTitle("Haven $versionTag")
                .setDescription("Downloading latest update...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Haven-$versionTag.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            installApk(ctxt, downloadManager.getUriForDownloadedFile(downloadId))
                            ctxt.unregisterReceiver(this)
                        }
                    }
                }
            }
            
            // Using RECEIVER_EXPORTED suitable for API >= 33, or just 0 for older but app targets SDK 36
            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            
            Toast.makeText(this, "Downloading update in background...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk(context: Context, apkUri: Uri?) {
        if (apkUri == null) return
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to initiate installation.", Toast.LENGTH_SHORT).show()
        }
    }
}
