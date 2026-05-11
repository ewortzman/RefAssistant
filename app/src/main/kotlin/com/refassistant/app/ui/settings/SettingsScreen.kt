package com.refassistant.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.rememberScalingLazyListState
import com.refassistant.app.model.AppSettings
import com.refassistant.app.model.ClockType
import com.refassistant.app.model.WeightFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleConfirm: (Boolean) -> Unit,
    onChangeDuration: (ClockType, Long) -> Unit,
    onToggleFormat: (WeightFormat, Boolean) -> Unit,
    isAmbient: Boolean = false
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onRotaryScrollEvent { event ->
                scope.launch { listState.scrollBy(event.verticalScrollPixels) }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        state = listState
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.title2,
                color = if (isAmbient) Color.DarkGray else Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
        item {
            ToggleChip(
                checked = settings.hapticsEnabled,
                onCheckedChange = onToggleHaptics,
                label = { Text("Haptics", style = MaterialTheme.typography.body2) },
                toggleControl = {
                    androidx.wear.compose.material.Switch(checked = settings.hapticsEnabled)
                },
                colors = ToggleChipDefaults.toggleChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            ToggleChip(
                checked = settings.confirmResetEnabled,
                onCheckedChange = onToggleConfirm,
                label = { Text("Confirm reset", style = MaterialTheme.typography.body2) },
                toggleControl = {
                    androidx.wear.compose.material.Switch(checked = settings.confirmResetEnabled)
                },
                colors = ToggleChipDefaults.toggleChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "Durations",
                style = MaterialTheme.typography.caption1,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp)
            )
        }
        ClockType.entries.forEach { type ->
            item {
                DurationRow(
                    label = type.label,
                    currentMs = settings.durationFor(type),
                    onChange = { onChangeDuration(type, it) }
                )
            }
        }
        item {
            Text(
                text = "Weight formats",
                style = MaterialTheme.typography.caption1,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp)
            )
        }
        WeightFormat.entries.filter { it != WeightFormat.EXH }.forEach { format ->
            item {
                val isEnabled = format in settings.enabledFormats
                ToggleChip(
                    checked = isEnabled,
                    onCheckedChange = { onToggleFormat(format, it) },
                    label = { Text(format.label, style = MaterialTheme.typography.body2) },
                    toggleControl = {
                        androidx.wear.compose.material.Switch(checked = isEnabled)
                    },
                    colors = ToggleChipDefaults.toggleChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DurationRow(
    label: String,
    currentMs: Long,
    onChange: (Long) -> Unit
) {
    Chip(
        onClick = {},
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label, style = MaterialTheme.typography.body2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { if (currentMs > 15_000L) onChange(currentMs - 15_000L) },
                        modifier = Modifier.size(28.dp),
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) { Text("−", style = MaterialTheme.typography.caption1) }
                    Text(
                        text = formatDuration(currentMs),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Button(
                        onClick = { onChange(currentMs + 15_000L) },
                        modifier = Modifier.size(28.dp),
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) { Text("+", style = MaterialTheme.typography.caption1) }
                }
            }
        },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return if (s == 0L) "${m}:00" else "%d:%02d".format(m, s)
}
