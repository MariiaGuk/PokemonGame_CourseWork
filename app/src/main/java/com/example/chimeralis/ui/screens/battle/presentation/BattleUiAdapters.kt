package com.example.chimeralis.ui.screens.battle.presentation

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes

/**
 * Calculates the EXP required to reach the next level.
 *
 * @receiver The int receiver used by this operation.
 * @return The calculated numeric value.
 */
internal fun Int.expToNextLevel(): Int {
    return (this * this * this).coerceAtLeast(1)
}

/**
 * Resolves the battle sprite image resource for one species.
 *
 * @receiver The chimera species receiver used by this operation.
 * @return The calculated numeric value.
 */
internal fun ChimeraSpecies.battleImageRes(): Int = chimeraImageRes()
