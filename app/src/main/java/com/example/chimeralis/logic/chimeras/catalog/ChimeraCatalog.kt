package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.toSaveLookupKey

/** Provides chimera species definitions to factories and UI adapters. */
interface ChimeraCatalog {
    val definitions: List<ChimeraDefinition>

    /** Finds the definition that belongs to a concrete species. */
    fun definitionFor(species: ChimeraSpecies): ChimeraDefinition {
        return definitions.firstOrNull { definition -> definition.species == species }
            ?: throw IllegalArgumentException("Unknown chimera species: $species")
    }

    /** Finds a species by its display name. */
    fun speciesByName(name: String): ChimeraSpecies? {
        val lookupKey = name.toSaveLookupKey()
        return definitions.firstOrNull { definition ->
            definition.saveLookupNames().any { candidate -> candidate.toSaveLookupKey() == lookupKey }
        }?.species
    }
}

/** Returns every supported saved-name candidate for one chimera definition. */
internal fun ChimeraDefinition.saveLookupNames(): List<String> {
    return listOf(displayName, species.javaClass.simpleName) + saveAliases
}
