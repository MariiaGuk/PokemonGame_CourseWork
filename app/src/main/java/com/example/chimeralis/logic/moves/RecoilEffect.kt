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
        val damageToTarget = (attacker.stats.attack + power - target.stats.defence) * effectiveness
        target.stats.takeDamage(damageToTarget.toInt())

        val recoilDamage = (damageToTarget * recoilPercent)
        attacker.stats.takeDamage(recoilDamage.toInt())
    }
}