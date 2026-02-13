package com.example.chimeralis

import com.example.chimeralis.logic.ChimeraRegistry
import com.example.chimeralis.logic.ChimeraType
import com.example.chimeralis.logic.Stats
import com.example.chimeralis.logic.moves.Move
import com.example.chimeralis.logic.moves.effects.DamageEffect
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
        assertTrue(sunflare.stats.maxHp in 13..14)
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

    //Accuracy Tests
    @Test
    fun accuracyTest() {
        val attacker = ChimeraRegistry.sunflare()
        val target = ChimeraRegistry.sunflare()
        val initialHp = target.stats.currentHp

        val brokenMove = Move(
            name = "Broken Move",
            type = ChimeraType.NORMAL,
            maxPp = 10,
            accuracy = 0,
            effects = listOf(DamageEffect(power = 100))
        )

        brokenMove.execute(attacker, target)

        assertEquals(initialHp, target.stats.currentHp)
        assertEquals(9, brokenMove.pp)
    }

    //Effects Tests
    @Test
    fun damageEffectTest() {
        val attacker = ChimeraRegistry.sunflare(level=5)
        val target = ChimeraRegistry.sunflare(level=5)

        assertTrue(target.stats.currentHp in 18..20)

        val initialHp = target.stats.currentHp
        attacker.moves[0].execute(attacker, target)
        assertEquals(initialHp - 5, target.stats.currentHp)
    }
    @Test
    fun statChangeEffectTest() {
        val attacker = ChimeraRegistry.sunflare(level=5)
        val target = ChimeraRegistry.sunflare(level=5)

        val initialAttack = target.stats.attack
        assertTrue(attacker.stats.attack in 10..11)

        attacker.moves[1].execute(attacker, target)

        assertTrue(target.stats.attack == initialAttack - 1)
        assertTrue(target.stats.attack in 9..10)
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