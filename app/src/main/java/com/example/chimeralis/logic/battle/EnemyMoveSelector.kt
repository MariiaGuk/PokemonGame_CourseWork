package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

/** Selects the enemy's move for a turn. */
class EnemyMoveSelector(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {
    /** Returns one available move from the enemy chimera. */
    fun selectMove(chimera: Chimera): Move {
        val moves = chimera.moves
        return moves[randomProvider.nextInt(moves.indices)]
    }
}
