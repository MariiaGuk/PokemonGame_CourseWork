package com.example.pokemon.logic

import com.example.pokemon.logic.moves.Move

/**
 * Basic class for every pokemon in the game.
 */
class Pokemon (
    var name: String,
    val type: PokemonType,
    val baseStats: Stats,
    val ivStats: Stats,
    level: Int,
    val learnableMoves: List<Pair<Int, () -> Move>>
){
    var exp: Int = 0
        private set

    var level: Int = level
        private set

    private val _moves = mutableListOf<Move>()
    val moves: List<Move> get() = _moves

    val stats: Stats = Stats(0, 0, 0, 0)

    init {
        recalculateStats()

        learnableMoves
            .filter { it.first <= this.level }
            .forEach { (_, moveProvider) ->
                val move = moveProvider()
                if (_moves.size < 4 && _moves.none { it.name == move.name }) {
                    _moves.add(move)
                }
            }
    }

    fun gainExp(amount: Int) {
        exp += amount
        //levelUp check
    }

    private fun recalculateStats() {
        val oldMaxHp = stats.maxHp

        stats.setStat("maxhp", (((baseStats.maxHp + ivStats.maxHp) * 2 * level) / 100) + level + 10)
        stats.setStat("attack", (((baseStats.attack + ivStats.attack) * 2 * level) / 100) + 5)
        stats.setStat("defence", (((baseStats.defence + ivStats.defence) * 2 * level) / 100) + 5)
        stats.setStat("speed", (((baseStats.speed + ivStats.speed) * 2 * level) / 100) + 5)

        val hpGain = stats.maxHp - oldMaxHp
        stats.heal(hpGain)
    }

    fun levelUp() {
        level++

        recalculateStats()

        if (level % 50 == 0) evolution()

        learnableMoves
            .filter { it.first == this.level }
            .forEach { (_, moveProvider) ->
                learnMove(moveProvider())
            }
    }

    private fun learnMove(move: Move) {
        if (_moves.any { it.name == move.name }) return

        if (moves.size < 4) {
            _moves.add(move)
        }
        else {
            //Ask the player if they want to replace one of the moves
        }
    }

    fun evolution(){
        //evolution logic
    }
}
