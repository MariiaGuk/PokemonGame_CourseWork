package com.example.pokemon.logic.trainers

import com.example.pokemon.logic.items.Inventory
import com.example.pokemon.logic.pokemons.Pokemon

class Player(
    name: String,
    team: MutableList<Pokemon> = mutableListOf(),
    val inventory: Inventory
) : Trainer(name, team)