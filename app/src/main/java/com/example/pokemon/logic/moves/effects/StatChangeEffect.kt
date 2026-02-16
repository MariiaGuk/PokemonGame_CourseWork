package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.PokemonType

/**
 * Class describes stats effect.
 */
class StatChangeEffect(
    private val statName: String,
    private val amount: Int,
    private val onTarget: Boolean = true
): IMoveEffect {
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
    {
        val subject = if (onTarget) target else attacker

        subject.stats.modifyStat(statName, amount)
    }
}