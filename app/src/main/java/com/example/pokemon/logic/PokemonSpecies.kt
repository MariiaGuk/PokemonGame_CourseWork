package com.example.pokemon.logic

enum class PokemonSpecies(val evolvesInto: PokemonSpecies? = null) {
    CHARIZARD,
    CHARMELEON(evolvesInto = CHARIZARD),
    CHARMANDER(evolvesInto = CHARMELEON),

    BULBASAUR(),
    SQUIRTLE(),
}