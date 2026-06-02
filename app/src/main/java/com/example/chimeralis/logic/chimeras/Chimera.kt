package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.moves.Move

/** Represents one playable or enemy chimera with stats, moves, growth, and evolution. */
class Chimera (
    name: String,
    val species: ChimeraSpecies,
    val type: ChimeraType,
    baseStats: Stats,
    ivStats: Stats,
    level: Int,
    learnableMoves: List<Pair<Int, () -> Move>>,
    val onLevelUp: ((Chimera) -> Unit)? = null,
    val onMoveLearn: ((Chimera, Move, onReplace: (Int) -> Unit) -> Unit)? = null
){
    private val baseStatsValue = baseStats.copy()
    private val ivStatsValue = ivStats.copy()
    private val learnableMoves = learnableMoves.toList()

    val baseStats: Stats get() = baseStatsValue.copy()
    val ivStats: Stats get() = ivStatsValue.copy()

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
    val moves: List<Move> get() = _moves.toList()
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

    /**
     * Renames the chimera after validating the nickname length and content.
     *
     * @param newName The new name value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun rename(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isNotBlank() && trimmedName.length <= 12) {
            this.name = trimmedName
        }
        else {
            throw IllegalArgumentException("Invalid name")
        }
    }

    /**
     * Adds experience and triggers all resulting level-ups.
     *
     * @param amount Numeric value used by this operation: amount.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun gainExp(amount: Int) {
        exp += amount
        var expNeeded = level * level * level
        while  (exp >= expNeeded) {
            exp -= expNeeded
            levelUp()
            expNeeded = level * level * level
        }
    }

    /**
     * Recalculates battle stats from base stats, IV stats, and current level.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun recalculateStats() {
        val oldMaxHp = stats.maxHp
        val calculatedMaxHp = (((baseStatsValue.maxHp + ivStatsValue.maxHp) * 2 * level) / 100) + level + 10
        val calculatedAttack = (((baseStatsValue.attack + ivStatsValue.attack) * 2 * level) / 100) + 5
        val calculatedDefence = (((baseStatsValue.defence + ivStatsValue.defence) * 2 * level) / 100) + 5
        val calculatedSpeed = (((baseStatsValue.speed + ivStatsValue.speed) * 2 * level) / 100) + 5

        stats.setStat(Stats.StatType.MAX_HP, calculatedMaxHp)
        stats.setStat(Stats.StatType.ATTACK, calculatedAttack)
        stats.setStat(Stats.StatType.DEFENCE, calculatedDefence)
        stats.setStat(Stats.StatType.SPEED, calculatedSpeed)

        val hpGain = stats.maxHp - oldMaxHp
        stats.heal(hpGain)
    }

    /**
     * Increases the level and resolves stat growth and new moves.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun levelUp() {
        level++
        recalculateStats()

        onLevelUp?.invoke(this)

        learnableMoves
            .filter { it.first == this.level }
            .forEach { (_, moveProvider) ->
                learnMove(moveProvider())
            }
    }

    /**
     * Learns a new move or stores it as pending when the move list is full.
     *
     * @param move Domain object used by this operation: move.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun learnMove(move: Move) {
        if (_moves.any { it.name == move.name }) return

        if (_moves.size < 4) {
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

    /**
     * Replaces an existing move with the pending move-learning request.
     *
     * @param index Numeric value used by this operation: index.
     * @return The resolved pair<move, move> value, or null when it is unavailable.
     */
    fun replaceMoveWithPending(index: Int): Pair<Move, Move>? {
        val pendingMove = pendingMoveToLearn ?: return null
        if (index !in _moves.indices) return null

        val forgottenMove = _moves[index]
        _moves[index] = pendingMove
        pendingMoveToLearn = null
        return forgottenMove to pendingMove
    }

    /**
     * Cancels the pending move-learning request and returns the skipped move.
     *
     * @return The resolved move value, or null when it is unavailable.
     */
    fun skipPendingMove(): Move? {
        val pendingMove = pendingMoveToLearn ?: return null
        pendingMoveToLearn = null
        return pendingMove
    }

}
