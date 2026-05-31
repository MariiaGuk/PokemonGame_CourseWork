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
import com.example.chimeralis.logic.battle.ChimeraEvolutionEvent
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.BattleMoveAnimation
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import kotlin.math.roundToInt

/** Represents the battle ui state. */
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
    var activeEvolutionEvent by mutableStateOf<ChimeraEvolutionEvent?>(null)
    var pendingEvolutionEvents by mutableStateOf<List<ChimeraEvolutionEvent>>(emptyList())
    var revealedEnemyDefeatCount by mutableIntStateOf(0)
    var uiVersion by mutableIntStateOf(0)
    private var lastEnemyDefeatRevealKey: String? = null

    val refreshKey: Int get() = uiVersion
    val currentBattleMessage: String get() = battleLogMessages.getOrElse(battleLogIndex) { "" }
    val isMoveAnimationPlaying: Boolean get() = activeMoveAnimation != null
    val isBattleFeedbackPlaying: Boolean get() = activeBattleFeedbacks.isNotEmpty()
    val isBattleInputLocked: Boolean get() = isBattleIntroLocked || isBattleExitPending
    val activeLogAnimation: BattleMoveAnimation?
        get() = battleLogAnimations[battleLogIndex].takeIf { panelMode == BattlePanelMode.Log }

    /** Unlocks player input after the opening battle intro delay. */
    fun unlockBattleIntro() {
        isBattleIntroLocked = false
    }

    /** Handles show battle log behavior. */
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

    /** Changes the active command panel. */
    fun openPanel(mode: BattlePanelMode) {
        panelMode = mode
    }

    /** Selects a regular item and asks the player for a target chimera. */
    fun selectBattleItem(item: Item) {
        selectedBattleItem = item
        panelMode = BattlePanelMode.ItemTarget
    }

    /** Uses the currently selected item on a chosen chimera. */
    fun useSelectedBattleItemOn(chimera: Chimera) {
        selectedBattleItem?.let { item ->
            performBattleAction(BattleAction.UseItem(item, chimera))
        }
    }

    /** Returns from nested battle panels to the correct parent panel. */
    fun backToActionSelection() {
        if (panelMode == BattlePanelMode.ItemTarget) {
            selectedBattleItem = null
            panelMode = BattlePanelMode.Bag
        } else {
            panelMode = BattlePanelMode.Actions
        }
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

    /** Handles show battle result behavior. */
    fun showBattleResult(
        log: List<String>,
        animations: List<BattleMoveAnimation>
    ) {
        showBattleLog(log, animations)
        uiVersion++
    }

    /** Starts displaying one active battle animation. */
    fun beginActiveAnimation(animation: BattleMoveAnimation) {
        activeMoveAnimation = animation
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
    }

    /** Clears the current animation frame state. */
    fun clearActiveAnimation() {
        activeMoveAnimation = null
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
    }

    /** Clears transient feedback state. */
    fun clearBattleFeedback() {
        activeBattleFeedbacks = emptyList()
        battleFeedbackFrameIndex = 0
    }

    /** Clears animation and feedback state after playback finishes. */
    fun finishAnimationPlayback() {
        clearActiveAnimation()
        clearBattleFeedback()
    }

    /** Handles apply animation visual state behavior. */
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

    /** Starts capture animation state. */
    fun beginCaptureAnimation(animation: BattleMoveAnimation) {
        beginActiveAnimation(animation)
        captureResultAnimation = animation
        isCaptureResultRevealed = false
        if (!animation.captureSucceeded) {
            isEnemyCapturedHidden = false
        }
    }

    /** Updates progress-dependent capture animation state. */
    fun updateCaptureProgress(progress: Float) {
        activeCaptureProgress = progress.coerceIn(0f, 1f)
        activeMoveFrameIndex = (activeCaptureProgress * 100f).roundToInt()
        if (activeMoveAnimation?.captureSucceeded == true &&
            activeCaptureProgress >= CaptureAbsorbEndProgress
        ) {
            isEnemyCapturedHidden = true
        }
    }

    /** Finishes capture animation state. */
    fun finishCaptureAnimation() {
        if (activeMoveAnimation?.captureSucceeded == true) {
            isEnemyCapturedHidden = true
        }
        finishAnimationPlayback()
    }

    /** Shows one animation frame without feedback. */
    fun showAnimationFrame(frameIndex: Int) {
        activeMoveFrameIndex = frameIndex
    }

    /** Shows feedback for one animation frame. */
    fun showFrameFeedback(frameIndex: Int, feedbacks: List<BattleFeedback>) {
        activeMoveFrameIndex = frameIndex
        activeBattleFeedbacks = feedbacks
    }

    /** Updates the active feedback frame index. */
    fun updateFeedbackFrame(frameIndex: Int) {
        battleFeedbackFrameIndex = frameIndex
    }

    /** Hides fighters that fainted during the active frame. */
    fun hideFaintedFeedbackSides(feedbacks: List<BattleFeedback>) {
        val faintedSides = feedbacks
            .filter { feedback -> feedback.type == BattleFeedbackType.Faint }
            .map { feedback -> feedback.side }
            .toSet()
        if (faintedSides.isNotEmpty()) {
            hiddenFaintedSides = hiddenFaintedSides + faintedSides
        }
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
        pendingEvolutionEvents = pendingEvolutionEvents + result.evolutions

        showBattleResult(result.log, result.animations)
    }

    /** Handles set visual player progress behavior. */
    fun setVisualPlayerProgress(level: Int, exp: Int) {
        visualPlayerLevel = level
        visualPlayerExp = exp
        uiVersion++
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
        return isTrainerBattle &&
                currentBattleMessage.startsWith("Enemy ") &&
                " has 0/" in currentBattleMessage &&
                currentBattleMessage.endsWith(" HP.")
    }

    /** Handles reveal enemy defeat for current message behavior. */
    fun revealEnemyDefeatForCurrentMessage() {
        val key = "$battleLogIndex:$currentBattleMessage"
        if (lastEnemyDefeatRevealKey == key) return

        lastEnemyDefeatRevealKey = key
        revealedEnemyDefeatCount = (revealedEnemyDefeatCount + 1).coerceAtMost(6)
    }

    /** Removes hidden-faint flags when fighters are visible again after switches or healing. */
    fun showRecoveredFighters(playerChimera: Chimera, enemyChimera: Chimera) {
        hiddenFaintedSides = hiddenFaintedSides
            .let { sides -> if (playerChimera.stats.currentHp > 0) sides - BattleSide.Player else sides }
            .let { sides -> if (enemyChimera.stats.currentHp > 0) sides - BattleSide.Enemy else sides }
    }

    /** Shows one pending evolution event. */
    fun showEvolution(event: ChimeraEvolutionEvent) {
        activeEvolutionEvent = event
    }

    /** Clears post-battle evolution overlay state. */
    fun clearEvolutionState() {
        activeEvolutionEvent = null
        pendingEvolutionEvents = emptyList()
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
