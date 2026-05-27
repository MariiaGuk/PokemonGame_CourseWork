package com.example.chimeralis.logic.chimeras

/** Stores mutable combat stats, HP, and temporary battle stages for a chimera. */
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
    private var baseAttack: Int = attack.coerceAtLeast(1)
    private var baseDefence: Int = defence.coerceAtLeast(1)
    private var baseSpeed: Int = speed.coerceAtLeast(1)

    var attack: Int = baseAttack
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var defence: Int = baseDefence
        private set(value) {
            field = value.coerceAtLeast(1)
        }
    var speed: Int = baseSpeed
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
        setStat(StatType.ATTACK, attack)
        setStat(StatType.DEFENCE, defence)
        setStat(StatType.SPEED, speed)
        this.currentHp = this.maxHp
    }

    /** Identifies the stat field targeted by stat operations. */
    enum class StatType {
        MAX_HP, ATTACK, DEFENCE, SPEED
    }

    /** Applies direct HP damage without dropping below zero. */
    fun takeDamage(damage: Int) {
        currentHp -= damage
    }

    /** Restores HP by a relative amount without exceeding max HP. */
    fun heal(amount: Int) {
        currentHp += amount
    }

    /** Sets HP to an absolute value constrained by max HP. */
    fun restoreHp(amount: Int) {
        currentHp = amount
    }

    /** Modifies a stat stage or max HP during battle. */
    fun modifyStat(statType: StatType, amount: Int) {
        when (statType) {
            StatType.ATTACK -> {
                attackStage += amount
                attack = stagedStat(baseAttack, attackStage)
            }
            StatType.DEFENCE -> {
                defenceStage += amount
                defence = stagedStat(baseDefence, defenceStage)
            }
            StatType.SPEED -> {
                speedStage += amount
                speed = stagedStat(baseSpeed, speedStage)
            }
            StatType.MAX_HP -> {
                maxHp += amount
                currentHp = currentHp
            }
        }
    }

    /** Sets a base stat value and reapplies its current battle stage. */
    fun setStat(statType: StatType, amount: Int) {
        when (statType) {
            StatType.ATTACK -> {
                baseAttack = amount.coerceAtLeast(1)
                attack = stagedStat(baseAttack, attackStage)
            }
            StatType.DEFENCE -> {
                baseDefence = amount.coerceAtLeast(1)
                defence = stagedStat(baseDefence, defenceStage)
            }
            StatType.SPEED -> {
                baseSpeed = amount.coerceAtLeast(1)
                speed = stagedStat(baseSpeed, speedStage)
            }
            StatType.MAX_HP -> {
                maxHp = amount
                currentHp = currentHp
            }
        }
    }

    /** Clears all temporary battle stat stages. */
    fun resetBattleStages() {
        attackStage = 0
        defenceStage = 0
        speedStage = 0
        attack = stagedStat(baseAttack, attackStage)
        defence = stagedStat(baseDefence, defenceStage)
        speed = stagedStat(baseSpeed, speedStage)
    }

    /** Returns whether the chimera can still fight. */
    fun isAlive(): Boolean = currentHp > 0

    companion object {
        private const val MIN_STAT_STAGE = -3
        private const val MAX_STAT_STAGE = 3

        /** Applies a simple stage modifier to a base stat. */
        private fun stagedStat(baseValue: Int, stage: Int): Int {
            return (baseValue + stage).coerceAtLeast(1)
        }
    }
}
