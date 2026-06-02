package com.example.chimeralis.ui.screens.battle.effects

import com.example.chimeralis.logic.battle.model.BattleAnimationKind
import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.R
import com.example.chimeralis.ui.screens.battle.BattleFeedbackFrameMillis
import com.example.chimeralis.ui.screens.battle.CaptureAnimationTickMillis
import com.example.chimeralis.ui.screens.battle.CaptureFailDurationMillis
import com.example.chimeralis.ui.screens.battle.CaptureSuccessDurationMillis
import com.example.chimeralis.ui.screens.battle.presentation.animationFrames
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedback
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedbackType
import com.example.chimeralis.ui.screens.battle.presentation.toBattleFeedbacks
import com.example.chimeralis.ui.screens.battle.state.BattleUiState
import kotlinx.coroutines.delay

/**
 * Plays the currently active log animation and updates UI state around its frames.
 *
 * @receiver The battle ui state receiver used by this operation.
 * @param animation The animation value used by this operation.
 * @param playSound The play sound value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
internal suspend fun BattleUiState.playLogAnimation(
    animation: BattleMoveAnimation,
    playSound: (Int) -> Unit
) {
    beginActiveAnimation(animation)

    when (animation.kind) {
        BattleAnimationKind.Move -> {
            applyAnimationVisualState(animation)
            playSound(R.raw.attack_sound)
            playMoveAnimationFrames(animation, playSound)
        }
        BattleAnimationKind.Item -> {
            applyAnimationVisualState(animation)
            delay(ItemAnimationHoldMillis)
            clearActiveAnimation()
        }
        BattleAnimationKind.Capture -> {
            playCaptureAnimation(animation)
        }
    }
}

/**
 * Plays capture progress and target visibility state.
 *
 * @receiver The battle ui state receiver used by this operation.
 * @param animation The animation value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
private suspend fun BattleUiState.playCaptureAnimation(animation: BattleMoveAnimation) {
    beginCaptureAnimation(animation)

    val durationMillis = if (animation.captureSucceeded) {
        CaptureSuccessDurationMillis
    } else {
        CaptureFailDurationMillis
    }
    val tickCount = (durationMillis / CaptureAnimationTickMillis).toInt().coerceAtLeast(1)

    repeat(tickCount + 1) { tick ->
        updateCaptureProgress((tick / tickCount.toFloat()).coerceIn(0f, 1f))
        delay(CaptureAnimationTickMillis)
    }

    delay(PostAnimationPauseMillis)
    finishCaptureAnimation()
}

/**
 * Plays move animation frames and feedback flashes.
 *
 * @receiver The battle ui state receiver used by this operation.
 * @param animation The animation value used by this operation.
 * @param playSound The play sound value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
private suspend fun BattleUiState.playMoveAnimationFrames(
    animation: BattleMoveAnimation,
    playSound: (Int) -> Unit
) {
    var playedFaintSound = false

    animation.animationFrames().forEachIndexed { frameIndex, frame ->
        showAnimationFrame(frameIndex)
        val feedbacks = frame.feedbacks.toBattleFeedbacks()

        if (feedbacks.isEmpty()) {
            delay(frame.durationMillis)
        } else {
            if (!playedFaintSound && feedbacks.any { feedback -> feedback.type == BattleFeedbackType.Faint }) {
                playSound(R.raw.chimera_faint)
                playedFaintSound = true
            }
            playFeedbackFrame(frameIndex, frame.durationMillis, feedbacks)
        }
    }

    delay(PostAnimationPauseMillis)
    finishAnimationPlayback()
}

/**
 * Plays one frame's feedback ticks and applies resulting hidden-faint state.
 *
 * @receiver The battle ui state receiver used by this operation.
 * @param frameIndex Numeric value used by this operation: frame index.
 * @param durationMillis The duration millis value used by this operation.
 * @param feedbacks The feedbacks value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
private suspend fun BattleUiState.playFeedbackFrame(
    frameIndex: Int,
    durationMillis: Long,
    feedbacks: List<BattleFeedback>
) {
    showFrameFeedback(frameIndex, feedbacks)

    val feedbackTicks = (durationMillis / BattleFeedbackFrameMillis)
        .toInt()
        .coerceAtLeast(1)
    repeat(feedbackTicks) { feedbackFrameIndex ->
        updateFeedbackFrame(feedbackFrameIndex)
        delay(BattleFeedbackFrameMillis)
    }

    val remainingDelay = durationMillis - feedbackTicks * BattleFeedbackFrameMillis
    if (remainingDelay > 0L) {
        delay(remainingDelay)
    }

    hideFaintedFeedbackSides(feedbacks)
    clearBattleFeedback()
}

private const val ItemAnimationHoldMillis = 220L
private const val PostAnimationPauseMillis = 90L
