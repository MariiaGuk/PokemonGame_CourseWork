package com.example.chimeralis.logic.chimeras.moves

import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.moveEffects.IMoveEffect

/** Describes how to create one configured battle move. */
data class MoveDefinition(
    val id: MoveName,
    val displayName: String,
    val type: ChimeraType,
    val maxPp: Int,
    val accuracy: Int,
    private val effectsFactory: () -> List<IMoveEffect>
) {

    /** Creates a fresh move instance with independent PP state. */
    fun createMove(): Move {
        return Move(
            id = id,
            name = displayName,
            type = type,
            maxPp = maxPp,
            accuracy = accuracy,
            effects = effectsFactory()
        )
    }
}
