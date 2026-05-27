package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Defines polymorphic behavior for effects produced by moves. */
interface IMoveEffect {

    /** Applies the effect from an attacker to a target using the move type. */
    fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
}
