package com.refassistant.app.model

import com.refassistant.app.R

enum class ClockType(val label: String, val iconRes: Int) {
    BLOOD("Blood", R.drawable.ic_blood_droplet),
    INJURY("Injury", R.drawable.ic_plus_outline),
    RECOVERY("Recovery", R.drawable.ic_recovery),
    HNC("HNC", R.drawable.ic_head_outline);
}
