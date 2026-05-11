package com.refassistant.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.refassistant.app.model.BoutOutcome
import com.refassistant.app.model.ChoiceSide
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BoutOutcomePicker(
    onSelect: (ChoiceSide, BoutOutcome) -> Unit,
    onSkip: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Result",
                    style = MaterialTheme.typography.title3,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Red", style = MaterialTheme.typography.caption2, color = Color(0xFFFF8A80))
                    Text("Green", style = MaterialTheme.typography.caption2, color = Color(0xFF69F0AE))
                }
            }
            BoutOutcome.entries.forEach { outcome ->
                item { OutcomeRow(outcome, onSelect) }
            }
            item {
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.secondaryButtonColors(),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(28.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Skip", style = MaterialTheme.typography.caption1)
                }
            }
        }
    }
}

@Composable
private fun OutcomeRow(outcome: BoutOutcome, onSelect: (ChoiceSide, BoutOutcome) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SideButton(
            label = outcome.shortLabel,
            color = Color(0xFFB71C1C),
            onClick = { onSelect(ChoiceSide.RED, outcome) },
            modifier = Modifier.weight(1f)
        )
        SideButton(
            label = outcome.shortLabel,
            color = Color(0xFF1B5E20),
            onClick = { onSelect(ChoiceSide.GREEN, outcome) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SideButton(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.caption1,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
