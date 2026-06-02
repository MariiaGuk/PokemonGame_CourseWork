package com.example.chimeralis.ui.screens.battle.presentation.model

import com.example.chimeralis.logic.chimeras.moves.Move

/** Stores one battle move option with its domain payload. */
internal data class BattleMoveOptionPresentation(
    val move: Move,
    val label: String,
    val enabled: Boolean
)
