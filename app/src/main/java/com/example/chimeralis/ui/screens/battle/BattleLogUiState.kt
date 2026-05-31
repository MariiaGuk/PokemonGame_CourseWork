package com.example.chimeralis.ui.screens.battle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.BattleAnimationKind
import com.example.chimeralis.logic.battle.BattleMoveAnimation

/** Stores battle log messages and their animation mapping. */
internal class BattleLogUiState(openingMessage: String) {
    var battleLogMessages by mutableStateOf(listOf(openingMessage))
        private set
    var battleLogIndex by mutableIntStateOf(0)
        private set
    var battleLogAnimations by mutableStateOf<Map<Int, BattleMoveAnimation>>(emptyMap())
        private set

    val currentBattleMessage: String get() = battleLogMessages.getOrElse(battleLogIndex) { "" }
    val hasNextMessage: Boolean get() = battleLogIndex < battleLogMessages.lastIndex

    /** Shows a new battle log sequence from the first message. */
    fun show(
        messages: List<String>,
        animations: List<BattleMoveAnimation>
    ) {
        battleLogMessages = messages.ifEmpty { listOf("Nothing happened.") }
        battleLogAnimations = mapAnimationsToLogMessages(messages, animations)
        battleLogIndex = 0
    }

    /** Moves the log cursor to the next message. */
    fun advance() {
        if (hasNextMessage) {
            battleLogIndex++
        }
    }

    /** Returns the animation that should play for the current log message. */
    fun activeAnimation(panelMode: BattlePanelMode): BattleMoveAnimation? {
        return battleLogAnimations[battleLogIndex].takeIf { panelMode == BattlePanelMode.Log }
    }

    /** Returns the current capture animation when the current message belongs to capture flow. */
    fun currentCaptureAnimation(): BattleMoveAnimation? {
        return battleLogAnimations[battleLogIndex]
            ?.takeIf { animation -> animation.kind == BattleAnimationKind.Capture }
    }
}
