package com.refassistant.app.ui.clocks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.util.formatElapsedTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopwatchQuadrant(
    clockType: ClockType,
    stopwatchState: StopwatchState,
    tickNanos: Long,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    isAmbient: Boolean = false,
    modifier: Modifier = Modifier
) {
    val remainingMs = stopwatchState.remainingMs(clockType.durationMs, tickNanos)
    val expired = remainingMs == 0L && stopwatchState.elapsedMs > 0L
    val timeColor = if (isAmbient) {
        if (stopwatchState.elapsedMs > 0L || stopwatchState.isRunning) Color.White else Color.DarkGray
    } else {
        when {
            expired -> Color.Red
            stopwatchState.isRunning -> Color.Yellow
            stopwatchState.elapsedMs > 0L -> Color(0xFFFFAB40)
            else -> Color.White
        }
    }
    val iconTint = if (isAmbient) Color.Gray else Color.White

    Column(
        modifier = modifier.combinedClickable(
            onClick = onTap,
            onLongClick = onLongPress
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = clockType.iconRes),
            contentDescription = clockType.label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = formatElapsedTime(remainingMs),
            style = MaterialTheme.typography.body2,
            color = timeColor
        )
    }
}
