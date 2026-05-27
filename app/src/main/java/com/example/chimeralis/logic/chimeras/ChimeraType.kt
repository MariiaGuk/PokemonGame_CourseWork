package com.example.chimeralis.logic.chimeras

/**
 * Enum class describes chimera types.
 */
enum class ChimeraType {
    NORMAL, FIRE, WATER, GRASS; //Can be added more later

    /** Handles type effectiveness behavior. */
    fun typeEffectiveness(targetType: ChimeraType): Double {
        return effectiveness[this]?.get(targetType) ?: 1.0
    }

    companion object {
        private val effectiveness = mapOf(
            FIRE to mapOf(
                GRASS to 2.0,
                WATER to 0.5
            ),
            WATER to mapOf(
                FIRE to 2.0,
                GRASS to 0.5
            ),
            GRASS to mapOf(
                WATER to 2.0,
                FIRE to 0.5
            )
        )
    }
}