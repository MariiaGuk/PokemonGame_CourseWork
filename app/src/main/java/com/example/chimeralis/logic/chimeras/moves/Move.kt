package com.example.chimeralis.logic.chimeras.moves

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.moveEffects.IMoveEffect

/** Represents a combat move with PP, accuracy, type, and effects. */
class Move (
    val id: MoveName,
    val name: String,
    val type: ChimeraType,
    val maxPp: Int,
    val accuracy: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp
        private set

    /** Restores PP to a constrained value within this move's maximum. */
    fun restorePp(value: Int) {
        pp = value.coerceIn(0, maxPp)
    }

    /** Executes the move against a target if PP and accuracy allow it. */
    fun execute(
        attacker: Chimera,
        target: Chimera,
        hits: Boolean
    ): MoveExecutionResult {
        if (pp <= 0) return MoveExecutionResult.NoPowerPoints

        pp--
        if (!hits) {
            return MoveExecutionResult.Missed
        }

        effects.forEach { effect ->
            effect.apply(attacker, target, type)
        }
        return MoveExecutionResult.Hit
    }

    companion object {
        const val MinAccuracyRoll = 1
        const val MaxAccuracyRoll = 100
    }
}
