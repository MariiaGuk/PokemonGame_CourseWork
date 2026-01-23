package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.types.ChimeraType

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: ChimeraType,
    var pp: Int,
    private val effects: List<IMoveEffect>
){
    fun execute(attacker: Chimera, target: Chimera) {
        if (pp <= 0) return

        pp--

        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}