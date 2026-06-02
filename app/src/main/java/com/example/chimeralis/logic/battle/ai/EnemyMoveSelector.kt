package com.example.chimeralis.logic.battle.ai

import com.example.chimeralis.logic.battle.random.DefaultRandomProvider
import com.example.chimeralis.logic.battle.random.RandomProvider
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

/** Selects the enemy's move for a turn. */
class EnemyMoveSelector(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /**
     * Returns one available move from the enemy chimera.
     *
     * @param chimera Domain object used by this operation: chimera.
     * @return The resulting Move value.
     */
    fun selectMove(chimera: Chimera): Move {
        val moves = chimera.moves
            .filter { move -> move.pp > 0 }
            .ifEmpty { chimera.moves }

        return moves[randomProvider.nextInt(moves.indices)]
    }
}
