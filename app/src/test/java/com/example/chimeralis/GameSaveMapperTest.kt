package com.example.chimeralis

import com.example.chimeralis.data.GameSaveMapper
import com.example.chimeralis.data.SavedChimera
import com.example.chimeralis.data.SavedItem
import com.example.chimeralis.data.SavedMovePp
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.MoveName
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSaveMapperTest {

    private val mapper = GameSaveMapper()

    @Test
    fun speciesAndItemLookupAcceptCurrentAndLegacySaveNames() {
        assertSame(ChimeraSpecies.Sunflare, mapper.toChimeraSpecies("Sunflare"))
        assertSame(ChimeraSpecies.Sunflare, mapper.toChimeraSpecies("sun flare"))
        assertSame(ChimeraSpecies.Sunflare, mapper.toChimeraSpecies("Sunflare"))
        assertSame(ChimeraSpecies.Aquantis, mapper.toChimeraSpecies("Aquantis"))

        assertEquals(ItemName.BINDING_STONE, mapper.toItemName("Binding Stone"))
        assertEquals(ItemName.BINDING_STONE, mapper.toItemName("BINDING_STONE"))
        assertEquals(ItemName.BINDING_STONE, mapper.toItemName("Binding Stone"))
        assertEquals(ItemName.SUPER_POTION, mapper.toItemName("SUPER_POTION"))
    }

    @Test
    fun savedChimeraRoundTripPreservesRuntimeState() {
        val chimera = testChimera(ChimeraSpecies.Sunflare, level = 6)
        chimera.rename("Blaze")
        chimera.gainExp(10)
        chimera.stats.takeDamage(7)
        val tackle = chimera.moves.first { move -> move.id == MoveName.TACKLE }
        tackle.restorePp(tackle.maxPp - 3)

        val saved = mapper.toSavedChimera(chimera)
        val restored = mapper.toChimera(saved)
        val restoredTackle = restored.moves.first { move -> move.id == MoveName.TACKLE }

        assertSame(chimera.species, restored.species)
        assertEquals("Blaze", restored.name)
        assertEquals(chimera.level, restored.level)
        assertEquals(chimera.exp, restored.exp)
        assertEquals(chimera.stats.currentHp, restored.stats.currentHp)
        assertEquals(chimera.ivStats.attack, restored.ivStats.attack)
        assertEquals(tackle.pp, restoredTackle.pp)
    }

    @Test
    fun savedMovePpCanBeRestoredFromDisplayNameOrEnumName() {
        val saved = SavedChimera(
            species = ChimeraSpecies.Sunflare,
            nickname = "Sunflare",
            level = 6,
            exp = 0,
            currentHp = -1,
            ivStats = Stats(1, 1, 1, 1),
            moves = listOf(
                SavedMovePp(moveName = "Tackle", pp = 12),
                SavedMovePp(moveName = "EMBER", pp = 7)
            )
        )

        val chimera = mapper.toChimera(saved)

        assertEquals(12, chimera.moves.first { move -> move.id == MoveName.TACKLE }.pp)
        assertEquals(7, chimera.moves.first { move -> move.id == MoveName.EMBER }.pp)
    }

    @Test
    fun invalidSavedNicknameIsIgnoredDuringRestore() {
        val saved = SavedChimera(
            species = ChimeraSpecies.Sylvhorn,
            nickname = "NameThatIsMuchTooLong",
            level = 1,
            exp = 0,
            currentHp = -1,
            ivStats = Stats(1, 1, 1, 1)
        )

        val chimera = mapper.toChimera(saved)

        assertEquals("Sylvhorn", chimera.name)
    }

    @Test
    fun inventoryMappingRestoresItemStacksAndSaveNames() {
        val inventory = mapper.toInventory(
            listOf(
                SavedItem(ItemName.POTION, amount = 2),
                SavedItem(ItemName.BINDING_STONE, amount = 1)
            )
        )
        val savedItem = mapper.toSavedItem(ItemFactory.createItem(ItemName.SUPER_POTION), amount = 3)

        assertEquals(2, inventory.amountOf(ItemName.POTION))
        assertEquals(1, inventory.amountOf(ItemName.BINDING_STONE))
        assertEquals(ItemName.SUPER_POTION, savedItem.itemName)
        assertEquals(3, savedItem.amount)
        assertEquals("Aquantis", mapper.speciesSaveName(ChimeraSpecies.Aquantis))
        assertEquals("Binding Stone", mapper.itemSaveName(ItemName.BINDING_STONE))
    }

    private fun com.example.chimeralis.logic.items.Inventory.amountOf(itemName: ItemName): Int {
        return items.entries.firstOrNull { (item, _) -> item.itemName == itemName }?.value ?: 0
    }
}
