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
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@Composable
internal fun BattleFighter(
    imageRes: Int,
    mirrored: Boolean,
    spriteWidth: Dp,
    spriteHeight: Dp,
    effectOffsetX: Dp = 0.dp,
    effectOffsetY: Dp = 0.dp,
    tintColor: Color? = null,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = tintColor?.let { ColorFilter.tint(it, BlendMode.SrcAtop) },
        modifier = modifier
            .offset(x = effectOffsetX, y = effectOffsetY)
            .width(spriteWidth)
            .height(spriteHeight)
            .graphicsLayer {
                scaleX = if (mirrored) -1f else 1f
                this.alpha = alpha
            }
    )
}

@Composable
internal fun CaptureBallAnimation(
    progress: Float,
    startX: Dp,
    startY: Dp,
    targetX: Dp,
    targetY: Dp,
    modifier: Modifier = Modifier
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val throwProgress = (clampedProgress / CaptureThrowEndProgress).coerceIn(0f, 1f)
    val dropProgress = ((clampedProgress - CaptureThrowEndProgress) /
            (CaptureAbsorbEndProgress - CaptureThrowEndProgress)).coerceIn(0f, 1f)
    val easedThrowProgress = easeInOutCubic(throwProgress)
    val easedDropProgress = easeInCubic(dropProgress)
    val airTargetY = targetY - 104.dp
    val arcLift = 64.dp * (1f - kotlin.math.abs(easedThrowProgress * 2f - 1f))
    val thrownX = startX + (targetX - startX) * easedThrowProgress
    val thrownY = startY + (airTargetY - startY) * easedThrowProgress - arcLift
    val droppedY = airTargetY + (targetY - airTargetY) * easedDropProgress
    val stoneX = if (clampedProgress < CaptureThrowEndProgress) thrownX else targetX
    val stoneY = if (clampedProgress < CaptureThrowEndProgress) thrownY else droppedY
    val phase = when {
        clampedProgress < CaptureThrowEndProgress -> CaptureBallPhase.Throwing
        else -> CaptureBallPhase.Absorbing
    }
    val flightRotation = if (phase == CaptureBallPhase.Throwing) {
        val previousPoint = captureThrowPoint(
            progress = throwProgress - 0.025f,
            startX = startX,
            startY = startY,
            targetX = targetX,
            targetY = airTargetY
        )
        val nextPoint = captureThrowPoint(
            progress = throwProgress + 0.025f,
            startX = startX,
            startY = startY,
            targetX = targetX,
            targetY = airTargetY
        )
        trajectoryRotationDegrees(previousPoint, nextPoint)
    } else {
        0f
    }
    val stoneAlpha = when (phase) {
        CaptureBallPhase.Open -> (1f - ((clampedProgress - CaptureOpenFadeStartProgress) /
                (1f - CaptureOpenFadeStartProgress))).coerceIn(0f, 1f)
        else -> 1f
    }
    val shakeOffset = if (phase == CaptureBallPhase.Shaking) {
        val shakeStep = (((clampedProgress - CaptureAbsorbEndProgress) * 34f).roundToInt() % 4)
        when (shakeStep) {
            0 -> (-8).dp
            1 -> 8.dp
            2 -> (-5).dp
            else -> 5.dp
        }
    } else {
        0.dp
    }

    Box(modifier = modifier) {
        if (phase == CaptureBallPhase.Absorbing) {
            CaptureAbsorbFlash(
                progress = easedDropProgress,
                modifier = Modifier
                    .offset(x = stoneX - 58.dp, y = stoneY - 58.dp)
                    .size(116.dp)
            )
        }

        BindingStoneCaptureSprite(
            phase = phase,
            modifier = Modifier
                .offset(x = stoneX - 36.dp + shakeOffset, y = stoneY - 36.dp)
                .size(
                    when (phase) {
                        CaptureBallPhase.Throwing -> 78.dp
                        CaptureBallPhase.Open -> 86.dp
                        else -> 82.dp
                    }
                )
                .graphicsLayer {
                    alpha = stoneAlpha
                    rotationZ = flightRotation
                }
        )
    }
}

@Composable
internal fun CaptureResultBindingStone(
    caught: Boolean,
    isResultRevealed: Boolean,
    targetX: Dp,
    targetY: Dp,
    modifier: Modifier = Modifier
) {
    val phase = when {
        !isResultRevealed -> CaptureBallPhase.Absorbing
        caught -> CaptureBallPhase.Locked
        else -> CaptureBallPhase.Open
    }

    Box(modifier = modifier) {
        BindingStoneCaptureSprite(
            phase = phase,
            modifier = Modifier
                .offset(x = targetX - 36.dp, y = targetY - 36.dp)
                .size(if (phase == CaptureBallPhase.Open) 86.dp else 82.dp)
        )
    }
}

@Composable
internal fun BindingStoneCaptureSprite(
    phase: CaptureBallPhase,
    modifier: Modifier = Modifier
) {
    val imageRes = when (phase) {
        CaptureBallPhase.Throwing -> R.drawable.binding_stone_thrown
        CaptureBallPhase.Absorbing,
        CaptureBallPhase.Shaking -> R.drawable.binding_stone_capturing
        CaptureBallPhase.Open -> R.drawable.binding_stone_broken
        CaptureBallPhase.Locked -> R.drawable.binding_stone_captured
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
internal fun CaptureAbsorbFlash(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * (0.52f - progress * 0.26f)
        drawCircle(
            color = Color.White.copy(alpha = (0.46f * (1f - progress)).coerceIn(0f, 0.46f)),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color(0xFFE84B3C).copy(alpha = (0.24f * (1f - progress)).coerceIn(0f, 0.24f)),
            radius = radius * 0.72f,
            center = center
        )
    }
}

internal enum class CaptureBallPhase {
    Throwing,
    Absorbing,
    Shaking,
    Open,
    Locked
}

internal fun captureThrowPoint(
    progress: Float,
    startX: Dp,
    startY: Dp,
    targetX: Dp,
    targetY: Dp
): Pair<Dp, Dp> {
    val easedProgress = easeInOutCubic(progress.coerceIn(0f, 1f))
    val arcLift = 64.dp * (1f - kotlin.math.abs(easedProgress * 2f - 1f))
    val x = startX + (targetX - startX) * easedProgress
    val y = startY + (targetY - startY) * easedProgress - arcLift

    return x to y
}

internal fun trajectoryRotationDegrees(
    previousPoint: Pair<Dp, Dp>,
    nextPoint: Pair<Dp, Dp>
): Float {
    val deltaX = (nextPoint.first - previousPoint.first).value
    val deltaY = (nextPoint.second - previousPoint.second).value
    val directionDegrees = Math.toDegrees(
        kotlin.math.atan2(deltaY.toDouble(), deltaX.toDouble())
    ).toFloat()

    return directionDegrees - 18f
}

internal fun easeInOutCubic(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return if (p < 0.5f) {
        4f * p * p * p
    } else {
        val t = -2f * p + 2f
        1f - (t * t * t) / 2f
    }
}

internal fun easeInCubic(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return p * p * p
}

