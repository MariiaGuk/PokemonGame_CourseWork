package com.example.pokemon.logic.pokemons.moves.moveEffects

import com.example.pokemon.logic.pokemons.Pokemon
import com.example.pokemon.logic.pokemons.PokemonType

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
    {
        attacker.stats.heal(healAmount)
    }
}
