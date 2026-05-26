package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.HealItemEffect
import com.example.chimeralis.logic.items.itemEffects.ReviveItemEffect

object ItemFactory {
    fun createItem(itemName: ItemName): Item = when (itemName) {
        ItemName.POTION -> Item(
            itemName = itemName,
            effects = listOf(HealItemEffect(20))
        )
        ItemName.SUPER_POTION -> Item(
            itemName = itemName,
            effects = listOf(HealItemEffect(60))
        )
        ItemName.REVIVE -> Item(
            itemName = itemName,
            effects = listOf(ReviveItemEffect())
        )
        ItemName.BINDING_STONE -> Item(
            itemName = itemName,
            effects = emptyList()
        )
    }
}
