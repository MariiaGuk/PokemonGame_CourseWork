package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: ChimeraType,
    val maxPp: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp // Поточне значення при створенні дорівнює максимальному
        private set

    fun execute(attacker: Chimera, target: Chimera) {
        if (pp <= 0) return

        pp--

        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}