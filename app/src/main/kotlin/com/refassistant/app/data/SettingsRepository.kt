package com.refassistant.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.refassistant.app.model.AppSettings
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.WeightFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val enabledRaw = prefs[ENABLED_FORMATS]
        val enabled = if (enabledRaw == null) AppSettings.DEFAULT_ENABLED_FORMATS
            else enabledRaw.split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { runCatching { WeightFormat.valueOf(it) }.getOrNull() }
                .toSet()
        AppSettings(
            hapticsEnabled = prefs[HAPTICS] ?: true,
            confirmResetEnabled = prefs[CONFIRM_RESET] ?: true,
            teamScoreTrackingEnabled = prefs[TEAM_SCORE_TRACKING] ?: true,
            bloodDurationMs = prefs[BLOOD_MS] ?: ClockType.BLOOD.defaultDurationMs,
            injuryDurationMs = prefs[INJURY_MS] ?: ClockType.INJURY.defaultDurationMs,
            recoveryDurationMs = prefs[RECOVERY_MS] ?: ClockType.RECOVERY.defaultDurationMs,
            hncDurationMs = prefs[HNC_MS] ?: ClockType.HNC.defaultDurationMs,
            enabledFormats = enabled
        )
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.settingsDataStore.edit { it[HAPTICS] = enabled }
    }

    suspend fun setConfirmReset(enabled: Boolean) {
        context.settingsDataStore.edit { it[CONFIRM_RESET] = enabled }
    }

    suspend fun setTeamScoreTracking(enabled: Boolean) {
        context.settingsDataStore.edit { it[TEAM_SCORE_TRACKING] = enabled }
    }

    suspend fun setDuration(type: ClockType, ms: Long) {
        val key = when (type) {
            ClockType.BLOOD -> BLOOD_MS
            ClockType.INJURY -> INJURY_MS
            ClockType.RECOVERY -> RECOVERY_MS
            ClockType.HNC -> HNC_MS
        }
        context.settingsDataStore.edit { it[key] = ms }
    }

    suspend fun setEnabledFormats(formats: Set<WeightFormat>) {
        val value = formats.joinToString(",") { it.name }
        context.settingsDataStore.edit { it[ENABLED_FORMATS] = value }
    }

    companion object {
        private val HAPTICS = booleanPreferencesKey("haptics")
        private val CONFIRM_RESET = booleanPreferencesKey("confirm_reset")
        private val TEAM_SCORE_TRACKING = booleanPreferencesKey("team_score_tracking")
        private val BLOOD_MS = longPreferencesKey("blood_ms")
        private val INJURY_MS = longPreferencesKey("injury_ms")
        private val RECOVERY_MS = longPreferencesKey("recovery_ms")
        private val HNC_MS = longPreferencesKey("hnc_ms")
        private val ENABLED_FORMATS = stringPreferencesKey("enabled_formats")
    }
}
