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

    /**
     * Applies direct HP damage without dropping below zero.
     *
     * @param damage The damage value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun takeDamage(damage: Int) {
        currentHp -= damage
    }

    /**
     * Restores HP by a relative amount without exceeding max HP.
     *
     * @param amount Numeric value used by this operation: amount.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun heal(amount: Int) {
        currentHp += amount
    }

    /**
     * Sets HP to an absolute value constrained by max HP.
     *
     * @param amount Numeric value used by this operation: amount.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun restoreHp(amount: Int) {
        currentHp = amount
    }

    /**
     * Modifies a stat stage or max HP during battle.
     *
     * @param statType The stat type value used by this operation.
     * @param amount Numeric value used by this operation: amount.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Sets a base stat value and reapplies its current battle stage.
     *
     * @param statType The stat type value used by this operation.
     * @param amount Numeric value used by this operation: amount.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Clears all temporary battle stat stages.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun resetBattleStages() {
        attackStage = 0
        defenceStage = 0
        speedStage = 0
        attack = stagedStat(baseAttack, attackStage)
        defence = stagedStat(baseDefence, defenceStage)
        speed = stagedStat(baseSpeed, speedStage)
    }

    /**
     * Returns whether the chimera can still fight.
     *
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
    fun isAlive(): Boolean = currentHp > 0

    /**
     * Creates an independent copy with the same stat values and battle stages.
     *
     * @return The resulting Stats value.
     */
    fun copy(): Stats {
        return Stats(maxHp, baseAttack, baseDefence, baseSpeed).also { copiedStats ->
            copiedStats.currentHp = currentHp
            copiedStats.attackStage = attackStage
            copiedStats.defenceStage = defenceStage
            copiedStats.speedStage = speedStage
            copiedStats.attack = stagedStat(copiedStats.baseAttack, copiedStats.attackStage)
            copiedStats.defence = stagedStat(copiedStats.baseDefence, copiedStats.defenceStage)
            copiedStats.speed = stagedStat(copiedStats.baseSpeed, copiedStats.speedStage)
        }
    }

    companion object {
        private const val MIN_STAT_STAGE = -3
        private const val MAX_STAT_STAGE = 3

        /**
         * Applies a simple stage modifier to a base stat.
         *
         * @param baseValue The base value value used by this operation.
         * @param stage The stage value used by this operation.
         * @return The calculated numeric value.
         */
        private fun stagedStat(baseValue: Int, stage: Int): Int {
            return (baseValue + stage).coerceAtLeast(1)
        }
    }
}
