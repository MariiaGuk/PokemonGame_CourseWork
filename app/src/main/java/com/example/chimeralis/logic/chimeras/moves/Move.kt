package com.example.chimeralis.logic.chimeras.moves

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.moveEffects.IMoveEffect
import kotlin.random.Random

/**
 * Basic class for every move in the game.
 */
class Move (
    val name: String,
    val type: ChimeraType,
    val maxPp: Int,
    val accuracy: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp
        private set

    fun execute(attacker: Chimera, target: Chimera) {
        if (pp <= 0) return

        val chance = Random.nextInt(1, 101)
        if (chance > accuracy) {
            pp--
            return
        }

        pp--
        effects.forEach { effect ->
            effect.apply(attacker,target,type)
        }
    }
}