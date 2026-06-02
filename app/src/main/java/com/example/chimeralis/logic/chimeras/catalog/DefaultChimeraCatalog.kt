package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Stores the default chimera definitions used by the game. */
object DefaultChimeraCatalog : ChimeraCatalog {
    override val definitions: List<ChimeraDefinition> = listOf(
        ChimeraDefinition(
            species = ChimeraSpecies.Sunflare,
            displayName = "Sunflare",
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(39, 52, 43, 65) },
            learnset = learnset(
                1 to MoveName.TACKLE,
                1 to MoveName.GROWL,
                6 to MoveName.EMBER,
                8 to MoveName.TAILWHIP,
                12 to MoveName.RECOVER
            ),
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Solflare,
                level = 10
            ),
            availability = ChimeraAvailability(
                starter = true,
                wild = true,
                trainerBattle = true
            ),
            visuals = ChimeraVisualSet(
                mainImage = "sunflare",
                fallbackImage = "sunflare",
                moveFrames = mapOf(
                    MoveName.EMBER to listOf("sunflare_ember_1", "sunflare_ember_2"),
                    MoveName.GROWL to listOf("sunflare_growl"),
                    MoveName.TACKLE to listOf("sunflare_tackle_1", "sunflare_tackle_2")
                )
            ),
            saveAliases = listOf("Sunflare")
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Solflare,
            displayName = "Solflare",
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(58, 64, 58, 80) },
            learnset = learnset(
                1 to MoveName.TACKLE,
                1 to MoveName.EMBER,
                8 to MoveName.GROWL,
                18 to MoveName.RECOVER
            ),
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Solignis,
                level = 36
            ),
            visuals = ChimeraVisualSet(
                mainImage = "solflare",
                fallbackImage = "solflare",
                moveFrames = mapOf(
                    MoveName.EMBER to listOf("solflare_ember_1", "solflare_ember_2"),
                    MoveName.GROWL to listOf("solflare_growl"),
                    MoveName.TACKLE to listOf("solflare_tackle_1", "solflare_tackle_2")
                )
            ),
            saveAliases = listOf("Solflare")
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Solignis,
            displayName = "Solignis",
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(78, 84, 78, 100) },
            learnset = learnset(
                1 to MoveName.EMBER,
                1 to MoveName.GROWL,
                12 to MoveName.TACKLE,
                28 to MoveName.RECOVER
            ),
            visuals = ChimeraVisualSet(
                mainImage = "solignis",
                fallbackImage = "solignis",
                moveFrames = mapOf(
                    MoveName.EMBER to listOf("solignis_ember_1", "solignis_ember_2"),
                    MoveName.GROWL to listOf("solignis_growl_1", "solignis_growl_2"),
                    MoveName.TACKLE to listOf("solignis_tackle_1", "solignis_tackle_2")
                )
            ),
            saveAliases = listOf("Solignis")
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Sylvhorn,
            displayName = "Sylvhorn",
            type = ChimeraType.GRASS,
            baseStatsFactory = { Stats(45, 49, 49, 45) },
            learnset = learnset(
                1 to MoveName.TACKLE,
                4 to MoveName.GROWL,
                12 to MoveName.RECOVER
            ),
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Sylvarchon,
                level = 32
            ),
            availability = ChimeraAvailability(
                starter = true,
                wild = true,
                trainerBattle = true
            ),
            visuals = ChimeraVisualSet(
                mainImage = "sylvhorn",
                fallbackImage = "sylvhorn",
                moveFrames = mapOf(
                    MoveName.GROWL to listOf("sylvhorn_growl"),
                    MoveName.TACKLE to listOf("sylvhorn_tackle_1", "sylvhorn_tackle_2")
                )
            ),
            saveAliases = listOf("Sylvhorn")
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Sylvarchon,
            displayName = "Sylvarchon",
            type = ChimeraType.GRASS,
            baseStatsFactory = { Stats(80, 82, 83, 80) },
            learnset = learnset(
                1 to MoveName.GROWL,
                1 to MoveName.TACKLE,
                20 to MoveName.RECOVER
            ),
            visuals = ChimeraVisualSet(
                mainImage = "sylvarchon",
                fallbackImage = "sylvarchon",
                moveFrames = mapOf(
                    MoveName.GROWL to listOf("sylvarchon_growl"),
                    MoveName.TACKLE to listOf("sylvarchon_tackle_1", "sylvarchon_tackle_2")
                )
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Aquantis,
            displayName = "Aquantis",
            type = ChimeraType.WATER,
            baseStatsFactory = { Stats(44, 48, 65, 43) },
            learnset = learnset(
                1 to MoveName.TAILWHIP,
                4 to MoveName.TACKLE,
                14 to MoveName.RECOVER
            ),
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Leviantis,
                level = 36
            ),
            availability = ChimeraAvailability(
                starter = true,
                wild = true,
                trainerBattle = true
            ),
            visuals = ChimeraVisualSet(
                mainImage = "aquantis",
                fallbackImage = "aquantis",
                moveFrames = mapOf(
                    MoveName.TAILWHIP to listOf("aquantis_tailwhip_1", "aquantis_tailwhip_2"),
                    MoveName.TACKLE to listOf("aquantis_tackle_1", "aquantis_tackle_2")
                )
            ),
            saveAliases = listOf("Aquantis")
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Leviantis,
            displayName = "Leviantis",
            type = ChimeraType.WATER,
            baseStatsFactory = { Stats(79, 83, 100, 78) },
            learnset = learnset(
                1 to MoveName.TACKLE,
                1 to MoveName.TAILWHIP,
                24 to MoveName.RECOVER
            ),
            visuals = ChimeraVisualSet(
                mainImage = "leviantis",
                fallbackImage = "leviantis",
                moveFrames = mapOf(
                    MoveName.TAILWHIP to listOf("leviantis_tailwhip_1", "leviantis_tailwhip_2"),
                    MoveName.TACKLE to listOf("leviantis_tackle_1", "leviantis_tackle_2")
                )
            )
        )
    )

    /**
     * Converts level-to-move pairs into learnset rows.
     *
     * @param moves The moves value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun learnset(vararg moves: Pair<Int, MoveName>): List<LearnableMove> {
        return moves.map { (level, moveName) -> LearnableMove(level, moveName) }
    }
}
