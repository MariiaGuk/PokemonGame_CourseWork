package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

class RecoilEffect(
    private val damage: Int
) : IMoveEffect
{
    override fun apply(attacker: Chimera, target: Chimera)
    {
        target.takeDamage(damage)
    }
}