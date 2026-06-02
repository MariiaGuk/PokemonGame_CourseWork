package com.example.chimeralis.ui.screens.battle.presentation.model

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
