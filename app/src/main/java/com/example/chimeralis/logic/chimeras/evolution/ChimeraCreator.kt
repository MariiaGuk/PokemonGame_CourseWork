package com.example.chimeralis.logic.chimeras.evolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats

/**
 * Creates chimera instances for services that should not depend on a concrete factory.
 *
 * @param species The species value used by this operation.
 * @param level Numeric value used by this operation: level.
 * @param ivStats The iv stats value used by this operation.
 * @return The resulting Chimera value.
 */
fun interface ChimeraCreator {
    /**
     * Handles create behavior.
     *
     * @param species The species value used by this operation.
     * @param level Numeric value used by this operation: level.
     * @param ivStats The iv stats value used by this operation.
     * @return The resulting Chimera value.
     */
    fun create(species: ChimeraSpecies, level: Int, ivStats: Stats): Chimera
}
