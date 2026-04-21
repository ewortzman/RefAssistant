package com.refassistant.app.model

data class StopwatchState(
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val startTimeNanos: Long = 0L
) {
    fun displayElapsedMs(currentNanos: Long): Long {
        return if (isRunning) {
            elapsedMs + (currentNanos - startTimeNanos) / 1_000_000
        } else {
            elapsedMs
        }
    }
}
