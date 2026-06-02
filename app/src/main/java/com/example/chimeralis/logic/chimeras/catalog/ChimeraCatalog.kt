package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.toSaveLookupKey

/** Provides chimera species definitions to factories and UI adapters. */
interface ChimeraCatalog {
    val definitions: List<ChimeraDefinition>

    /**
     * Finds the definition that belongs to a concrete species.
     *
     * @param species The species value used by this operation.
     * @return The resulting ChimeraDefinition value.
     */
    fun definitionFor(species: ChimeraSpecies): ChimeraDefinition {
        return definitions.firstOrNull { definition -> definition.species == species }
            ?: throw IllegalArgumentException("Unknown chimera species: $species")
    }

    /**
     * Finds a species by its display name.
     *
     * @param name The name value used by this operation.
     * @return The resolved chimera species value, or null when it is unavailable.
     */
    fun speciesByName(name: String): ChimeraSpecies? {
        val lookupKey = name.toSaveLookupKey()
        return definitions.firstOrNull { definition ->
            definition.saveLookupNames().any { candidate -> candidate.toSaveLookupKey() == lookupKey }
        }?.species
    }
}

/**
 * Returns every supported saved-name candidate for one chimera definition.
 *
 * @receiver The chimera definition receiver used by this operation.
 * @return The collection produced by this operation.
 */
internal fun ChimeraDefinition.saveLookupNames(): List<String> {
    return listOf(displayName, species.javaClass.simpleName) + saveAliases
}
