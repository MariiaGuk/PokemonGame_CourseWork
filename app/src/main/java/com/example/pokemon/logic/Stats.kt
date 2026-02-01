package com.example.pokemon.logic

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
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var attack: Int = attack
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var defence: Int = defence
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var speed: Int = speed
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var currentHp: Int = maxHp
        set(value) {
            field = value.coerceAtLeast(0).coerceAtMost(maxHp)
        }

    fun takeDamage(damage: Int) {
        currentHp -= damage
    }

    fun heal(healAmount: Int) {
        currentHp += healAmount
    }

    fun isAlive(): Boolean = currentHp > 0
    fun modifyStat(type: String, amount: Int){}
    fun upgrade(maxHp: Int = 0, attack: Int = 0, defence: Int = 0, speed: Int = 0) {
        this.maxHp += maxHp
        this.currentHp += maxHp
        this.attack += attack
        this.defence += defence
        this.speed += speed
    }
}