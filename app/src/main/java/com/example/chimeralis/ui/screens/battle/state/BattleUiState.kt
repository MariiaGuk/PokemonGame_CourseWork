package com.example.chimeralis.ui.screens.battle.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.BattleMoveAnimation
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.battle.ChimeraEvolutionEvent
import com.example.chimeralis.logic.battle.reporting.toBattleStatsSnapshot
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.screens.battle.presentation.BattleFeedback
import com.example.chimeralis.ui.screens.battle.presentation.BattlePanelMode

/** Represents the battle ui state. */
internal class BattleUiState(
    private val battleManager: BattleManager,
    openingMessage: String
) {
    private val commands = BattleCommandUiState()
    private val log = BattleLogUiState(openingMessage)
    private val animation = BattleAnimationUiState()
    private val visual = BattleVisualUiState(
        playerChimera = battleManager.playerChimera,
        enemyChimera = battleManager.enemyChimera
    )
    private val evolutions = BattleEvolutionUiState()
    private val trainerProgress = BattleTrainerProgressUiState()

    val panelMode: BattlePanelMode get() = commands.panelMode
    val battleLogMessages: List<String> get() = log.battleLogMessages
    val battleLogIndex: Int get() = log.battleLogIndex
    val battleLogAnimations: Map<Int, BattleMoveAnimation> get() = log.battleLogAnimations
    val activeMoveAnimation: BattleMoveAnimation? get() = animation.activeMoveAnimation
    val activeMoveFrameIndex: Int get() = animation.activeMoveFrameIndex
    val activeCaptureProgress: Float get() = animation.activeCaptureProgress
    val activeBattleFeedbacks: List<BattleFeedback> get() = animation.activeBattleFeedbacks
    val battleFeedbackFrameIndex: Int get() = animation.battleFeedbackFrameIndex
    val hiddenFaintedSides: Set<BattleSide> get() = animation.hiddenFaintedSides
    val captureResultAnimation: BattleMoveAnimation? get() = animation.captureResultAnimation
    val isCaptureResultRevealed: Boolean get() = animation.isCaptureResultRevealed
    val isEnemyCapturedHidden: Boolean get() = animation.isEnemyCapturedHidden
    val visualPlayerStats get() = visual.visualPlayerStats
    val visualWildStats get() = visual.visualWildStats
    val visualPlayerLevel: Int get() = visual.visualPlayerLevel
    val visualPlayerExp: Int get() = visual.visualPlayerExp
    val selectedBattleItem: Item? get() = commands.selectedBattleItem
    val isBattleIntroLocked: Boolean get() = commands.isBattleIntroLocked
    val isBattleExitPending: Boolean get() = commands.isBattleExitPending
    val activeEvolutionEvent: ChimeraEvolutionEvent? get() = evolutions.activeEvolutionEvent
    val pendingEvolutionEvents: List<ChimeraEvolutionEvent> get() = evolutions.pendingEvolutionEvents
    val revealedEnemyDefeatCount: Int get() = trainerProgress.revealedEnemyDefeatCount
    val uiVersion: Int get() = visual.uiVersion

    val refreshKey: Int get() = visual.refreshKey
    val currentBattleMessage: String get() = log.currentBattleMessage
    val isMoveAnimationPlaying: Boolean get() = animation.isMoveAnimationPlaying
    val isBattleFeedbackPlaying: Boolean get() = animation.isBattleFeedbackPlaying
    val isBattleInputLocked: Boolean get() = commands.isBattleInputLocked
    val activeLogAnimation: BattleMoveAnimation? get() = log.activeAnimation(panelMode)

    /** Unlocks player input after the opening battle intro delay. */
    fun unlockBattleIntro() {
        commands.unlockBattleIntro()
    }

    /** Handles show battle log behavior. */
    fun showBattleLog(
        messages: List<String>,
        animations: List<BattleMoveAnimation> = emptyList()
    ) {
        log.show(messages, animations)
        animation.resetForNewLog()
        commands.showLog()
    }

    /** Changes the active command panel. */
    fun openPanel(mode: BattlePanelMode) {
        commands.openPanel(mode)
    }

    /** Selects a regular item and asks the player for a target chimera. */
    fun selectBattleItem(item: Item) {
        commands.selectBattleItem(item)
    }

    /** Uses the currently selected item on a chosen chimera. */
    fun useSelectedBattleItemOn(chimera: Chimera) {
        selectedBattleItem?.let { item ->
            performBattleAction(BattleAction.UseItem(item, chimera))
        }
    }

    /** Returns from nested battle panels to the correct parent panel. */
    fun backToActionSelection() {
        commands.backToActionSelection()
    }

    /** Shows the trainer-battle capture rejection message. */
    fun showTrainerCaptureBlocked() {
        showBattleResult(
            log = listOf("You cannot catch another trainer's chimera."),
            animations = emptyList()
        )
    }

    /** Resolves a move-learning decision and shows its resulting messages. */
    fun resolvePendingMoveLearning(replaceIndex: Int?) {
        showBattleResult(
            log = battleManager.resolvePendingMoveLearning(replaceIndex),
            animations = emptyList()
        )
    }

    /** Handles advance battle log behavior. */
    fun advanceBattleLog() {
        if (panelMode != BattlePanelMode.Log ||
            isMoveAnimationPlaying ||
            isBattleFeedbackPlaying ||
            isBattleInputLocked
        ) {
            return
        }

        val currentCaptureAnimation = log.currentCaptureAnimation()

        if (log.hasNextMessage) {
            if (currentCaptureAnimation != null) {
                animation.revealCaptureResult(currentCaptureAnimation)
            } else {
                animation.clearFailedCaptureResultIfRevealed()
            }
            log.advance()
        } else if (battleManager.isWaitingForMoveLearning) {
            animation.clearCaptureResult()
            commands.openPanel(BattlePanelMode.MoveLearning)
        } else if (battleManager.isBattleActive) {
            animation.clearCaptureResult()
            battleManager.resolvePendingEnemySwitch()
            visual.setWildStats(battleManager.enemyChimera.stats.toBattleStatsSnapshot())
            commands.openPanel(
                if (battleManager.isWaitingForPlayerSwitch) {
                    BattlePanelMode.Team
                } else {
                    BattlePanelMode.Actions
                }
            )
        } else {
            commands.requestBattleExit()
        }
    }

    /** Handles show battle result behavior. */
    fun showBattleResult(
        log: List<String>,
        animations: List<BattleMoveAnimation>
    ) {
        showBattleLog(log, animations)
        visual.refresh()
    }

    /** Starts displaying one active battle animation. */
    fun beginActiveAnimation(animation: BattleMoveAnimation) {
        this.animation.beginActiveAnimation(animation)
    }

    /** Clears the current animation frame state. */
    fun clearActiveAnimation() {
        animation.clearActiveAnimation()
    }

    /** Clears transient feedback state. */
    fun clearBattleFeedback() {
        animation.clearBattleFeedback()
    }

    /** Clears animation and feedback state after playback finishes. */
    fun finishAnimationPlayback() {
        animation.finishAnimationPlayback()
    }

    /** Handles apply animation visual state behavior. */
    fun applyAnimationVisualState(animation: BattleMoveAnimation) {
        visual.applyAnimationVisualState(animation)
    }

    /** Starts capture animation state. */
    fun beginCaptureAnimation(animation: BattleMoveAnimation) {
        this.animation.beginCaptureAnimation(animation)
    }

    /** Updates progress-dependent capture animation state. */
    fun updateCaptureProgress(progress: Float) {
        animation.updateCaptureProgress(progress)
    }

    /** Finishes capture animation state. */
    fun finishCaptureAnimation() {
        animation.finishCaptureAnimation()
    }

    /** Shows one animation frame without feedback. */
    fun showAnimationFrame(frameIndex: Int) {
        animation.showAnimationFrame(frameIndex)
    }

    /** Shows feedback for one animation frame. */
    fun showFrameFeedback(frameIndex: Int, feedbacks: List<BattleFeedback>) {
        animation.showFrameFeedback(frameIndex, feedbacks)
    }

    /** Updates the active feedback frame index. */
    fun updateFeedbackFrame(frameIndex: Int) {
        animation.updateFeedbackFrame(frameIndex)
    }

    /** Hides fighters that fainted during the active frame. */
    fun hideFaintedFeedbackSides(feedbacks: List<BattleFeedback>) {
        animation.hideFaintedFeedbackSides(feedbacks)
    }

    /** Handles perform battle action behavior. */
    fun performBattleAction(action: BattleAction) {
        val playerChimera = battleManager.playerChimera
        val wildChimera = battleManager.enemyChimera
        val playerBefore = playerChimera.stats.toBattleStatsSnapshot()
        val wildBefore = wildChimera.stats.toBattleStatsSnapshot()
        val playerLevelBefore = playerChimera.level
        val playerExpBefore = playerChimera.exp
        val result = battleManager.performTurnWithAnimations(action)

        val playerStats = when (action) {
            is BattleAction.SwitchChimera -> result.animations
                .firstOrNull { it.side == BattleSide.Enemy }
                ?.targetBefore
                ?: battleManager.playerChimera.stats.toBattleStatsSnapshot()
            else -> playerBefore
        }
        val playerLevel = when (action) {
            is BattleAction.SwitchChimera -> battleManager.playerChimera.level
            else -> playerLevelBefore
        }
        val playerExp = when (action) {
            is BattleAction.SwitchChimera -> battleManager.playerChimera.exp
            else -> playerExpBefore
        }

        visual.setPlayerSnapshot(
            stats = playerStats,
            level = playerLevel,
            exp = playerExp
        )
        visual.setWildStats(wildBefore)
        evolutions.addPending(result.evolutions)

        showBattleResult(result.log, result.animations)
    }

    /** Handles set visual player progress behavior. */
    fun setVisualPlayerProgress(level: Int, exp: Int) {
        visual.setPlayerProgress(level, exp)
    }

    /** Updates the visible player EXP state when the current log message reports progress. */
    fun syncPlayerProgressForCurrentMessage(chimera: Chimera) {
        val message = currentBattleMessage
        val shouldSyncProgress =
            (message.contains(" gained ") && message.endsWith(" EXP.")) ||
                    " grew to Lv." in message

        if (shouldSyncProgress && message.startsWith("${chimera.name} ")) {
            setVisualPlayerProgress(chimera.level, chimera.exp)
        }
    }

    /** Returns true when the current log message is a level-up message. */
    fun isCurrentMessageLevelUp(): Boolean {
        return " grew to Lv." in currentBattleMessage
    }

    /** Returns true when the current log message should reveal one defeated trainer chimera. */
    fun shouldRevealEnemyDefeatForCurrentMessage(isTrainerBattle: Boolean): Boolean {
        return trainerProgress.shouldRevealEnemyDefeat(
            isTrainerBattle = isTrainerBattle,
            message = currentBattleMessage
        )
    }

    /** Handles reveal enemy defeat for current message behavior. */
    fun revealEnemyDefeatForCurrentMessage() {
        trainerProgress.revealEnemyDefeat(
            messageIndex = battleLogIndex,
            message = currentBattleMessage
        )
    }

    /** Removes hidden-faint flags when fighters are visible again after switches or healing. */
    fun showRecoveredFighters(playerChimera: Chimera, enemyChimera: Chimera) {
        animation.showRecoveredFighters(playerChimera, enemyChimera)
    }

    /** Shows one pending evolution event. */
    fun showEvolution(event: ChimeraEvolutionEvent) {
        evolutions.showEvolution(event)
    }

    /** Clears post-battle evolution overlay state. */
    fun clearEvolutionState() {
        evolutions.clearEvolutionState()
    }
}

/** Remembers the remember battle ui state state. */
@Composable
internal fun rememberBattleUiState(
    battleManager: BattleManager,
    openingMessage: String
): BattleUiState {
    return remember(battleManager, openingMessage) { BattleUiState(battleManager, openingMessage) }
}
