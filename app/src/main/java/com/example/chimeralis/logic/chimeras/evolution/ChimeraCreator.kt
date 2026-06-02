package com.example.chimeralis.logic.chimeras.evolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats

/** Creates chimera instances for services that should not depend on a concrete factory. */
fun interface ChimeraCreator {
    fun create(species: ChimeraSpecies, level: Int, ivStats: Stats): Chimera
}
