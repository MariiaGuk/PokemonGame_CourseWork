package com.example.chimeralis.logic.battle.random

/** Abstraction over randomness used to keep battle logic testable. */
interface RandomProvider {

    /**
     * Returns a random double in the default range.
     *
     * @return The calculated numeric value.
     */
    fun nextDouble(): Double

    /**
     * Returns a random integer from an inclusive range.
     *
     * @param range The range value used by this operation.
     * @return The calculated numeric value.
     */
    fun nextInt(range: IntRange): Int
}
