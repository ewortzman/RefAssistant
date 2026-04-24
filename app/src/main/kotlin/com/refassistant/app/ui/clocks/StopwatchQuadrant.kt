package com.refassistant.app.ui.clocks

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.StopwatchState
import com.refassistant.app.util.formatElapsedTime

@Composable
fun StopwatchQuadrant(
    clockType: ClockType,
    stopwatchState: StopwatchState,
    tickNanos: Long,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    injuryTimeouts: Int = 0,
    hncUsed: Boolean = false,
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
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { onDoubleTap() },
                onLongPress = { onLongPress() },
                onTap = { onTap() }
            )
        },
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
        val dotText = when (clockType) {
            ClockType.INJURY -> {
                if (injuryTimeouts >= 3) "DEF"
                else "•".repeat(injuryTimeouts)
            }
            ClockType.HNC -> {
                if (hncUsed) "•" else ""
            }
            else -> ""
        }
        if (dotText.isNotEmpty()) {
            val isDefault = clockType == ClockType.INJURY && injuryTimeouts >= 3
            Text(
                text = dotText,
                fontSize = if (isDefault) 10.sp else 8.sp,
                fontWeight = if (isDefault) FontWeight.Bold else FontWeight.Normal,
                color = if (isDefault) Color.Red else Color.White,
                letterSpacing = if (!isDefault) 2.sp else 0.sp
            )
        }
    }
}
