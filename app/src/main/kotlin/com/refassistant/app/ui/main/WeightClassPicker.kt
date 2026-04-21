package com.refassistant.app.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
fun WeightClassPicker(
    currentFormat: WeightFormat,
    onSelect: (WeightFormat, WeightClass) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf<WeightFormat?>(null) }

    if (selectedFormat == null) {
        FormatPicker(
            currentFormat = currentFormat,
            onSelectFormat = { selectedFormat = it }
        )
    } else {
        WeightPicker(
            format = selectedFormat!!,
            onSelectWeight = { weight -> onSelect(selectedFormat!!, weight) },
            onBack = { selectedFormat = null }
        )
    }
}

@Composable
private fun FormatPicker(
    currentFormat: WeightFormat,
    onSelectFormat: (WeightFormat) -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
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
        items(WeightFormat.entries) { format ->
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

@Composable
private fun WeightPicker(
    format: WeightFormat,
    onSelectWeight: (WeightClass) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val weights = remember(format) { WeightClass.listFor(format) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
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
