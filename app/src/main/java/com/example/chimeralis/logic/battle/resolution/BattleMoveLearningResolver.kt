package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.trainers.Player

/** Describes a pending move-learning choice for one chimera. */
data class MoveLearnRequest(
    val chimera: Chimera,
    val move: Move
)

/** Resolves pending move-learning decisions for the player's team. */
class BattleMoveLearningResolver(
    private val player: Player
) {

    /** Returns the first chimera currently waiting for a move replacement decision. */
    fun pendingRequest(): MoveLearnRequest? {
        val chimera = player.team.firstOrNull { candidate ->
            candidate.pendingMoveToLearn != null
        } ?: return null
        val pendingMove = chimera.pendingMoveToLearn ?: return null

        return MoveLearnRequest(
            chimera = chimera,
            move = pendingMove
        )
    }

    /** Applies the player's decision for the current pending move-learning request. */
    fun resolve(replaceIndex: Int?): List<String> {
        val request = pendingRequest() ?: return emptyList()
        val log = mutableListOf<String>()

        if (replaceIndex == null) {
            val skippedMove = request.chimera.skipPendingMove()
            if (skippedMove != null) {
                log.add("${request.chimera.name} did not learn ${skippedMove.name}.")
            }
        } else {
            val learnedMoves = request.chimera.replaceMoveWithPending(replaceIndex)
            if (learnedMoves != null) {
                val (forgottenMove, learnedMove) = learnedMoves
                log.add("${request.chimera.name} forgot ${forgottenMove.name}.")
                log.add("${request.chimera.name} learned ${learnedMove.name}!")
            }
        }

        pendingRequest()?.let { nextRequest ->
            log.add("${nextRequest.chimera.name} wants to learn ${nextRequest.move.name}.")
            log.add("Choose a move to forget, or keep the old moves.")
        }

        return log.ifEmpty { listOf("Nothing happened.") }
    }
}
