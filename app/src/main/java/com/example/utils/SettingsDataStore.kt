package com.example.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "glyph_settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val CAFFEINE_DURATION = intPreferencesKey("caffeine_duration")
        val THEATER_BRIGHTNESS = intPreferencesKey("theater_brightness")
        val THEATER_SYSTEM_AUDIO = intPreferencesKey("theater_system_audio")
        val THEATER_APP_AUDIO = intPreferencesKey("theater_app_audio")
        val THEATER_DND = booleanPreferencesKey("theater_dnd")
        val CLIPBOARD_INTERVAL = intPreferencesKey("clipboard_interval")
        val PRIVATE_DNS = stringPreferencesKey("private_dns")
        val IS_DNS_ACTIVE = booleanPreferencesKey("is_dns_active")
        val IS_CAFFEINE_ACTIVE = booleanPreferencesKey("is_caffeine_active")
        val IS_THEATER_ACTIVE = booleanPreferencesKey("is_theater_active")
        val IS_APP_AUDIO_ISOLATED = booleanPreferencesKey("is_app_audio_isolated")
        val FOCUS_SANDBOX_ACTIVE = booleanPreferencesKey("focus_sandbox_active")
        val FOCUS_END_TIME = longPreferencesKey("focus_end_time")
        val FOCUS_START_TIME = longPreferencesKey("focus_start_time")
        val FOCUS_ALLOWED_APPS = stringSetPreferencesKey("focus_allowed_apps")
        val IS_MONOCHROME = booleanPreferencesKey("is_monochrome")
        val SELECTED_PALETTE = stringPreferencesKey("selected_palette")
        val TILE_ORDER = stringPreferencesKey("tile_order")
    }

    val caffeineDurationFlow: Flow<Int> = context.dataStore.data.map { it[CAFFEINE_DURATION] ?: -1 }
    val theaterBrightnessFlow: Flow<Int> = context.dataStore.data.map { it[THEATER_BRIGHTNESS] ?: 5 }
    val theaterSystemAudioFlow: Flow<Int> = context.dataStore.data.map { it[THEATER_SYSTEM_AUDIO] ?: 50 }
    val theaterAppAudioFlow: Flow<Int> = context.dataStore.data.map { it[THEATER_APP_AUDIO] ?: 30 }
    val theaterDndFlow: Flow<Boolean> = context.dataStore.data.map { it[THEATER_DND] ?: true }
    val clipboardIntervalFlow: Flow<Int> = context.dataStore.data.map { it[CLIPBOARD_INTERVAL] ?: 0 }
    val privateDnsFlow: Flow<String> = context.dataStore.data.map { it[PRIVATE_DNS] ?: "dns.google" }
    val isDnsActiveFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_DNS_ACTIVE] ?: false }
    val isCaffeineActiveFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_CAFFEINE_ACTIVE] ?: false }
    val isTheaterActiveFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_THEATER_ACTIVE] ?: false }
    val isAppAudioIsolatedFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_APP_AUDIO_ISOLATED] ?: false }
    val focusSandboxActiveFlow: Flow<Boolean> = context.dataStore.data.map { it[FOCUS_SANDBOX_ACTIVE] ?: false }
    val focusEndTimeFlow: Flow<Long> = context.dataStore.data.map { it[FOCUS_END_TIME] ?: 0L }
    val focusStartTimeFlow: Flow<Long> = context.dataStore.data.map { it[FOCUS_START_TIME] ?: 0L }
    val focusAllowedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { it[FOCUS_ALLOWED_APPS] ?: emptySet() }
    val isMonochromeFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_MONOCHROME] ?: false }
    val selectedPaletteFlow: Flow<String> = context.dataStore.data.map { it[SELECTED_PALETTE] ?: "NATURAL" }
    val tileOrderFlow: Flow<String> = context.dataStore.data.map { it[TILE_ORDER] ?: "" }

    suspend fun setCaffeineDuration(duration: Int) { context.dataStore.edit { it[CAFFEINE_DURATION] = duration } }
    suspend fun setCaffeineActive(isActive: Boolean) { context.dataStore.edit { it[IS_CAFFEINE_ACTIVE] = isActive } }
    suspend fun setTheaterActive(isActive: Boolean) { context.dataStore.edit { it[IS_THEATER_ACTIVE] = isActive } }
    suspend fun setTheaterBrightness(brightness: Int) { context.dataStore.edit { it[THEATER_BRIGHTNESS] = brightness } }
    suspend fun setTheaterSystemAudio(vol: Int) { context.dataStore.edit { it[THEATER_SYSTEM_AUDIO] = vol } }
    suspend fun setTheaterAppAudio(vol: Int) { context.dataStore.edit { it[THEATER_APP_AUDIO] = vol } }
    suspend fun setTheaterDnd(enabled: Boolean) { context.dataStore.edit { it[THEATER_DND] = enabled } }
    suspend fun setClipboardInterval(interval: Int) { context.dataStore.edit { it[CLIPBOARD_INTERVAL] = interval } }
    suspend fun setPrivateDns(dns: String) { context.dataStore.edit { it[PRIVATE_DNS] = dns } }
    suspend fun setDnsActive(active: Boolean) { context.dataStore.edit { it[IS_DNS_ACTIVE] = active } }
    suspend fun setAppAudioIsolated(isIsolated: Boolean) { context.dataStore.edit { it[IS_APP_AUDIO_ISOLATED] = isIsolated } }
    suspend fun setSandboxActive(active: Boolean, endTime: Long = 0L) {
        context.dataStore.edit {
            it[FOCUS_SANDBOX_ACTIVE] = active
            it[FOCUS_END_TIME] = endTime
        }
    }
    suspend fun setAllowedApps(apps: Set<String>) { context.dataStore.edit { it[FOCUS_ALLOWED_APPS] = apps } }
    suspend fun setMonochrome(isMonochrome: Boolean) { context.dataStore.edit { it[IS_MONOCHROME] = isMonochrome } }
    suspend fun setSelectedPalette(palette: String) { context.dataStore.edit { it[SELECTED_PALETTE] = palette } }
    suspend fun setTileOrder(order: String) { context.dataStore.edit { it[TILE_ORDER] = order } }
}
