package com.example.chimeralis

import com.example.chimeralis.logic.ChimeraRegistry
import com.example.chimeralis.logic.ChimeraType
import com.example.chimeralis.logic.Stats
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    //Stats Tests
    @Test
    fun currentHpLimitsTest() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 10, speed = 10)

        stats.heal(50)
        assertEquals(100, stats.currentHp)

        stats.takeDamage(150)
        assertEquals(0, stats.currentHp)
    }

    @Test
    fun otherStatsLimitsTest() {
        val stats = Stats(maxHp = 100, attack = 10, defence = 10, speed = 10)

        stats.maxHp = -5
        assertEquals(1, stats.maxHp)
        stats.attack = -5
        assertEquals(1, stats.attack)
        stats.defence = -5
        assertEquals(1, stats.defence)
        stats.speed = -5
        assertEquals(1, stats.speed)
    }

    //Level up Tests
    @Test
    fun levelUpFormulaTest() {
        val sunflare = ChimeraRegistry.sunflare()

        sunflare.levelUp()

        assertEquals(2, sunflare.level)
        assertEquals(13, sunflare.stats.maxHp)
        assertEquals(7, sunflare.stats.attack)
    }

    //Effectiveness Tests
    @Test
    fun effectivenessTest() {
        val fire = ChimeraType.FIRE

        assertEquals(2.0, fire.typeEffectiveness(ChimeraType.GRASS), 0.01)
        assertEquals(0.5, fire.typeEffectiveness(ChimeraType.WATER), 0.01)
        assertEquals(1.0, fire.typeEffectiveness(ChimeraType.NORMAL), 0.01)
    }

    //Effects Tests
    @Test
    fun damageEffectTest() {
        val attacker = ChimeraRegistry.sunflare(level=5)
        val target = ChimeraRegistry.sunflare(level=5)

        assertEquals(18, target.stats.currentHp)

        attacker.moves[0].execute(attacker, target)

        assertEquals(13, target.stats.currentHp)
    }
    @Test
    fun statChangeEffectTest() {
        val attacker = ChimeraRegistry.sunflare(level=5)
        val target = ChimeraRegistry.sunflare(level=5)

        assertEquals(10, target.stats.attack)

        attacker.moves[1].execute(attacker, target)

        assertEquals(9, target.stats.attack)
    }
    @Test
    fun healEffectTest() {
        //Test when heal moves added
    }
    @Test
    fun recoilEffectTest() {
        //Test when recoil moves added
    }

    //Adding new moves Tests
    @Test
    fun newMoveTest() {
        val sunflare = ChimeraRegistry.sunflare(level = 3)

        assertFalse(sunflare.moves.any { it.name == "Ember" })
        sunflare.levelUp()

        assertEquals(4, sunflare.level)
        assertTrue(sunflare.moves.any { it.name == "Ember" })
    }
}