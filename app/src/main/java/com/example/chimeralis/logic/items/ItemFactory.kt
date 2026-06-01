package com.example.chimeralis.logic.items

/** Creates item instances from item identifiers. */
object ItemFactory {
    var catalog: ItemCatalog = DefaultItemCatalog
        private set

    init {
        validateCatalog(catalog)
    }

    /** Replaces the catalog source for alternative item data. */
    fun configureCatalog(newCatalog: ItemCatalog) {
        validateCatalog(newCatalog)
        catalog = newCatalog
    }

    /** Builds an item with the effects required by its name. */
    fun createItem(itemName: ItemName): Item {
        return catalog.definitionFor(itemName).createItem()
    }

    /** Validates that the catalog covers every item id exactly once. */
    private fun validateCatalog(catalog: ItemCatalog) {
        val itemNames = catalog.definitions.map { definition -> definition.itemName }
        require(itemNames.toSet().size == itemNames.size) { "Item catalog contains duplicate item names" }

        val missingItems = ItemName.values().filterNot { itemName -> itemName in itemNames }
        require(missingItems.isEmpty()) { "Item catalog is missing definitions for: $missingItems" }
    }
}
