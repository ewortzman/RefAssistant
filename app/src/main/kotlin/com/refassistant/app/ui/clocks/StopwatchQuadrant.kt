package com.refassistant.app.ui.clocks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    durationMs: Long,
    tickNanos: Long,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    injuryTimeouts: Int = 0,
    hncUsed: Boolean = false,
    isAmbient: Boolean = false,
    undoPulseKey: Int = 0,
    modifier: Modifier = Modifier
) {
    val remainingMs = stopwatchState.remainingMs(durationMs, tickNanos)
    val expired = remainingMs == 0L && stopwatchState.elapsedMs > 0L
    val isDefault = clockType == ClockType.INJURY && injuryTimeouts >= 3
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

    val undoAlpha = remember { Animatable(0f) }
    LaunchedEffect(undoPulseKey) {
        if (undoPulseKey > 0) {
            undoAlpha.snapTo(1f)
            undoAlpha.animateTo(0f, tween(500))
        }
    }

    val defFlash = remember { Animatable(0f) }
    LaunchedEffect(isDefault) {
        if (isDefault) {
            repeat(3) {
                defFlash.snapTo(1f)
                defFlash.animateTo(0f, tween(250))
            }
        }
    }

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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp)
        ) {
            val progressColor = when {
                expired -> Color.Red
                stopwatchState.isRunning -> Color.Yellow
                stopwatchState.elapsedMs > 0L -> Color(0xFFFFAB40)
                else -> Color.White.copy(alpha = 0.35f)
            }
            val progress = if (durationMs > 0)
                (remainingMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            else 0f
            if (!isAmbient) {
                Canvas(modifier = Modifier.size(32.dp)) {
                    val stroke = 2.dp.toPx()
                    val topLeft = Offset(stroke / 2f, stroke / 2f)
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = Color.White.copy(alpha = 0.12f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    val pulse = maxOf(undoAlpha.value, defFlash.value * 0.8f)
                    if (pulse > 0f) {
                        val pulseColor = if (defFlash.value > 0f) Color.Red else Color.Cyan
                        drawArc(
                            color = pulseColor.copy(alpha = pulse),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke * 1.5f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Icon(
                painter = painterResource(id = clockType.iconRes),
                contentDescription = clockType.label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = formatElapsedTime(remainingMs),
            style = MaterialTheme.typography.body2,
            color = timeColor
        )
        when (clockType) {
            ClockType.INJURY -> {
                if (injuryTimeouts >= 3) {
                    Text(
                        text = "DEF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                } else if (injuryTimeouts > 0) {
                    DotRow(count = injuryTimeouts)
                } else {
                    Spacer(Modifier.height(6.dp))
                }
            }
            ClockType.HNC -> {
                if (hncUsed) DotRow(count = 1) else Spacer(Modifier.height(6.dp))
            }
            else -> Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun DotRow(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(6.dp)
    ) {
        repeat(count) {
            Canvas(modifier = Modifier.size(4.dp)) {
                drawCircle(Color.White)
            }
        }
    }
}
