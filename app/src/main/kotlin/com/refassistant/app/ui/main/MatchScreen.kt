package com.refassistant.app.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.model.WeightClass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchScreen(
    currentWeight: WeightClass,
    onNextMatch: () -> Unit,
    onSetStartingWeight: (WeightClass) -> Unit,
    isAmbient: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker && !isAmbient) {
        WeightClassPicker(
            onSelect = { weight ->
                onSetStartingWeight(weight)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
        return
    }

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
                text = "Match",
                style = MaterialTheme.typography.caption1,
                color = if (isAmbient) Color.DarkGray else Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentWeight.label,
                style = MaterialTheme.typography.display1,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = if (isAmbient) Modifier else Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { showPicker = true }
                )
            )

            if (!isAmbient) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onNextMatch) {
                    Text("Next Match", style = MaterialTheme.typography.body2)
                }
            }
        }

        if (!isAmbient) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Green.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }
}
