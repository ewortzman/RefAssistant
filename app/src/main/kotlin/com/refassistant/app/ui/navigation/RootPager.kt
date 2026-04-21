package com.refassistant.app.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.refassistant.app.ui.clocks.ClockScreen
import com.refassistant.app.ui.jvcounter.JvCounterScreen
import com.refassistant.app.ui.main.MatchScreen
import com.refassistant.app.viewmodel.ClockColor
import com.refassistant.app.viewmodel.MatchViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RootPager(viewModel: MatchViewModel) {
    val state by viewModel.uiState.collectAsState()
    val tickNanos by viewModel.tickNanos.collectAsState()
    val verticalPagerState = rememberPagerState(initialPage = 0) { 2 }

    VerticalPager(state = verticalPagerState) { row ->
        when (row) {
            0 -> {
                val horizontalPagerState = rememberPagerState(initialPage = 1) { 3 }
                HorizontalPager(state = horizontalPagerState) { col ->
                    when (col) {
                        0 -> ClockScreen(
                            color = ClockColor.RED,
                            clocks = state.redClocks,
                            tickNanos = tickNanos,
                            onToggle = { viewModel.toggleClock(ClockColor.RED, it) },
                            onReset = { viewModel.resetClock(ClockColor.RED, it) }
                        )
                        1 -> MatchScreen(
                            currentWeight = state.currentWeight,
                            onNextMatch = viewModel::nextMatch,
                            onSetStartingWeight = viewModel::setStartingWeight
                        )
                        2 -> ClockScreen(
                            color = ClockColor.GREEN,
                            clocks = state.greenClocks,
                            tickNanos = tickNanos,
                            onToggle = { viewModel.toggleClock(ClockColor.GREEN, it) },
                            onReset = { viewModel.resetClock(ClockColor.GREEN, it) }
                        )
                    }
                }
            }
            1 -> JvCounterScreen(
                jvCount = state.jvCount,
                onIncrement = viewModel::incrementJv,
                onDecrement = viewModel::decrementJv,
                onReset = viewModel::resetJv
            )
        }
    }
}
