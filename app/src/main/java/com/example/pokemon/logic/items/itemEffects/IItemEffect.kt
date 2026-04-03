package com.example.pokemon.logic.items.itemEffects

import com.example.pokemon.logic.pokemons.Pokemon

interface IItemEffect {
    fun apply(target: Pokemon)
}