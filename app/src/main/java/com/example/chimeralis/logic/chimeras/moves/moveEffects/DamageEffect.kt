package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType

/** Applies direct damage to the target chimera. */
class DamageEffect(private val power: Int): IMoveEffect
{
    /** Calculates and applies damage to the target. */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType) {
        val damage = calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damage)
    }
    companion object {

        /** Calculates typed damage with effectiveness and same-type attack bonus. */
        fun calculateDamageAmount(attacker: Chimera, target: Chimera, moveType: ChimeraType, power: Int): Int {
            val effectiveness = moveType.typeEffectiveness(target.type)
            val stab = if (attacker.type == moveType) 1.5 else 1.0

            val baseDamage = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2)

            return (baseDamage * effectiveness * stab).toInt().coerceAtLeast(1)
        }
    }
}
