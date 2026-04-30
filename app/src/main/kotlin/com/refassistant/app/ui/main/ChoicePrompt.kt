package com.refassistant.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.model.ChoiceParity
import com.refassistant.app.model.ChoiceSide

@Composable
fun ChoicePrompt(
    onSelect: (ChoiceSide, ChoiceParity) -> Unit,
    onSkip: () -> Unit
) {
    var winner by remember { mutableStateOf<ChoiceSide?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (winner == null) {
                Text(
                    "Who won toss?",
                    style = MaterialTheme.typography.body1,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { winner = ChoiceSide.RED },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFB71C1C)),
                        modifier = Modifier.size(60.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Red", color = Color.White) }
                    Button(
                        onClick = { winner = ChoiceSide.GREEN },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1B5E20)),
                        modifier = Modifier.size(60.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Green", color = Color.White) }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.secondaryButtonColors(),
                    modifier = Modifier.size(80.dp, 28.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Skip", style = MaterialTheme.typography.caption2) }
            } else {
                Text(
                    "Chose...",
                    style = MaterialTheme.typography.body1,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onSelect(winner!!, ChoiceParity.ODD) },
                        modifier = Modifier.size(64.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Odds") }
                    Button(
                        onClick = { onSelect(winner!!, ChoiceParity.EVEN) },
                        modifier = Modifier.size(64.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Evens") }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { winner = null },
                    colors = ButtonDefaults.secondaryButtonColors(),
                    modifier = Modifier.size(60.dp, 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Back", style = MaterialTheme.typography.caption2) }
            }
        }
    }
}
