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

        val damage = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2) * effectiveness

        target.stats.currentHp -= damage.toInt()
    }
}
