package com.example.chimeralis.logic.chimeras.moves

/** Creates move instances from move identifiers. */
object MoveFactory {
    var catalog: MoveCatalog = DefaultMoveCatalog
        private set

    init {
        validateCatalog(catalog)
    }

    /** Replaces the catalog source for alternative move data. */
    fun configureCatalog(newCatalog: MoveCatalog) {
        validateCatalog(newCatalog)
        catalog = newCatalog
    }

    /** Builds one move with type, PP, accuracy, and effects. */
    fun createMove(move: MoveName): Move {
        return catalog.definitionFor(move).createMove()
    }

    /** Validates that the catalog covers every move id exactly once. */
    private fun validateCatalog(catalog: MoveCatalog) {
        val moveIds = catalog.definitions.map { definition -> definition.id }
        require(moveIds.toSet().size == moveIds.size) { "Move catalog contains duplicate move ids" }

        val missingMoves = MoveName.values().filterNot { moveName -> moveName in moveIds }
        require(missingMoves.isEmpty()) { "Move catalog is missing definitions for: $missingMoves" }
    }
}
