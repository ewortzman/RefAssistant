package com.refassistant.app.model

enum class WeightFormat(val label: String, val weights: List<Int>) {
    COED_14("Coed 14", listOf(106, 113, 120, 126, 132, 138, 144, 150, 157, 165, 175, 190, 215, 285)),
    COED_13("Coed 13", listOf(107, 114, 121, 127, 133, 139, 145, 152, 160, 172, 189, 215, 285)),
    COED_12("Coed 12", listOf(108, 116, 124, 131, 138, 145, 152, 160, 170, 190, 215, 285)),
    GIRLS_14("Girls 14", listOf(100, 105, 110, 115, 120, 125, 130, 135, 140, 145, 155, 170, 190, 235)),
    GIRLS_13("Girls 13", listOf(100, 106, 112, 118, 124, 130, 136, 142, 148, 155, 170, 190, 235)),
    GIRLS_12("Girls 12", listOf(100, 107, 114, 120, 126, 132, 138, 145, 152, 165, 185, 235)),
    JV("JV", emptyList());
}

data class WeightClass(val label: String) {
    val isJv: Boolean get() = label == "JV"

    companion object {
        val JV = WeightClass("JV")

        fun listFor(format: WeightFormat): List<WeightClass> {
            return format.weights.map { WeightClass(it.toString()) }
        }

        fun buildMatchOrder(format: WeightFormat, startingWeight: WeightClass): List<WeightClass> {
            val all = listFor(format)
            val startIdx = all.indexOfFirst { it.label == startingWeight.label }
            if (startIdx < 0) return all
            return all.subList(startIdx, all.size) + all.subList(0, startIdx)
        }

        fun defaultFirst(): WeightClass = WeightClass("106")
    }
}
