package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

class EnemyMoveSelector(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {
    fun selectMove(chimera: Chimera): Move {
        val moves = chimera.moves
        return moves[randomProvider.nextInt(moves.indices)]
    }
}
