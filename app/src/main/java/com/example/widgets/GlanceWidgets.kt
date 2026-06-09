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
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.services.CaffeineWakeLockService
import com.example.utils.FocusDataStore
import com.example.utils.NetworkPinger
import com.example.utils.SettingsDataStore
import kotlinx.coroutines.flow.first

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
