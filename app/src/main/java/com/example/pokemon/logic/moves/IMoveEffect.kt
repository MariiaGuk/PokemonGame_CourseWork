package com.example.pokemon.logic.moves

import com.example.pokemon.logic.Pokemon

interface IMoveEffect {
    fun apply(attacker: Pokemon, target: Pokemon)
}