package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType
import com.example.chimeralis.logic.moves.effects.IMoveEffect

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: ChimeraType,
    val maxPp: Int,
    val accurecy: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp
        private set

    fun execute(attacker: Chimera, target: Chimera) {
        if (pp <= 0) return

        pp--

        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}