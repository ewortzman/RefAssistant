package com.refassistant.app.ui.clocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.ui.common.ConfirmDialog
import com.refassistant.app.viewmodel.ClockColor

private val DeepRed = Color(0xFFB71C1C)
private val DeepGreen = Color(0xFF1B5E20)

@Composable
fun ClockScreen(
    color: ClockColor,
    clocks: Map<ClockType, StopwatchState>,
    tickNanos: Long,
    injuryTimeouts: Int,
    hncUsed: Boolean,
    onToggle: (ClockType) -> Unit,
    onDoubleTap: (ClockType) -> Unit,
    onReset: (ClockType) -> Unit,
    isAmbient: Boolean = false
) {
    val bgColor = if (isAmbient) Color.Black
        else if (color == ClockColor.RED) DeepRed else DeepGreen
    val quadrantSize = 80.dp
    var confirmResetType by remember { mutableStateOf<ClockType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        StopwatchQuadrant(
            clockType = ClockType.BLOOD,
            stopwatchState = clocks[ClockType.BLOOD] ?: StopwatchState(),
            tickNanos = tickNanos,
            onTap = { if (!isAmbient) onToggle(ClockType.BLOOD) },
            onDoubleTap = { if (!isAmbient) onDoubleTap(ClockType.BLOOD) },
            onLongPress = { if (!isAmbient) confirmResetType = ClockType.BLOOD },
            isAmbient = isAmbient,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .size(quadrantSize)
        )

        StopwatchQuadrant(
            clockType = ClockType.INJURY,
            stopwatchState = clocks[ClockType.INJURY] ?: StopwatchState(),
            tickNanos = tickNanos,
            onTap = { if (!isAmbient) onToggle(ClockType.INJURY) },
            onDoubleTap = { if (!isAmbient) onDoubleTap(ClockType.INJURY) },
            onLongPress = { if (!isAmbient) confirmResetType = ClockType.INJURY },
            injuryTimeouts = injuryTimeouts,
            isAmbient = isAmbient,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(quadrantSize)
        )

        StopwatchQuadrant(
            clockType = ClockType.RECOVERY,
            stopwatchState = clocks[ClockType.RECOVERY] ?: StopwatchState(),
            tickNanos = tickNanos,
            onTap = { if (!isAmbient) onToggle(ClockType.RECOVERY) },
            onDoubleTap = { if (!isAmbient) onDoubleTap(ClockType.RECOVERY) },
            onLongPress = { if (!isAmbient) confirmResetType = ClockType.RECOVERY },
            isAmbient = isAmbient,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(quadrantSize)
        )

        StopwatchQuadrant(
            clockType = ClockType.HNC,
            stopwatchState = clocks[ClockType.HNC] ?: StopwatchState(),
            tickNanos = tickNanos,
            onTap = { if (!isAmbient) onToggle(ClockType.HNC) },
            onDoubleTap = { if (!isAmbient) onDoubleTap(ClockType.HNC) },
            onLongPress = { if (!isAmbient) confirmResetType = ClockType.HNC },
            hncUsed = hncUsed,
            isAmbient = isAmbient,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(quadrantSize)
        )

        if (!isAmbient) {
            confirmResetType?.let { type ->
                ConfirmDialog(
                    message = "Reset ${type.label}?",
                    onConfirm = {
                        onReset(type)
                        confirmResetType = null
                    },
                    onDismiss = { confirmResetType = null }
                )
            }
        }
    }
}
