package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats

/**
 * Class describes stats effect.
 */
class StatChangeEffect(
    private val statType: Stats.StatType,
    private val amount: Int,
    private val onTarget: Boolean = true
): IMoveEffect {
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        val subject = if (onTarget) target else attacker

        subject.stats.modifyStat(statType, amount)
    }
}