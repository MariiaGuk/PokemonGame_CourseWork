package com.example.chimeralis.logic.chimeras

import com.example.chimeralis.logic.chimeras.catalog.ChimeraCatalog
import com.example.chimeralis.logic.chimeras.catalog.ChimeraCatalogValidator
import com.example.chimeralis.logic.chimeras.catalog.ChimeraDefinition
import com.example.chimeralis.logic.chimeras.catalog.ChimeraEvolution
import com.example.chimeralis.logic.chimeras.catalog.ChimeraVisualSet
import com.example.chimeralis.logic.chimeras.catalog.DefaultChimeraCatalog
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Creates configured chimera instances from catalog definitions. */
object ChimeraFactory {
    var catalog: ChimeraCatalog = DefaultChimeraCatalog
        private set

    init {
        validateCatalog(catalog)
    }

    /**
     * Replaces the catalog source for tests or alternative game data.
     *
     * @param newCatalog The new catalog value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun configureCatalog(newCatalog: ChimeraCatalog) {
        validateCatalog(newCatalog)
        catalog = newCatalog
    }

    /**
     * Generates random individual values for a new chimera.
     *
     * @return The resulting Stats value.
     */
    fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )

    /**
     * Returns all species known by the catalog.
     *
     * @return The collection produced by this operation.
     */
    fun allSpecies(): List<ChimeraSpecies> {
        return catalog.definitions.map { definition -> definition.species }
    }

    /**
     * Returns the species that can be chosen at the start of the game.
     *
     * @return The collection produced by this operation.
     */
    fun starterSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.starter }
            .map { definition -> definition.species }
    }

    /**
     * Returns the species that can appear as wild encounters.
     *
     * @return The collection produced by this operation.
     */
    fun wildSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.wild }
            .map { definition -> definition.species }
    }

    /**
     * Returns the species available to trainer battle teams.
     *
     * @return The collection produced by this operation.
     */
    fun trainerBattleSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.trainerBattle }
            .map { definition -> definition.species }
    }

    /**
     * Returns the display name configured for a species.
     *
     * @param species The species value used by this operation.
     * @return The text value produced by this operation.
     */
    fun speciesName(species: ChimeraSpecies): String {
        return definitionFor(species).displayName
    }

    /**
     * Returns the elemental type configured for a species.
     *
     * @param species The species value used by this operation.
     * @return The resulting ChimeraType value.
     */
    fun speciesType(species: ChimeraSpecies): ChimeraType {
        return definitionFor(species).type
    }

    /**
     * Returns the visual set configured for a species.
     *
     * @param species The species value used by this operation.
     * @return The resulting ChimeraVisualSet value.
     */
    fun speciesVisuals(species: ChimeraSpecies): ChimeraVisualSet {
        return definitionFor(species).visuals
    }

    /**
     * Returns the evolution rule configured for a species, if it can evolve.
     *
     * @param species The species value used by this operation.
     * @return The resolved chimera evolution value, or null when it is unavailable.
     */
    fun speciesEvolution(species: ChimeraSpecies): ChimeraEvolution? {
        return definitionFor(species).evolution
    }

    /**
     * Finds a species by the name used in save files.
     *
     * @param name The name value used by this operation.
     * @return The resolved chimera species value, or null when it is unavailable.
     */
    fun speciesByName(name: String): ChimeraSpecies? {
        return catalog.speciesByName(name)
    }

    /**
     * Creates one chimera with species, level, IV stats, and learnable moves.
     *
     * @param species The species value used by this operation.
     * @param level Numeric value used by this operation: level.
     * @param ivStats The iv stats value used by this operation.
     * @return The resulting Chimera value.
     */
    fun createChimera(
        species: ChimeraSpecies,
        level: Int = 1,
        ivStats: Stats = generateRandomIV()
    ): Chimera {
        val definition = definitionFor(species)
        return Chimera(
            name = definition.displayName,
            species = definition.species,
            type = definition.type,
            baseStats = definition.baseStatsFactory(),
            ivStats = ivStats,
            level = level,
            learnableMoves = definition.learnset.map { learnableMove ->
                learnableMove.level to moveProvider(learnableMove.moveName)
            }
        )
    }

    /**
     * Finds the definition that belongs to a concrete species.
     *
     * @param species The species value used by this operation.
     * @return The resulting ChimeraDefinition value.
     */
    private fun definitionFor(species: ChimeraSpecies): ChimeraDefinition {
        return catalog.definitionFor(species)
    }

    /**
     * Creates a fresh move provider for the given move id.
     *
     * @param moveName The move name value used by this operation.
     * @return The resulting () -> Move value.
     */
    private fun moveProvider(moveName: MoveName): () -> Move {
        return { MoveFactory.createMove(moveName) }
    }

    /**
     * Validates that a catalog can safely be used by factories and save mapping.
     *
     * @param catalog Domain object used by this operation: catalog.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateCatalog(catalog: ChimeraCatalog) {
        ChimeraCatalogValidator.validate(catalog)
    }
}
