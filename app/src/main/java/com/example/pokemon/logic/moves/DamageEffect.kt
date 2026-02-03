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

        val damage = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2) * effectiveness

        target.stats.currentHp -= damage.toInt()
    }
}
