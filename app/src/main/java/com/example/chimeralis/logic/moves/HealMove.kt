package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.types.ChimeraType

class HealMove(healAmount: Int, name: String, type: ChimeraType, pp: Int): Move(name, type, pp)
{
    override fun execute(attacker: Chimera, target: Chimera)
    {

    }
}