package com.example.pokemon.logic

import com.example.pokemon.logic.moves.MoveRegistry

object PokemonRegistry {
    fun charmander(level: Int = 1) = Pokemon(
        name = "Charmander",
        type = PokemonType.FIRE,
        baseStats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        exp = 0,
        level = level,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.growl()),
        learnableMoves = mapOf(
            4 to { MoveRegistry.ember() },
            //...
        )
    )

    fun bulbasaur() = Pokemon(
        name = "Bulbasaur",
        type = PokemonType.GRASS,
        baseStats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        exp = 0,
        level = 1,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.growl()),
        learnableMoves = mapOf()
    )

    fun squirtle() = Pokemon(
        name = "Squirtle",
        type = PokemonType.WATER,
        baseStats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        exp = 0,
        level = 1,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.tailWhip()),
        learnableMoves = mapOf()
    )
}