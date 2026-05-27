package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Restores HP to the attacking chimera. */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    /** Applies healing to the move user. */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        attacker.stats.heal(healAmount)
    }
}
