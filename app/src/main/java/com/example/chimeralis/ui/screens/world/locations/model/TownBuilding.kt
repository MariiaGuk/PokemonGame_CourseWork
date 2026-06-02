package com.example.chimeralis.ui.screens.world.locations.model

/** Stores town building data. */
internal data class TownBuilding(
    val imageRes: Int,
    val column: Int,
    val row: Int,
    val columns: Int = 4,
    val rows: Int = 4
)
