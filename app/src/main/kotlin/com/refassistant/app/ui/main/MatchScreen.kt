package com.refassistant.app.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import com.refassistant.app.model.BoutOutcome
import com.refassistant.app.model.ChoiceParity
import com.refassistant.app.model.ChoiceSide
import com.refassistant.app.model.WeightClass
import com.refassistant.app.model.WeightFormat
import com.refassistant.app.util.rememberBatteryPercent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchScreen(
    currentWeight: WeightClass,
    currentFormat: WeightFormat,
    boutNumber: Int,
    totalBouts: Int,
    choiceForBout: ChoiceSide,
    choicePrompted: Boolean,
    availableFormats: List<WeightFormat>,
    redTeamScore: Int,
    greenTeamScore: Int,
    lastDualRedScore: Int?,
    lastDualGreenScore: Int?,
    teamScoreTrackingEnabled: Boolean,
    onNextMatch: () -> Unit,
    onRecordBoutResult: (ChoiceSide, BoutOutcome) -> Unit,
    onSetFormatAndWeight: (WeightFormat, WeightClass) -> Unit,
    onSetChoice: (ChoiceSide, ChoiceParity) -> Unit,
    onDismissChoicePrompt: () -> Unit,
    isAmbient: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }
    var showOutcomePicker by remember { mutableStateOf(false) }

    if (showPicker && !isAmbient) {
        WeightClassPicker(
            currentFormat = currentFormat,
            availableFormats = availableFormats,
            onSelect = { format, weight ->
                onSetFormatAndWeight(format, weight)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
        return
    }

    if (!isAmbient && !choicePrompted && !currentWeight.isExhibition) {
        ChoicePrompt(
            onSelect = onSetChoice,
            onSkip = onDismissChoicePrompt
        )
        return
    }

    if (showOutcomePicker && !isAmbient) {
        BoutOutcomePicker(
            onSelect = { winner, outcome ->
                onRecordBoutResult(winner, outcome)
                showOutcomePicker = false
            },
            onSkip = {
                onNextMatch()
                showOutcomePicker = false
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val batteryPct = rememberBatteryPercent()
        val batteryColor = when {
            isAmbient -> Color.DarkGray
            batteryPct <= 15 -> Color(0xFFFF5252)
            batteryPct <= 30 -> Color(0xFFFFAB40)
            else -> Color.Gray
        }
        TimeText(
            timeTextStyle = TimeTextDefaults.timeTextStyle(
                color = if (isAmbient) Color.DarkGray else Color.White
            )
        )
        Text(
            text = "${batteryPct}%",
            style = MaterialTheme.typography.caption3,
            color = batteryColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 6.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (boutNumber > 0) {
                Text(
                    text = "Bout $boutNumber / $totalBouts",
                    style = MaterialTheme.typography.caption2,
                    color = if (isAmbient) Color.DarkGray else Color.Gray
                )
            }

            val showLiveScore = teamScoreTrackingEnabled && !currentWeight.isExhibition
            val showLastScore = teamScoreTrackingEnabled && currentWeight.isExhibition &&
                lastDualRedScore != null && lastDualGreenScore != null
            if (showLiveScore) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$redTeamScore",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color(0xFFFF8A80)
                    )
                    Text(
                        text = "  -  ",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color.Gray
                    )
                    Text(
                        text = "$greenTeamScore",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color(0xFF69F0AE)
                    )
                }
            } else if (showLastScore) {
                Text(
                    text = "Last dual",
                    style = MaterialTheme.typography.caption3,
                    color = if (isAmbient) Color.DarkGray else Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$lastDualRedScore",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color(0xFFFF8A80)
                    )
                    Text(
                        text = "  -  ",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color.Gray
                    )
                    Text(
                        text = "$lastDualGreenScore",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.DarkGray else Color(0xFF69F0AE)
                    )
                }
            }

            Text(
                text = currentFormat.label,
                style = MaterialTheme.typography.caption1,
                color = if (isAmbient) Color.DarkGray else Color.Gray
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ChoiceArrow(
                    side = ChoiceSide.RED,
                    active = choiceForBout == ChoiceSide.RED,
                    leftSlot = true,
                    isAmbient = isAmbient
                )
                Spacer(Modifier.width(4.dp))
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
                Spacer(Modifier.width(4.dp))
                ChoiceArrow(
                    side = ChoiceSide.GREEN,
                    active = choiceForBout == ChoiceSide.GREEN,
                    leftSlot = false,
                    isAmbient = isAmbient
                )
            }

            if (!isAmbient) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentWeight.isExhibition || !teamScoreTrackingEnabled) onNextMatch()
                        else showOutcomePicker = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(36.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Next Match", style = MaterialTheme.typography.body2)
                    }
                }
            }
        }

        if (!isAmbient) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(15.dp)
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
                    .width(15.dp)
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

@Composable
private fun ChoiceArrow(side: ChoiceSide, active: Boolean, leftSlot: Boolean, isAmbient: Boolean) {
    val color = when {
        !active -> Color.Transparent
        isAmbient -> Color.White
        side == ChoiceSide.RED -> Color(0xFFFF5252)
        else -> Color(0xFF69F0AE)
    }
    // Arrow points AWAY from the weight text toward its side, so left slot (red) points left
    Text(
        text = if (leftSlot) "◀" else "▶",
        style = MaterialTheme.typography.body1,
        color = color
    )
}
