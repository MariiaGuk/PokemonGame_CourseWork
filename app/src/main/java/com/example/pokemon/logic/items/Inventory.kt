package com.example.pokemon.logic.items

import com.example.pokemon.logic.pokemons.Pokemon

class Inventory {
    private val _items = mutableMapOf<Item, Int>()
    val items: Map<Item, Int> get() = _items

    fun addItem(item: Item, amount: Int = 1) {
        _items[item] = (_items[item] ?: 0) + amount
    }

    fun useItem(item: Item, target: Pokemon): Boolean {
        val count = _items[item] ?: return false
        item.use(target)
        if (count <= 1) _items.remove(item) else _items[item] = count - 1
        return true
    }
}