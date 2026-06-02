package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.random.DefaultRandomProvider
import com.example.chimeralis.logic.battle.random.RandomProvider
import com.example.chimeralis.logic.chimeras.Chimera

/** Calculates whether a capture attempt succeeds. */
class BattleCaptureResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /**
     * Resolves a capture attempt for one target chimera.
     *
     * @param target The target value used by this operation.
     * @return The resulting BattleCaptureResult value.
     */
    fun resolve(target: Chimera): BattleCaptureResult {
        val chance = catchChance(target)
        return BattleCaptureResult(
            chance = chance,
            caught = randomProvider.nextDouble() < chance
        )
    }

    /**
     * Computes catch chance from target HP.
     *
     * @param target The target value used by this operation.
     * @return The calculated numeric value.
     */
    private fun catchChance(target: Chimera): Float {
        val hpRatio = target.stats.currentHp.toFloat() / target.stats.maxHp.toFloat()
        return (BaseChance + (1f - hpRatio) * MissingHpBonus).coerceIn(MinChance, MaxChance)
    }

    private companion object {
        const val BaseChance = 0.28f
        const val MissingHpBonus = 0.55f
        const val MinChance = 0.25f
        const val MaxChance = 0.9f
    }
}
