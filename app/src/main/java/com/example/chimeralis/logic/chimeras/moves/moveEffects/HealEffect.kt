package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        attacker.stats.heal(healAmount)
    }
}
