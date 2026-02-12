package com.example.pokemon.logic.moves

import com.example.pokemon.logic.PokemonType
import com.example.pokemon.logic.moves.effects.DamageEffect
import com.example.pokemon.logic.moves.effects.HealEffect
import com.example.pokemon.logic.moves.effects.StatChangeEffect

object MoveRegistry {
    fun tackle() = Move(
        name = "Tackle",
        type = PokemonType.NORMAL,
        maxPp = 35,
        accuracy = 100,
        effects = listOf(DamageEffect(power = 40))
    )

    fun ember() = Move(
        name = "Ember",
        type = PokemonType.FIRE,
        maxPp = 25,
        accuracy = 100,
        effects = listOf(DamageEffect(power = 40))
    )

    fun growl() = Move(
        name = "Growl",
        type = PokemonType.NORMAL,
        maxPp = 40,
        accuracy = 100,
        effects = listOf(StatChangeEffect(statName = "attack", amount = -1, true))
    )

    fun tailWhip() = Move(
        name = "Tail Whip",
        type = PokemonType.NORMAL,
        maxPp = 30,
        accuracy = 100,
        effects = listOf(StatChangeEffect(statName = "defence", amount = -1, true))
    )

    fun recover() = Move(
        name = "Recover",
        type = PokemonType.NORMAL,
        maxPp = 5,
        accuracy = 100,
        effects = listOf(HealEffect(healAmount = 50))
    )
}