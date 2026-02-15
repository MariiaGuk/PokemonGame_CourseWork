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
        val effectiveness = moveType.typeEffectiveness(target.type)

        val stab = if (attacker.type == moveType) 1.5 else 1.0

        val damageToTarget = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2) * effectiveness * stab

        target.stats.currentHp -= damageToTarget.toInt()

        val recoilDamage = (damageToTarget * recoilPercent)
        attacker.stats.currentHp -= recoilDamage.toInt()
    }
}