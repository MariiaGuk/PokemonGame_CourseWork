package com.example.chimeralis.ui.screens.battle.presentation

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.items.Item

/** Stores battle fighter status data prepared for UI rendering. */
internal data class BattleFighterStatusPresentation(
    val name: String,
    val level: Int,
    val imageRes: Int,
    val currentHp: Int,
    val maxHp: Int,
    val currentExp: Int?,
    val expToNextLevel: Int?,
    val attackStage: Int,
    val defenceStage: Int,
    val speedStage: Int,
    val refreshKey: Int
)

/** Stores battle panel data prepared for UI rendering. */
internal data class BattlePanelPresentation(
    val moves: List<BattleMoveOptionPresentation>,
    val moveLearning: BattleMoveLearningPresentation?,
    val inventoryItems: List<BattleItemOptionPresentation>,
    val teamSelection: BattleTeamPresentation,
    val itemTargetSelection: BattleTeamPresentation
)

/** Stores one battle move option with its domain payload. */
internal data class BattleMoveOptionPresentation(
    val move: Move,
    val label: String,
    val enabled: Boolean
)

/** Stores one item option with its domain payload. */
internal data class BattleItemOptionPresentation(
    val item: Item,
    val label: String,
    val enabled: Boolean,
    val isCaptureItem: Boolean
)

/** Stores a move-learning prompt and its possible replacement slots. */
internal data class BattleMoveLearningPresentation(
    val message: String,
    val replacementMoves: List<BattleMoveReplacementOptionPresentation?>
)

/** Stores one move replacement option for move-learning UI. */
internal data class BattleMoveReplacementOptionPresentation(
    val index: Int,
    val label: String
)

/** Stores a fixed-size team slot grid prepared for battle panels. */
internal data class BattleTeamPresentation(
    val slots: List<BattleChimeraSlotPresentation?>
)

/** Stores one chimera selection slot with its domain payload. */
internal data class BattleChimeraSlotPresentation(
    val chimera: Chimera,
    val name: String,
    val levelLabel: String,
    val imageRes: Int,
    val hpRatio: Float,
    val hpText: String,
    val isActive: Boolean,
    val enabled: Boolean
)
