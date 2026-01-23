package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.types.PokemonType

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: PokemonType,
    var pp: Int,
    private val effects: List<IMoveEffect>
){
    fun execute(attacker: Pokemon, target: Pokemon) {
        if (pp <= 0) return

        pp--

        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}