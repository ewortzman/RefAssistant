package com.refassistant.app.ui.exhcounter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.ui.common.ConfirmDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExhCounterScreen(
    exhCount: Int,
    dualsInEvent: Int,
    dualActive: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onEndDual: () -> Unit,
    onNewEvent: () -> Unit,
    onOpenSummary: () -> Unit,
    confirmReset: Boolean = true,
    isAmbient: Boolean = false
) {
    var showConfirm by remember { mutableStateOf(false) }
    var showEndDualConfirm by remember { mutableStateOf(false) }
    var showNewEventConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Event — $dualsInEvent duals",
                style = MaterialTheme.typography.caption2,
                color = if (isAmbient) Color.DarkGray else Color.Gray,
                modifier = if (isAmbient) Modifier else Modifier.combinedClickable(
                    onClick = onOpenSummary,
                    onLongClick = {}
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Exhibitions",
                style = MaterialTheme.typography.caption1,
                color = if (isAmbient) Color.DarkGray else Color.Gray
            )

            Text(
                text = "$exhCount",
                style = MaterialTheme.typography.display2,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = if (isAmbient) Modifier else Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (confirmReset) showConfirm = true
                        else onReset()
                    }
                )
            )

            if (!isAmbient) {
                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDecrement,
                        modifier = Modifier.size(40.dp),
                        colors = ButtonDefaults.secondaryButtonColors(),
                        shape = CircleShape
                    ) {
                        Text("−", style = MaterialTheme.typography.title2)
                    }
                    Button(
                        onClick = onIncrement,
                        modifier = Modifier.size(40.dp),
                        colors = ButtonDefaults.primaryButtonColors(),
                        shape = CircleShape
                    ) {
                        Text("+", style = MaterialTheme.typography.title2)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (dualActive) {
                        CompactChip(
                            onClick = { showEndDualConfirm = true },
                            colors = ChipDefaults.secondaryChipColors(),
                            label = {
                                Text(
                                    "End Dual",
                                    style = MaterialTheme.typography.caption2,
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                    CompactChip(
                        onClick = { showNewEventConfirm = true },
                        colors = ChipDefaults.secondaryChipColors(),
                        label = {
                            Text(
                                "New Event",
                                style = MaterialTheme.typography.caption2,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            }
        }

        if (!isAmbient && showConfirm) {
            ConfirmDialog(
                message = "Reset Exhibition count?",
                onConfirm = {
                    onReset()
                    showConfirm = false
                },
                onDismiss = { showConfirm = false }
            )
        }

        if (!isAmbient && showEndDualConfirm) {
            ConfirmDialog(
                message = "End this dual?",
                onConfirm = {
                    onEndDual()
                    showEndDualConfirm = false
                },
                onDismiss = { showEndDualConfirm = false }
            )
        }

        if (!isAmbient && showNewEventConfirm) {
            ConfirmDialog(
                message = "Start new event? All history cleared.",
                onConfirm = {
                    onNewEvent()
                    showNewEventConfirm = false
                },
                onDismiss = { showNewEventConfirm = false }
            )
        }
    }
}
