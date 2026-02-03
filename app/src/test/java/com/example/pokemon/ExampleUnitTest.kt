package com.example.pokemon

import com.example.pokemon.logic.PokemonRegistry
import com.example.pokemon.logic.PokemonType
import com.example.pokemon.logic.Stats
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
        assertEquals(13, charmander.stats.maxHp)
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

    //Effects Tests
    @Test
    fun damageEffectTest() {
        val attacker = PokemonRegistry.charmander(level=5)
        val target = PokemonRegistry.charmander(level=5)

        assertEquals(18, target.stats.currentHp)

        attacker.moves[0].execute(attacker, target)

        assertEquals(13, target.stats.currentHp)
    }
    @Test
    fun statChangeEffectTest() {
        val attacker = PokemonRegistry.charmander(level=5)
        val target = PokemonRegistry.charmander(level=5)

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
}