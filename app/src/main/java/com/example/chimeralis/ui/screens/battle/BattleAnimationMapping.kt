package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.battle.BattleAnimationKind
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleMoveFeedback
import com.example.chimeralis.logic.battle.BattleMoveFeedbackType
import com.example.chimeralis.logic.battle.BattleMoveAnimation
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.MoveLearnRequest
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.battle.BattleStatsSnapshot
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.chimera.battleMoveFrames
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Stores battle feedback data. */
internal data class BattleFeedback(
    val side: BattleSide,
    val type: BattleFeedbackType
)

/** Lists the battle feedback type values. */
internal enum class BattleFeedbackType {
    Damage,
    Faint,
    StatChange
}

/** Lists the battle panel mode values. */
internal enum class BattlePanelMode {
    Actions,
    Moves,
    Bag,
    ItemTarget,
    Team,
    MoveLearning,
    Log
}

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

/** Converts data into battle stats snapshot. */
internal fun com.example.chimeralis.logic.chimeras.Stats.toBattleStatsSnapshot(): BattleStatsSnapshot {
    return BattleStatsSnapshot(
        currentHp = currentHp,
        maxHp = maxHp,
        attack = attack,
        defence = defence,
        speed = speed,
        attackStage = attackStage,
        defenceStage = defenceStage,
        speedStage = speedStage
    )
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

/** Stores battle animation frame data. */
internal data class BattleAnimationFrame(
    val imageRes: Int,
    val durationMillis: Long,
    val feedbacks: List<BattleMoveFeedback> = emptyList()
)

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

