package com.example.pokemon.logic.moves

import com.example.pokemon.logic.PokemonType
import com.example.pokemon.logic.Stats.StatType
import com.example.pokemon.logic.moves.effects.DamageEffect
import com.example.pokemon.logic.moves.effects.HealEffect
import com.example.pokemon.logic.moves.effects.StatChangeEffect

/**
 * Registry for every move in the game.
 */
object MoveFactory {
    fun createMove(move: MoveName): Move {
        return when (move) {
            MoveName.TACKLE -> Move(
                name = "Tackle",
                type = PokemonType.NORMAL,
                maxPp = 35,
                accuracy = 100,
                effects = listOf(DamageEffect(power = 40))
            )

            MoveName.EMBER -> Move(
                name = "Ember",
                type = PokemonType.FIRE,
                maxPp = 25,
                accuracy = 100,
                effects = listOf(DamageEffect(power = 40))
            )

            MoveName.GROWL -> Move(
                name = "Growl",
                type = PokemonType.NORMAL,
                maxPp = 40,
                accuracy = 100,
                effects = listOf(StatChangeEffect(statType = StatType.ATTACK, amount = -1, true))
            )

            MoveName.TAILWHIP -> Move(
                name = "Tail Whip",
                type = PokemonType.NORMAL,
                maxPp = 30,
                accuracy = 100,
                effects = listOf(StatChangeEffect(statType = StatType.DEFENCE, amount = -1, true))
            )

            MoveName.RECOVER -> Move(
                name = "Recover",
                type = PokemonType.NORMAL,
                maxPp = 5,
                accuracy = 100,
                effects = listOf(HealEffect(healAmount = 50))
            )
        }
    }
}