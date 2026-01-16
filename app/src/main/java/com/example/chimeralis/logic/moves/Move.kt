package com.example.chimeralis.logic.moves

import com.example.chimeralis.logic.Chimera
import com.example.chimeralis.logic.types.ChimeraType

/**
 * Basic abstract class for every chimera in the game.
 */
abstract class Move (
    var name: String,
    val type: ChimeraType,
    var pp: Int
){
    abstract fun execute(attacker: Chimera, target: Chimera)
}