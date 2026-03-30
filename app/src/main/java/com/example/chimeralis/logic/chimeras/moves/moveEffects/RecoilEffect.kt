package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

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