package com.example.chimeralis.logic.chimeras.moves

import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.moveEffects.DamageEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.HealEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.StatChangeEffect

/** Default move catalog used by the game. */
object DefaultMoveCatalog : MoveCatalog {
    override val definitions: List<MoveDefinition> = listOf(
        MoveDefinition(
            id = MoveName.TACKLE,
            displayName = "Tackle",
            type = ChimeraType.NORMAL,
            maxPp = 35,
            accuracy = 100,
            effectsFactory = { listOf(DamageEffect(power = 40)) }
        ),
        MoveDefinition(
            id = MoveName.EMBER,
            displayName = "Ember",
            type = ChimeraType.FIRE,
            maxPp = 25,
            accuracy = 100,
            effectsFactory = { listOf(DamageEffect(power = 40)) }
        ),
        MoveDefinition(
            id = MoveName.GROWL,
            displayName = "Growl",
            type = ChimeraType.NORMAL,
            maxPp = 40,
            accuracy = 100,
            effectsFactory = {
                listOf(
                    StatChangeEffect(
                        statType = Stats.StatType.ATTACK,
                        amount = -1,
                        onTarget = true
                    )
                )
            }
        ),
        MoveDefinition(
            id = MoveName.TAILWHIP,
            displayName = "Tail Whip",
            type = ChimeraType.NORMAL,
            maxPp = 30,
            accuracy = 100,
            effectsFactory = {
                listOf(
                    StatChangeEffect(
                        statType = Stats.StatType.DEFENCE,
                        amount = -1,
                        onTarget = true
                    )
                )
            }
        ),
        MoveDefinition(
            id = MoveName.RECOVER,
            displayName = "Recover",
            type = ChimeraType.NORMAL,
            maxPp = 5,
            accuracy = 100,
            effectsFactory = { listOf(HealEffect(healAmount = 50)) }
        )
    )
}
