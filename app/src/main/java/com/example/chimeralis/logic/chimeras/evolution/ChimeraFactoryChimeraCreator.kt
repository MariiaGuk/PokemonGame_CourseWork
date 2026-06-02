package com.example.chimeralis.logic.chimeras.evolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats

/** Default chimera creator backed by the configured application factory. */
internal object ChimeraFactoryChimeraCreator : ChimeraCreator {
    /**
     * Handles create behavior.
     *
     * @param species The species value used by this operation.
     * @param level Numeric value used by this operation: level.
     * @param ivStats The iv stats value used by this operation.
     * @return The resulting Chimera value.
     */
    override fun create(species: ChimeraSpecies, level: Int, ivStats: Stats): Chimera {
        return ChimeraFactory.createChimera(
            species = species,
            level = level,
            ivStats = ivStats
        )
    }
}
