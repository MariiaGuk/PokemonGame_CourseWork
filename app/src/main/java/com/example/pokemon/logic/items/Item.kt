package com.example.pokemon.logic.items

import com.example.pokemon.logic.items.itemEffects.IItemEffect
import com.example.pokemon.logic.pokemons.Pokemon

class Item(
    val name: String,
    val effects: List<IItemEffect>
) {
    fun use(target: Pokemon) {
        effects.forEach { it.apply(target) }
    }
}