package com.example.chimeralis.logic.battle.resolution

/** Describes battle-state changes caused by the player's active chimera fainting. */
data class PlayerFaintResolution(
    val isBattleActive: Boolean,
    val isWaitingForPlayerSwitch: Boolean,
    val message: String
)
