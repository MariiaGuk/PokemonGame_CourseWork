package com.example.pokemon.logic.trainers

import com.example.pokemon.logic.pokemons.Pokemon

class NPC(
    name: String,
    team: MutableList<Pokemon> = mutableListOf(),
    val dialogue: String = ""
) : Trainer(name, team)