package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.DefaultRandomProvider
import com.example.chimeralis.logic.battle.RandomProvider
import com.example.chimeralis.logic.chimeras.Chimera

/** Resolves which side acts first during a move turn. */
class BattleTurnOrderResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /** Returns true when the player's chimera should act before the enemy. */
    fun playerActsFirst(playerChimera: Chimera, enemyChimera: Chimera): Boolean {
        return when {
            playerChimera.stats.speed > enemyChimera.stats.speed -> true
            playerChimera.stats.speed < enemyChimera.stats.speed -> false
            else -> randomProvider.nextDouble() < SpeedTiePlayerChance
        }
    }

    private companion object {
        const val SpeedTiePlayerChance = 0.5
    }
}
