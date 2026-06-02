package com.example.chimeralis.ui.screens.battle.effects

import androidx.annotation.RawRes

/** Describes one sound that should be played for a battle event. */
internal data class BattleSoundEvent(
    @param:RawRes val soundResId: Int,
    val startsBattleResult: Boolean = false
)
