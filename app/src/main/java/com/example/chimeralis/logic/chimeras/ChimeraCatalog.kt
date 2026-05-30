package com.example.chimeralis.logic.chimeras

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
        return definitions.firstOrNull { definition -> definition.displayName == name }?.species
    }
}
