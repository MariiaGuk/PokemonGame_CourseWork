package com.example.pokemon.logic.items.itemEffects

import com.example.pokemon.logic.pokemons.Pokemon

class ReviveItemEffect : IItemEffect {
    override fun apply(target: Pokemon) {
        if (!target.stats.isAlive()) {
            target.stats.heal(target.stats.maxHp / 2)
        }
    }
}