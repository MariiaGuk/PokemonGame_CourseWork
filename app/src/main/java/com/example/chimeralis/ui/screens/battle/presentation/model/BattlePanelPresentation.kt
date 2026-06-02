package com.example.chimeralis.ui.screens.battle.presentation.model

/** Stores battle panel data prepared for UI rendering. */
internal data class BattlePanelPresentation(
    val moves: List<BattleMoveOptionPresentation>,
    val moveLearning: BattleMoveLearningPresentation?,
    val inventoryItems: List<BattleItemOptionPresentation>,
    val teamSelection: BattleTeamPresentation,
    val itemTargetSelection: BattleTeamPresentation
)
