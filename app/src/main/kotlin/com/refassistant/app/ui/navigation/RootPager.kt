package com.refassistant.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.refassistant.app.model.ClockType
import com.refassistant.app.ui.clocks.ClockScreen
import com.refassistant.app.ui.exhcounter.ExhCounterScreen
import com.refassistant.app.ui.main.MatchScreen
import com.refassistant.app.ui.settings.SettingsGateScreen
import com.refassistant.app.ui.settings.SettingsScreen
import com.refassistant.app.viewmodel.ClockColor
import com.refassistant.app.viewmodel.ClockEffect
import com.refassistant.app.viewmodel.MatchViewModel
import kotlinx.coroutines.flow.collectLatest

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

    var settingsOpen by remember { mutableStateOf(false) }
    val verticalPagerState = rememberPagerState(initialPage = 0) { 3 }

    // Close settings when pager moves away from the settings gate page
    LaunchedEffect(verticalPagerState) {
        snapshotCollectPage(verticalPagerState) { page ->
            if (page != 2) settingsOpen = false
        }
    }

    BackHandler(enabled = settingsOpen) {
        settingsOpen = false
    }

    VerticalPager(
        state = verticalPagerState,
        userScrollEnabled = !isAmbient && !settingsOpen
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
                            availableFormats = settings.visibleFormats(),
                            redTeamScore = state.redTeamScore,
                            greenTeamScore = state.greenTeamScore,
                            onNextMatch = viewModel::nextMatch,
                            onRecordBoutResult = viewModel::recordBoutResult,
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
            1 -> ExhCounterScreen(
                exhCount = state.exhCount,
                onIncrement = viewModel::incrementExh,
                onDecrement = viewModel::decrementExh,
                onReset = viewModel::resetExh,
                confirmReset = settings.confirmResetEnabled,
                isAmbient = isAmbient
            )
            2 -> {
                SettingsGateScreen(
                    onOpen = { settingsOpen = true },
                    isAmbient = isAmbient
                )
                if (settingsOpen && !isAmbient) {
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissValue.Dismissed) {
                            settingsOpen = false
                            dismissState.snapTo(SwipeToDismissValue.Default)
                        }
                    }
                    SwipeToDismissBox(state = dismissState) { isBackground ->
                        if (!isBackground) {
                            SettingsScreen(
                                settings = settings,
                                onToggleHaptics = viewModel::setHapticsEnabled,
                                onToggleConfirm = viewModel::setConfirmResetEnabled,
                                onChangeDuration = viewModel::setClockDuration,
                                onToggleFormat = viewModel::setFormatEnabled,
                                isAmbient = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private suspend fun snapshotCollectPage(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPage: (Int) -> Unit
) {
    androidx.compose.runtime.snapshotFlow { pagerState.currentPage }
        .collectLatest { onPage(it) }
}
