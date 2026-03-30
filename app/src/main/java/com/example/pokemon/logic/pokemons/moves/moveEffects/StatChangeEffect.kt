package com.example.pokemon.logic.pokemons.moves.moveEffects

import com.example.pokemon.logic.pokemons.Pokemon
import com.example.pokemon.logic.pokemons.PokemonType
import com.example.pokemon.logic.pokemons.Stats

/**
 * Class describes stats effect.
 */
class StatChangeEffect(
    private val statType: Stats.StatType,
    private val amount: Int,
    private val onTarget: Boolean = true
): IMoveEffect {
    override fun apply(attacker: Pokemon, target: Pokemon, moveType: PokemonType)
    {
        val subject = if (onTarget) target else attacker

        subject.stats.modifyStat(statType, amount)
    }
}