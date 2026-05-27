package com.example.chimeralis.logic.chimeras.moves

import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.moveEffects.DamageEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.HealEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.StatChangeEffect
import com.example.chimeralis.logic.chimeras.Stats

/** Creates move instances from move identifiers. */
object MoveFactory {

    /** Builds one move with type, PP, accuracy, and effects. */
    fun createMove(move: MoveName): Move {
        return when (move) {
            MoveName.TACKLE -> Move(
                name = "Tackle",
                type = ChimeraType.NORMAL,
                maxPp = 35,
                accuracy = 100,
                effects = listOf(DamageEffect(power = 40))
            )

            MoveName.EMBER -> Move(
                name = "Ember",
                type = ChimeraType.FIRE,
                maxPp = 25,
                accuracy = 100,
                effects = listOf(DamageEffect(power = 40))
            )

            MoveName.GROWL -> Move(
                name = "Growl",
                type = ChimeraType.NORMAL,
                maxPp = 40,
                accuracy = 100,
                effects = listOf(
                    StatChangeEffect(
                        statType = Stats.StatType.ATTACK,
                        amount = -1,
                        true
                    )
                )
            )

            MoveName.TAILWHIP -> Move(
                name = "Tail Whip",
                type = ChimeraType.NORMAL,
                maxPp = 30,
                accuracy = 100,
                effects = listOf(
                    StatChangeEffect(
                        statType = Stats.StatType.DEFENCE,
                        amount = -1,
                        true
                    )
                )
            )

            MoveName.RECOVER -> Move(
                name = "Recover",
                type = ChimeraType.NORMAL,
                maxPp = 5,
                accuracy = 100,
                effects = listOf(HealEffect(healAmount = 50))
            )
        }
    }
}
