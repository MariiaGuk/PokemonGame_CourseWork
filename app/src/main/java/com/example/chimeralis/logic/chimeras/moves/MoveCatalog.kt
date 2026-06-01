package com.example.chimeralis.logic.chimeras.moves

/** Provides move definitions available to factories and learnsets. */
interface MoveCatalog {
    val definitions: List<MoveDefinition>

    /** Finds a move definition by its stable identifier. */
    fun definitionFor(moveName: MoveName): MoveDefinition {
        return definitions.firstOrNull { definition -> definition.id == moveName }
            ?: throw IllegalArgumentException("Unknown move: $moveName")
    }
}
