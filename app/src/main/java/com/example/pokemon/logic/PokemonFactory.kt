package com.example.pokemon.logic

import com.example.pokemon.logic.moves.MoveFactory
import com.example.pokemon.logic.moves.MoveName

/**
 * Registry for every pokemon in the game.
 */
object PokemonFactory {
    private fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )
    fun createPokemon(species: PokemonSpecies, level: Int = 1): Pokemon {
        val ivStats = generateRandomIV()

        return when (species) {
            PokemonSpecies.CHARMANDER -> Pokemon(
                name = "Charmander",
                type = PokemonType.FIRE,
                baseStats = Stats(39, 52, 43, 65),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.GROWL)},
                    4 to {MoveFactory.createMove(MoveName.EMBER)},
                    //...
                )
            )
            PokemonSpecies.BULBASAUR -> Pokemon(
                name = "Bulbasaur",
                type = PokemonType.GRASS,
                baseStats = Stats(45, 49, 49, 45),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.GROWL)},
                    //...
                )
            )
            PokemonSpecies.SQUIRTLE -> Pokemon(
                name = "Squirtle",
                type = PokemonType.WATER,
                baseStats = Stats(44, 48, 65, 43),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.TAILWHIP)},
                    //...
                )
            )
        }
    }
}