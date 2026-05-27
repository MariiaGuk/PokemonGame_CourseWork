package com.example.chimeralis.ui.screens.battle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleAnimationKind
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.BattleMoveAnimation
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.items.Item

internal class BattleUiState(
    private val battleManager: BattleManager,
    openingMessage: String
) {
    var panelMode by mutableStateOf(BattlePanelMode.Log)
    var battleLogMessages by mutableStateOf(listOf(openingMessage))
    var battleLogIndex by mutableIntStateOf(0)
    var battleLogAnimations by mutableStateOf<Map<Int, BattleMoveAnimation>>(emptyMap())
    var activeMoveAnimation by mutableStateOf<BattleMoveAnimation?>(null)
    var activeMoveFrameIndex by mutableIntStateOf(0)
    var activeCaptureProgress by mutableFloatStateOf(0f)
    var activeBattleFeedbacks by mutableStateOf<List<BattleFeedback>>(emptyList())
    var battleFeedbackFrameIndex by mutableIntStateOf(0)
    var hiddenFaintedSides by mutableStateOf<Set<BattleSide>>(emptySet())
    var captureResultAnimation by mutableStateOf<BattleMoveAnimation?>(null)
    var isCaptureResultRevealed by mutableStateOf(false)
    var isEnemyCapturedHidden by mutableStateOf(false)
    var visualPlayerStats by mutableStateOf(battleManager.playerChimera.stats.toBattleStatsSnapshot())
    var visualWildStats by mutableStateOf(battleManager.enemyChimera.stats.toBattleStatsSnapshot())
    var visualPlayerLevel by mutableIntStateOf(battleManager.playerChimera.level)
    var visualPlayerExp by mutableIntStateOf(battleManager.playerChimera.exp)
    var selectedBattleItem by mutableStateOf<Item?>(null)
    var isBattleIntroLocked by mutableStateOf(true)
    var isBattleExitPending by mutableStateOf(false)
    var revealedEnemyDefeatCount by mutableIntStateOf(0)
    var uiVersion by mutableIntStateOf(0)
    private var lastEnemyDefeatRevealKey: String? = null

    val refreshKey: Int get() = uiVersion
    val currentBattleMessage: String get() = battleLogMessages.getOrElse(battleLogIndex) { "" }
    val isMoveAnimationPlaying: Boolean get() = activeMoveAnimation != null
    val isBattleFeedbackPlaying: Boolean get() = activeBattleFeedbacks.isNotEmpty()
    val isBattleInputLocked: Boolean get() = isBattleIntroLocked || isBattleExitPending

    fun showBattleLog(
        messages: List<String>,
        animations: List<BattleMoveAnimation> = emptyList()
    ) {
        battleLogMessages = messages.ifEmpty { listOf("Nothing happened.") }
        battleLogAnimations = mapAnimationsToLogMessages(messages, animations)
        battleLogIndex = 0
        activeMoveAnimation = null
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
        activeBattleFeedbacks = emptyList()
        battleFeedbackFrameIndex = 0
        captureResultAnimation = null
        isCaptureResultRevealed = false
        selectedBattleItem = null
        panelMode = BattlePanelMode.Log
    }

    fun advanceBattleLog() {
        if (panelMode != BattlePanelMode.Log ||
            isMoveAnimationPlaying ||
            isBattleFeedbackPlaying ||
            isBattleInputLocked
        ) {
            return
        }

        val currentCaptureAnimation = battleLogAnimations[battleLogIndex]
            ?.takeIf { it.kind == BattleAnimationKind.Capture }

        if (battleLogIndex < battleLogMessages.lastIndex) {
            if (currentCaptureAnimation != null) {
                captureResultAnimation = currentCaptureAnimation
                isCaptureResultRevealed = true
            } else if (
                captureResultAnimation?.captureSucceeded == false &&
                isCaptureResultRevealed
            ) {
                captureResultAnimation = null
                isCaptureResultRevealed = false
            }
            battleLogIndex++
        } else if (battleManager.isWaitingForMoveLearning) {
            captureResultAnimation = null
            isCaptureResultRevealed = false
            panelMode = BattlePanelMode.MoveLearning
        } else if (battleManager.isBattleActive) {
            captureResultAnimation = null
            isCaptureResultRevealed = false
            battleManager.resolvePendingEnemySwitch()
            visualWildStats = battleManager.enemyChimera.stats.toBattleStatsSnapshot()
            panelMode = if (battleManager.isWaitingForPlayerSwitch) {
                BattlePanelMode.Team
            } else {
                BattlePanelMode.Actions
            }
        } else {
            isBattleExitPending = true
        }
    }

    fun showBattleResult(
        log: List<String>,
        animations: List<BattleMoveAnimation>
    ) {
        showBattleLog(log, animations)
        uiVersion++
    }

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

    fun performBattleAction(action: BattleAction) {
        val playerChimera = battleManager.playerChimera
        val wildChimera = battleManager.enemyChimera
        val playerBefore = playerChimera.stats.toBattleStatsSnapshot()
        val wildBefore = wildChimera.stats.toBattleStatsSnapshot()
        val playerLevelBefore = playerChimera.level
        val playerExpBefore = playerChimera.exp
        val result = battleManager.performTurnWithAnimations(action)

        visualPlayerStats = when (action) {
            is BattleAction.SwitchChimera -> result.animations
                .firstOrNull { it.side == BattleSide.Enemy }
                ?.targetBefore
                ?: battleManager.playerChimera.stats.toBattleStatsSnapshot()
            else -> playerBefore
        }
        visualPlayerLevel = when (action) {
            is BattleAction.SwitchChimera -> battleManager.playerChimera.level
            else -> playerLevelBefore
        }
        visualPlayerExp = when (action) {
            is BattleAction.SwitchChimera -> battleManager.playerChimera.exp
            else -> playerExpBefore
        }
        visualWildStats = wildBefore

        showBattleResult(result.log, result.animations)
    }

    fun setVisualPlayerProgress(level: Int, exp: Int) {
        visualPlayerLevel = level
        visualPlayerExp = exp
        uiVersion++
    }

    fun revealEnemyDefeatForCurrentMessage() {
        val key = "$battleLogIndex:$currentBattleMessage"
        if (lastEnemyDefeatRevealKey == key) return

        lastEnemyDefeatRevealKey = key
        revealedEnemyDefeatCount = (revealedEnemyDefeatCount + 1).coerceAtMost(6)
    }
}

@Composable
internal fun rememberBattleUiState(
    battleManager: BattleManager,
    openingMessage: String
): BattleUiState {
    return remember(battleManager, openingMessage) { BattleUiState(battleManager, openingMessage) }
}
