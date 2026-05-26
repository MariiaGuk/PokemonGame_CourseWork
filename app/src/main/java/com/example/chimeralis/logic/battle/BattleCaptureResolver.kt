package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera

data class BattleCaptureResult(
    val chance: Float,
    val caught: Boolean
)

class BattleCaptureResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {
    fun resolve(target: Chimera): BattleCaptureResult {
        val chance = catchChance(target)
        return BattleCaptureResult(
            chance = chance,
            caught = randomProvider.nextDouble() < chance
        )
    }

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
