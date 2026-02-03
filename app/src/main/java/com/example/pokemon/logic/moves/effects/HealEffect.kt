package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        attacker.stats.currentHp += healAmount
    }
}
