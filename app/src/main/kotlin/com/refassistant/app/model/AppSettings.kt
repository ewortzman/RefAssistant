package com.refassistant.app.model

data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val confirmResetEnabled: Boolean = true,
    val bloodDurationMs: Long = 300_000L,
    val injuryDurationMs: Long = 90_000L,
    val recoveryDurationMs: Long = 120_000L,
    val hncDurationMs: Long = 300_000L,
    val enabledFormats: Set<WeightFormat> = DEFAULT_ENABLED_FORMATS
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

    /**
     * Returns formats visible in picker. JV always included.
     * If user has toggled off every non-JV format, fall back to all enabled so picker stays usable.
     */
    fun visibleFormats(): List<WeightFormat> {
        val nonJv = WeightFormat.entries.filter { it != WeightFormat.JV && it in enabledFormats }
        val effective = if (nonJv.isEmpty()) WeightFormat.entries.filter { it != WeightFormat.JV } else nonJv
        return effective + WeightFormat.JV
    }

    companion object {
        val DEFAULT_ENABLED_FORMATS: Set<WeightFormat> = WeightFormat.entries
            .filter { it != WeightFormat.JV }
            .toSet()
    }
}
