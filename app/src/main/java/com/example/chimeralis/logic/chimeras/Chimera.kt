package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.moves.Move

/** Represents one playable or enemy chimera with stats, moves, growth, and evolution. */
class Chimera (
    name: String,
    val species: ChimeraSpecies,
    val type: ChimeraType,
    val baseStats: Stats,
    val ivStats: Stats,
    level: Int,
    val learnableMoves: List<Pair<Int, () -> Move>>,
    val onLevelUp: ((Chimera) -> Unit)? = null,
    val onMoveLearn: ((Chimera, Move, onReplace: (Int) -> Unit) -> Unit)? = null,
    val onEvolution: ((old: Chimera, new: Chimera) -> Unit)? = null
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
    var pendingMoveToLearn: Move? = null
        private set

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

    /** Renames the chimera after validating the nickname length and content. */
    fun rename(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isNotBlank() && trimmedName.length <= 12) {
            this.name = trimmedName
        }
        else {
            throw IllegalArgumentException("Invalid name")
        }
    }

    /** Adds experience and triggers all resulting level-ups. */
    fun gainExp(amount: Int) {
        exp += amount
        var expNeeded = level * level * level
        while  (exp >= expNeeded) {
            exp -= expNeeded
            levelUp()
            expNeeded = level * level * level
        }
    }

    /** Recalculates battle stats from base stats, IV stats, and current level. */
    private fun recalculateStats() {
        val oldMaxHp = stats.maxHp

        stats.setStat(Stats.StatType.MAX_HP, (((baseStats.maxHp + ivStats.maxHp) * 2 * level) / 100) + level + 10)
        stats.setStat(Stats.StatType.ATTACK, (((baseStats.attack + ivStats.attack) * 2 * level) / 100) + 5)
        stats.setStat(Stats.StatType.DEFENCE, (((baseStats.defence + ivStats.defence) * 2 * level) / 100) + 5)
        stats.setStat(Stats.StatType.SPEED, (((baseStats.speed + ivStats.speed) * 2 * level) / 100) + 5)

        val hpGain = stats.maxHp - oldMaxHp
        stats.heal(hpGain)
    }

    /** Increases the level and resolves stat growth, evolution, and new moves. */
    fun levelUp() {
        level++
        recalculateStats()

        onLevelUp?.invoke(this)

        if (level == species.evolutionLevel) evolution()

        learnableMoves
            .filter { it.first == this.level }
            .forEach { (_, moveProvider) ->
                learnMove(moveProvider())
            }
    }

    /** Learns a new move or stores it as pending when the move list is full. */
    private fun learnMove(move: Move) {
        if (_moves.any { it.name == move.name }) return

        if (moves.size < 4) {
            _moves.add(move)
        }
        else {
            pendingMoveToLearn = move
            onMoveLearn?.invoke(this, move, { index ->
                _moves[index] = move
                pendingMoveToLearn = null
            })
        }
    }

    /** Replaces an existing move with the pending move-learning request. */
    fun replaceMoveWithPending(index: Int): Pair<Move, Move>? {
        val pendingMove = pendingMoveToLearn ?: return null
        if (index !in _moves.indices) return null

        val forgottenMove = _moves[index]
        _moves[index] = pendingMove
        pendingMoveToLearn = null
        return forgottenMove to pendingMove
    }

    /** Cancels the pending move-learning request and returns the skipped move. */
    fun skipPendingMove(): Move? {
        val pendingMove = pendingMoveToLearn ?: return null
        pendingMoveToLearn = null
        return pendingMove
    }

    /** Creates the evolved chimera form when the current species supports it. */
    fun evolution() {
        val nextSpecies = species.evolvesInto ?: return
        val evolved = ChimeraFactory.createChimera(nextSpecies, level)
        evolved.gainExp(exp)
        onEvolution?.invoke(this, evolved)
    }
}
