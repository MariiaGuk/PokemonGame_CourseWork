package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Defines polymorphic behavior for effects produced by moves. */
interface IMoveEffect {

    /**
     * Applies the effect from an attacker to a target using the move type.
     *
     * @param attacker The attacker value used by this operation.
     * @param target The target value used by this operation.
     * @param moveType The move type value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
}
