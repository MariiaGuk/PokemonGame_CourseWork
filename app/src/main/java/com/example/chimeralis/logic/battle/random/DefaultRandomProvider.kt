package com.example.chimeralis.logic.battle.random

import kotlin.random.Random

/** Production random provider backed by Kotlin Random. */
object DefaultRandomProvider : RandomProvider {

    /**
     * Returns a random double in the default range.
     *
     * @return The calculated numeric value.
     */
    override fun nextDouble(): Double = Random.nextDouble()

    /**
     * Returns a random integer from an inclusive range.
     *
     * @param range The range value used by this operation.
     * @return The calculated numeric value.
     */
    override fun nextInt(range: IntRange): Int = Random.nextInt(range.first, range.last + 1)
}
