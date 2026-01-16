package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.types.PokemonType

/**
 * Basic abstract class for every pokemon in the game.
 */
abstract class Move (
    var name: String,
    val type: PokemonType,
    var pp: Int
){
    abstract fun execute(attacker: Pokemon, target: Pokemon)
}