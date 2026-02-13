package com.example.pokemon.logic

import com.example.pokemon.logic.moves.Move

/**
 * Basic class for every pokemon in the game.
 */
class Pokemon (
    var name: String,
    val type: PokemonType,
    val baseStats: Stats,
    val IVStats: Stats,
    var exp: Int,
    var level: Int,
    val moves: MutableList<Move>,
    val learnableMoves: Map<Int, () -> Move>
){
    val stats: Stats = Stats(0, 0, 0, 0)

    init {
        recalculateStats()

        learnableMoves.forEach { (lvl, moveProvider) ->
            if (this.level >= lvl && moves.size < 4 && moves.none { it.name == moveProvider().name }) {
                moves.add(moveProvider())
            }
        }
    }

    fun recalculateStats() {
        val oldMaxHp = stats.maxHp

        stats.maxHp = (((baseStats.maxHp + IVStats.maxHp) * 2 * level) / 100) + level + 10
        stats.attack = (((baseStats.attack + IVStats.attack) * 2 * level) / 100) + 5
        stats.defence = (((baseStats.defence + IVStats.defence) * 2 * level) / 100) + 5
        stats.speed = (((baseStats.speed + IVStats.speed) * 2 * level) / 100) + 5

        val hpGain = stats.maxHp - oldMaxHp

        stats.heal(hpGain)
    }

    fun levelUp() {
        level++

        recalculateStats()

        if (level % 50 == 0) evolution()

        learnableMoves[level]?.let { moveProvider ->
            learnMove(moveProvider())
        }
    }

    private fun learnMove(move: Move) {
        if (moves.size < 4) {
            moves.add(move)
        }
        else {
            //Ask the player if they want to replace one of the moves
        }
    }

    fun evolution(){
        //evolution logic
    }
}
