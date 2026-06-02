package com.example.chimeralis.data.save

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats

/** Serializable chimera snapshot stored in SharedPreferences. */
data class SavedChimera(
    val species: ChimeraSpecies,
    val nickname: String,
    val level: Int,
    val exp: Int,
    val currentHp: Int,
    val ivStats: Stats,
    val moves: List<SavedMovePp> = emptyList()
)
