package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

/**
 * Class describes damage effect.
 */
class DamageEffect(private val power: Int): IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera)
    {
        val effectiveness = attacker.type.typeEffectiveness(target.type)

        val damage = (attacker.stats.attack + power - target.stats.defence) * effectiveness

        target.stats.attack -= damage.toInt()
    }
}
