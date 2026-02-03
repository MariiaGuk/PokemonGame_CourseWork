package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.MoveRegistry

object ChimeraRegistry {
    fun sunflare(level: Int = 1) = Chimera(
        name = "Sunflare",
        type = ChimeraType.FIRE,
        baseStats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        exp = 0,
        level = level,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.growl()),
        learnableMoves = mapOf(
            4 to { MoveRegistry.ember() },
            //...
        )
    )

    fun sylvhorn() = Chimera(
        name = "Sylvhorn",
        type = ChimeraType.GRASS,
        baseStats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        exp = 0,
        level = 1,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.growl()),
        learnableMoves = mapOf()
    )

    fun aquantis() = Chimera(
        name = "Aquantis",
        type = ChimeraType.WATER,
        baseStats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        exp = 0,
        level = 1,
        moves = mutableListOf(MoveRegistry.tackle(),MoveRegistry.tailWhip()),
        learnableMoves = mapOf()
    )
}