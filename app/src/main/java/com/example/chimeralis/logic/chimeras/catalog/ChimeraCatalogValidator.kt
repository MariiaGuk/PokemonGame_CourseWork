package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.toSaveLookupKey

/** Validates chimera catalog consistency and reports all detected issues at once. */
internal object ChimeraCatalogValidator {

    /**
     * Validates that a catalog can safely be used by factories and save mapping.
     *
     * @param catalog Domain object used by this operation: catalog.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun validate(catalog: ChimeraCatalog) {
        val definitions = catalog.definitions
        val errors = mutableListOf<String>()

        if (definitions.isEmpty()) {
            errors.add("Catalog must contain at least one chimera definition.")
        }

        validateSpeciesUniqueness(definitions, errors)
        validateDefinitionFields(definitions, errors)
        validateLookupNames(definitions, errors)
        validateEvolutionRules(definitions, errors)
        validateAvailability(definitions, errors)

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(
                buildString {
                    appendLine("Invalid chimera catalog:")
                    errors.forEach { error -> appendLine("- $error") }
                }.trimEnd()
            )
        }
    }

    /**
     * Validates that every species appears only once in the catalog.
     *
     * @param definitions The definitions value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateSpeciesUniqueness(
        definitions: List<ChimeraDefinition>,
        errors: MutableList<String>
    ) {
        definitions
            .withIndex()
            .groupBy { (_, definition) -> definition.species }
            .filterValues { entries -> entries.size > 1 }
            .forEach { (species, entries) ->
                val indexes = entries.joinToString { (index, _) -> "#$index" }
                errors.add("Species ${species.catalogName()} is defined more than once at $indexes.")
            }
    }

    /**
     * Validates required fields inside every definition.
     *
     * @param definitions The definitions value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateDefinitionFields(
        definitions: List<ChimeraDefinition>,
        errors: MutableList<String>
    ) {
        definitions.forEachIndexed { index, definition ->
            val label = definition.catalogLabel(index)

            if (definition.displayName.isBlank()) {
                errors.add("$label has a blank displayName.")
            }
            if (definition.learnset.isEmpty()) {
                errors.add("$label has an empty learnset.")
            }
            if (definition.learnset.none { move -> move.level == 1 }) {
                errors.add("$label must know at least one move at level 1.")
            }
            definition.learnset
                .filter { move -> move.level < 1 }
                .forEach { move ->
                    errors.add("$label has ${move.moveName} configured at invalid level ${move.level}.")
                }

            definition.learnset
                .groupBy { move -> move.moveName }
                .filterValues { moves -> moves.size > 1 }
                .forEach { (moveName, moves) ->
                    val levels = moves.joinToString { move -> move.level.toString() }
                    errors.add("$label learns $moveName more than once at levels $levels.")
                }

            validateVisuals(label, definition.visuals, errors)
            validateBaseStatsFactory(label, definition, errors)
        }
    }

    /**
     * Validates required visual resource names for one definition.
     *
     * @param label The label value used by this operation.
     * @param visuals The visuals value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateVisuals(
        label: String,
        visuals: ChimeraVisualSet,
        errors: MutableList<String>
    ) {
        if (visuals.mainImage.isBlank()) {
            errors.add("$label has a blank mainImage visual resource name.")
        }
        if (visuals.fallbackImage.isBlank()) {
            errors.add("$label has a blank fallbackImage visual resource name.")
        }
        visuals.moveFrames.forEach { (moveName, frames) ->
            if (frames.isEmpty()) {
                errors.add("$label configures $moveName with an empty moveFrames list.")
            }
            frames.forEachIndexed { frameIndex, frame ->
                if (frame.isBlank()) {
                    errors.add("$label configures $moveName with a blank frame name at index $frameIndex.")
                }
            }
        }
    }

    /**
     * Validates that base stat factories are executable and produce usable stats.
     *
     * @param label The label value used by this operation.
     * @param definition The definition value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateBaseStatsFactory(
        label: String,
        definition: ChimeraDefinition,
        errors: MutableList<String>
    ) {
        val baseStats = runCatching { definition.baseStatsFactory() }
            .onFailure { error ->
                errors.add("$label baseStatsFactory failed: ${error.message ?: error::class.simpleName}.")
            }
            .getOrNull()
            ?: return

        if (baseStats.maxHp < 1 ||
            baseStats.attack < 1 ||
            baseStats.defence < 1 ||
            baseStats.speed < 1
        ) {
            errors.add("$label baseStatsFactory must produce positive maxHp, attack, defence, and speed.")
        }
    }

    /**
     * Validates save names and aliases used by save-file lookup.
     *
     * @param definitions The definitions value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateLookupNames(
        definitions: List<ChimeraDefinition>,
        errors: MutableList<String>
    ) {
        definitions.forEachIndexed { index, definition ->
            val label = definition.catalogLabel(index)

            definition.saveAliases.forEachIndexed { aliasIndex, alias ->
                if (alias.isBlank()) {
                    errors.add("$label has a blank save alias at index $aliasIndex.")
                }
            }
            definition.saveLookupNames().forEach { name ->
                if (name.toSaveLookupKey().isBlank()) {
                    errors.add("$label has lookup name '$name' that normalizes to an empty key.")
                }
            }
        }

        definitions
            .flatMapIndexed { index, definition ->
                definition.saveLookupNames().map { name ->
                    CatalogLookupName(
                        key = name.toSaveLookupKey(),
                        originalName = name,
                        definitionLabel = definition.catalogLabel(index)
                    )
                }
            }
            .filter { lookup -> lookup.key.isNotBlank() }
            .groupBy { lookup -> lookup.key }
            .filterValues { lookups ->
                lookups.map { lookup -> lookup.definitionLabel }.distinct().size > 1
            }
            .forEach { (lookupKey, lookups) ->
                val candidates = lookups.joinToString { lookup ->
                    "${lookup.definitionLabel} as '${lookup.originalName}'"
                }
                errors.add("Saved-name lookup key '$lookupKey' is shared by $candidates.")
            }
    }

    /**
     * Validates evolution targets, levels, and cycles.
     *
     * @param definitions The definitions value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateEvolutionRules(
        definitions: List<ChimeraDefinition>,
        errors: MutableList<String>
    ) {
        val definedSpecies = definitions.map { definition -> definition.species }.toSet()
        val evolutionTargets = definitions.associate { definition ->
            definition.species to definition.evolution?.evolvesInto
        }

        definitions.forEachIndexed { index, definition ->
            val label = definition.catalogLabel(index)
            val evolution = definition.evolution ?: return@forEachIndexed

            if (evolution.level <= 1) {
                errors.add("$label evolution level must be greater than 1, but was ${evolution.level}.")
            }
            if (evolution.evolvesInto == definition.species) {
                errors.add("$label cannot evolve into itself.")
            }
            if (evolution.evolvesInto !in definedSpecies) {
                errors.add(
                    "$label evolves into ${evolution.evolvesInto.catalogName()}, " +
                            "but that species is not defined in the catalog."
                )
            }
        }

        definitions.forEachIndexed { index, definition ->
            val cycle = definition.species.evolutionCycle(evolutionTargets)
            if (cycle != null) {
                errors.add("${definition.catalogLabel(index)} participates in evolution cycle: $cycle.")
            }
        }
    }

    /**
     * Validates that required gameplay pools are not accidentally empty.
     *
     * @param definitions The definitions value used by this operation.
     * @param errors The errors value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun validateAvailability(
        definitions: List<ChimeraDefinition>,
        errors: MutableList<String>
    ) {
        if (definitions.none { definition -> definition.availability.starter }) {
            errors.add("Catalog must contain at least one starter chimera.")
        }
        if (definitions.none { definition -> definition.availability.wild }) {
            errors.add("Catalog must contain at least one wild chimera.")
        }
        if (definitions.none { definition -> definition.availability.trainerBattle }) {
            errors.add("Catalog must contain at least one trainer-battle chimera.")
        }
    }

    /**
     * Formats one definition for validation errors.
     *
     * @receiver The chimera definition receiver used by this operation.
     * @param index Numeric value used by this operation: index.
     * @return The text value produced by this operation.
     */
    private fun ChimeraDefinition.catalogLabel(index: Int): String {
        return "definition #$index (${species.catalogName()}, displayName='$displayName')"
    }

    /**
     * Formats one species identifier for validation errors.
     *
     * @receiver The chimera species receiver used by this operation.
     * @return The text value produced by this operation.
     */
    private fun ChimeraSpecies.catalogName(): String {
        return javaClass.simpleName
    }

    /**
     * Returns a readable cycle path if this species belongs to an evolution cycle.
     *
     * @receiver The chimera species receiver used by this operation.
     * @param evolutionTargets The evolution targets value used by this operation.
     * @return The resolved string value, or null when it is unavailable.
     */
    private fun ChimeraSpecies.evolutionCycle(
        evolutionTargets: Map<ChimeraSpecies, ChimeraSpecies?>
    ): String? {
        val visited = mutableMapOf<ChimeraSpecies, Int>()
        val path = mutableListOf<ChimeraSpecies>()
        var current: ChimeraSpecies? = this

        while (current != null) {
            val seenAt = visited[current]
            if (seenAt != null) {
                return (path.drop(seenAt) + current)
                    .joinToString(" -> ") { species -> species.catalogName() }
            }
            visited[current] = path.size
            path.add(current)
            current = evolutionTargets[current]
        }

        return null
    }

    /** Stores one normalized saved-name mapping candidate for validation. */
    private data class CatalogLookupName(
        val key: String,
        val originalName: String,
        val definitionLabel: String
    )
}
