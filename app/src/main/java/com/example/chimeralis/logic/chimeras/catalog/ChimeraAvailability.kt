package com.example.chimeralis.logic.chimeras.catalog

/** Describes where one chimera species can be used by game systems. */
data class ChimeraAvailability(
    val starter: Boolean = false,
    val wild: Boolean = false,
    val trainerBattle: Boolean = false
)
