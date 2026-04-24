package com.refassistant.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.model.WeightClass
import com.refassistant.app.model.WeightFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ClockColor { RED, GREEN }

data class ClockUndoSnapshot(
    val clockState: StopwatchState,
    val injuryTimeouts: Int,
    val hncUsed: Boolean
)

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
    val jvCount: Int = 0
)

class MatchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _tickNanos = MutableStateFlow(System.nanoTime())
    val tickNanos: StateFlow<Long> = _tickNanos.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(32)
                if (anyClockRunning()) {
                    val now = System.nanoTime()
                    _tickNanos.value = now
                    autoStopExpired(now)
                }
            }
        }
    }

    private fun autoStopExpired(now: Long) {
        _uiState.update { state ->
            var changed = false
            fun stopIfExpired(clocks: Map<ClockType, StopwatchState>): Map<ClockType, StopwatchState> {
                return clocks.mapValues { (type, sw) ->
                    if (sw.isRunning && sw.remainingMs(type.durationMs, now) == 0L) {
                        changed = true
                        sw.copy(
                            elapsedMs = type.durationMs,
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
    }

    private fun anyClockRunning(): Boolean {
        val state = _uiState.value
        return state.redClocks.values.any { it.isRunning } ||
                state.greenClocks.values.any { it.isRunning }
    }

    private val freshClocks get() = ClockType.entries.associateWith { StopwatchState() }

    fun setFormatAndWeight(format: WeightFormat, weight: WeightClass) {
        val matchOrder = if (format == WeightFormat.JV) emptyList()
            else WeightClass.buildMatchOrder(format, weight)
        _uiState.update {
            it.copy(
                weightFormat = format,
                matchOrder = matchOrder,
                matchIndex = 0,
                currentWeight = if (format == WeightFormat.JV) WeightClass.JV else weight,
                redClocks = freshClocks, greenClocks = freshClocks,
                redInjuryTimeouts = 0, greenInjuryTimeouts = 0,
                redHncUsed = false, greenHncUsed = false,
                redUndo = emptyMap(), greenUndo = emptyMap()
            )
        }
    }

    fun nextMatch() {
        _uiState.update { state ->
            val base = state.copy(
                redClocks = freshClocks, greenClocks = freshClocks,
                redInjuryTimeouts = 0, greenInjuryTimeouts = 0,
                redHncUsed = false, greenHncUsed = false,
                redUndo = emptyMap(), greenUndo = emptyMap()
            )
            if (state.currentWeight.isJv) {
                base.copy(jvCount = state.jvCount + 1)
            } else {
                val nextIndex = state.matchIndex + 1
                if (nextIndex >= state.matchOrder.size) {
                    base.copy(matchIndex = nextIndex, currentWeight = WeightClass.JV)
                } else {
                    base.copy(matchIndex = nextIndex, currentWeight = state.matchOrder[nextIndex])
                }
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
        _uiState.update { state ->
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val undos = if (color == ClockColor.RED) state.redUndo else state.greenUndo
            val current = clocks[type] ?: StopwatchState()
            if (!current.isRunning && current.elapsedMs >= type.durationMs) return@update state

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

            // Starting clock — track injury timeouts
            var s = state
            val injuryTimeouts = getInjuryTimeouts(s, color)
            if (type == ClockType.INJURY) {
                s = setInjuryTimeouts(s, color, injuryTimeouts + 1)
            } else if (type == ClockType.HNC && !getHncUsed(s, color)) {
                s = setHncUsed(s, color, true)
                s = setInjuryTimeouts(s, color, injuryTimeouts + 1)
            }

            val updated = current.copy(isRunning = true, startTimeNanos = now)
            val newClocks = (if (color == ClockColor.RED) s.redClocks else s.greenClocks) + (type to updated)
            val newUndos = undos + (type to snapshot)
            if (color == ClockColor.RED) s.copy(redClocks = newClocks, redUndo = newUndos)
            else s.copy(greenClocks = newClocks, greenUndo = newUndos)
        }
    }

    fun undoClock(color: ClockColor, type: ClockType) {
        _uiState.update { state ->
            val undos = if (color == ClockColor.RED) state.redUndo else state.greenUndo
            val snapshot = undos[type] ?: return@update state
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val newClocks = clocks + (type to snapshot.clockState)
            val newUndos = undos - type
            var s = setInjuryTimeouts(state, color, snapshot.injuryTimeouts)
            s = setHncUsed(s, color, snapshot.hncUsed)
            if (color == ClockColor.RED) s.copy(redClocks = newClocks, redUndo = newUndos)
            else s.copy(greenClocks = newClocks, greenUndo = newUndos)
        }
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

    fun incrementJv() {
        _uiState.update { it.copy(jvCount = it.jvCount + 1) }
    }

    fun decrementJv() {
        _uiState.update { it.copy(jvCount = maxOf(0, it.jvCount - 1)) }
    }

    fun resetJv() {
        _uiState.update { it.copy(jvCount = 0) }
    }
}
