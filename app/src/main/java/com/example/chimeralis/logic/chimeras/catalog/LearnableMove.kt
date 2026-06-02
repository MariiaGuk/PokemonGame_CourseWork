package com.example.chimeralis.logic.chimeras.catalog

import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Describes one move that becomes available at a specific level. */
data class LearnableMove(
    val level: Int,
    val moveName: MoveName
)
