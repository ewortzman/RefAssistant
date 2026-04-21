package com.refassistant.app.util

fun formatElapsedTime(elapsedMs: Long): String {
    val totalSeconds = elapsedMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (elapsedMs % 1000) / 100
    return "%d:%02d.%d".format(minutes, seconds, tenths)
}
