package com.refassistant.app.model

enum class ChoiceSide { NONE, RED, GREEN }

enum class ChoiceParity { ODD, EVEN }

/**
 * Returns which side has choice for bout [boutNumber] (1-indexed).
 * If [winner] took [winnerTook] parity, winner gets choice on those bouts,
 * loser gets choice on the other parity. Returns NONE if winner is NONE.
 */
fun choiceForBout(
    winner: ChoiceSide,
    winnerTook: ChoiceParity,
    boutNumber: Int
): ChoiceSide {
    if (winner == ChoiceSide.NONE) return ChoiceSide.NONE
    val boutParity = if (boutNumber % 2 == 1) ChoiceParity.ODD else ChoiceParity.EVEN
    return if (boutParity == winnerTook) winner else opposite(winner)
}

private fun opposite(side: ChoiceSide): ChoiceSide = when (side) {
    ChoiceSide.RED -> ChoiceSide.GREEN
    ChoiceSide.GREEN -> ChoiceSide.RED
    ChoiceSide.NONE -> ChoiceSide.NONE
}
