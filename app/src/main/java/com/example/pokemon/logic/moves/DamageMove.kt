package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon
import com.example.pokemon.logic.types.PokemonType

class DamageMove(power: Int, name: String, type: PokemonType, pp: Int): Move(name, type, pp)
{
    override fun execute(attacker: Pokemon, target: Pokemon)
    {

    }
}