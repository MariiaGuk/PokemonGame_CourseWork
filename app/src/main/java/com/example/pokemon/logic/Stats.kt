package com.example.pokemon.logic

import kotlin.math.max
import kotlin.math.min

/**
 * Class for stats of the pokemon.
 */
class Stats(
    maxHp: Int,
    attack: Int,
    defence: Int,
    speed: Int
) {
    var maxHp: Int = maxHp
        private set
    var attack: Int = attack
        private set
    var defence: Int = defence
        private set
    var speed: Int = speed
        private set

    var currentHp: Int = maxHp
        private set


    fun takeDamage(damage: Int) {
        currentHp = max(0, currentHp - damage)
    }

    fun heal(healAmount: Int) {
        currentHp = min(maxHp, currentHp + healAmount)
    }

    fun isAlive(): Boolean = currentHp > 0

    fun upgrade(hpGain: Int, atkGain: Int, defGain: Int, spdGain: Int) {
        maxHp += hpGain
        currentHp += hpGain
        attack += atkGain
        defence += defGain
        speed += spdGain
    }
}