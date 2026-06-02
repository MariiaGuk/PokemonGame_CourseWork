package com.example.chimeralis.ui.screens.battle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.logic.battle.model.BattleSide
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.ui.screens.battle.CaptureAbsorbEndProgress
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedback
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedbackType
import kotlin.math.roundToInt

/** Stores transient battle animation and feedback state. */
internal class BattleAnimationUiState {
    var activeMoveAnimation by mutableStateOf<BattleMoveAnimation?>(null)
        private set
    var activeMoveFrameIndex by mutableIntStateOf(0)
        private set
    var activeCaptureProgress by mutableFloatStateOf(0f)
        private set
    var activeBattleFeedbacks by mutableStateOf<List<BattleFeedback>>(emptyList())
        private set
    var battleFeedbackFrameIndex by mutableIntStateOf(0)
        private set
    var hiddenFaintedSides by mutableStateOf<Set<BattleSide>>(emptySet())
        private set
    var captureResultAnimation by mutableStateOf<BattleMoveAnimation?>(null)
        private set
    var isCaptureResultRevealed by mutableStateOf(false)
        private set
    var isEnemyCapturedHidden by mutableStateOf(false)
        private set

    val isMoveAnimationPlaying: Boolean get() = activeMoveAnimation != null
    val isBattleFeedbackPlaying: Boolean get() = activeBattleFeedbacks.isNotEmpty()

    /**
     * Resets transient animation state before a new log sequence is shown.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun resetForNewLog() {
        clearActiveAnimation()
        clearBattleFeedback()
        clearCaptureResult()
    }

    /**
     * Starts displaying one active battle animation.
     *
     * @param animation The animation value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun beginActiveAnimation(animation: BattleMoveAnimation) {
        activeMoveAnimation = animation
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
    }

    /**
     * Clears the current animation frame state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearActiveAnimation() {
        activeMoveAnimation = null
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
    }

    /**
     * Clears transient feedback state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearBattleFeedback() {
        activeBattleFeedbacks = emptyList()
        battleFeedbackFrameIndex = 0
    }

    /**
     * Clears animation and feedback state after playback finishes.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun finishAnimationPlayback() {
        clearActiveAnimation()
        clearBattleFeedback()
    }

    /**
     * Starts capture animation state.
     *
     * @param animation The animation value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun beginCaptureAnimation(animation: BattleMoveAnimation) {
        beginActiveAnimation(animation)
        captureResultAnimation = animation
        isCaptureResultRevealed = false
        if (!animation.captureSucceeded) {
            isEnemyCapturedHidden = false
        }
    }

    /**
     * Updates progress-dependent capture animation state.
     *
     * @param progress The progress value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun updateCaptureProgress(progress: Float) {
        activeCaptureProgress = progress.coerceIn(0f, 1f)
        activeMoveFrameIndex = (activeCaptureProgress * 100f).roundToInt()
        if (activeMoveAnimation?.captureSucceeded == true &&
            activeCaptureProgress >= CaptureAbsorbEndProgress
        ) {
            isEnemyCapturedHidden = true
        }
    }

    /**
     * Finishes capture animation state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun finishCaptureAnimation() {
        if (activeMoveAnimation?.captureSucceeded == true) {
            isEnemyCapturedHidden = true
        }
        finishAnimationPlayback()
    }

    /**
     * Reveals the capture result after the throw animation message is shown.
     *
     * @param animation The animation value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun revealCaptureResult(animation: BattleMoveAnimation) {
        captureResultAnimation = animation
        isCaptureResultRevealed = true
    }

    /**
     * Clears capture result state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearCaptureResult() {
        captureResultAnimation = null
        isCaptureResultRevealed = false
    }

    /**
     * Clears a revealed failed-capture result once the log moves past it.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearFailedCaptureResultIfRevealed() {
        if (captureResultAnimation?.captureSucceeded == false && isCaptureResultRevealed) {
            clearCaptureResult()
        }
    }

    /**
     * Shows one animation frame without feedback.
     *
     * @param frameIndex Numeric value used by this operation: frame index.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showAnimationFrame(frameIndex: Int) {
        activeMoveFrameIndex = frameIndex
    }

    /**
     * Shows feedback for one animation frame.
     *
     * @param frameIndex Numeric value used by this operation: frame index.
     * @param feedbacks The feedbacks value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showFrameFeedback(frameIndex: Int, feedbacks: List<BattleFeedback>) {
        activeMoveFrameIndex = frameIndex
        activeBattleFeedbacks = feedbacks
    }

    /**
     * Updates the active feedback frame index.
     *
     * @param frameIndex Numeric value used by this operation: frame index.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun updateFeedbackFrame(frameIndex: Int) {
        battleFeedbackFrameIndex = frameIndex
    }

    /**
     * Hides fighters that fainted during the active frame.
     *
     * @param feedbacks The feedbacks value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun hideFaintedFeedbackSides(feedbacks: List<BattleFeedback>) {
        val faintedSides = feedbacks
            .filter { feedback -> feedback.type == BattleFeedbackType.Faint }
            .map { feedback -> feedback.side }
            .toSet()
        if (faintedSides.isNotEmpty()) {
            hiddenFaintedSides = hiddenFaintedSides + faintedSides
        }
    }

    /**
     * Removes hidden-faint flags when fighters are visible again after switches or healing.
     *
     * @param playerChimera The player chimera value used by this operation.
     * @param enemyChimera The enemy chimera value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showRecoveredFighters(playerChimera: Chimera, enemyChimera: Chimera) {
        hiddenFaintedSides = hiddenFaintedSides
            .let { sides -> if (playerChimera.stats.currentHp > 0) sides - BattleSide.Player else sides }
            .let { sides -> if (enemyChimera.stats.currentHp > 0) sides - BattleSide.Enemy else sides }
    }
}
