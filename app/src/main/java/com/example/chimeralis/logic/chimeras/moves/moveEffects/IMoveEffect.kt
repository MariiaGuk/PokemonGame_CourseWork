package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
}