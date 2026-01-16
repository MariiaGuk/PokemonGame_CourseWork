package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.Move
import com.example.chimeralis.logic.types.ChimeraType

/**
 * Basic abstract class for every chimera in the game.
 */
abstract class Chimera (
    var name: String,
    val type: ChimeraType,
    var maxHp: Int,
    var level: Int,
    val moves: List<Move>
){
    var currentHp: Int = maxHp
        private set

    fun takeDamage(damage: Int) {
        currentHp -= damage
        if (currentHp < 0) currentHp = 0
    }

    fun isAlive(): Boolean = currentHp > 0

    abstract fun calculateAttackDamage(): Int
}
