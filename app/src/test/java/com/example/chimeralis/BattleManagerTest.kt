package com.example.chimeralis

import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.scenario.BattleScenarioFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.NPC
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleManagerTest {

    @Test
    fun scenarioFactoryStartsBattleWithFirstLivingPlayerChimera() {
        val fainted = testChimera(ChimeraSpecies.Sunflare)
        val living = testChimera(ChimeraSpecies.Sylvhorn)
        fainted.stats.takeDamage(999)
        val player = testPlayer(fainted, living)

        val battle = BattleScenarioFactory.createWildBattle(
            player = player,
            wildSpecies = ChimeraSpecies.Aquantis,
            randomProvider = FixedRandomProvider(ints = listOf(0))
        )

        assertSame(living, player.activeChimera)
        assertSame(living, battle.playerChimera)
        assertTrue(battle.isBattleActive)
    }

    @Test
    fun performMoveTurnReturnsLogAndAnimation() {
        val playerChimera = testChimera(ChimeraSpecies.Sunflare, level = 6)
        val enemyChimera = testChimera(ChimeraSpecies.Sylvhorn, level = 5)
        val player = testPlayer(playerChimera)
        val enemy = NPC("Wild", listOf(enemyChimera))
        val battle = BattleManager(
            player = player,
            enemy = enemy,
            randomProvider = FixedRandomProvider(ints = listOf(1))
        )

        val result = battle.performTurnWithAnimations(BattleAction.UseMove(playerChimera.moves.first()))

        assertTrue(result.log.any { it == "Your ${playerChimera.name} used Tackle!" })
        assertTrue(result.animations.isNotEmpty())
        assertTrue(enemyChimera.stats.currentHp < enemyChimera.stats.maxHp)
    }

    @Test
    fun battlePromptsForcedSwitchWhenActivePlayerChimeraIsFainted() {
        val first = testChimera(ChimeraSpecies.Sunflare)
        val second = testChimera(ChimeraSpecies.Sylvhorn)
        val player = testPlayer(first, second)
        val enemy = NPC("Wild", listOf(testChimera(ChimeraSpecies.Aquantis)))
        val battle = BattleManager(player = player, enemy = enemy)
        first.stats.takeDamage(999)

        val result = battle.performTurnWithAnimations(BattleAction.UseMove(first.moves.first()))

        assertTrue(battle.isWaitingForPlayerSwitch)
        assertTrue(result.log.contains("Choose your next chimera!"))
    }

    @Test
    fun resolvingPendingEnemySwitchSendsOutNextLivingEnemy() {
        val playerChimera = testChimera(ChimeraSpecies.Sunflare, level = 20)
        val firstEnemy = testChimera(ChimeraSpecies.Sylvhorn, level = 1)
        val secondEnemy = testChimera(ChimeraSpecies.Aquantis, level = 5)
        val battle = BattleManager(
            player = testPlayer(playerChimera),
            enemy = NPC("Rival", listOf(firstEnemy, secondEnemy)),
            randomProvider = FixedRandomProvider(ints = listOf(1))
        )

        firstEnemy.stats.takeDamage(999)
        battle.performTurnWithAnimations(BattleAction.UseMove(playerChimera.moves.first()))
        battle.resolvePendingEnemySwitch()

        assertSame(secondEnemy, battle.enemyChimera)
    }

    @Test
    fun runActionEndsBattleWhenEscapeSucceeds() {
        val playerChimera = testChimera(ChimeraSpecies.Sunflare, level = 20)
        val enemyChimera = testChimera(ChimeraSpecies.Sylvhorn, level = 1)
        val battle = BattleManager(
            player = testPlayer(playerChimera),
            enemy = NPC("Wild", listOf(enemyChimera)),
            randomProvider = FixedRandomProvider(doubles = listOf(0.0))
        )

        val result = battle.performTurnWithAnimations(BattleAction.Run)

        assertFalse(battle.isBattleActive)
        assertEquals(listOf("Got away safely!"), result.log)
    }
}
