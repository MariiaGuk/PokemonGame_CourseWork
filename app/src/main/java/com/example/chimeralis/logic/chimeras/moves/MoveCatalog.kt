package com.example.chimeralis.logic.chimeras.moves

/** Provides move definitions available to factories and learnsets. */
interface MoveCatalog {
    val definitions: List<MoveDefinition>

    /**
     * Finds a move definition by its stable identifier.
     *
     * @param moveName The move name value used by this operation.
     * @return The resulting MoveDefinition value.
     */
    fun definitionFor(moveName: MoveName): MoveDefinition {
        return definitions.firstOrNull { definition -> definition.id == moveName }
            ?: throw IllegalArgumentException("Unknown move: $moveName")
    }
}
