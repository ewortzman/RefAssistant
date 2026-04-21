package com.refassistant.app.ui.jvcounter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.refassistant.app.ui.common.ConfirmDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JvCounterScreen(
    jvCount: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JVs",
                style = MaterialTheme.typography.caption1,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$jvCount",
                style = MaterialTheme.typography.display1,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { showConfirm = true }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDecrement,
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.secondaryButtonColors(),
                    shape = CircleShape
                ) {
                    Text("−", style = MaterialTheme.typography.title1)
                }

                Button(
                    onClick = onIncrement,
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.primaryButtonColors(),
                    shape = CircleShape
                ) {
                    Text("+", style = MaterialTheme.typography.title1)
                }
            }
        }

        if (showConfirm) {
            ConfirmDialog(
                message = "Reset JV count?",
                onConfirm = {
                    onReset()
                    showConfirm = false
                },
                onDismiss = { showConfirm = false }
            )
        }
    }
}
