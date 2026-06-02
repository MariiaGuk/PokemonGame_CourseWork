package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.random.DefaultRandomProvider
import com.example.chimeralis.logic.battle.random.RandomProvider

/** Calculates whether the player can escape from a battle. */
class BattleEscapeResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /**
     * Returns true when speed and attempt count allow escape.
     *
     * @param playerSpeed The player speed value used by this operation.
     * @param enemySpeed The enemy speed value used by this operation.
     * @param escapeAttempts The escape attempts value used by this operation.
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
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
