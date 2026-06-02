package com.example.chimeralis

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ChimeraDomainTest {

    @Test
    fun factoryCreatesSpeciesWithIndependentStartingLearnsets() {
        val sunflare = ChimeraFactory.createChimera(ChimeraSpecies.Sunflare, level = 1, ivStats = zeroIvStats())
        val sylvhorn = ChimeraFactory.createChimera(ChimeraSpecies.Sylvhorn, level = 1, ivStats = zeroIvStats())
        val aquantis = ChimeraFactory.createChimera(ChimeraSpecies.Aquantis, level = 1, ivStats = zeroIvStats())

        assertEquals(listOf("Tackle", "Growl"), sunflare.moves.map { it.name })
        assertEquals(listOf("Tackle"), sylvhorn.moves.map { it.name })
        assertEquals(listOf("Tail Whip"), aquantis.moves.map { it.name })
    }

    @Test
    fun levelUpLearnsConfiguredMoveForThatSpecies() {
        val sunflare = ChimeraFactory.createChimera(ChimeraSpecies.Sunflare, level = 5, ivStats = zeroIvStats())

        assertFalse(sunflare.moves.any { it.id == MoveName.EMBER })

        sunflare.levelUp()

        assertEquals(6, sunflare.level)
        assertTrue(sunflare.moves.any { it.id == MoveName.EMBER })
    }

    @Test
    fun fullMoveListCreatesPendingMoveAndCanReplaceOrSkipIt() {
        val chimera = Chimera(
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

        assertEquals(4, chimera.moves.size)

        chimera.levelUp()

        assertEquals("Recover", chimera.pendingMoveToLearn?.name)
        val replaced = chimera.replaceMoveWithPending(0)

        assertEquals("Tackle", replaced?.first?.name)
        assertEquals("Recover", replaced?.second?.name)
        assertNull(chimera.pendingMoveToLearn)
        assertEquals("Recover", chimera.moves.first().name)

        chimera.levelUp()
        assertNull(chimera.skipPendingMove())
    }

    @Test
    fun invalidRenameThrowsAndValidRenameUpdatesName() {
        val chimera = testChimera()

        chimera.rename("Sol")
        assertEquals("Sol", chimera.name)

        assertThrows(IllegalArgumentException::class.java) {
            chimera.rename("")
        }
        assertThrows(IllegalArgumentException::class.java) {
            chimera.rename("NameThatIsTooLong")
        }
    }

    @Test
    fun statAccessorsReturnDefensiveCopies() {
        val chimera = testChimera()

        val baseStats = chimera.baseStats
        baseStats.setStat(Stats.StatType.ATTACK, 1)

        val ivStats = chimera.ivStats
        ivStats.setStat(Stats.StatType.SPEED, 15)

        assertNotSame(baseStats, chimera.baseStats)
        assertTrue(chimera.baseStats.attack > 1)
        assertEquals(1, chimera.ivStats.speed)
    }

    @Test
    fun evolutionServicePreservesNicknameIvHpAndExp() {
        val chimera = ChimeraFactory.createChimera(ChimeraSpecies.Sunflare, level = 10, ivStats = Stats(3, 4, 5, 6))
        chimera.rename("Flare")
        chimera.stats.takeDamage(5)
        chimera.gainExp(12)

        val evolved = com.example.chimeralis.logic.chimeras.evolution.ChimeraEvolutionService().evolve(chimera)

        requireNotNull(evolved)
        assertEquals(ChimeraSpecies.Solflare, evolved.species)
        assertEquals("Flare", evolved.name)
        assertEquals(chimera.level, evolved.level)
        assertEquals(chimera.exp, evolved.exp)
        assertEquals(chimera.ivStats.attack, evolved.ivStats.attack)
        assertTrue(evolved.stats.currentHp > chimera.stats.currentHp)
        assertSame(ChimeraSpecies.Sunflare, chimera.species)
    }
}
