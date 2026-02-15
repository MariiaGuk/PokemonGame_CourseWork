package com.example.chimeralis.logic.moves.effects

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
}