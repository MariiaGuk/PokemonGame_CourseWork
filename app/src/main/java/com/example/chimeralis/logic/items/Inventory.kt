package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.chimeras.Chimera

class Inventory {
    private val _items = mutableMapOf<Item, Int>()
    val items: Map<Item, Int> get() = _items

    fun addItem(item: Item, amount: Int = 1) {
        _items[item] = (_items[item] ?: 0) + amount
    }

    fun useItem(item: Item, target: Chimera): Boolean {
        val count = _items[item] ?: return false
        if (!item.canUseOn(target)) return false

        item.use(target)
        consumeItem(item, count)
        return true
    }

    fun consumeItem(item: Item): Boolean {
        val count = _items[item] ?: return false
        consumeItem(item, count)
        return true
    }

    private fun consumeItem(item: Item, count: Int) {
        if (count <= 1) _items.remove(item) else _items[item] = count - 1
    }
}
