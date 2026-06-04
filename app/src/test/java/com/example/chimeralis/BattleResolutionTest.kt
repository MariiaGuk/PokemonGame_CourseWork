package com.example.chimeralis

import com.example.chimeralis.logic.battle.ai.EnemyMoveSelector
import com.example.chimeralis.logic.battle.resolution.BattleCaptureResolver
import com.example.chimeralis.logic.battle.resolution.BattleEscapeResolver
import com.example.chimeralis.logic.battle.resolution.BattleFaintResolver
import com.example.chimeralis.logic.battle.resolution.BattleItemResolver
import com.example.chimeralis.logic.battle.resolution.BattleMoveAccuracyResolver
import com.example.chimeralis.logic.battle.resolution.BattleMoveLearningResolver
import com.example.chimeralis.logic.battle.resolution.BattleTurnOrderResolver
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName
import com.example.chimeralis.logic.chimeras.moves.moveEffects.DamageEffect
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.NPCDialogue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleResolutionTest {

    @Test
    fun accuracyResolverUsesConfiguredMoveAccuracy() {
        val move = MoveFactory.createMove(MoveName.TACKLE)

        assertTrue(BattleMoveAccuracyResolver(FixedRandomProvider(ints = listOf(100))).moveHits(move))
        assertFalse(
            BattleMoveAccuracyResolver(FixedRandomProvider(ints = listOf(51))).moveHits(
                Move(
                    id = MoveName.TACKLE,
                    name = "Coin Flip",
                    type = ChimeraType.NORMAL,
                    maxPp = 10,
                    accuracy = 50,
                    effects = listOf(DamageEffect(40))
                )
            )
        )
    }

    @Test
    fun turnOrderUsesSpeedAndInjectedRandomForTies() {
        val fast = testChimera(ChimeraSpecies.Sunflare, level = 10)
        val slow = testChimera(ChimeraSpecies.Sylvhorn, level = 5)

        assertTrue(BattleTurnOrderResolver().playerActsFirst(fast, slow))
        assertFalse(BattleTurnOrderResolver().playerActsFirst(slow, fast))

        val firstTieResolver = BattleTurnOrderResolver(FixedRandomProvider(doubles = listOf(0.49)))
        val secondTieResolver = BattleTurnOrderResolver(FixedRandomProvider(doubles = listOf(0.51)))
        val tieA = testChimera(ChimeraSpecies.Sylvhorn, level = 5)
        val tieB = testChimera(ChimeraSpecies.Sylvhorn, level = 5)

        assertTrue(firstTieResolver.playerActsFirst(tieA, tieB))
        assertFalse(secondTieResolver.playerActsFirst(tieA, tieB))
    }

    @Test
    fun captureResolverUsesHpBasedChanceAndInjectedRandom() {
        val target = testChimera(ChimeraSpecies.Sylvhorn)
        val caught = BattleCaptureResolver(FixedRandomProvider(doubles = listOf(0.27))).resolve(target)
        val escaped = BattleCaptureResolver(FixedRandomProvider(doubles = listOf(0.29))).resolve(target)

        assertEquals(0.28f, caught.chance, 0.01f)
        assertTrue(caught.caught)
        assertFalse(escaped.caught)

        target.stats.takeDamage(target.stats.maxHp - 1)
        val weakened = BattleCaptureResolver(FixedRandomProvider(doubles = listOf(0.5))).resolve(target)
        assertTrue(weakened.chance > caught.chance)
        assertTrue(weakened.caught)
    }

    @Test
    fun escapeResolverCanGuaranteeOrRandomizeEscape() {
        assertTrue(BattleEscapeResolver().canEscape(playerSpeed = 999, enemySpeed = 1, escapeAttempts = 0))
        assertFalse(
            BattleEscapeResolver(FixedRandomProvider(doubles = listOf(1.0)))
                .canEscape(playerSpeed = 5, enemySpeed = 50, escapeAttempts = 0)
        )
    }

    @Test
    fun enemyMoveSelectorIgnoresMovesWithoutPpWhenPossible() {
        val chimera = testChimera(ChimeraSpecies.Sunflare, level = 6)
        val firstMove = chimera.moves.first()
        firstMove.restorePp(0)

        val selected = EnemyMoveSelector(FixedRandomProvider(ints = listOf(0))).selectMove(chimera)

        assertTrue(selected.pp > 0)
        assertTrue(selected !== firstMove)
    }

    @Test
    fun faintResolverPromptsSwitchOrEndsBattle() {
        val fainted = testChimera(ChimeraSpecies.Sunflare)
        val living = testChimera(ChimeraSpecies.Sylvhorn)
        val player = testPlayer(fainted, living)
        fainted.stats.takeDamage(999)
        player.resetActiveChimeraToTeamLead()

        val playerResolution = BattleFaintResolver().resolvePlayerFaint(player)

        assertNotNull(playerResolution)
        assertTrue(playerResolution!!.isWaitingForPlayerSwitch)
        assertTrue(playerResolution.isBattleActive)

        living.stats.takeDamage(999)
        val defeatResolution = BattleFaintResolver().promptForcedSwitch(player)
        assertFalse(defeatResolution.isBattleActive)
        assertFalse(defeatResolution.isWaitingForPlayerSwitch)
    }

    @Test
    fun enemyFaintResolutionReturnsNextLivingEnemyOrVictory() {
        val first = testChimera(ChimeraSpecies.Sunflare)
        val second = testChimera(ChimeraSpecies.Sylvhorn)
        val enemy = NPC("Rival", listOf(first, second))
        first.stats.takeDamage(999)

        val switchResolution = BattleFaintResolver().resolveEnemyFaint(enemy, first)

        assertNotNull(switchResolution)
        assertSame(second, switchResolution!!.nextChimera)
        assertFalse(switchResolution.shouldAwardMoney)

        second.stats.takeDamage(999)
        val victoryResolution = BattleFaintResolver().resolveEnemyFaint(enemy, second)
        assertNotNull(victoryResolution)
        assertFalse(victoryResolution!!.isBattleActive)
        assertTrue(victoryResolution.shouldAwardMoney)
    }

    @Test
    fun enemyFaintResolutionUsesNpcDialogueWhenAvailable() {
        val first = testChimera(ChimeraSpecies.Sunflare)
        val second = testChimera(ChimeraSpecies.Sylvhorn)
        val enemy = NPC(
            name = "Rival",
            team = listOf(first, second),
            dialogue = NPCDialogue(
                nextChimeraLine = "{npc}: Try {chimera}!",
                defeatLine = "{npc}: You got me."
            )
        )
        first.stats.takeDamage(999)

        val switchResolution = BattleFaintResolver().resolveEnemyFaint(enemy, first)

        assertEquals("Rival: Try ${second.name}!", switchResolution!!.message)

        second.stats.takeDamage(999)
        val victoryResolution = BattleFaintResolver().resolveEnemyFaint(enemy, second)

        assertEquals("You won!", victoryResolution!!.message)
        assertEquals(listOf("Rival: You got me."), victoryResolution.extraMessages)
    }

    @Test
    fun moveLearningResolverAppliesReplaceAndSkipChoices() {
        val chimera = com.example.chimeralis.logic.chimeras.Chimera(
            name = "Learner",
            species = ChimeraSpecies.Sunflare,
            type = ChimeraType.FIRE,
            baseStats = Stats(39, 52, 43, 65),
            ivStats = zeroIvStats(),
            level = 1,
            learnableMoves = listOf(
                1 to { MoveFactory.createMove(MoveName.TACKLE) },
                1 to { MoveFactory.createMove(MoveName.GROWL) },
                1 to { MoveFactory.createMove(MoveName.EMBER) },
                1 to { MoveFactory.createMove(MoveName.TAILWHIP) },
                2 to { MoveFactory.createMove(MoveName.RECOVER) }
            )
        )
        val player = testPlayer(chimera)
        chimera.levelUp()
        val resolver = BattleMoveLearningResolver(player)

        assertEquals("Recover", resolver.pendingRequest()?.move?.name)

        val replaceLog = resolver.resolve(replaceIndex = 1)

        assertTrue(replaceLog.any { "forgot Growl" in it })
        assertTrue(replaceLog.any { "learned Recover" in it })
        assertNull(resolver.pendingRequest())
    }

    @Test
    fun itemResolverBlocksTrainerCaptureAndDoesNotConsumeItem() {
        val bindingStone = ItemFactory.createItem(ItemName.BINDING_STONE)
        val inventory = Inventory()
        inventory.addItem(bindingStone)
        val playerChimera = testChimera(ChimeraSpecies.Sunflare)
        val enemyChimera = testChimera(ChimeraSpecies.Sylvhorn)
        val player = testPlayer(playerChimera, inventory = inventory)

        val resolution = BattleItemResolver(canCaptureEnemy = false).resolve(
            item = bindingStone,
            target = null,
            player = player,
            playerChimera = playerChimera,
            enemyChimera = enemyChimera
        )

        assertEquals(listOf("You cannot catch another trainer's chimera."), resolution.log)
        assertEquals(1, player.inventory.items[bindingStone])
        assertTrue(resolution.isBattleActive)
    }
}
