package com.example.chimeralis.ui.screens.battle

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes

/** Calculates the EXP required to reach the next level. */
internal fun Int.expToNextLevel(): Int {
    return (this * this * this).coerceAtLeast(1)
}

/** Resolves the battle sprite image resource for one species. */
internal fun ChimeraSpecies.battleImageRes(): Int = chimeraImageRes()
