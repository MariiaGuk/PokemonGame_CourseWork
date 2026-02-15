package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
}