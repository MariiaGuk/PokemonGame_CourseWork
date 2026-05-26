package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.ChimeraSpecies

data class BattleTurnResult(
    val log: List<String>,
    val animations: List<BattleMoveAnimation>
)

data class BattleMoveAnimation(
    val side: BattleSide,
    val species: ChimeraSpecies,
    val chimeraName: String,
    val moveName: String,
    val feedbacks: List<BattleMoveFeedback> = emptyList(),
    val kind: BattleAnimationKind = BattleAnimationKind.Move,
    val captureSucceeded: Boolean = false,
    val userBefore: BattleStatsSnapshot? = null,
    val userAfter: BattleStatsSnapshot? = null,
    val targetBefore: BattleStatsSnapshot? = null,
    val targetAfter: BattleStatsSnapshot? = null
)

data class BattleStatsSnapshot(
    val currentHp: Int,
    val maxHp: Int,
    val attack: Int,
    val defence: Int,
    val speed: Int,
    val attackStage: Int,
    val defenceStage: Int,
    val speedStage: Int
)

data class BattleMoveFeedback(
    val side: BattleSide,
    val type: BattleMoveFeedbackType
)

enum class BattleMoveFeedbackType {
    Damage,
    Faint,
    StatChange
}

enum class BattleSide {
    Player,
    Enemy
}

enum class BattleAnimationKind {
    Move,
    Capture,
    Item
}
