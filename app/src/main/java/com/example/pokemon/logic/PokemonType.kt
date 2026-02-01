package com.example.pokemon.logic

/**
 * Enum class describes pokemon types.
 */
enum class PokemonType {
    NORMAL, FIRE, WATER, GRASS; //Can be added more later

    fun typeEffectiveness(targetType: PokemonType): Double {
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