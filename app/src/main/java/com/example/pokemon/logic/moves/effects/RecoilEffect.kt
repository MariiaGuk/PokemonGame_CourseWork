package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon

/**
 * Class describes recoil effect.
 */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        val effectiveness = attacker.type.typeEffectiveness(target.type)

        val damageToTarget = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2) * effectiveness

        target.stats.currentHp -= damageToTarget.toInt()

        val recoilDamage = (damageToTarget * recoilPercent)
        attacker.stats.currentHp -= recoilDamage.toInt()
    }
}