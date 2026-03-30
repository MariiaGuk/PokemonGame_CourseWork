package com.example.pokemon.logic.pokemons.moves.moveEffects

import com.example.pokemon.logic.pokemons.Pokemon
import com.example.pokemon.logic.pokemons.PokemonType

/**
 * Interface for applying move effects.
 */
interface IMoveEffect {
    fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
}