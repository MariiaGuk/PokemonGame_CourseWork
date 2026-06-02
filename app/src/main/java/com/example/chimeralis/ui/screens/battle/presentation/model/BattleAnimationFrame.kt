package com.example.chimeralis.ui.screens.battle.presentation.model

import com.example.chimeralis.logic.battle.model.BattleMoveFeedback

/** Stores battle animation frame data. */
internal data class BattleAnimationFrame(
    val imageRes: Int,
    val durationMillis: Long,
    val feedbacks: List<BattleMoveFeedback> = emptyList()
)
