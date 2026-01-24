package com.example.pokemon.logic

/**
 * Enum class describes pokemon types.
 */
enum class PokemonType {
    FIRE, WATER, GRASS; //Can be added more later

    fun typeEffectiveness(targetType: PokemonType): Double {
        return when (this) {
            FIRE -> if (targetType == GRASS) 2.0 else if (targetType == WATER) 0.5 else 1.0
            WATER -> if (targetType == FIRE) 2.0 else if (targetType == GRASS) 0.5 else 1.0
            GRASS -> if (targetType == WATER) 2.0 else if (targetType == FIRE) 0.5 else 1.0
        }
    }
}