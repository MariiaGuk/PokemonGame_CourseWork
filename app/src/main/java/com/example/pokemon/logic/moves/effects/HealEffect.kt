package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
    {
        attacker.stats.currentHp += healAmount
    }
}
