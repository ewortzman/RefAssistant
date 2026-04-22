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

data class MatchUiState(
    val weightFormat: WeightFormat = WeightFormat.BOYS_14,
    val matchOrder: List<WeightClass> = WeightClass.buildMatchOrder(WeightFormat.BOYS_14, WeightClass.defaultFirst()),
    val matchIndex: Int = 0,
    val currentWeight: WeightClass = WeightClass.defaultFirst(),
    val redClocks: Map<ClockType, StopwatchState> = ClockType.entries.associateWith { StopwatchState() },
    val greenClocks: Map<ClockType, StopwatchState> = ClockType.entries.associateWith { StopwatchState() },
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
                    _tickNanos.value = System.nanoTime()
                }
            }
        }
    }

    private fun anyClockRunning(): Boolean {
        val state = _uiState.value
        return state.redClocks.values.any { it.isRunning } ||
                state.greenClocks.values.any { it.isRunning }
    }

    fun setFormatAndWeight(format: WeightFormat, weight: WeightClass) {
        val matchOrder = if (format == WeightFormat.JV) emptyList()
            else WeightClass.buildMatchOrder(format, weight)
        _uiState.update {
            it.copy(
                weightFormat = format,
                matchOrder = matchOrder,
                matchIndex = 0,
                currentWeight = if (format == WeightFormat.JV) WeightClass.JV else weight,
                redClocks = ClockType.entries.associateWith { StopwatchState() },
                greenClocks = ClockType.entries.associateWith { StopwatchState() }
            )
        }
    }

    fun nextMatch() {
        _uiState.update { state ->
            val resetClocks = ClockType.entries.associateWith { StopwatchState() }
            if (state.currentWeight.isJv) {
                state.copy(
                    jvCount = state.jvCount + 1,
                    redClocks = resetClocks,
                    greenClocks = resetClocks
                )
            } else {
                val nextIndex = state.matchIndex + 1
                if (nextIndex >= state.matchOrder.size) {
                    state.copy(
                        matchIndex = nextIndex,
                        currentWeight = WeightClass.JV,
                        redClocks = resetClocks,
                        greenClocks = resetClocks
                    )
                } else {
                    state.copy(
                        matchIndex = nextIndex,
                        currentWeight = state.matchOrder[nextIndex],
                        redClocks = resetClocks,
                        greenClocks = resetClocks
                    )
                }
            }
        }
    }

    fun toggleClock(color: ClockColor, type: ClockType) {
        val now = System.nanoTime()
        _uiState.update { state ->
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val current = clocks[type] ?: StopwatchState()
            val updated = if (current.isRunning) {
                current.copy(
                    elapsedMs = current.elapsedMs + (now - current.startTimeNanos) / 1_000_000,
                    isRunning = false,
                    startTimeNanos = 0L
                )
            } else {
                current.copy(
                    isRunning = true,
                    startTimeNanos = now
                )
            }
            val newClocks = clocks + (type to updated)
            if (color == ClockColor.RED) state.copy(redClocks = newClocks)
            else state.copy(greenClocks = newClocks)
        }
    }

    fun resetClock(color: ClockColor, type: ClockType) {
        _uiState.update { state ->
            val clocks = if (color == ClockColor.RED) state.redClocks else state.greenClocks
            val newClocks = clocks + (type to StopwatchState())
            if (color == ClockColor.RED) state.copy(redClocks = newClocks)
            else state.copy(greenClocks = newClocks)
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
