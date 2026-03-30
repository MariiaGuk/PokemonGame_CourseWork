package com.example.pokemon.logic.pokemons.moves

import com.example.pokemon.logic.pokemons.PokemonType
import com.example.pokemon.logic.pokemons.moves.moveEffects.DamageEffect
import com.example.pokemon.logic.pokemons.moves.moveEffects.HealEffect
import com.example.pokemon.logic.pokemons.moves.moveEffects.StatChangeEffect
import com.example.pokemon.logic.pokemons.Stats

/**
 * Factory for every move in the game.
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
                type = PokemonType.NORMAL,
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
                type = PokemonType.NORMAL,
                maxPp = 5,
                accuracy = 100,
                effects = listOf(HealEffect(healAmount = 50))
            )
        }
    }
}