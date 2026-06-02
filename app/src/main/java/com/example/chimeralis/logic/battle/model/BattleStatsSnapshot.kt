package com.example.chimeralis.logic.battle.model

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
