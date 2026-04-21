package com.refassistant.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography

val AppTypography = Typography(
    display1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    display2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    title1 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    title2 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    caption1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    )
)
