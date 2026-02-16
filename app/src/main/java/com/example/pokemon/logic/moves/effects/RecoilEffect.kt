package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Class describes recoil effect.
 */
class RecoilEffect(private val power: Int, private val recoilPercent: Int) : IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
    {
        val damageToTarget = DamageEffect.calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damageToTarget)

        val recoilDamage = (damageToTarget / 100 * recoilPercent)
        attacker.stats.takeDamage(recoilDamage)
    }
}