package com.example.chimeralis.logic.moves.effects

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType

/**
 * Class describes recoil effect.
 */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        val damageToTarget = DamageEffect.calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damageToTarget)

        val recoilDamage = (damageToTarget / 100 * recoilPercent)
        attacker.stats.takeDamage(recoilDamage)
    }
}