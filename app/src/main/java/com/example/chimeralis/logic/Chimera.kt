package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.Move
import kotlin.random.Random

/**
 * Basic abstract class for every chimera in the game.
 */
abstract class Chimera (
    var name: String,
    val type: ChimeraType,
    val stats: Stats,
    var level: Int,
    val moves: List<Move>
){
    fun levelUp() {
        level++
        if (level % 5 == 0) evolution()

        val hpGain = (stats.maxHp * (Random.nextInt(1, 11) / 100.0)).toInt().coerceAtLeast(1)
        val atkGain = (stats.attack * (Random.nextInt(1, 11) / 100.0)).toInt().coerceAtLeast(1)
        val defGain = (stats.defence * (Random.nextInt(1, 11) / 100.0)).toInt().coerceAtLeast(1)
        val spdGain = (stats.speed * (Random.nextInt(1, 11) / 100.0)).toInt().coerceAtLeast(1)

        stats.upgrade(hpGain, atkGain, defGain, spdGain)
    }

    fun evolution(){
        //evolution logic
    }
}
