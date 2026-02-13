package com.example.pokemon

import com.example.pokemon.logic.PokemonRegistry
import com.example.pokemon.logic.PokemonType
import com.example.pokemon.logic.Stats
import com.example.pokemon.logic.moves.Move
import com.example.pokemon.logic.moves.effects.DamageEffect
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
        val charmander = PokemonRegistry.charmander()

        charmander.levelUp()

        assertEquals(2, charmander.level)
        assertTrue(charmander.stats.maxHp in 13..14)
        assertEquals(7, charmander.stats.attack)
    }

    //Effectiveness Tests
    @Test
    fun effectivenessTest() {
        val fire = PokemonType.FIRE

        assertEquals(2.0, fire.typeEffectiveness(PokemonType.GRASS), 0.01)
        assertEquals(0.5, fire.typeEffectiveness(PokemonType.WATER), 0.01)
        assertEquals(1.0, fire.typeEffectiveness(PokemonType.NORMAL), 0.01)
    }

    //Accuracy Tests
    @Test
    fun accuracyTest() {
        val attacker = PokemonRegistry.charmander()
        val target = PokemonRegistry.charmander()
        val initialHp = target.stats.currentHp

        val brokenMove = Move(
            name = "Broken Move",
            type = PokemonType.NORMAL,
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
        val attacker = PokemonRegistry.charmander(level=5)
        val target = PokemonRegistry.charmander(level=5)

        assertTrue(target.stats.currentHp in 18..20)

        val initialHp = target.stats.currentHp
        attacker.moves[0].execute(attacker, target)
        assertEquals(initialHp - 5, target.stats.currentHp)
    }
    @Test
    fun statChangeEffectTest() {
        val attacker = PokemonRegistry.charmander(level=5)
        val target = PokemonRegistry.charmander(level=5)

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
        val charmander = PokemonRegistry.charmander(level = 3)

        assertFalse(charmander.moves.any { it.name == "Ember" })
        charmander.levelUp()

        assertEquals(4, charmander.level)
        assertTrue(charmander.moves.any { it.name == "Ember" })
    }
}