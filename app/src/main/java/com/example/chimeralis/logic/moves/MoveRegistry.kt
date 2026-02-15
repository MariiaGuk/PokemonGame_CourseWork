package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.ChimeraType
import com.example.chimeralis.logic.moves.effects.DamageEffect
import com.example.chimeralis.logic.moves.effects.HealEffect
import com.example.chimeralis.logic.moves.effects.StatChangeEffect

/**
 * Registry for every move in the game.
 */
object MoveRegistry {
    fun tackle() = Move(
        name = "Tackle",
        type = ChimeraType.NORMAL,
        maxPp = 35,
        accuracy = 100,
        effects = listOf(DamageEffect(power = 40))
    )

    fun ember() = Move(
        name = "Ember",
        type = ChimeraType.FIRE,
        maxPp = 25,
        accuracy = 100,
        effects = listOf(DamageEffect(power = 40))
    )

    fun growl() = Move(
        name = "Growl",
        type = ChimeraType.NORMAL,
        maxPp = 40,
        accuracy = 100,
        effects = listOf(StatChangeEffect(statName = "attack", amount = -1, true))
    )

    fun tailWhip() = Move(
        name = "Tail Whip",
        type = ChimeraType.NORMAL,
        maxPp = 30,
        accuracy = 100,
        effects = listOf(StatChangeEffect(statName = "defence", amount = -1, true))
    )

    fun recover() = Move(
        name = "Recover",
        type = ChimeraType.NORMAL,
        maxPp = 5,
        accuracy = 100,
        effects = listOf(HealEffect(healAmount = 50))
    )
}