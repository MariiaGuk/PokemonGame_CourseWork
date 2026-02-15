package com.example.chimeralis.logic.moves.effects

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.ChimeraType

/**
 * Class describes heal effect.
 */
class HealEffect(private val healAmount: Int): IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        attacker.stats.currentHp += healAmount
    }
}
