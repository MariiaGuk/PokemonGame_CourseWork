package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon

/**
 * Class describes damage effect.
 */
class DamageEffect(private val power: Int): IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        val effectiveness = attacker.type.typeEffectiveness(target.type)

        val damage = (attacker.stats.attack + power - target.stats.defence) * effectiveness

        target.stats.takeDamage(damage.toInt())
    }
}
