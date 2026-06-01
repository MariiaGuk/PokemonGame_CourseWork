package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.IItemEffect

/** Describes how to create one configured inventory item. */
data class ItemDefinition(
    val itemName: ItemName,
    private val effectsFactory: () -> List<IItemEffect>
) {

    /** Creates a fresh item instance with its configured effects. */
    fun createItem(): Item {
        return Item(
            itemName = itemName,
            effects = effectsFactory()
        )
    }
}
