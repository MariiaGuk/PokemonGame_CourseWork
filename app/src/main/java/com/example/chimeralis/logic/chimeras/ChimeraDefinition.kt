package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Describes one chimera species without storing per-instance battle state. */
data class ChimeraDefinition(
    val species: ChimeraSpecies,
    val displayName: String,
    val type: ChimeraType,
    val baseStatsFactory: () -> Stats,
    val learnset: List<LearnableMove>,
    val evolution: ChimeraEvolution? = null,
    val availability: ChimeraAvailability = ChimeraAvailability(),
    val visuals: ChimeraVisualSet
)

/** Describes the next species and level required for evolution. */
data class ChimeraEvolution(
    val evolvesInto: ChimeraSpecies,
    val level: Int
)

/** Describes where one chimera species can be used by game systems. */
data class ChimeraAvailability(
    val starter: Boolean = false,
    val wild: Boolean = false,
    val trainerBattle: Boolean = false
)

/** Describes one move that becomes available at a specific level. */
data class LearnableMove(
    val level: Int,
    val moveName: MoveName
)

/** Describes drawable resource names used by the UI for one species. */
data class ChimeraVisualSet(
    val mainImage: String,
    val fallbackImage: String,
    val moveFrames: Map<MoveName, List<String>> = emptyMap()
)
