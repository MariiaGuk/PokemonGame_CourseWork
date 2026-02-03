package com.example.chimeralis.logic.moves.effects

import com.example.chimeralis.logic.Chimera

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Chimera, target: Chimera)
}