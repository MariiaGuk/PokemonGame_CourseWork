package com.example.chimeralis.ui.screens.battle.presentation.model

/** Stores a move-learning prompt and its possible replacement slots. */
internal data class BattleMoveLearningPresentation(
    val message: String,
    val replacementMoves: List<BattleMoveReplacementOptionPresentation?>
)
