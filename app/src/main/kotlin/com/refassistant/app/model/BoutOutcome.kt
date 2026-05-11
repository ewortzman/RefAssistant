package com.refassistant.app.model

/**
 * NFHS dual-meet team points awarded for each bout result.
 * Forfeit, default, and DQ currently award the winning team the same as a fall (6).
 */
enum class BoutOutcome(val label: String, val shortLabel: String, val teamPoints: Int) {
    FALL("Fall", "Fall", 6),
    TECH("Tech Fall", "Tech", 5),
    MAJOR("Major Decision", "Maj", 4),
    DECISION("Decision", "Dec", 3),
    FORFEIT("Forfeit", "FFT", 6),
    DEFAULT("Default", "Def", 6),
    DQ("Disqualification", "DQ", 6),
    DOUBLE_FORFEIT("Double Forfeit", "DFF", 0)
}
