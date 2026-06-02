package com.example.chimeralis.logic.battle.model

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Stores battle move animation data. */
data class BattleMoveAnimation(
    val side: BattleSide,
    val species: ChimeraSpecies,
    val chimeraName: String,
    val moveName: String,
    val moveId: MoveName?,
    val feedbacks: List<BattleMoveFeedback> = emptyList(),
    val kind: BattleAnimationKind = BattleAnimationKind.Move,
    val captureSucceeded: Boolean = false,
    val userBefore: BattleStatsSnapshot? = null,
    val userAfter: BattleStatsSnapshot? = null,
    val targetBefore: BattleStatsSnapshot? = null,
    val targetAfter: BattleStatsSnapshot? = null
)
