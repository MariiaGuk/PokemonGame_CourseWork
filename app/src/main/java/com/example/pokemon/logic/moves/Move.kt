package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType
import com.example.pokemon.logic.moves.effects.IMoveEffect
import kotlin.random.Random

/**
 * Basic class for every move in the game.
 */
class Move (
    var name: String,
    val type: PokemonType,
    val maxPp: Int,
    val accuracy: Int,
    private val effects: List<IMoveEffect>
){
    var pp: Int = maxPp
        private set

    fun execute(attacker: Pokemon, target: Pokemon) {
        if (pp <= 0) return

        val chance = Random.nextInt(1, 101)
        if (chance > accuracy) {
            pp--
            return
        }

        pp--
        effects.forEach { effect ->
            effect.apply(attacker,target)
        }
    }
}