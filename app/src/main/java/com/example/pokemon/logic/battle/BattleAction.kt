package com.example.pokemon.logic.battle

import com.example.pokemon.logic.pokemons.Pokemon
import com.example.pokemon.logic.pokemons.moves.Move

sealed class BattleAction {
    data class UseMove(val move: Move) : BattleAction()
//    data class UseItem(val item: Item) : BattleAction()
    data class SwapPokemon(val pokemon: Pokemon) : BattleAction()
    object Run : BattleAction()
}