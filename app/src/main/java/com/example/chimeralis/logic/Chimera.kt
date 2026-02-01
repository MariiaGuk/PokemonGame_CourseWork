package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.Move
import kotlin.random.Random

/**
 * Basic class for every chimera in the game.
 */
class Chimera (
    var name: String,
    val type: ChimeraType,
    val baseStats: Stats,
    val stats: Stats,
    var level: Int,
    val moves: List<Move>
){
    fun levelUp() {
        level++

        stats.maxHp = ((2 * baseStats.maxHp * level) / 100) + level + 10
        stats.attack = ((2 * baseStats.attack * level) / 100) + 5
        stats.defence = ((2 * baseStats.defence * level) / 100) + 5
        stats.speed = ((2 * baseStats.speed * level) / 100) + 5

        if (level % 5 == 0) evolution()
    }

    fun evolution(){
        //evolution logic
    }
}
