package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

/**
 * Class describes recoil effect.
 */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera)
    {
        val effectiveness = attacker.type.typeEffectiveness(target.type)

        val damageToTarget = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2) * effectiveness

        target.stats.currentHp -= damageToTarget.toInt()

        val recoilDamage = (damageToTarget * recoilPercent)
        attacker.stats.currentHp -= recoilDamage.toInt()
    }
}