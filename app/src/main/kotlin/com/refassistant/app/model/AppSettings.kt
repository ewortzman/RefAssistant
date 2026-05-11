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
     * Returns formats visible in picker. Exhibition always included.
     * If user has toggled off every non-Exhibition format, fall back to all enabled so picker stays usable.
     */
    fun visibleFormats(): List<WeightFormat> {
        val regular = WeightFormat.entries.filter { it != WeightFormat.EXH && it in enabledFormats }
        val effective = if (regular.isEmpty()) WeightFormat.entries.filter { it != WeightFormat.EXH } else regular
        return effective + WeightFormat.EXH
    }

    companion object {
        val DEFAULT_ENABLED_FORMATS: Set<WeightFormat> = WeightFormat.entries
            .filter { it != WeightFormat.EXH }
            .toSet()
    }
}
