package com.example.chimeralis.logic.battle

class BattleEscapeResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {
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
