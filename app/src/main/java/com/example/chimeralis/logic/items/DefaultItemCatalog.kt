package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.HealItemEffect
import com.example.chimeralis.logic.items.itemEffects.ReviveItemEffect

/** Default item catalog used by the game. */
object DefaultItemCatalog : ItemCatalog {
    override val definitions: List<ItemDefinition> = listOf(
        ItemDefinition(
            itemName = ItemName.POTION,
            effectsFactory = { listOf(HealItemEffect(20)) }
        ),
        ItemDefinition(
            itemName = ItemName.SUPER_POTION,
            effectsFactory = { listOf(HealItemEffect(60)) }
        ),
        ItemDefinition(
            itemName = ItemName.REVIVE,
            effectsFactory = { listOf(ReviveItemEffect()) }
        ),
        ItemDefinition(
            itemName = ItemName.BINDING_STONE,
            effectsFactory = { emptyList() }
        )
    )
}
