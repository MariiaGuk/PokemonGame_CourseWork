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
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var attack: Int = attack
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var defence: Int = defence
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var speed: Int = speed
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var currentHp: Int = maxHp
        private set(value) {
            field = value.coerceIn(0, maxHp)
        }

    init {
        this.maxHp = maxHp
        this.attack = attack
        this.defence = defence
        this.speed = speed
        this.currentHp = maxHp
    }

    enum class StatType {
        MAX_HP, ATTACK, DEFENCE, SPEED
    }

    fun takeDamage(damage: Int) {
        currentHp -= damage
    }

    fun heal(amount: Int) {
        currentHp += amount
    }

    fun modifyStat(statType: StatType, amount: Int) {
        when (statType) {
            StatType.ATTACK -> attack += amount
            StatType.DEFENCE -> defence += amount
            StatType.SPEED -> speed += amount
            StatType.MAX_HP -> {
                maxHp += amount
                currentHp = currentHp
            }
        }
    }

    fun setStat(statType: StatType, amount: Int) {
        when (statType) {
            StatType.ATTACK -> attack = amount
            StatType.DEFENCE -> defence = amount
            StatType.SPEED -> speed = amount
            StatType.MAX_HP -> {
                maxHp = amount
                currentHp = currentHp
            }
        }
    }

    fun isAlive(): Boolean = currentHp > 0
}