package com.example.chimeralis.logic.moves.effects

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType
import com.example.chimeralis.logic.Stats

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