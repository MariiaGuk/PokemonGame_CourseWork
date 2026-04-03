package com.example.pokemon.logic.pokemons

import com.example.pokemon.logic.pokemons.moves.MoveFactory
import com.example.pokemon.logic.pokemons.moves.MoveName

/**
 * Factory for every pokemon in the game.
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
            PokemonSpecies.Charizard -> Pokemon(
                name = "Charizard",
                species = species,
                type = PokemonType.FIRE,
                baseStats = Stats(78, 84, 78, 100),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    4 to { MoveFactory.createMove(MoveName.EMBER)},
                    //...
                )
            )
            PokemonSpecies.Charmeleon -> Pokemon(
                name = "Charmeleon",
                species = species,
                type = PokemonType.FIRE,
                baseStats = Stats(58, 64, 58, 80),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    4 to { MoveFactory.createMove(MoveName.EMBER)},
                    //...
                )
            )
            PokemonSpecies.Charmander -> Pokemon(
                name = "Charmander",
                species = species,
                type = PokemonType.FIRE,
                baseStats = Stats(39, 52, 43, 65),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    4 to { MoveFactory.createMove(MoveName.EMBER)},
                    //...
                )
            )
            PokemonSpecies.Bulbasaur -> Pokemon(
                name = "Bulbasaur",
                species = species,
                type = PokemonType.GRASS,
                baseStats = Stats(45, 49, 49, 45),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    //...
                )
            )
            PokemonSpecies.Squirtle -> Pokemon(
                name = "Squirtle",
                species = species,
                type = PokemonType.WATER,
                baseStats = Stats(44, 48, 65, 43),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.TAILWHIP)},
                    //...
                )
            )
        }
    }
}
