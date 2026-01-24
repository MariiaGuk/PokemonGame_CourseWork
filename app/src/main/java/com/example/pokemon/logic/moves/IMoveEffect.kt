package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Pokemon, target: Pokemon)
}