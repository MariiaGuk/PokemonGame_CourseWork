package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Damages the target and then applies recoil to the attacker. */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : MoveEffect
{
    /**
     * Applies target damage and recoil damage in one effect.
     *
     * @param attacker The attacker value used by this operation.
     * @param target The target value used by this operation.
     * @param moveType The move type value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        val damageToTarget = DamageEffect.calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damageToTarget)

        if (recoilPercent > 0) {
            val recoilDamage = (damageToTarget * recoilPercent / 100).coerceAtLeast(1)
            attacker.stats.takeDamage(recoilDamage)
        }
    }
}
