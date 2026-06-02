package com.example.chimeralis.data.save

import com.example.chimeralis.logic.chimeras.ChimeraSpecies

/** Full save snapshot used by the continue menu and persistence layer. */
data class GameSave(
    val trainerName: String,
    val team: List<SavedChimera>,
    val storage: List<SavedChimera> = emptyList(),
    val inventoryItems: List<SavedItem> = emptyList(),
    val money: Int = 0,
    val playerColumn: Int,
    val playerRow: Int,
    val location: SavedGameLocation = SavedGameLocation.LavaField,
    val updatedAt: Long
) {

    /** Returns the first team member species for compact save descriptions. */
    val starterSpecies: ChimeraSpecies get() = team.first().species

    /** Returns the first team member nickname for compact save descriptions. */
    val starterNickname: String get() = team.first().nickname
}
