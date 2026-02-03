package com.example.pokemon.logic.moves.effects

import com.example.pokemon.logic.Pokemon

/**
 * Class describes stats effect.
 */
class StatChangeEffect(
    private val statName: String,
    private val amount: Int,
    private val onTarget: Boolean = true
): IMoveEffect {
    override fun apply(attacker: Pokemon, target: Pokemon)
    {
        val subject = if (onTarget) target else attacker

        when (statName) {
            "attack" -> subject.stats.attack += amount
            "defence" -> subject.stats.defence += amount
            "speed" -> subject.stats.speed += amount
        }
    }
}