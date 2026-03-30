package com.example.pokemon.logic

import com.example.pokemon.logic.Stats.StatType
import com.example.pokemon.logic.moves.Move

/**
 * Basic class for every pokemon in the game.
 */
class Pokemon (
    name: String,
    val species: PokemonSpecies,
    val type: PokemonType,
    val baseStats: Stats,
    val ivStats: Stats,
    level: Int,
    val learnableMoves: List<Pair<Int, () -> Move>>,
    val onLevelUp: ((Pokemon) -> Unit)? = null,
    val onMoveLearn: ((Pokemon, Move, onReplace: (Int) -> Unit) -> Unit)? = null,
    val onEvolution: ((old: Pokemon, new: Pokemon) -> Unit)? = null
){

    var name: String = name.trim()
        private set
    var exp: Int = 0
        private set(value) {
            field = value.coerceAtLeast(0)
        }

    var level: Int = level
        private set(value) {
            field = value.coerceAtLeast(1)
        }

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

    fun rename(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isNotBlank() && trimmedName.length <= 12) {
            this.name = trimmedName
        }
        else {
            throw IllegalArgumentException("Invalid name")
        }
    }

    fun gainExp(amount: Int) {
        exp += amount
        val expNeeded = level * level * level
        if (exp >= expNeeded) {
            exp -= expNeeded
            levelUp()
        }
    }

    private fun recalculateStats() {
        val oldMaxHp = stats.maxHp

        stats.setStat(StatType.MAX_HP, (((baseStats.maxHp + ivStats.maxHp) * 2 * level) / 100) + level + 10)
        stats.setStat(StatType.ATTACK, (((baseStats.attack + ivStats.attack) * 2 * level) / 100) + 5)
        stats.setStat(StatType.DEFENCE, (((baseStats.defence + ivStats.defence) * 2 * level) / 100) + 5)
        stats.setStat(StatType.SPEED, (((baseStats.speed + ivStats.speed) * 2 * level) / 100) + 5)

        val hpGain = stats.maxHp - oldMaxHp
        stats.heal(hpGain)
    }

    fun levelUp() {
        level++
        recalculateStats()

        onLevelUp?.invoke(this)

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
            onMoveLearn?.invoke(this, move) { index ->
                _moves[index] = move
            }
        }
    }

    fun evolution() {
        val nextSpecies = species.evolvesInto ?: return
        val evolved = PokemonFactory.createPokemon(nextSpecies, level)
        evolved.gainExp(exp)
        onEvolution?.invoke(this, evolved)
    }
}