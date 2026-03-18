package com.example.chimeralis.logic

import com.example.chimeralis.logic.moves.MoveFactory
import com.example.chimeralis.logic.moves.MoveName

/**
 * Registry for every chimera in the game.
 */
object ChimeraFactory {
    private fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )
    fun createChimera(species: ChimeraSpecies, level: Int = 1): Chimera {
        val ivStats = generateRandomIV()

        return when (species) {
            ChimeraSpecies.SUNFLARE -> Chimera(
                name = "Sunflare",
                type = ChimeraType.FIRE,
                baseStats = Stats(39, 52, 43, 65),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.GROWL)},
                    4 to {MoveFactory.createMove(MoveName.EMBER)},
                    //...
                )
            )
            ChimeraSpecies.SYLVHORN -> Chimera(
                name = "Sylvhorn",
                type = ChimeraType.GRASS,
                baseStats = Stats(45, 49, 49, 45),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.GROWL)},
                    //...
                )
            )
            ChimeraSpecies.AQUANTIS -> Chimera(
                name = "Aquantis",
                type = ChimeraType.WATER,
                baseStats = Stats(44, 48, 65, 43),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to {MoveFactory.createMove(MoveName.TACKLE)},
                    1 to {MoveFactory.createMove(MoveName.TAILWHIP)},
                    //...
                )
            )
        }
    }
}