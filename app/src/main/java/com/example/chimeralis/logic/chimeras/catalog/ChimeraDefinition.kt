package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats

/** Describes one chimera species without storing per-instance battle state. */
data class ChimeraDefinition(
    val species: ChimeraSpecies,
    val displayName: String,
    val type: ChimeraType,
    val baseStatsFactory: () -> Stats,
    val learnset: List<LearnableMove>,
    val evolution: ChimeraEvolution? = null,
    val availability: ChimeraAvailability = ChimeraAvailability(),
    val visuals: ChimeraVisualSet,
    val saveAliases: List<String> = emptyList()
)
