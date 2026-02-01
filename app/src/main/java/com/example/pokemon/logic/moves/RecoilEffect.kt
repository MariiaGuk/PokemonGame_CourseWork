package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon

/**
 * Class describes recoil effect.
 */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        val effectiveness = attacker.type.typeEffectiveness(target.type)
        val damageToTarget = (attacker.stats.attack + power - target.stats.defence) * effectiveness
        target.stats.attack -= damageToTarget.toInt()

        val recoilDamage = (damageToTarget * recoilPercent)
        attacker.stats.attack -= recoilDamage.toInt()
    }
}