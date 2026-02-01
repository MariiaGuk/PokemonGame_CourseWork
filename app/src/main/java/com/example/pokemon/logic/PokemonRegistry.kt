package com.example.pokemon.logic

import com.example.pokemon.logic.moves.MoveRegistry

object PokemonRegistry {
    fun charmander() = Pokemon(
        name = "Charmander",
        type = PokemonType.FIRE,
        baseStats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        stats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.growl())
    )

    fun bulbasaur() = Pokemon(
        name = "Bulbasaur",
        type = PokemonType.GRASS,
        baseStats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        stats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.growl())
    )

    fun squirtle() = Pokemon(
        name = "Squirtle",
        type = PokemonType.WATER,
        baseStats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        stats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.tailWhip())
    )
}