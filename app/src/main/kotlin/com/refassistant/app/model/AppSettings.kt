package com.refassistant.app.model

data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val confirmResetEnabled: Boolean = true,
    val bloodDurationMs: Long = 300_000L,
    val injuryDurationMs: Long = 90_000L,
    val recoveryDurationMs: Long = 120_000L,
    val hncDurationMs: Long = 300_000L
) {
    fun durationFor(type: ClockType): Long = when (type) {
        ClockType.BLOOD -> bloodDurationMs
        ClockType.INJURY -> injuryDurationMs
        ClockType.RECOVERY -> recoveryDurationMs
        ClockType.HNC -> hncDurationMs
    }

    fun withDurationFor(type: ClockType, ms: Long): AppSettings = when (type) {
        ClockType.BLOOD -> copy(bloodDurationMs = ms)
        ClockType.INJURY -> copy(injuryDurationMs = ms)
        ClockType.RECOVERY -> copy(recoveryDurationMs = ms)
        ClockType.HNC -> copy(hncDurationMs = ms)
    }
}
