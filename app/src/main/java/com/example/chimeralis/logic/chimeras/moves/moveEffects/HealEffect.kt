package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Restores HP to the attacking chimera. */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    /**
     * Applies healing to the move user.
     *
     * @param attacker The attacker value used by this operation.
     * @param target The target value used by this operation.
     * @param moveType The move type value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        attacker.stats.heal(healAmount)
    }
}
