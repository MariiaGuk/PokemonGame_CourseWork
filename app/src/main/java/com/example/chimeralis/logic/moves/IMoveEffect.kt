package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera

interface IMoveEffect {
    fun apply(attacker: Chimera, target: Chimera)
}