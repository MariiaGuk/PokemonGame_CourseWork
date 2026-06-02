package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.random.DefaultRandomProvider
import com.example.chimeralis.logic.battle.random.RandomProvider
import com.example.chimeralis.logic.chimeras.Chimera

/** Resolves which side acts first during a move turn. */
class BattleTurnOrderResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /**
     * Returns true when the player's chimera should act before the enemy.
     *
     * @param playerChimera The player chimera value used by this operation.
     * @param enemyChimera The enemy chimera value used by this operation.
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
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
