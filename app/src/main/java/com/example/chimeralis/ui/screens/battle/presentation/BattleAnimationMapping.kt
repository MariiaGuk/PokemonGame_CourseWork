package com.example.chimeralis.ui.screens.battle.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.example.chimeralis.logic.battle.model.BattleAnimationKind
import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.logic.battle.model.BattleMoveFeedback
import com.example.chimeralis.logic.battle.model.BattleMoveFeedbackType
import com.example.chimeralis.logic.battle.model.BattleSide
import com.example.chimeralis.ui.screens.battle.BattleMoveFrameMillis
import com.example.chimeralis.ui.screens.battle.CaptureAbsorbEndProgress
import com.example.chimeralis.ui.screens.battle.CaptureAnimationTickMillis
import com.example.chimeralis.ui.screens.battle.CaptureThrowEndProgress
import com.example.chimeralis.ui.screens.battle.IdleBattleMoveFrameMillis
import com.example.chimeralis.ui.screens.battle.SingleActionBattleMoveFrameMillis
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleAnimationFrame
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedback
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFeedbackType
import com.example.chimeralis.ui.screens.chimera.battleMoveFrames

/** Converts data into battle feedbacks. */
internal fun List<BattleMoveFeedback>.toBattleFeedbacks(): List<BattleFeedback> {
    return map { feedback ->
        BattleFeedback(
            side = feedback.side,
            type = when (feedback.type) {
                BattleMoveFeedbackType.Damage -> BattleFeedbackType.Damage
                BattleMoveFeedbackType.Faint -> BattleFeedbackType.Faint
                BattleMoveFeedbackType.StatChange -> BattleFeedbackType.StatChange
            }
        )
    }
}

/** Handles shake offset behavior. */
internal fun BattleFeedback?.shakeOffset(frameIndex: Int): Dp {
    if (this?.type != BattleFeedbackType.Damage && this?.type != BattleFeedbackType.Faint) return 0.dp

    return when (frameIndex % 4) {
        0 -> (-7).dp
        1 -> 7.dp
        2 -> (-4).dp
        else -> 4.dp
    }
}

/** Handles faint drop offset behavior. */
internal fun BattleFeedback?.faintDropOffset(frameIndex: Int): Dp {
    if (this?.type != BattleFeedbackType.Faint) return 0.dp

    return (34f * faintProgress(frameIndex)).dp
}

/** Handles tint color behavior. */
internal fun BattleFeedback?.tintColor(): Color? {
    return when (this?.type) {
        BattleFeedbackType.Damage -> Color(0xFFFF3535).copy(alpha = 0.42f)
        BattleFeedbackType.Faint -> Color(0xFFFF3535).copy(alpha = 0.48f)
        BattleFeedbackType.StatChange -> Color(0xFF49A7FF).copy(alpha = 0.42f)
        null -> null
    }
}

/** Handles fighter alpha behavior. */
internal fun fighterAlpha(
    currentHp: Int,
    hasPendingFaint: Boolean,
    isHiddenAfterFaint: Boolean,
    activeFeedback: BattleFeedback?,
    frameIndex: Int
): Float {
    if (activeFeedback?.type == BattleFeedbackType.Faint) {
        return (1f - faintProgress(frameIndex)).coerceIn(0f, 1f)
    }

    if (isHiddenAfterFaint || (currentHp <= 0 && !hasPendingFaint)) {
        return 0f
    }

    return 1f
}

/** Handles faint progress behavior. */
internal fun faintProgress(frameIndex: Int): Float {
    return (frameIndex / 6f).coerceIn(0f, 1f)
}

/** Checks whether faint feedback exists. */
internal fun BattleMoveAnimation?.hasFaintFeedback(side: BattleSide): Boolean {
    return this?.feedbacks?.any {
        it.side == side && it.type == BattleMoveFeedbackType.Faint
    } == true
}

/** Handles capture target alpha behavior. */
internal fun captureTargetAlpha(animation: BattleMoveAnimation?, progress: Float): Float {
    if (animation == null) return 1f

    val clampedProgress = progress.coerceIn(0f, 1f)
    return when {
        clampedProgress < CaptureThrowEndProgress -> 1f
        clampedProgress < CaptureAbsorbEndProgress ->
            (1f - ((clampedProgress - CaptureThrowEndProgress) /
                    (CaptureAbsorbEndProgress - CaptureThrowEndProgress))).coerceIn(0f, 1f)
        else -> 0f
    }
}

/** Handles map animations to log messages behavior. */
internal fun mapAnimationsToLogMessages(
    messages: List<String>,
    animations: List<BattleMoveAnimation>
): Map<Int, BattleMoveAnimation> {
    val mappedAnimations = mutableMapOf<Int, BattleMoveAnimation>()
    var searchStart = 0

    animations.forEach { animation ->
        val message = animation.message()
        val messageIndex = messages
            .withIndex()
            .drop(searchStart)
            .firstOrNull { (_, value) -> value == message }
            ?.index

        if (messageIndex != null) {
            mappedAnimations[messageIndex] = animation
            searchStart = messageIndex + 1
        }
    }

    return mappedAnimations
}

/** Handles message behavior. */
internal fun BattleMoveAnimation.message(): String {
    when (kind) {
        BattleAnimationKind.Capture -> return "You threw a $moveName!"
        BattleAnimationKind.Item -> return "Used $moveName on $chimeraName!"
        BattleAnimationKind.Move -> Unit
    }

    val owner = when (side) {
        BattleSide.Player -> "Your"
        BattleSide.Enemy -> "Enemy"
    }

    return "$owner $chimeraName used $moveName!"
}

/** Handles animation frames behavior. */
internal fun BattleMoveAnimation.animationFrames(): List<BattleAnimationFrame> {
    if (kind == BattleAnimationKind.Capture) {
        val frameCount = if (captureSucceeded) 15 else 16
        return List(frameCount) {
            BattleAnimationFrame(
                imageRes = species.battleImageRes(),
                durationMillis = CaptureAnimationTickMillis
            )
        }
    }

    val baseFrame = species.battleImageRes()
    val actionFrames = species.battleMoveFrames(moveId)

    if (actionFrames.isEmpty()) {
        return listOf(
            BattleAnimationFrame(
                imageRes = baseFrame,
                durationMillis = SingleActionBattleMoveFrameMillis,
                feedbacks = feedbacks
            )
        )
    }

    val actionFrameDuration = if (actionFrames.size == 1) {
        SingleActionBattleMoveFrameMillis
    } else {
        BattleMoveFrameMillis
    }

    return buildList {
        add(
            BattleAnimationFrame(
                imageRes = baseFrame,
                durationMillis = IdleBattleMoveFrameMillis
            )
        )
        actionFrames.forEach { imageRes ->
            add(
                BattleAnimationFrame(
                    imageRes = imageRes,
                    durationMillis = actionFrameDuration,
                    feedbacks = feedbacks
                )
            )
        }
        add(
            BattleAnimationFrame(
                imageRes = baseFrame,
                durationMillis = IdleBattleMoveFrameMillis
            )
        )
    }
}

