package com.refassistant.app.model

/**
 * Snapshot of a completed dual meet. Stored in event history.
 */
data class DualSummary(
    val format: WeightFormat,
    val startingWeight: String,
    val boutsCompleted: Int,
    val totalBouts: Int,
    val redTeamScore: Int,
    val greenTeamScore: Int,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long
) {
    val durationMs: Long get() = endedAtEpochMs - startedAtEpochMs
}
