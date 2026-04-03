package com.example.pokemon.logic.pokemons

sealed class PokemonSpecies(
    val evolvesInto: PokemonSpecies? = null,
    val evolutionLevel: Int? = null
) {
    object Charizard : PokemonSpecies()
    object Charmeleon : PokemonSpecies(evolvesInto = Charizard, evolutionLevel = 36)
    object Charmander : PokemonSpecies(evolvesInto = Charmeleon, evolutionLevel = 16)

    object Bulbasaur : PokemonSpecies()
    object Squirtle : PokemonSpecies()
}