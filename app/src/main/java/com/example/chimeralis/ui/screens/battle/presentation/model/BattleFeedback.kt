package com.example.chimeralis.ui.screens.battle.presentation.model

import com.example.chimeralis.logic.battle.model.BattleSide

/** Stores battle feedback data. */
internal data class BattleFeedback(
    val side: BattleSide,
    val type: BattleFeedbackType
)
