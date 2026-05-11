package com.refassistant.app.ui.main

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState
import com.refassistant.app.model.WeightClass
import com.refassistant.app.model.WeightFormat
import kotlinx.coroutines.launch

@Composable
fun WeightClassPicker(
    currentFormat: WeightFormat,
    availableFormats: List<WeightFormat>,
    onSelect: (WeightFormat, WeightClass) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf<WeightFormat?>(null) }

    if (selectedFormat == null) {
        FormatPicker(
            currentFormat = currentFormat,
            formats = availableFormats,
            onSelectFormat = { format ->
                if (format == WeightFormat.EXH) {
                    onSelect(format, WeightClass.EXH)
                } else {
                    selectedFormat = format
                }
            }
        )
    } else {
        WeightPicker(
            format = selectedFormat!!,
            onSelectWeight = { weight -> onSelect(selectedFormat!!, weight) },
            onBack = { selectedFormat = null }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FormatPicker(
    currentFormat: WeightFormat,
    formats: List<WeightFormat>,
    onSelectFormat: (WeightFormat) -> Unit
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberScope()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                scope.launch {
                    listState.scrollBy(event.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        state = listState
    ) {
        item {
            Text(
                text = "Weight Format",
                style = MaterialTheme.typography.title2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(formats) { format ->
            val isCurrent = format == currentFormat
            Chip(
                onClick = { onSelectFormat(format) },
                label = {
                    Text(
                        text = format.label,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = if (isCurrent) ChipDefaults.primaryChipColors()
                    else ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun WeightPicker(
    format: WeightFormat,
    onSelectWeight: (WeightClass) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val weights = remember(format) { WeightClass.listFor(format) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberScope()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                scope.launch {
                    listState.scrollBy(event.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        state = listState
    ) {
        item {
            Chip(
                onClick = onBack,
                label = {
                    Text(
                        text = "< ${format.label}",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(weights) { weight ->
            Chip(
                onClick = { onSelectWeight(weight) },
                label = {
                    Text(
                        text = weight.label,
                        style = MaterialTheme.typography.title1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun rememberScope(): kotlinx.coroutines.CoroutineScope =
    androidx.compose.runtime.rememberCoroutineScope()
