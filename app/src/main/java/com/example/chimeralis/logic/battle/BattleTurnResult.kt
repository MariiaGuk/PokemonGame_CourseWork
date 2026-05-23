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
    val feedbacks: List<BattleMoveFeedback> = emptyList()
)

data class BattleMoveFeedback(
    val side: BattleSide,
    val type: BattleMoveFeedbackType
)

enum class BattleMoveFeedbackType {
    Damage,
    StatChange
}

enum class BattleSide {
    Player,
    Enemy
}
