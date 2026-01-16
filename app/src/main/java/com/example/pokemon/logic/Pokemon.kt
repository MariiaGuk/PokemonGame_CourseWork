package com.example.pokemon.logic

import com.example.pokemon.logic.moves.Move
import com.example.pokemon.logic.types.PokemonType

/**
 * Basic abstract class for every pokemon in the game.
 */
abstract class Pokemon (
    var name: String,
    val type: PokemonType,
    var maxHp: Int,
    var level: Int,
    val moves: List<Move>
){
    var currentHp: Int = maxHp
        private set

    fun takeDamage(damage: Int) {
        currentHp -= damage
        if (currentHp < 0) currentHp = 0
    }

    fun isAlive(): Boolean = currentHp > 0

    abstract fun calculateAttackDamage(): Int
}
