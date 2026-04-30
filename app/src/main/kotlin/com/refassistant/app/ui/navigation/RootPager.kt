package com.refassistant.app.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.refassistant.app.model.ClockType
import com.refassistant.app.ui.clocks.ClockScreen
import com.refassistant.app.ui.jvcounter.JvCounterScreen
import com.refassistant.app.ui.main.MatchScreen
import com.refassistant.app.ui.settings.SettingsScreen
import com.refassistant.app.viewmodel.ClockColor
import com.refassistant.app.viewmodel.ClockEffect
import com.refassistant.app.viewmodel.MatchViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RootPager(viewModel: MatchViewModel, isAmbient: Boolean = false) {
    val state by viewModel.uiState.collectAsState()
    val tickNanos by viewModel.tickNanos.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val redUndoPulses = remember { mutableStateMapOf<ClockType, Int>() }
    val greenUndoPulses = remember { mutableStateMapOf<ClockType, Int>() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is ClockEffect.Undone) {
                val map = if (effect.color == ClockColor.RED) redUndoPulses else greenUndoPulses
                map[effect.type] = (map[effect.type] ?: 0) + 1
            }
        }
    }

    val verticalPagerState = rememberPagerState(initialPage = 0) { 3 }

    VerticalPager(
        state = verticalPagerState,
        userScrollEnabled = !isAmbient
    ) { row ->
        when (row) {
            0 -> {
                val horizontalPagerState = rememberPagerState(initialPage = 1) { 3 }
                HorizontalPager(
                    state = horizontalPagerState,
                    userScrollEnabled = !isAmbient
                ) { col ->
                    when (col) {
                        0 -> ClockScreen(
                            color = ClockColor.RED,
                            clocks = state.redClocks,
                            tickNanos = tickNanos,
                            injuryTimeouts = state.redInjuryTimeouts,
                            hncUsed = state.redHncUsed,
                            settings = settings,
                            undoPulseKeys = redUndoPulses,
                            onToggle = { viewModel.toggleClock(ClockColor.RED, it) },
                            onDoubleTap = { viewModel.undoClock(ClockColor.RED, it) },
                            onReset = { viewModel.resetClock(ClockColor.RED, it) },
                            isAmbient = isAmbient
                        )
                        1 -> MatchScreen(
                            currentWeight = state.currentWeight,
                            currentFormat = state.weightFormat,
                            boutNumber = state.boutNumber,
                            totalBouts = state.totalBouts,
                            choiceForBout = state.choiceForCurrentBout,
                            choicePrompted = state.choicePrompted,
                            onNextMatch = viewModel::nextMatch,
                            onSetFormatAndWeight = viewModel::setFormatAndWeight,
                            onSetChoice = viewModel::setChoice,
                            onDismissChoicePrompt = viewModel::dismissChoicePrompt,
                            isAmbient = isAmbient
                        )
                        2 -> ClockScreen(
                            color = ClockColor.GREEN,
                            clocks = state.greenClocks,
                            tickNanos = tickNanos,
                            injuryTimeouts = state.greenInjuryTimeouts,
                            hncUsed = state.greenHncUsed,
                            settings = settings,
                            undoPulseKeys = greenUndoPulses,
                            onToggle = { viewModel.toggleClock(ClockColor.GREEN, it) },
                            onDoubleTap = { viewModel.undoClock(ClockColor.GREEN, it) },
                            onReset = { viewModel.resetClock(ClockColor.GREEN, it) },
                            isAmbient = isAmbient
                        )
                    }
                }
            }
            1 -> JvCounterScreen(
                jvCount = state.jvCount,
                onIncrement = viewModel::incrementJv,
                onDecrement = viewModel::decrementJv,
                onReset = viewModel::resetJv,
                confirmReset = settings.confirmResetEnabled,
                isAmbient = isAmbient
            )
            2 -> SettingsScreen(
                settings = settings,
                onToggleHaptics = viewModel::setHapticsEnabled,
                onToggleConfirm = viewModel::setConfirmResetEnabled,
                onChangeDuration = viewModel::setClockDuration,
                isAmbient = isAmbient
            )
        }
    }
}
