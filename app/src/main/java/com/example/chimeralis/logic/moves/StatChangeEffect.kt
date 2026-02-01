package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

/**
 * Class describes stats effect.
 */
class StatChangeEffect(
    private val statName: String,
    private val amount: Int,
    private val onTarget: Boolean = true
): IMoveEffect{
    override fun apply(attacker: Chimera, target: Chimera)
    {
        val subject = if (onTarget) target else attacker

        when (statName) {
            "attack" -> subject.stats.attack += amount
            "defence" -> subject.stats.defence += amount
            "speed" -> subject.stats.speed += amount
        }
    }
}