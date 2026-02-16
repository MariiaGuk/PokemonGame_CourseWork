package com.example.pokemon.logic

import com.example.pokemon.logic.moves.MoveRegistry

/**
 * Registry for every pokemon in the game.
 */
object PokemonRegistry {
    private fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )
    fun charmander(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Pokemon(
        name = "Charmander",
        type = PokemonType.FIRE,
        baseStats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.growl()},
            4 to {MoveRegistry.ember()},
            //...
        )
    )

    fun bulbasaur(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Pokemon(
        name = "Bulbasaur",
        type = PokemonType.GRASS,
        baseStats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.growl()},
            //...
        )
    )

    fun squirtle(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Pokemon(
        name = "Squirtle",
        type = PokemonType.WATER,
        baseStats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.tailWhip()},
            //...
        )
    )
}