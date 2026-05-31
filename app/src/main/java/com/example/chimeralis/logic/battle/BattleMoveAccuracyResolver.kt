package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.moves.Move

/** Resolves whether an attempted move passes its accuracy check. */
class BattleMoveAccuracyResolver(
    private val randomProvider: RandomProvider = DefaultRandomProvider
) {

    /** Returns true when the move hits according to its configured accuracy. */
    fun moveHits(move: Move): Boolean {
        val accuracy = move.accuracy.coerceIn(0, Move.MaxAccuracyRoll)
        val roll = randomProvider.nextInt(Move.MinAccuracyRoll..Move.MaxAccuracyRoll)

        return roll <= accuracy
    }
}
