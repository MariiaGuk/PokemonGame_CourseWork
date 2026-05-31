package com.example.chimeralis.ui.screens.battle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.BattleMoveAnimation
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.battle.BattleStatsSnapshot
import com.example.chimeralis.logic.battle.toBattleStatsSnapshot
import com.example.chimeralis.logic.chimeras.Chimera

/** Stores visible fighter stats that may temporarily differ during animations. */
internal class BattleVisualUiState(
    playerChimera: Chimera,
    enemyChimera: Chimera
) {
    var visualPlayerStats by mutableStateOf(playerChimera.stats.toBattleStatsSnapshot())
        private set
    var visualWildStats by mutableStateOf(enemyChimera.stats.toBattleStatsSnapshot())
        private set
    var visualPlayerLevel by mutableIntStateOf(playerChimera.level)
        private set
    var visualPlayerExp by mutableIntStateOf(playerChimera.exp)
        private set
    var uiVersion by mutableIntStateOf(0)
        private set

    val refreshKey: Int get() = uiVersion

    /** Applies post-animation stats to the visible fighters. */
    fun applyAnimationVisualState(animation: BattleMoveAnimation) {
        val userAfter = animation.userAfter
        val targetAfter = animation.targetAfter

        if (userAfter != null) {
            when (animation.side) {
                BattleSide.Player -> visualPlayerStats = userAfter
                BattleSide.Enemy -> visualWildStats = userAfter
            }
        }

        if (targetAfter != null) {
            when (animation.side) {
                BattleSide.Player -> visualWildStats = targetAfter
                BattleSide.Enemy -> visualPlayerStats = targetAfter
            }
        }
    }

    /** Sets the visible player snapshot before action animations start. */
    fun setPlayerSnapshot(
        stats: BattleStatsSnapshot,
        level: Int,
        exp: Int
    ) {
        visualPlayerStats = stats
        visualPlayerLevel = level
        visualPlayerExp = exp
    }

    /** Sets the visible enemy stats before or after battle flow changes. */
    fun setWildStats(stats: BattleStatsSnapshot) {
        visualWildStats = stats
    }

    /** Handles set visual player progress behavior. */
    fun setPlayerProgress(level: Int, exp: Int) {
        visualPlayerLevel = level
        visualPlayerExp = exp
        refresh()
    }

    /** Increments the version used by status widgets to refresh their animation state. */
    fun refresh() {
        uiVersion++
    }
}
