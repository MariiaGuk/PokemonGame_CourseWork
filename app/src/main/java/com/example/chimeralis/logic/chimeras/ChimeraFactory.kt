package com.example.chimeralis.logic.chimeras

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

    /** Replaces the catalog source for tests or alternative game data. */
    fun configureCatalog(newCatalog: ChimeraCatalog) {
        validateCatalog(newCatalog)
        catalog = newCatalog
    }

    /** Generates random individual values for a new chimera. */
    fun generateRandomIV(): Stats = Stats(
        maxHp = (0..15).random(),
        attack = (0..15).random(),
        defence = (0..15).random(),
        speed = (0..15).random()
    )

    /** Returns all species known by the catalog. */
    fun allSpecies(): List<ChimeraSpecies> {
        return catalog.definitions.map { definition -> definition.species }
    }

    /** Returns the species that can be chosen at the start of the game. */
    fun starterSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.starter }
            .map { definition -> definition.species }
    }

    /** Returns the species that can appear as wild encounters. */
    fun wildSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.wild }
            .map { definition -> definition.species }
    }

    /** Returns the species available to trainer battle teams. */
    fun trainerBattleSpecies(): List<ChimeraSpecies> {
        return catalog.definitions
            .filter { definition -> definition.availability.trainerBattle }
            .map { definition -> definition.species }
    }

    /** Returns the display name configured for a species. */
    fun speciesName(species: ChimeraSpecies): String {
        return definitionFor(species).displayName
    }

    /** Returns the elemental type configured for a species. */
    fun speciesType(species: ChimeraSpecies): ChimeraType {
        return definitionFor(species).type
    }

    /** Returns the visual set configured for a species. */
    fun speciesVisuals(species: ChimeraSpecies): ChimeraVisualSet {
        return definitionFor(species).visuals
    }

    /** Returns the evolution rule configured for a species, if it can evolve. */
    fun speciesEvolution(species: ChimeraSpecies): ChimeraEvolution? {
        return definitionFor(species).evolution
    }

    /** Finds a species by the name used in save files. */
    fun speciesByName(name: String): ChimeraSpecies? {
        return catalog.speciesByName(name)
    }

    /** Creates one chimera with species, level, IV stats, and learnable moves. */
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

    /** Finds the definition that belongs to a concrete species. */
    private fun definitionFor(species: ChimeraSpecies): ChimeraDefinition {
        return catalog.definitionFor(species)
    }

    /** Creates a fresh move provider for the given move id. */
    private fun moveProvider(moveName: MoveName): () -> Move {
        return { MoveFactory.createMove(moveName) }
    }

    /** Validates that a catalog can safely be used by factories and save mapping. */
    private fun validateCatalog(catalog: ChimeraCatalog) {
        val definitions = catalog.definitions
        require(definitions.isNotEmpty()) { "Chimera catalog must contain at least one definition" }
        require(definitions.map { definition -> definition.species }.distinct().size == definitions.size) {
            "Chimera catalog contains duplicate species"
        }
        require(definitions.all { definition -> definition.displayName.isNotBlank() }) {
            "Chimera catalog contains a blank display name"
        }
        require(definitions.flatMap { definition -> definition.saveAliases }.all { alias -> alias.isNotBlank() }) {
            "Chimera catalog contains a blank save alias"
        }

        val duplicateLookupKey = definitions
            .flatMap { definition ->
                definition.saveLookupNames().map { name -> name.toLookupKey() to definition.species }
            }
            .groupBy(
                keySelector = { (lookupKey, _) -> lookupKey },
                valueTransform = { (_, species) -> species }
            )
            .firstNotNullOfOrNull { (lookupKey, species) ->
                lookupKey.takeIf { species.distinct().size > 1 }
            }

        require(duplicateLookupKey == null) {
            "Chimera catalog contains duplicate saved-name mapping: $duplicateLookupKey"
        }
    }

    /** Returns every supported saved-name candidate for one chimera definition. */
    private fun ChimeraDefinition.saveLookupNames(): List<String> {
        return listOf(displayName, species.javaClass.simpleName) + saveAliases
    }

    /** Normalizes persisted identifiers for duplicate lookup validation. */
    private fun String.toLookupKey(): String {
        return filter(Char::isLetterOrDigit).lowercase()
    }
}
