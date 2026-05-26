package com.refassistant.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.refassistant.app.data.MatchStateRepository
import com.refassistant.app.data.SettingsRepository
import com.refassistant.app.model.AppSettings
import com.refassistant.app.model.BoutOutcome
import com.refassistant.app.model.ChoiceParity
import com.refassistant.app.model.ChoiceSide
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.DualSummary
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.model.WeightClass
import com.refassistant.app.model.WeightFormat
import com.refassistant.app.model.choiceForBout
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ClockColor { RED, GREEN }

data class ClockUndoSnapshot(
    val clockState: StopwatchState,
    val injuryTimeouts: Int,
    val hncUsed: Boolean
)

sealed class ClockEffect {
    object Expired : ClockEffect()
    object Defaulted : ClockEffect()
    object LastBoutReached : ClockEffect()
    data class Undone(val color: ClockColor, val type: ClockType) : ClockEffect()
}

data class MatchUiState(
    val weightFormat: WeightFormat = WeightFormat.COED_14,
    val matchOrder: List<WeightClass> = WeightClass.buildMatchOrder(WeightFormat.COED_14, WeightClass.defaultFirst()),
    val matchIndex: Int = 0,
    val currentWeight: WeightClass = WeightClass.defaultFirst(),
    val redClocks: Map<ClockType, StopwatchState> = ClockType.entries.associateWith { StopwatchState() },
    val greenClocks: Map<ClockType, StopwatchState> = ClockType.entries.associateWith { StopwatchState() },
    val redInjuryTimeouts: Int = 0,
    val greenInjuryTimeouts: Int = 0,
    val redHncUsed: Boolean = false,
    val greenHncUsed: Boolean = false,
    val redUndo: Map<ClockType, ClockUndoSnapshot> = emptyMap(),
    val greenUndo: Map<ClockType, ClockUndoSnapshot> = emptyMap(),
    val exhCount: Int = 0,
    val choiceWinner: ChoiceSide = ChoiceSide.NONE,
    val choiceWinnerTook: ChoiceParity = ChoiceParity.ODD,
    val choicePrompted: Boolean = false,
    val redTeamScore: Int = 0,
    val greenTeamScore: Int = 0,
    val dualStartedAtEpochMs: Long = 0L,
    val eventHistory: List<DualSummary> = emptyList(),
    val eventStartedAtEpochMs: Long = 0L
) {
    /** Bout number (1-indexed). 0 if in Exhibition mode. */
    val boutNumber: Int
        get() = if (currentWeight.isExhibition) 0 else matchIndex + 1

    val totalBouts: Int
        get() = matchOrder.size

    val choiceForCurrentBout: ChoiceSide
        get() = if (boutNumber == 0) ChoiceSide.NONE
            else choiceForBout(choiceWinner, choiceWinnerTook, boutNumber)
}

@OptIn(FlowPreview::class)
class MatchViewModel(
    application: Application,
    private val settingsRepo: SettingsRepository,
    private val stateRepo: MatchStateRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _tickNanos = MutableStateFlow(System.nanoTime())
    val tickNanos: StateFlow<Long> = _tickNanos.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _effects = MutableSharedFlow<ClockEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<ClockEffect> = _effects.asSharedFlow()

    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    private var startingWeight: String = "106"

    init {
        viewModelScope.launch {
            val restored = stateRepo.loadOnce()
            if (restored != null) {
                _uiState.value = restored
                startingWeight = restored.matchOrder.firstOrNull()?.label ?: "106"
            }
            _initialized.value = true
        }

        // Tick loop: only runs when at least one clock is running, and checks every 100ms
        viewModelScope.launch {
            while (true) {
                if (anyClockRunning()) {
                    val now = System.nanoTime()
                    _tickNanos.value = now
                    autoStopExpired(now)
                    delay(100)
                } else {
                    // Wait for state change that makes any clock run
                    _uiState.first { state ->
                        state.redClocks.values.any { it.isRunning } ||
                            state.greenClocks.values.any { it.isRunning }
                    }
                }
            }
        }

        // Persist state changes, debounced
        viewModelScope.launch {
            _uiState
                .drop(1)
                .debounce(300)
                .onEach { stateRepo.save(it, startingWeight) }
                .distinctUntilChanged()
                .collect {}
        }
    }

    private fun anyClockRunning(): Boolean {
        val state = _uiState.value
        return state.redClocks.values.any { it.isRunning } ||
            state.greenClocks.values.any { it.isRunning }
    }

    private fun autoStopExpired(now: Long) {
        var fired = false
        _uiState.update { state ->
            var changed = false
            fun stopIfExpired(clocks: Map<ClockType, StopwatchState>): Map<ClockType, StopwatchState> {
                return clocks.mapValues { (type, sw) ->
                    val duration = settings.value.durationFor(type)
                    if (sw.isRunning && sw.remainingMs(duration, now) == 0L) {
                        changed = true
                        fired = true
                        sw.copy(
                            elapsedMs = duration,
                            isRunning = false,
                            startTimeNanos = 0L
                        )
                    } else sw
                }
            }
            val newRed = stopIfExpired(state.redClocks)
            val newGreen = stopIfExpired(state.greenClocks)
            if (changed) state.copy(redClocks = newRed, greenClocks = newGreen) else state
        }
        if (fired) viewModelScope.launch { _effects.emit(ClockEffect.Expired) }
    }

    private val freshClocks get() = ClockType.entries.associateWith { StopwatchState() }

    fun setFormatAndWeight(format: WeightFormat, weight: WeightClass) {
        val matchOrder = if (format == WeightFormat.EXH) emptyList()
            else WeightClass.buildMatchOrder(format, weight)
        startingWeight = if (format == WeightFormat.EXH) "106" else weight.label
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                weightFormat = format,
                matchOrder = matchOrder,
                matchIndex = 0,
                currentWeight = if (format == WeightFormat.EXH) WeightClass.EXH else weight,
                redClocks = freshClocks, greenClocks = freshClocks,
                redInjuryTimeouts = 0, greenInjuryTimeouts = 0,
                redHncUsed = false, greenHncUsed = false,
                redUndo = emptyMap(), greenUndo = emptyMap(),
                choiceWinner = ChoiceSide.NONE,
                choiceWinnerTook = ChoiceParity.ODD,
                choicePrompted = false,
                redTeamScore = 0,
                greenTeamScore = 0,
                dualStartedAtEpochMs = if (format == WeightFormat.EXH) it.dualStartedAtEpochMs else now,
                eventStartedAtEpochMs = if (it.eventStartedAtEpochMs == 0L) now else it.eventStartedAtEpochMs
            )
        }
    }

    /**
     * End the current dual: snapshot it into event history, then reset dual state
     * to Exhibition mode (clocks/scores/choice cleared, exhCount preserved).
     * No-op if currently in Exhibition mode or no dual has been started.
     */
    fun endDualAndRecord() {
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            val format = state.weightFormat
            if (format == WeightFormat.EXH) return@update state
            val startedAt = if (state.dualStartedAtEpochMs > 0L) state.dualStartedAtEpochMs else now
            val summary = DualSummary(
                format = format,
                startingWeight = state.matchOrder.firstOrNull()?.label ?: "?",
                boutsCompleted = state.matchIndex.coerceAtMost(state.matchOrder.size),
                totalBouts = state.matchOrder.size,
                redTeamScore = state.redTeamScore,
                greenTeamScore = state.greenTeamScore,
                startedAtEpochMs = startedAt,
                endedAtEpochMs = now
            )
            state.copy(
                eventHistory = state.eventHistory + summary,
                weightFormat = WeightFormat.EXH,
                matchOrder = emptyList(),
                matchIndex = 0,
                currentWeight = WeightClass.EXH,
                redClocks = freshClocks, greenClocks = freshClocks,
                redInjuryTimeouts = 0, greenInjuryTimeouts = 0,
                redHncUsed = false, greenHncUsed = false,
                redUndo = emptyMap(), greenUndo = emptyMap(),
                choiceWinner = ChoiceSide.NONE,
                choiceWinnerTook = ChoiceParity.ODD,
                choicePrompted = false,
                redTeamScore = 0,
                greenTeamScore = 0,
                dualStartedAtEpochMs = 0L
            )
        }
    }

    /** Reset event: clears dual history, Exhibition count, and all dual state. */
    fun newEvent() {
        startingWeight = "106"
        _uiState.value = MatchUiState()
    }

    fun setChoice(winner: ChoiceSide, took: ChoiceParity) {
        _uiState.update {
            it.copy(choiceWinner = winner, choiceWinnerTook = took, choicePrompted = true)
        }
    }

    fun dismissChoicePrompt() {
        _uiState.update { it.copy(choicePrompted = true) }
    }

    fun nextMatch() {
        var reachedLast = false
        _uiState.update { state ->
            val next = advanceState(state)
            reachedLast = isLastBoutTransition(state, next)
            next
        }
        if (reachedLast) viewModelScope.launch { _effects.emit(ClockEffect.LastBoutReached) }
    }

    /**
     * Record a dual-meet bout result with winner and outcome, then advance to next bout.
     * No-op team scoring for Exhibition (matches user's admin-only scope).
     */
    fun recordBoutResult(winner: ChoiceSide, outcome: BoutOutcome) {
        var reachedLast = false
        _uiState.update { state ->
            val scored = if (state.currentWeight.isExhibition) state else applyBoutScore(state, winner, outcome)
            val next = advanceState(scored)
            reachedLast = isLastBoutTransition(state, next)
            next
        }
        if (reachedLast) viewModelScope.launch { _effects.emit(ClockEffect.LastBoutReached) }
    }

    private fun isLastBoutTransition(prev: MatchUiState, next: MatchUiState): Boolean {
        if (next.matchOrder.isEmpty()) return false
        val prevIsLast = !prev.currentWeight.isExhibition && prev.matchIndex == prev.matchOrder.lastIndex
        val nextIsLast = !next.currentWeight.isExhibition && next.matchIndex == next.matchOrder.lastIndex
        return nextIsLast && !prevIsLast
    }

    private fun applyBoutScore(state: MatchUiState, winner: ChoiceSide, outcome: BoutOutcome): MatchUiState {
        if (winner == ChoiceSide.NONE || outcome == BoutOutcome.DOUBLE_FORFEIT) return state
        val points = outcome.teamPoints
        return when (winner) {
            ChoiceSide.RED -> state.copy(redTeamScore = state.redTeamScore + points)
            ChoiceSide.GREEN -> state.copy(greenTeamScore = state.greenTeamScore + points)
            ChoiceSide.NONE -> state
        }
    }

    private fun advanceState(state: MatchUiState): MatchUiState {
        val base = state.copy(
            redClocks = freshClocks, greenClocks = freshClocks,
            redInjuryTimeouts = 0, greenInjuryTimeouts = 0,
            redHncUsed = false, greenHncUsed = false,
            redUndo = emptyMap(), greenUndo = emptyMap()
        )
        return if (state.currentWeight.isExhibition) {
            base.copy(exhCount = state.exhCount + 1)
        } else {
            val nextIndex = state.matchIndex + 1
            if (nextIndex >= state.matchOrder.size) {
                base.copy(matchIndex = nextIndex, currentWeight = WeightClass.EXH)
            } else {
                base.copy(matchIndex = nextIndex, currentWeight = state.matchOrder[nextIndex])
            }
        }
    }

    private fun getInjuryTimeouts(state: MatchUiState, color: ClockColor): Int =
        if (color == ClockColor.RED) state.redInjuryTimeouts else state.greenInjuryTimeouts

    private fun getHncUsed(state: MatchUiState, color: ClockColor): Boolean =
        if (color == ClockColor.RED) state.redHncUsed else state.greenHncUsed

    private fun setInjuryTimeouts(state: MatchUiState, color: ClockColor, value: Int): MatchUiState =
        if (color == ClockColor.RED) state.copy(redInjuryTimeouts = value)
        else state.copy(greenInjuryTimeouts = value)

    private fun setHncUsed(state: MatchUiState, color: ClockColor, value: Boolean): MatchUiState =
        if (color == ClockColor.RED) state.copy(redHncUsed = value)
        else state.copy(greenHncUsed = value)

    fun toggleClock(color: ClockColor, type: ClockType) {
        val now = System.nanoTime()
        var defTriggered = false
        _uiState.update { state ->
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val undos = if (color == ClockColor.RED) state.redUndo else state.greenUndo
            val current = clocks[type] ?: StopwatchState()
            val duration = settings.value.durationFor(type)
            if (!current.isRunning && current.elapsedMs >= duration) return@update state

            val snapshot = ClockUndoSnapshot(
                clockState = current,
                injuryTimeouts = getInjuryTimeouts(state, color),
                hncUsed = getHncUsed(state, color)
            )

            if (current.isRunning) {
                val updated = current.copy(
                    elapsedMs = current.elapsedMs + (now - current.startTimeNanos) / 1_000_000,
                    isRunning = false,
                    startTimeNanos = 0L
                )
                val newClocks = clocks + (type to updated)
                val newUndos = undos + (type to snapshot)
                return@update if (color == ClockColor.RED)
                    state.copy(redClocks = newClocks, redUndo = newUndos)
                else state.copy(greenClocks = newClocks, greenUndo = newUndos)
            }

            var s = state
            val injuryBefore = getInjuryTimeouts(s, color)
            if (type == ClockType.INJURY) {
                val newCount = injuryBefore + 1
                s = setInjuryTimeouts(s, color, newCount)
                if (injuryBefore < 3 && newCount == 3) defTriggered = true
            } else if (type == ClockType.HNC && !getHncUsed(s, color)) {
                s = setHncUsed(s, color, true)
                val newCount = injuryBefore + 1
                s = setInjuryTimeouts(s, color, newCount)
                if (injuryBefore < 3 && newCount == 3) defTriggered = true
            }

            val updated = current.copy(isRunning = true, startTimeNanos = now)
            val newClocks = (if (color == ClockColor.RED) s.redClocks else s.greenClocks) + (type to updated)
            val newUndos = undos + (type to snapshot)
            if (color == ClockColor.RED) s.copy(redClocks = newClocks, redUndo = newUndos)
            else s.copy(greenClocks = newClocks, greenUndo = newUndos)
        }
        if (defTriggered) viewModelScope.launch { _effects.emit(ClockEffect.Defaulted) }
    }

    fun undoClock(color: ClockColor, type: ClockType) {
        var didUndo = false
        _uiState.update { state ->
            val undos = if (color == ClockColor.RED) state.redUndo else state.greenUndo
            val snapshot = undos[type] ?: return@update state
            didUndo = true
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val newClocks = clocks + (type to snapshot.clockState)
            val newUndos = undos - type
            var s = setInjuryTimeouts(state, color, snapshot.injuryTimeouts)
            s = setHncUsed(s, color, snapshot.hncUsed)
            if (color == ClockColor.RED) s.copy(redClocks = newClocks, redUndo = newUndos)
            else s.copy(greenClocks = newClocks, greenUndo = newUndos)
        }
        if (didUndo) viewModelScope.launch { _effects.emit(ClockEffect.Undone(color, type)) }
    }

    fun resetClock(color: ClockColor, type: ClockType) {
        _uiState.update { state ->
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val undos = if (color == ClockColor.RED) state.redUndo else state.greenUndo
            val newClocks = clocks + (type to StopwatchState())
            val newUndos = undos - type
            if (color == ClockColor.RED) state.copy(redClocks = newClocks, redUndo = newUndos)
            else state.copy(greenClocks = newClocks, greenUndo = newUndos)
        }
    }

    fun incrementExh() {
        _uiState.update { it.copy(exhCount = it.exhCount + 1) }
    }

    fun decrementExh() {
        _uiState.update { it.copy(exhCount = maxOf(0, it.exhCount - 1)) }
    }

    fun resetExh() {
        _uiState.update { it.copy(exhCount = 0) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setHaptics(enabled) }
    }

    fun setConfirmResetEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setConfirmReset(enabled) }
    }

    fun setTeamScoreTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setTeamScoreTracking(enabled) }
    }

    fun setClockDuration(type: ClockType, ms: Long) {
        viewModelScope.launch { settingsRepo.setDuration(type, ms) }
    }

    fun setFormatEnabled(format: WeightFormat, enabled: Boolean) {
        val current = settings.value.enabledFormats
        val updated = if (enabled) current + format else current - format
        viewModelScope.launch { settingsRepo.setEnabledFormats(updated) }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MatchViewModel(
                    application = application,
                    settingsRepo = SettingsRepository(application),
                    stateRepo = MatchStateRepository(application)
                )
            }
        }
    }
}
