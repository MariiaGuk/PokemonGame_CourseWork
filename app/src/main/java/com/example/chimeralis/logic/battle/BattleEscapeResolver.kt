package com.example.chimeralis.logic.battle

/** Calculates whether the player can escape from a battle. */
class BattleEscapeResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {
    /** Returns true when speed and attempt count allow escape. */
    fun canEscape(playerSpeed: Int, enemySpeed: Int, escapeAttempts: Int): Boolean {
        val odds = ((playerSpeed * 32) / (enemySpeed / 4).coerceAtLeast(1) % 256) +
                EscapeAttemptBonus * escapeAttempts

        return odds >= GuaranteedEscapeOdds || randomProvider.nextDouble() < odds / GuaranteedEscapeOdds.toDouble()
    }

    private companion object {
        const val EscapeAttemptBonus = 30
        const val GuaranteedEscapeOdds = 255
    }
}
