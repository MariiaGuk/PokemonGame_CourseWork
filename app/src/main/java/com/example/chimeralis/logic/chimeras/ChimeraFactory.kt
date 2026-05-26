package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName

/**
 * Factory for every chimera in the game.
 */
object ChimeraFactory {
    fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )
    fun createChimera(
        species: ChimeraSpecies,
        level: Int = 1,
        ivStats: Stats = generateRandomIV()
    ): Chimera {
        return when (species) {
            ChimeraSpecies.Solignis -> Chimera(
                name = "Solignis",
                species = species,
                type = ChimeraType.FIRE,
                baseStats = Stats(78, 84, 78, 100),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    4 to { MoveFactory.createMove(MoveName.EMBER)}
                )
            )
            ChimeraSpecies.Solflare -> Chimera(
                name = "Solflare",
                species = species,
                type = ChimeraType.FIRE,
                baseStats = Stats(58, 64, 58, 80),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    4 to { MoveFactory.createMove(MoveName.EMBER)}
                )
            )
            ChimeraSpecies.Sunflare -> Chimera(
                name = "Sunflare",
                species = species,
                type = ChimeraType.FIRE,
                baseStats = Stats(39, 52, 43, 65),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)},
                    6 to { MoveFactory.createMove(MoveName.EMBER)},
                    6 to { MoveFactory.createMove(MoveName.TAILWHIP)},
                    6 to { MoveFactory.createMove(MoveName.RECOVER)}
                )
            )
            ChimeraSpecies.Sylvhorn -> Chimera(
                name = "Sylvhorn",
                species = species,
                type = ChimeraType.GRASS,
                baseStats = Stats(45, 49, 49, 45),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.GROWL)}
                )
            )
            ChimeraSpecies.Aquantis -> Chimera(
                name = "Aquantis",
                species = species,
                type = ChimeraType.WATER,
                baseStats = Stats(44, 48, 65, 43),
                ivStats = ivStats,
                level = level,
                learnableMoves = listOf(
                    1 to { MoveFactory.createMove(MoveName.TACKLE)},
                    1 to { MoveFactory.createMove(MoveName.TAILWHIP)}
                )
            )
        }
    }
}
