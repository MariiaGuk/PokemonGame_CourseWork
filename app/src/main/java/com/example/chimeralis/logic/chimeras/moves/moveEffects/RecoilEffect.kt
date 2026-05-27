package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Damages the target and then applies recoil to the attacker. */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    /** Applies target damage and recoil damage in one effect. */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        val damageToTarget = DamageEffect.calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damageToTarget)

        val recoilDamage = (damageToTarget / 100 * recoilPercent)
        attacker.stats.takeDamage(recoilDamage)
    }
}
