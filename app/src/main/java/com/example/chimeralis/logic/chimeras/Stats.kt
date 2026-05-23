package com.example.chimeralis.logic.chimeras

/**
 * Class for stats of the chimera.
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
    var attackStage: Int = 0
        private set(value) {
            field = value.coerceIn(MIN_STAT_STAGE, MAX_STAT_STAGE)
        }
    var defenceStage: Int = 0
        private set(value) {
            field = value.coerceIn(MIN_STAT_STAGE, MAX_STAT_STAGE)
        }
    var speedStage: Int = 0
        private set(value) {
            field = value.coerceIn(MIN_STAT_STAGE, MAX_STAT_STAGE)
        }

    init {
        this.maxHp = maxHp
        this.attack = attack
        this.defence = defence
        this.speed = speed
        this.currentHp = this.maxHp
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

    fun restoreHp(amount: Int) {
        currentHp = amount
    }

    fun modifyStat(statType: StatType, amount: Int) {
        when (statType) {
            StatType.ATTACK -> {
                val oldStage = attackStage
                attackStage += amount
                attack += attackStage - oldStage
            }
            StatType.DEFENCE -> {
                val oldStage = defenceStage
                defenceStage += amount
                defence += defenceStage - oldStage
            }
            StatType.SPEED -> {
                val oldStage = speedStage
                speedStage += amount
                speed += speedStage - oldStage
            }
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

    companion object {
        private const val MIN_STAT_STAGE = -6
        private const val MAX_STAT_STAGE = 6
    }
}
