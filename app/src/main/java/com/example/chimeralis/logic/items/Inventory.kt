package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.chimeras.Chimera

/** Stores item stacks and applies item usage to chimeras. */
class Inventory {
    private val _items = mutableMapOf<Item, Int>()
    val items: Map<Item, Int> get() = _items

    /** Adds an item stack, merging by item name when possible. */
    fun addItem(item: Item, amount: Int = 1) {
        if (amount <= 0) return

        val existingItem = _items.keys.firstOrNull { it.itemName == item.itemName } ?: item
        _items[existingItem] = (_items[existingItem] ?: 0) + amount
    }

    /** Uses one item on a target chimera if the item is available and valid. */
    fun useItem(item: Item, target: Chimera): Boolean {
        val count = _items[item] ?: return false
        if (!item.canUseOn(target)) return false

        item.use(target)
        consumeItem(item, count)
        return true
    }

    /** Consumes one item without applying an effect. */
    fun consumeItem(item: Item): Boolean {
        val count = _items[item] ?: return false
        consumeItem(item, count)
        return true
    }

    /** Decrements or removes one item stack. */
    private fun consumeItem(item: Item, count: Int) {
        if (count <= 1) _items.remove(item) else _items[item] = count - 1
    }
}
