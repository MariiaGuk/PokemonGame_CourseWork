package com.example.pokemon.logic.items.itemEffects

import com.example.pokemon.logic.pokemons.Pokemon

class HealItemEffect(private val amount: Int) : IItemEffect {
    override fun apply(target: Pokemon) {
        target.stats.heal(amount)
    }
}