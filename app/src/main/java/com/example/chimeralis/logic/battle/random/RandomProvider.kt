package com.example.chimeralis.logic.battle.random

/** Abstraction over randomness used to keep battle logic testable. */
interface RandomProvider {

    /** Returns a random double in the default range. */
    fun nextDouble(): Double

    /** Returns a random integer from an inclusive range. */
    fun nextInt(range: IntRange): Int
}
