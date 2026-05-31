package com.example.chimeralis.ui.screens.battle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Tracks trainer battle progress that is revealed through log messages. */
internal class BattleTrainerProgressUiState {
    var revealedEnemyDefeatCount by mutableIntStateOf(0)
        private set
    private var lastEnemyDefeatRevealKey: String? = null

    /** Returns true when the current log message should reveal one defeated trainer chimera. */
    fun shouldRevealEnemyDefeat(
        isTrainerBattle: Boolean,
        message: String
    ): Boolean {
        return isTrainerBattle &&
                message.startsWith("Enemy ") &&
                " has 0/" in message &&
                message.endsWith(" HP.")
    }

    /** Handles reveal enemy defeat for current message behavior. */
    fun revealEnemyDefeat(
        messageIndex: Int,
        message: String
    ) {
        val key = "$messageIndex:$message"
        if (lastEnemyDefeatRevealKey == key) return

        lastEnemyDefeatRevealKey = key
        revealedEnemyDefeatCount = (revealedEnemyDefeatCount + 1).coerceAtMost(6)
    }
}
