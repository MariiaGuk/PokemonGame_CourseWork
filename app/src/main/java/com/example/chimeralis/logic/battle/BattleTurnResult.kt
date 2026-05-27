package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.ChimeraSpecies

/** Stores battle turn result data. */
data class BattleTurnResult(
    val log: List<String>,
    val animations: List<BattleMoveAnimation>
)

/** Stores battle move animation data. */
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

/** Stores battle stats snapshot data. */
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

/** Stores battle move feedback data. */
data class BattleMoveFeedback(
    val side: BattleSide,
    val type: BattleMoveFeedbackType
)

/** Lists the battle move feedback type values. */
enum class BattleMoveFeedbackType {
    Damage,
    Faint,
    StatChange
}

/** Lists the battle side values. */
enum class BattleSide {
    Player,
    Enemy
}

/** Lists the battle animation kind values. */
enum class BattleAnimationKind {
    Move,
    Capture,
    Item
}
