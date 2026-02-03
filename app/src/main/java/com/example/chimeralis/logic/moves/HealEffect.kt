package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera)
    {
        attacker.stats.currentHp += healAmount
    }
}
