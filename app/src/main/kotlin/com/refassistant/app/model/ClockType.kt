package com.refassistant.app.model

import com.refassistant.app.R

enum class ClockType(val label: String, val iconRes: Int, val durationMs: Long) {
    BLOOD("Blood", R.drawable.ic_blood_droplet, 300_000L),
    INJURY("Injury", R.drawable.ic_plus_outline, 90_000L),
    RECOVERY("Recovery", R.drawable.ic_recovery, 120_000L),
    HNC("HNC", R.drawable.ic_head_outline, 300_000L);
}
