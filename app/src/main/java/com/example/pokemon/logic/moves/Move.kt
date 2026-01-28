package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: PokemonType,
    val maxPp: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp // Поточне значення при створенні дорівнює максимальному
        private set

    fun execute(attacker: Pokemon, target: Pokemon) {
        if (pp <= 0) return

        pp--

        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}