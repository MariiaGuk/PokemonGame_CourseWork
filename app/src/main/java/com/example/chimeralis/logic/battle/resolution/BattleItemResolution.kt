package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.logic.chimeras.Chimera

/** Describes the result of resolving one item action. */
data class BattleItemResolution(
    val log: List<String>,
    val animation: BattleMoveAnimation? = null,
    val shouldEnemyAct: Boolean = false,
    val isBattleActive: Boolean = true,
    val caughtChimera: Chimera? = null
)
