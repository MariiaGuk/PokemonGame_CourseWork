package com.example.chimeralis

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemsInventoryTest {

    @Test
    fun healingAndReviveItemsCanOnlyBeUsedOnValidTargets() {
        val chimera = testChimera()
        val potion = ItemFactory.createItem(ItemName.POTION)
        val revive = ItemFactory.createItem(ItemName.REVIVE)

        assertFalse(potion.canUseOn(chimera))
        assertFalse(revive.canUseOn(chimera))

        chimera.stats.takeDamage(10)
        assertTrue(potion.canUseOn(chimera))
        assertFalse(revive.canUseOn(chimera))

        chimera.stats.takeDamage(999)
        assertFalse(potion.canUseOn(chimera))
        assertTrue(revive.canUseOn(chimera))
    }

    @Test
    fun inventoryStacksItemsByItemNameAndExposesCopy() {
        val inventory = Inventory()
        val potionA = ItemFactory.createItem(ItemName.POTION)
        val potionB = ItemFactory.createItem(ItemName.POTION)

        inventory.addItem(potionA, amount = 1)
        inventory.addItem(potionB, amount = 2)

        assertEquals(1, inventory.items.size)
        assertEquals(3, inventory.items.values.single())

        val copy = inventory.items.toMutableMap()
        copy.clear()
        assertEquals(1, inventory.items.size)
    }

    @Test
    fun usingInventoryItemConsumesExactlyOneStackEntry() {
        val inventory = Inventory()
        val potion = ItemFactory.createItem(ItemName.POTION)
        val chimera = testChimera(ChimeraSpecies.Sunflare)
        val damage = (chimera.stats.maxHp - 1).coerceAtMost(5)
        chimera.stats.takeDamage(damage)
        inventory.addItem(potion, amount = 2)

        assertTrue(inventory.useItem(potion, chimera))

        assertEquals(1, inventory.items[potion])
        assertTrue(chimera.stats.currentHp > chimera.stats.maxHp - damage)

        chimera.stats.takeDamage(1)
        assertTrue(inventory.useItem(potion, chimera))
        assertFalse(inventory.items.containsKey(potion))
    }

    @Test
    fun captureItemCanBeConsumedWithoutTargetEffect() {
        val inventory = Inventory()
        val bindingStone = ItemFactory.createItem(ItemName.BINDING_STONE)
        inventory.addItem(bindingStone, amount = 1)

        assertTrue(bindingStone.isCaptureItem)
        assertTrue(inventory.consumeItem(bindingStone))
        assertFalse(inventory.consumeItem(bindingStone))
    }
}
