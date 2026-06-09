package com.example.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.width
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.background

import com.example.MainActivity
import com.example.services.CaffeineWakeLockService
import com.example.utils.FocusDataStore
import com.example.utils.NetworkPinger
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.remember

class OpenBatterySettingsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
class OpenDateSettingsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(android.provider.Settings.ACTION_DATE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
class OpenSoundSettingsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent("android.settings.SOUND_SETTINGS").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
    }
}
class OpenWifiSettingsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(intent)
    }
}

class CaffeineWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isActive = SettingsDataStore(context).isCaffeineActiveFlow.first()
        provideContent {
            GlanceTheme {
                Box(
                    GlanceModifier.fillMaxSize()
                        .background(if (isActive) Color.Black else Color.LightGray)
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "CAFFEINE",
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Gray))
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            if (isActive) "ON" else "OFF",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(if (isActive) Color.White else Color.Black)
                            )
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Button("Toggle", actionRunCallback<CaffeineToggleAction>())
                    }
                }
            }
        }
    }
}

class CaffeineToggleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val dataStore = SettingsDataStore(context)
        val isOn = dataStore.isCaffeineActiveFlow.first()
        dataStore.setCaffeineActive(!isOn)
        if (!isOn) {
            context.startForegroundService(Intent(context, CaffeineWakeLockService::class.java))
        } else {
            context.stopService(Intent(context, CaffeineWakeLockService::class.java))
        }
        CaffeineWidget().update(context, glanceId)
    }
}

class CaffeineWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CaffeineWidget()
}

class SystemPillWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pingMs = NetworkPinger.pingCloudflare()
        val statusText = when {
            pingMs < 0 -> "OFFLINE"
            pingMs < 50 -> "${pingMs}ms ●"
            pingMs < 150 -> "${pingMs}ms ◑"
            else -> "${pingMs}ms ○"
        }
        provideContent {
            GlanceTheme {
                Row(
                    GlanceModifier.fillMaxWidth()
                        .background(ColorProvider(Color.Black))
                        .cornerRadius(50.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NET", style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.Gray), fontWeight = FontWeight.Bold))
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        statusText,
                        style = TextStyle(fontSize = 12.sp, color = ColorProvider(if (pingMs < 0) Color.Red else Color.White), fontWeight = FontWeight.Bold)
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Button("↻", actionRunCallback<PingCloudflareAction>())
                }
            }
        }
    }
}

class PingCloudflareAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        SystemPillWidget().update(context, glanceId)
    }
}

class SystemPillWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SystemPillWidget()
}

class FocusLauncherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isActive = FocusDataStore.isSandboxActive(context)
        provideContent {
            GlanceTheme {
                Box(
                    GlanceModifier.fillMaxSize()
                        .background(ColorProvider(Color.Black))
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    Column {
                        Text("DEEP FOCUS", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.Gray), fontWeight = FontWeight.Bold))
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            if (isActive) "ACTIVE" else "READY",
                            style = TextStyle(fontSize = 22.sp, color = ColorProvider(Color.White), fontWeight = FontWeight.Bold)
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        if (!isActive) {
                            Button("START", actionRunCallback<StartFocusAction>())
                        } else {
                            Text("Session in progress", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.Red)))
                        }
                    }
                }
            }
        }
    }
}

class StartFocusAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("openFocus", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}

class FocusLauncherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FocusLauncherWidget()
}

class BatteryStatusWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0
        val isCharging = intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) == android.os.BatteryManager.BATTERY_STATUS_CHARGING

        provideContent {
            GlanceTheme {
                Row(
                    modifier = GlanceModifier.fillMaxSize()
                        .background(Color(0xFF101010))
                        .cornerRadius(24.dp)
                        .padding(12.dp)
                        .clickable(actionRunCallback<OpenBatterySettingsAction>()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCharging) "⚡" else "🔋",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp),
                        modifier = GlanceModifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$batteryPct%",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

class BatteryStatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BatteryStatusWidget()
}

class ClockPillWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Row(
                    modifier = GlanceModifier.fillMaxSize()
                        .background(Color(0xFF101010))
                        .cornerRadius(24.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(actionRunCallback<OpenDateSettingsAction>()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "20:30",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = " • MON",
                        style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

class ClockPillWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ClockPillWidget()
}

class ClipboardPurgeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier.fillMaxSize()
                        .background(Color(0xFFEA3B3B))
                        .cornerRadius(12.dp)
                        .clickable(actionRunCallback<ClipboardPurgeAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PURGE",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

class ClipboardPurgeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            clipboard.clearPrimaryClip()
        }
        com.example.utils.AudioHapticEngine.triggerSuccess(context)
    }
}

class ClipboardPurgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ClipboardPurgeWidget()
}

class TileShortcutWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Row(
                    modifier = GlanceModifier.fillMaxSize().background(Color(0xFF101010)).cornerRadius(12.dp).padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(text = "Ring", onClick = actionRunCallback<OpenSoundSettingsAction>(), modifier = GlanceModifier.defaultWeight())
                    Button(text = "WiFi", onClick = actionRunCallback<OpenWifiSettingsAction>(), modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

class TileShortcutWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TileShortcutWidget()
}
