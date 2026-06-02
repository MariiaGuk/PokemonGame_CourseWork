package com.example.chimeralis.logic.battle.model

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies

/** Stores one chimera evolution that should be shown after battle. */
data class ChimeraEvolutionEvent(
    val oldChimera: Chimera,
    val newChimera: Chimera,
    val oldSpecies: ChimeraSpecies,
    val newSpecies: ChimeraSpecies,
    val oldName: String,
    val newName: String
)
