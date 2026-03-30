package com.example.pokemon.logic.trainers

import com.example.pokemon.logic.pokemons.Pokemon

abstract class Trainer(
    val name: String,
    val team: MutableList<Pokemon> = mutableListOf()
) {
    init {
        require(team.isNotEmpty()) { "Trainer must have at least one pokemon" }
    }
    var activePokemon: Pokemon = team.first()
        protected set

    fun swapPokemon(pokemon: Pokemon) {
        require(pokemon in team) { "Pokemon is not in team" }
        require(pokemon.stats.isAlive()) { "Cannot swap to fainted pokemon" }
        activePokemon = pokemon
    }

    fun isDefeated() = team.none { it.stats.isAlive() }
}