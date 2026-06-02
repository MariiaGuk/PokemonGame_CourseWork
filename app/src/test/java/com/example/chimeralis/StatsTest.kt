package com.example.chimeralis

import com.example.chimeralis.logic.chimeras.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {

    @Test
    fun hpOperationsStayWithinBounds() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 10, speed = 10)

        stats.heal(50)
        assertEquals(100, stats.currentHp)

        stats.takeDamage(150)
        assertEquals(0, stats.currentHp)
        assertFalse(stats.isAlive())

        stats.restoreHp(40)
        assertEquals(40, stats.currentHp)
        assertTrue(stats.isAlive())
    }

    @Test
    fun baseStatsAreCoercedAndBattleStagesAreClamped() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 10, speed = 10)

        stats.setStat(Stats.StatType.MAX_HP, -5)
        assertEquals(1, stats.maxHp)

        stats.setStat(Stats.StatType.ATTACK, -5)
        assertEquals(1, stats.attack)

        stats.setStat(Stats.StatType.DEFENCE, 10)
        stats.modifyStat(Stats.StatType.DEFENCE, -10)
        assertEquals(-3, stats.defenceStage)
        assertEquals(7, stats.defence)

        stats.setStat(Stats.StatType.SPEED, 10)
        stats.modifyStat(Stats.StatType.SPEED, 10)
        assertEquals(3, stats.speedStage)
        assertEquals(13, stats.speed)
    }

    @Test
    fun resetBattleStagesRestoresBaseBattleStats() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 11, speed = 12)

        stats.modifyStat(Stats.StatType.ATTACK, -2)
        stats.modifyStat(Stats.StatType.DEFENCE, 2)
        stats.modifyStat(Stats.StatType.SPEED, -1)

        stats.resetBattleStages()

        assertEquals(0, stats.attackStage)
        assertEquals(0, stats.defenceStage)
        assertEquals(0, stats.speedStage)
        assertEquals(10, stats.attack)
        assertEquals(11, stats.defence)
        assertEquals(12, stats.speed)
    }

    @Test
    fun copyCreatesIndependentStatsInstance() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 10, speed = 10)
        stats.takeDamage(30)

        val copy = stats.copy()
        copy.heal(10)

        assertNotSame(stats, copy)
        assertEquals(70, stats.currentHp)
        assertEquals(80, copy.currentHp)
    }
}
