package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Class describes damage effect.
 */
class DamageEffect(private val power: Int): IMoveEffect
{
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType) {
        val damage = calculateDamageAmount(attacker, target, moveType, power)
        target.stats.takeDamage(damage)
    }
    companion object {
        fun calculateDamageAmount(attacker: Pokemon, target: Pokemon, moveType: PokemonType, power: Int): Int {
            val effectiveness = moveType.typeEffectiveness(target.type)
            val stab = if (attacker.type == moveType) 1.5 else 1.0

            val baseDamage = (((2.0 * attacker.level / 5.0 + 2) * power * attacker.stats.attack / target.stats.defence) / 50.0 + 2)

            return (baseDamage * effectiveness * stab).toInt()
        }
    }
}
