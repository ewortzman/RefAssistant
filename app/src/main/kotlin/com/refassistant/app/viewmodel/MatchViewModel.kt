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
    val weightList: List<WeightClass> = WeightClass.listFor(WeightFormat.BOYS_14),
    val currentWeight: WeightClass = WeightClass.defaultFirst(),
    val startingWeight: WeightClass = WeightClass.defaultFirst(),
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
        val list = WeightClass.listFor(format)
        _uiState.update {
            it.copy(
                weightFormat = format,
                weightList = list,
                currentWeight = weight,
                startingWeight = weight,
                redClocks = ClockType.entries.associateWith { StopwatchState() },
                greenClocks = ClockType.entries.associateWith { StopwatchState() }
            )
        }
    }

    fun nextMatch() {
        _uiState.update { state ->
            val isJv = state.currentWeight.isJv
            state.copy(
                currentWeight = if (isJv) state.currentWeight else state.currentWeight.next(state.weightList),
                jvCount = if (isJv) state.jvCount + 1 else state.jvCount,
                redClocks = ClockType.entries.associateWith { StopwatchState() },
                greenClocks = ClockType.entries.associateWith { StopwatchState() }
            )
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
