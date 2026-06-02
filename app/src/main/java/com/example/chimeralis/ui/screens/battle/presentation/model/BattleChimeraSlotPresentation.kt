package com.example.chimeralis.ui.screens.battle.presentation.model

import com.example.chimeralis.logic.chimeras.Chimera

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
