package com.refassistant.app.model

enum class WeightClass(val label: String) {
    W106("106"),
    W113("113"),
    W120("120"),
    W126("126"),
    W132("132"),
    W138("138"),
    W144("144"),
    W150("150"),
    W157("157"),
    W165("165"),
    W175("175"),
    W190("190"),
    W215("215"),
    W285("285"),
    JV("JV");

    fun next(): WeightClass {
        if (this == JV) return JV
        val vals = entries
        val idx = vals.indexOf(this)
        return if (idx + 1 < vals.size) vals[idx + 1] else vals.last()
    }
}
