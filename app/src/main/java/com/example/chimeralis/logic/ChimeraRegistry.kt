package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.MoveRegistry

object ChimeraRegistry {
    fun sunflare() = Chimera(
        name = "Sunflare",
        type = ChimeraType.FIRE,
        stats = Stats(maxHp = 39, attack = 52, defence = 43, speed = 65),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.growl())
    )

    fun sylvhorn() = Chimera(
        name = "Sylvhorn",
        type = ChimeraType.GRASS,
        stats = Stats(maxHp = 45, attack = 49, defence = 49, speed = 45),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.growl())
    )

    fun aquantis() = Chimera(
        name = "Aquantis",
        type = ChimeraType.WATER,
        stats = Stats(maxHp = 44, attack = 48, defence = 65, speed = 43),
        level = 1,
        moves = listOf(MoveRegistry.tackle(),MoveRegistry.tailWhip())
    )
}