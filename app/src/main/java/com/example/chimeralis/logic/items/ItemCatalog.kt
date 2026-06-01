package com.example.chimeralis.logic.items

/** Provides item definitions available to the item factory. */
interface ItemCatalog {
    val definitions: List<ItemDefinition>

    /** Finds an item definition by its stable identifier. */
    fun definitionFor(itemName: ItemName): ItemDefinition {
        return definitions.firstOrNull { definition -> definition.itemName == itemName }
            ?: throw IllegalArgumentException("Unknown item: $itemName")
    }
}
