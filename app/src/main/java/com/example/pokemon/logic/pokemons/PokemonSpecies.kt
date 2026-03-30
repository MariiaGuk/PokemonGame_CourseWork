package com.example.pokemon.logic.pokemons

enum class PokemonSpecies(val evolvesInto: PokemonSpecies? = null) {
    CHARIZARD,
    CHARMELEON(evolvesInto = CHARIZARD),
    CHARMANDER(evolvesInto = CHARMELEON),

    BULBASAUR(),
    SQUIRTLE(),
}