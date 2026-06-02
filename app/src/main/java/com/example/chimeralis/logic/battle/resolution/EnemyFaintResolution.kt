package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.chimeras.Chimera

/** Describes battle-state changes caused by an enemy chimera fainting. */
data class EnemyFaintResolution(
    val isBattleActive: Boolean,
    val nextChimera: Chimera?,
    val shouldAwardMoney: Boolean,
    val message: String
)
