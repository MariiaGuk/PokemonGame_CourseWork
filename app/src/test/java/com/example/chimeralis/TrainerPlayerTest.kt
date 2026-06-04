package com.example.chimeralis

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.NPCDialogue
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.logic.trainers.PlayerChimeraPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainerPlayerTest {

    @Test
    fun trainerSelectsFirstLivingChimeraAndRejectsFaintedSwitches() {
        val fainted = testChimera(ChimeraSpecies.Sunflare)
        val living = testChimera(ChimeraSpecies.Sylvhorn)
        fainted.stats.takeDamage(999)
        val player = testPlayer(fainted, living)

        assertTrue(player.selectFirstLivingChimera())
        assertSame(living, player.activeChimera)
        assertThrows(IllegalArgumentException::class.java) {
            player.switchChimera(fainted)
        }
    }

    @Test
    fun trainerTeamListIsAReadOnlySnapshot() {
        val first = testChimera(ChimeraSpecies.Sunflare)
        val second = testChimera(ChimeraSpecies.Sylvhorn)
        val npc = NPC("Rival", listOf(first, second))

        val snapshot = npc.team.toMutableList()
        snapshot.clear()

        assertEquals(2, npc.team.size)
    }

    @Test
    fun npcStoresReusableDialogueLines() {
        val chimera = testChimera(ChimeraSpecies.Sunflare)
        val dialogue = NPCDialogue(
            challengeLines = listOf("First line.", "Second line."),
            battleOpeningLine = "{npc} challenged you with {chimera}!",
            nextChimeraLine = "{npc}: Go, {chimera}!",
            defeatLine = "{npc}: I need more training."
        )
        val npc = NPC("Rival", listOf(chimera), dialogue)

        assertSame(dialogue, npc.dialogue)
        assertEquals("First line.", npc.dialogue.challengeLine(0))
        assertEquals("Second line.", npc.dialogue.challengeLine(99))
        assertEquals(
            "Rival challenged you with ${chimera.name}!",
            npc.dialogue.battleOpening(npc.name, chimera.name)
        )
        assertEquals("Rival: Go, ${chimera.name}!", npc.dialogue.nextChimera(npc.name, chimera.name))
        assertEquals("Rival: I need more training.", npc.dialogue.defeat(npc.name))
    }

    @Test
    fun playerStorageOperationsPreserveTeamInvariants() {
        val first = testChimera(ChimeraSpecies.Sunflare)
        val second = testChimera(ChimeraSpecies.Sylvhorn)
        val stored = testChimera(ChimeraSpecies.Aquantis)
        val player = Player(
            name = "Tester",
            team = listOf(first, second),
            inventory = Inventory(),
            storage = listOf(stored)
        )

        assertTrue(player.depositTeamMember(1))
        assertEquals(listOf(first), player.team)
        assertEquals(2, player.storage.size)

        assertTrue(player.withdrawStoredChimera(0))
        assertEquals(2, player.team.size)
        assertEquals(1, player.storage.size)

        assertTrue(player.swapTeamWithStorage(teamIndex = 1, storageIndex = 0))
        assertEquals(2, player.team.size)
        assertEquals(1, player.storage.size)
    }

    @Test
    fun caughtChimeraGoesToTeamBeforeStorage() {
        val player = testPlayer(testChimera())
        val caught = testChimera(ChimeraSpecies.Aquantis)

        val placement = player.addCaughtChimera(caught)

        assertEquals(PlayerChimeraPlacement.Team, placement)
        assertTrue(caught in player.team)
    }

    @Test
    fun playerUsesInventoryOnlyOnOwnTeamMember() {
        val own = testChimera(ChimeraSpecies.Sunflare)
        val stranger = testChimera(ChimeraSpecies.Sylvhorn)
        val potion = ItemFactory.createItem(ItemName.POTION)
        val inventory = Inventory()
        inventory.addItem(potion)
        val player = testPlayer(own, inventory = inventory)
        own.stats.takeDamage(10)
        stranger.stats.takeDamage(10)

        assertTrue(player.useInventoryItem(potion, own))
        assertFalse(player.useInventoryItem(potion, stranger))
    }

    @Test
    fun healTeamRestoresHpAndPp() {
        val chimera = testChimera(ChimeraSpecies.Sunflare, level = 6)
        val target = testChimera(ChimeraSpecies.Sylvhorn)
        chimera.stats.takeDamage(10)
        chimera.moves.first().execute(chimera, target, hits = true)
        val spentMove = chimera.moves.first()
        assertTrue(spentMove.pp < spentMove.maxPp)

        val player = testPlayer(chimera)
        player.healTeam()

        assertEquals(chimera.stats.maxHp, chimera.stats.currentHp)
        assertEquals(spentMove.maxPp, chimera.moves.first().pp)
    }
}
