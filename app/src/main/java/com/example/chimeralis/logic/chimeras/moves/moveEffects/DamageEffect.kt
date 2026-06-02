package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Applies direct damage to the target chimera. */
class DamageEffect(private val power: Int): IMoveEffect
{
    /**
     * Calculates and applies damage to the target.
     *
     * @param attacker The attacker value used by this operation.
     * @param target The target value used by this operation.
     * @param moveType The move type value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType) {
        val damage = calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damage)
    }
    companion object {

        /**
         * Calculates typed damage with effectiveness and same-type attack bonus.
         *
         * @param attacker The attacker value used by this operation.
         * @param target The target value used by this operation.
         * @param moveType The move type value used by this operation.
         * @param power The power value used by this operation.
         * @return The calculated numeric value.
         */
        fun calculateDamageAmount(attacker: Chimera, target: Chimera, moveType: ChimeraType, power: Int): Int {
            val effectiveness = moveType.typeEffectiveness(target.type)
            val stab = if (attacker.type == moveType) 1.5 else 1.0

            val baseDamage = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2)

            return (baseDamage * effectiveness * stab).toInt().coerceAtLeast(1)
        }
    }
}
