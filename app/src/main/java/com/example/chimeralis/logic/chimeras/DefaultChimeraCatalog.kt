package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Stores the default chimera definitions used by the game. */
object DefaultChimeraCatalog : ChimeraCatalog {
    private val fireEvolutionLearnset = learnset(
        1 to MoveName.TACKLE,
        1 to MoveName.GROWL,
        4 to MoveName.EMBER
    )
    private val grassLearnset = learnset(
        1 to MoveName.TACKLE,
        1 to MoveName.GROWL
    )
    private val waterLearnset = learnset(
        1 to MoveName.TACKLE,
        1 to MoveName.TAILWHIP
    )

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
                6 to MoveName.TAILWHIP,
                6 to MoveName.RECOVER
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
                fallbackImage = "starter_fire",
                moveFrames = mapOf(
                    MoveName.EMBER to listOf("sunflare_ember_1", "sunflare_ember_2"),
                    MoveName.GROWL to listOf("sunflare_growl"),
                    MoveName.TACKLE to listOf("sunflare_tackle_1", "sunflare_tackle_2")
                )
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Solflare,
            displayName = "Solflare",
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(58, 64, 58, 80) },
            learnset = fireEvolutionLearnset,
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Solignis,
                level = 36
            ),
            visuals = ChimeraVisualSet(
                mainImage = "solflare",
                fallbackImage = "starter_fire",
                moveFrames = mapOf(
                    MoveName.EMBER to listOf("solflare_ember_1", "solflare_ember_2"),
                    MoveName.GROWL to listOf("solflare_growl"),
                    MoveName.TACKLE to listOf("solflare_tackle_1", "solflare_tackle_2")
                )
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Solignis,
            displayName = "Solignis",
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(78, 84, 78, 100) },
            learnset = fireEvolutionLearnset,
            visuals = ChimVisuals.fire(
                mainImage = "solignis",
                ember = listOf("solignis_ember_1", "solignis_ember_2"),
                growl = listOf("solignis_growl_1", "solignis_growl_2"),
                tackle = listOf("solignis_tackle_1", "solignis_tackle_2")
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Sylvhorn,
            displayName = "Sylvhorn",
            type = ChimeraType.GRASS,
            baseStatsFactory = { Stats(45, 49, 49, 45) },
            learnset = grassLearnset,
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Sylvarchon,
                level = 32
            ),
            availability = ChimeraAvailability(
                starter = true,
                wild = true,
                trainerBattle = true
            ),
            visuals = ChimVisuals.grass(
                mainImage = "sylvhorn",
                growl = listOf("sylvhorn_growl"),
                tackle = listOf("sylvhorn_tackle_1", "sylvhorn_tackle_2")
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Sylvarchon,
            displayName = "Sylvarchon",
            type = ChimeraType.GRASS,
            baseStatsFactory = { Stats(80, 82, 83, 80) },
            learnset = grassLearnset,
            visuals = ChimVisuals.grass(
                mainImage = "sylvarchon",
                growl = listOf("sylvarchon_growl"),
                tackle = listOf("sylvarchon_tackle_1", "sylvarchon_tackle_2")
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Aquantis,
            displayName = "Aquantis",
            type = ChimeraType.WATER,
            baseStatsFactory = { Stats(44, 48, 65, 43) },
            learnset = waterLearnset,
            evolution = ChimeraEvolution(
                evolvesInto = ChimeraSpecies.Leviantis,
                level = 36
            ),
            availability = ChimeraAvailability(
                starter = true,
                wild = true,
                trainerBattle = true
            ),
            visuals = ChimVisuals.water(
                mainImage = "aquantis",
                tailWhip = listOf("aquantis_tailwhip_1", "aquantis_tailwhip_2"),
                tackle = listOf("aquantis_tackle_1", "aquantis_tackle_2")
            )
        ),
        ChimeraDefinition(
            species = ChimeraSpecies.Leviantis,
            displayName = "Leviantis",
            type = ChimeraType.WATER,
            baseStatsFactory = { Stats(79, 83, 100, 78) },
            learnset = waterLearnset,
            visuals = ChimVisuals.water(
                mainImage = "leviantis",
                tailWhip = listOf("leviantis_tailwhip_1", "leviantis_tailwhip_2"),
                tackle = listOf("leviantis_tackle_1", "leviantis_tackle_2")
            )
        )
    )

    /** Converts level-to-move pairs into learnset rows. */
    private fun learnset(vararg moves: Pair<Int, MoveName>): List<LearnableMove> {
        return moves.map { (level, moveName) -> LearnableMove(level, moveName) }
    }
}

/** Provides small visual-set builders for repeated families. */
private object ChimVisuals {
    /** Builds a fire visual set. */
    fun fire(
        mainImage: String,
        ember: List<String>,
        growl: List<String>,
        tackle: List<String>
    ): ChimeraVisualSet {
        return ChimeraVisualSet(
            mainImage = mainImage,
            fallbackImage = "starter_fire",
            moveFrames = mapOf(
                MoveName.EMBER to ember,
                MoveName.GROWL to growl,
                MoveName.TACKLE to tackle
            )
        )
    }

    /** Builds a grass visual set. */
    fun grass(
        mainImage: String,
        growl: List<String>,
        tackle: List<String>
    ): ChimeraVisualSet {
        return ChimeraVisualSet(
            mainImage = mainImage,
            fallbackImage = "starter_grass",
            moveFrames = mapOf(
                MoveName.GROWL to growl,
                MoveName.TACKLE to tackle
            )
        )
    }

    /** Builds a water visual set. */
    fun water(
        mainImage: String,
        tailWhip: List<String>,
        tackle: List<String>
    ): ChimeraVisualSet {
        return ChimeraVisualSet(
            mainImage = mainImage,
            fallbackImage = "starter_water",
            moveFrames = mapOf(
                MoveName.TAILWHIP to tailWhip,
                MoveName.TACKLE to tackle
            )
        )
    }
}
