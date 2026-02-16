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

    fun takeDamage(damage: Int) {
        currentHp -= damage
    }

    fun heal(amount: Int) {
        currentHp += amount
    }

    fun modifyStat(statName: String, amount: Int) {
        when (statName.lowercase()) {
            "attack" -> attack += amount
            "defence" -> defence += amount
            "speed" -> speed += amount
            "maxhp" -> {
                maxHp += amount
                currentHp = currentHp
            }
        }
    }

    fun setStat(statName: String, amount: Int) {
        when (statName.lowercase()) {
            "attack" -> attack = amount
            "defence" -> defence = amount
            "speed" -> speed = amount
            "maxhp" -> {
                maxHp = amount
                currentHp = currentHp
            }
        }
    }

    fun isAlive(): Boolean = currentHp > 0
}