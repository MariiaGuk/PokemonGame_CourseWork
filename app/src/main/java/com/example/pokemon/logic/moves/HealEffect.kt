package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon

class HealEffect(
    private val damage: Int
) : IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        target.takeDamage(damage)
    }
}
