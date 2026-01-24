package com.example.pokemon.logic

import com.example.pokemon.logic.moves.Move
import com.example.pokemon.logic.PokemonType
import kotlin.random.Random

/**
 * Basic abstract class for every pokemon in the game.
 */
abstract class Pokemon (
    var name: String,
    val type: PokemonType,
    val stats: Stats,
    var level: Int,
    val moves: List<Move>
){
    fun takeDamage(damage: Int) {
        stats.takeDamage(damage)
    }

    fun heal(healAmount: Int) {
        stats.heal(healAmount)
    }

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

    abstract fun calculateAttackDamage(): Int
}
