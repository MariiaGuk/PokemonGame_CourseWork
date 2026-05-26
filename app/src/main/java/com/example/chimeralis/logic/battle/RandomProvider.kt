package com.example.chimeralis.logic.battle

import kotlin.random.Random

interface RandomProvider {
    fun nextDouble(): Double
    fun nextInt(range: IntRange): Int
}

object DefaultRandomProvider : RandomProvider {
    override fun nextDouble(): Double = Random.nextDouble()

    override fun nextInt(range: IntRange): Int = Random.nextInt(range.first, range.last + 1)
}
