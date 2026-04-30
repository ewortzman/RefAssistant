package com.refassistant.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.refassistant.app.model.AppSettings
import com.refassistant.app.model.ClockType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            hapticsEnabled = prefs[HAPTICS] ?: true,
            confirmResetEnabled = prefs[CONFIRM_RESET] ?: true,
            bloodDurationMs = prefs[BLOOD_MS] ?: ClockType.BLOOD.defaultDurationMs,
            injuryDurationMs = prefs[INJURY_MS] ?: ClockType.INJURY.defaultDurationMs,
            recoveryDurationMs = prefs[RECOVERY_MS] ?: ClockType.RECOVERY.defaultDurationMs,
            hncDurationMs = prefs[HNC_MS] ?: ClockType.HNC.defaultDurationMs
        )
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.settingsDataStore.edit { it[HAPTICS] = enabled }
    }

    suspend fun setConfirmReset(enabled: Boolean) {
        context.settingsDataStore.edit { it[CONFIRM_RESET] = enabled }
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

    companion object {
        private val HAPTICS = booleanPreferencesKey("haptics")
        private val CONFIRM_RESET = booleanPreferencesKey("confirm_reset")
        private val BLOOD_MS = longPreferencesKey("blood_ms")
        private val INJURY_MS = longPreferencesKey("injury_ms")
        private val RECOVERY_MS = longPreferencesKey("recovery_ms")
        private val HNC_MS = longPreferencesKey("hnc_ms")
    }
}
