package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.MoveRegistry

/**
 * Registry for every chimera in the game.
 */
object ChimeraRegistry {
    private fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )
    fun sunflare(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Chimera(
        name = "Sunflare",
        type = ChimeraType.FIRE,
        baseStats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.growl()},
            4 to {MoveRegistry.ember()},
            //...
        )
    )

    fun sylvhorn(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Chimera(
        name = "Sylvhorn",
        type = ChimeraType.GRASS,
        baseStats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.growl()},
            //...
        )
    )

    fun aquantis(level: Int = 1, ivStats: Stats = generateRandomIV() ) = Chimera(
        name = "Aquantis",
        type = ChimeraType.WATER,
        baseStats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        ivStats = ivStats,
        level = level,
        learnableMoves = listOf(
            1 to {MoveRegistry.tackle()},
            1 to {MoveRegistry.tailWhip()},
            //...
        )
    )
}