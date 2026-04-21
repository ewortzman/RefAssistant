package com.refassistant.app.model

enum class WeightFormat(val label: String, val weights: List<Int>) {
    BOYS_14("Boys 14", listOf(106, 113, 120, 126, 132, 138, 144, 150, 157, 165, 175, 190, 215, 285)),
    BOYS_13("Boys 13", listOf(107, 114, 121, 127, 133, 139, 145, 152, 160, 172, 189, 215, 285)),
    BOYS_12("Boys 12", listOf(108, 116, 124, 131, 138, 145, 152, 160, 170, 190, 215, 285)),
    GIRLS_14("Girls 14", listOf(100, 105, 110, 115, 120, 125, 130, 135, 140, 145, 155, 170, 190, 235)),
    GIRLS_13("Girls 13", listOf(100, 106, 112, 118, 124, 130, 136, 142, 148, 155, 170, 190, 235)),
    GIRLS_12("Girls 12", listOf(100, 107, 114, 120, 126, 132, 138, 145, 152, 165, 185, 235));
}

data class WeightClass(val label: String, val index: Int, val format: WeightFormat) {
    val isJv: Boolean get() = label == "JV"

    fun next(allWeights: List<WeightClass>): WeightClass {
        if (isJv) return this
        val nextIndex = allWeights.indexOf(this) + 1
        return if (nextIndex < allWeights.size) allWeights[nextIndex] else allWeights.last()
    }

    companion object {
        fun listFor(format: WeightFormat): List<WeightClass> {
            val weights = format.weights.mapIndexed { i, w ->
                WeightClass(label = w.toString(), index = i, format = format)
            }
            return weights + WeightClass(label = "JV", index = weights.size, format = format)
        }

        fun defaultFirst(): WeightClass = listFor(WeightFormat.BOYS_14).first()
    }
}
