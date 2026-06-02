package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies

/** Describes the next species and level required for evolution. */
data class ChimeraEvolution(
    val evolvesInto: ChimeraSpecies,
    val level: Int
)
