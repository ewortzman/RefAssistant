package com.refassistant.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.refassistant.app.model.ChoiceParity
import com.refassistant.app.model.ChoiceSide
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.model.WeightClass
import com.refassistant.app.model.WeightFormat
import com.refassistant.app.viewmodel.MatchUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.matchStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "match_state")

class MatchStateRepository(private val context: Context) {

    val state: Flow<MatchUiState?> = context.matchStateDataStore.data.map { prefs ->
        val formatName = prefs[FORMAT] ?: return@map null
        val format = runCatching { WeightFormat.valueOf(formatName) }.getOrNull() ?: return@map null
        val weightLabel = prefs[CURRENT_WEIGHT] ?: return@map null
        val matchIndex = prefs[MATCH_INDEX] ?: 0
        val matchOrder = if (format == WeightFormat.EXH) emptyList()
            else WeightClass.buildMatchOrder(format, WeightClass(prefs[STARTING_WEIGHT] ?: "106"))
        val currentWeight = if (weightLabel == WeightClass.EXH_LABEL) WeightClass.EXH else WeightClass(weightLabel)

        MatchUiState(
            weightFormat = format,
            matchOrder = matchOrder,
            matchIndex = matchIndex,
            currentWeight = currentWeight,
            redClocks = readClocks(prefs, "red"),
            greenClocks = readClocks(prefs, "green"),
            redInjuryTimeouts = prefs[intPreferencesKey("red_injury_timeouts")] ?: 0,
            greenInjuryTimeouts = prefs[intPreferencesKey("green_injury_timeouts")] ?: 0,
            redHncUsed = prefs[booleanPreferencesKey("red_hnc_used")] ?: false,
            greenHncUsed = prefs[booleanPreferencesKey("green_hnc_used")] ?: false,
            exhCount = prefs[intPreferencesKey("exh_count")] ?: 0,
            choiceWinner = runCatching {
                ChoiceSide.valueOf(prefs[stringPreferencesKey("choice_winner")] ?: "NONE")
            }.getOrDefault(ChoiceSide.NONE),
            choiceWinnerTook = runCatching {
                ChoiceParity.valueOf(prefs[stringPreferencesKey("choice_winner_took")] ?: "ODD")
            }.getOrDefault(ChoiceParity.ODD),
            choicePrompted = prefs[booleanPreferencesKey("choice_prompted")] ?: false
        )
    }

    private fun readClocks(prefs: Preferences, prefix: String): Map<ClockType, StopwatchState> {
        return ClockType.entries.associateWith { type ->
            val elapsed = prefs[longPreferencesKey("${prefix}_${type.name}_elapsed")] ?: 0L
            // Don't persist running state — on restore, clock is paused at its elapsed position
            StopwatchState(elapsedMs = elapsed, isRunning = false, startTimeNanos = 0L)
        }
    }

    suspend fun save(state: MatchUiState, startingWeight: String) {
        context.matchStateDataStore.edit { prefs ->
            prefs[FORMAT] = state.weightFormat.name
            prefs[CURRENT_WEIGHT] = state.currentWeight.label
            prefs[STARTING_WEIGHT] = startingWeight
            prefs[MATCH_INDEX] = state.matchIndex
            writeClocks(prefs, "red", state.redClocks)
            writeClocks(prefs, "green", state.greenClocks)
            prefs[intPreferencesKey("red_injury_timeouts")] = state.redInjuryTimeouts
            prefs[intPreferencesKey("green_injury_timeouts")] = state.greenInjuryTimeouts
            prefs[booleanPreferencesKey("red_hnc_used")] = state.redHncUsed
            prefs[booleanPreferencesKey("green_hnc_used")] = state.greenHncUsed
            prefs[intPreferencesKey("exh_count")] = state.exhCount
            prefs[stringPreferencesKey("choice_winner")] = state.choiceWinner.name
            prefs[stringPreferencesKey("choice_winner_took")] = state.choiceWinnerTook.name
            prefs[booleanPreferencesKey("choice_prompted")] = state.choicePrompted
        }
    }

    private fun writeClocks(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        prefix: String,
        clocks: Map<ClockType, StopwatchState>
    ) {
        val now = System.nanoTime()
        clocks.forEach { (type, sw) ->
            val elapsed = sw.displayElapsedMs(now)
            prefs[longPreferencesKey("${prefix}_${type.name}_elapsed")] = elapsed
        }
    }

    suspend fun clear() {
        context.matchStateDataStore.edit { it.clear() }
    }

    suspend fun loadOnce(): MatchUiState? = state.first()

    companion object {
        private val FORMAT = stringPreferencesKey("format")
        private val CURRENT_WEIGHT = stringPreferencesKey("current_weight")
        private val STARTING_WEIGHT = stringPreferencesKey("starting_weight")
        private val MATCH_INDEX = intPreferencesKey("match_index")
    }
}
