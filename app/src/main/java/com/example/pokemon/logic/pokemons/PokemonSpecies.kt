package com.example.pokemon.logic.pokemons

enum class PokemonSpecies(
    val evolvesInto: PokemonSpecies? = null,
    val evolutionLevel: Int? = null
){
    CHARIZARD,
    CHARMELEON(evolvesInto = CHARIZARD, evolutionLevel = 36),
    CHARMANDER(evolvesInto = CHARMELEON, evolutionLevel = 16),

    BULBASAUR(),
    SQUIRTLE(),
}