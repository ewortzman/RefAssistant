package com.refassistant.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors
import androidx.compose.ui.graphics.Color

private val WearColors = Colors(
    primary = Color(0xFF90CAF9),
    primaryVariant = Color(0xFF42A5F5),
    secondary = Color(0xFFA5D6A7),
    secondaryVariant = Color(0xFF66BB6A),
    background = Color.Black,
    surface = Color(0xFF1A1A1A),
    error = Color(0xFFEF5350),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

@Composable
fun RefAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        typography = AppTypography,
        content = content
    )
}
