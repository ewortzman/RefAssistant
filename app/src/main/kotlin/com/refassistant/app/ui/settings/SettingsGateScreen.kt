package com.refassistant.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import com.refassistant.app.R

@Composable
fun SettingsGateScreen(
    onOpen: () -> Unit,
    isAmbient: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!isAmbient) {
            Button(
                onClick = onOpen,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.secondaryButtonColors()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cog),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_cog),
                contentDescription = "Settings",
                tint = Color.DarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
