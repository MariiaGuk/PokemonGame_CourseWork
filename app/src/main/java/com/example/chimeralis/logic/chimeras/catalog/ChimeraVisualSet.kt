package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Describes drawable resource names used by the UI for one species. */
data class ChimeraVisualSet(
    val mainImage: String,
    val fallbackImage: String,
    val moveFrames: Map<MoveName, List<String>> = emptyMap()
)
