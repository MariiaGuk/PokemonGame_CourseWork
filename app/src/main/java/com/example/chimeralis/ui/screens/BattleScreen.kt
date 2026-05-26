package com.example.chimeralis.ui.screens

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
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val MaxBattleTeamSize = 6
private val BattlePanelHorizontalPadding = 30.dp
private val BattleBackButtonSize = 38.dp
private val BattleBackButtonGap = 14.dp
private const val BattleMoveFrameMillis = 420L
private const val IdleBattleMoveFrameMillis = 180L
private const val SingleActionBattleMoveFrameMillis = 1000L
private const val BattleFeedbackFrameMillis = 70L
private const val BattleIntroInputLockMillis = 1200L
private const val BattleEndInputLockMillis = 500L
private const val BattleSpriteFrameAspectRatio = 1321f / 708f
private const val CaptureAnimationTickMillis = 16L
private const val CaptureSuccessDurationMillis = 2600L
private const val CaptureFailDurationMillis = 2850L
private const val CaptureThrowEndProgress = 0.34f
private const val CaptureAbsorbEndProgress = 0.48f
private const val CaptureShakeEndProgress = 0.78f
private const val CaptureOpenFadeStartProgress = 0.9f

@Composable
fun BattleScreen(
    player: Player,
    battleKey: Any? = null,
    wildSpecies: ChimeraSpecies,
    onBattleFinished: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val battleManager = remember(player, battleKey, wildSpecies) {
        createBattleManager(
            player = player,
            wildSpecies = wildSpecies
        )
    }
    var panelMode by remember(battleManager) { mutableStateOf(BattlePanelMode.Log) }
    var battleLogMessages by remember(battleManager) {
        mutableStateOf(listOf("A wild ${battleManager.enemyChimera.name} appeared!"))
    }
    var battleLogIndex by remember(battleManager) { mutableIntStateOf(0) }
    var battleLogAnimations by remember(battleManager) {
        mutableStateOf<Map<Int, BattleMoveAnimation>>(emptyMap())
    }
    var activeMoveAnimation by remember(battleManager) { mutableStateOf<BattleMoveAnimation?>(null) }
    var activeMoveFrameIndex by remember(battleManager) { mutableIntStateOf(0) }
    var activeCaptureProgress by remember(battleManager) { mutableFloatStateOf(0f) }
    var activeBattleFeedbacks by remember(battleManager) { mutableStateOf<List<BattleFeedback>>(emptyList()) }
    var battleFeedbackFrameIndex by remember(battleManager) { mutableIntStateOf(0) }
    var hiddenFaintedSides by remember(battleManager) { mutableStateOf<Set<BattleSide>>(emptySet()) }
    var captureResultAnimation by remember(battleManager) { mutableStateOf<BattleMoveAnimation?>(null) }
    var isCaptureResultRevealed by remember(battleManager) { mutableStateOf(false) }
    var isEnemyCapturedHidden by remember(battleManager) { mutableStateOf(false) }
    var isBattleIntroLocked by remember(battleManager) { mutableStateOf(true) }
    var isBattleExitPending by remember(battleManager) { mutableStateOf(false) }
    var uiVersion by remember(battleManager) { mutableIntStateOf(0) }
    val refreshKey = uiVersion
    val playerChimera = battleManager.playerChimera
    val wildChimera = battleManager.enemyChimera
    val currentBattleMessage = battleLogMessages.getOrElse(battleLogIndex) { "" }
    val isMoveAnimationPlaying = activeMoveAnimation != null
    val isBattleFeedbackPlaying = activeBattleFeedbacks.isNotEmpty()
    val isBattleInputLocked = isBattleIntroLocked || isBattleExitPending

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
        } else if (battleManager.isBattleActive) {
            captureResultAnimation = null
            isCaptureResultRevealed = false
            panelMode = BattlePanelMode.Actions
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

    LaunchedEffect(battleManager) {
        delay(BattleIntroInputLockMillis)
        isBattleIntroLocked = false
    }

    LaunchedEffect(isBattleExitPending) {
        if (!isBattleExitPending) return@LaunchedEffect

        delay(BattleEndInputLockMillis)
        player.team.forEach { chimera ->
            chimera.stats.resetBattleStages()
        }
        wildChimera.stats.resetBattleStages()
        onBattleFinished()
    }

    LaunchedEffect(panelMode, battleLogIndex, battleLogAnimations) {
        val animation = if (panelMode == BattlePanelMode.Log) {
            battleLogAnimations[battleLogIndex]
        } else {
            null
        }

        if (animation == null) {
            activeMoveAnimation = null
            activeMoveFrameIndex = 0
            activeCaptureProgress = 0f
            return@LaunchedEffect
        }

        activeMoveAnimation = animation
        if (animation.kind == BattleAnimationKind.Move) {
            GameSoundPlayer.play(context, R.raw.attack_sound)
        }

        if (animation.kind == BattleAnimationKind.Capture) {
            activeCaptureProgress = 0f
            captureResultAnimation = animation
            isCaptureResultRevealed = false
            if (!animation.captureSucceeded) {
                isEnemyCapturedHidden = false
            }

            val durationMillis = if (animation.captureSucceeded) {
                CaptureSuccessDurationMillis
            } else {
                CaptureFailDurationMillis
            }
            val tickCount = (durationMillis / CaptureAnimationTickMillis).toInt().coerceAtLeast(1)

            repeat(tickCount + 1) { tick ->
                val progress = (tick / tickCount.toFloat()).coerceIn(0f, 1f)
                activeCaptureProgress = progress
                activeMoveFrameIndex = (progress * 100f).roundToInt()
                if (animation.captureSucceeded && progress >= CaptureAbsorbEndProgress) {
                    isEnemyCapturedHidden = true
                }
                delay(CaptureAnimationTickMillis)
            }

            if (animation.captureSucceeded) {
                isEnemyCapturedHidden = true
            }

            delay(90L)

            activeMoveAnimation = null
            activeMoveFrameIndex = 0
            activeCaptureProgress = 0f
            activeBattleFeedbacks = emptyList()
            battleFeedbackFrameIndex = 0
            return@LaunchedEffect
        }

        val frames = animation.animationFrames()
        var playedFaintSound = false
        frames.forEachIndexed { frameIndex, frame ->
            activeMoveFrameIndex = frameIndex
            val feedbacks = frame.feedbacks.toBattleFeedbacks()

            if (feedbacks.isEmpty()) {
                delay(frame.durationMillis)
            } else {
                if (!playedFaintSound && feedbacks.any { it.type == BattleFeedbackType.Faint }) {
                    GameSoundPlayer.play(context, R.raw.dying_sound)
                    playedFaintSound = true
                }
                activeBattleFeedbacks = feedbacks
                val feedbackTicks = (frame.durationMillis / BattleFeedbackFrameMillis)
                    .toInt()
                    .coerceAtLeast(1)
                repeat(feedbackTicks) { feedbackFrameIndex ->
                    battleFeedbackFrameIndex = feedbackFrameIndex
                    delay(BattleFeedbackFrameMillis)
                }
                val remainingDelay = frame.durationMillis - feedbackTicks * BattleFeedbackFrameMillis
                if (remainingDelay > 0L) {
                    delay(remainingDelay)
                }
                val faintedSides = feedbacks
                    .filter { it.type == BattleFeedbackType.Faint }
                    .map { it.side }
                    .toSet()
                if (faintedSides.isNotEmpty()) {
                    hiddenFaintedSides = hiddenFaintedSides + faintedSides
                }
                activeBattleFeedbacks = emptyList()
                battleFeedbackFrameIndex = 0
            }
        }

        delay(90L)

        activeMoveAnimation = null
        activeMoveFrameIndex = 0
        activeCaptureProgress = 0f
        activeBattleFeedbacks = emptyList()
        battleFeedbackFrameIndex = 0
    }

    LaunchedEffect(panelMode, battleLogIndex, currentBattleMessage) {
        if (panelMode != BattlePanelMode.Log) return@LaunchedEffect

        when {
            currentBattleMessage == "Got away safely!" -> {
                GameSoundPlayer.play(context, R.raw.ran_away)
            }
            " grew to Lv." in currentBattleMessage -> {
                GameSoundPlayer.play(context, R.raw.level_up)
            }
        }
    }

    LaunchedEffect(playerChimera, playerChimera.stats.currentHp, wildChimera, wildChimera.stats.currentHp) {
        hiddenFaintedSides = hiddenFaintedSides
            .let { sides -> if (playerChimera.stats.currentHp > 0) sides - BattleSide.Player else sides }
            .let { sides -> if (wildChimera.stats.currentHp > 0) sides - BattleSide.Enemy else sides }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (panelMode == BattlePanelMode.Log &&
                    !isMoveAnimationPlaying &&
                    !isBattleFeedbackPlaying &&
                    !isBattleInputLocked
                ) {
                    Modifier.pointerInput(battleLogIndex, battleLogMessages, battleManager.isBattleActive) {
                        detectTapGestures(onTap = { advanceBattleLog() })
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val screenWidth = maxWidth
        val panelHeight = 100.dp
        val statusWidth = 310.dp
        val platformY = minOf(maxHeight * 0.68f, maxHeight - panelHeight - 42.dp)
        val playerPlatformX = maxWidth * 0.35f
        val wildPlatformX = maxWidth * 0.65f
        val captureTargetX = wildPlatformX - 10.dp
        val captureTargetY = platformY - 8.dp
        val spriteSize = minOf(maxWidth * 0.25f, maxHeight * 0.45f)
        val activeCaptureAnimation = activeMoveAnimation?.takeIf { it.kind == BattleAnimationKind.Capture }
        val playerAnimation = activeMoveAnimation?.takeIf {
            it.kind == BattleAnimationKind.Move && it.side == BattleSide.Player
        }
        val wildAnimation = activeMoveAnimation?.takeIf {
            it.kind == BattleAnimationKind.Move && it.side == BattleSide.Enemy
        }
        val playerFrameResources = playerAnimation?.animationFrames()
        val wildFrameResources = wildAnimation?.animationFrames()
        val playerImageRes = playerFrameResources?.getOrNull(activeMoveFrameIndex)?.imageRes
            ?: playerChimera.species.battleImageRes()
        val wildImageRes = wildFrameResources?.getOrNull(activeMoveFrameIndex)?.imageRes
            ?: wildChimera.species.battleImageRes()
        val spriteFrameWidth = spriteSize * BattleSpriteFrameAspectRatio
        val playerFeedback = activeBattleFeedbacks.firstOrNull { it.side == BattleSide.Player }
        val wildFeedback = activeBattleFeedbacks.firstOrNull { it.side == BattleSide.Enemy }
        val playerFeedbackOffset = playerFeedback.shakeOffset(battleFeedbackFrameIndex)
        val wildFeedbackOffset = wildFeedback.shakeOffset(battleFeedbackFrameIndex)
        val playerDropOffset = playerFeedback.faintDropOffset(battleFeedbackFrameIndex)
        val wildDropOffset = wildFeedback.faintDropOffset(battleFeedbackFrameIndex)
        val playerTintColor = playerFeedback.tintColor()
        val wildTintColor = wildFeedback.tintColor()
        val playerFaintPending = activeMoveAnimation.hasFaintFeedback(BattleSide.Player) &&
                BattleSide.Player !in hiddenFaintedSides
        val wildFaintPending = activeMoveAnimation.hasFaintFeedback(BattleSide.Enemy) &&
                BattleSide.Enemy !in hiddenFaintedSides
        val isEnemyHeldInCaptureStone =
            activeCaptureAnimation != null && activeCaptureProgress >= CaptureAbsorbEndProgress ||
                    activeCaptureAnimation == null &&
                    captureResultAnimation != null &&
                    !isCaptureResultRevealed
        val playerAlpha = fighterAlpha(
            currentHp = playerChimera.stats.currentHp,
            hasPendingFaint = playerFaintPending,
            isHiddenAfterFaint = BattleSide.Player in hiddenFaintedSides,
            activeFeedback = playerFeedback,
            frameIndex = battleFeedbackFrameIndex
        )
        val wildAlpha = fighterAlpha(
            currentHp = wildChimera.stats.currentHp,
            hasPendingFaint = wildFaintPending,
            isHiddenAfterFaint = BattleSide.Enemy in hiddenFaintedSides,
            activeFeedback = wildFeedback,
            frameIndex = battleFeedbackFrameIndex
        ) * captureTargetAlpha(activeCaptureAnimation, activeCaptureProgress) *
                if (isEnemyCapturedHidden || isEnemyHeldInCaptureStone) 0f else 1f

        Image(
            painter = painterResource(id = R.drawable.battle_arena),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (10).dp)
                .graphicsLayer {
                    scaleX = 1.07f
                    scaleY = 1.07f
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.08f))
        ) {
            StatusPlate(
                name = playerChimera.name,
                level = playerChimera.level,
                currentHp = playerChimera.stats.currentHp,
                maxHp = playerChimera.stats.maxHp,
                currentExp = playerChimera.exp,
                expToNextLevel = playerChimera.expToNextLevel(),
                attackStage = playerChimera.stats.attackStage,
                defenceStage = playerChimera.stats.defenceStage,
                speedStage = playerChimera.stats.speedStage,
                refreshKey = refreshKey,
                modifier = Modifier
                    .width(statusWidth)
                    .offset(
                        x = 22.dp,
                        y = 14.dp
                    )
            )

            StatusPlate(
                name = wildChimera.name,
                level = wildChimera.level,
                currentHp = wildChimera.stats.currentHp,
                maxHp = wildChimera.stats.maxHp,
                currentExp = null,
                expToNextLevel = null,
                attackStage = wildChimera.stats.attackStage,
                defenceStage = wildChimera.stats.defenceStage,
                speedStage = wildChimera.stats.speedStage,
                refreshKey = refreshKey,
                modifier = Modifier
                    .width(statusWidth)
                    .offset(
                        x = screenWidth - statusWidth - 22.dp,
                        y = 14.dp
                    )
            )

            BattleFighter(
                imageRes = playerImageRes,
                mirrored = true,
                spriteWidth = spriteFrameWidth,
                spriteHeight = spriteSize,
                effectOffsetX = playerFeedbackOffset,
                effectOffsetY = playerDropOffset,
                tintColor = playerTintColor,
                alpha = playerAlpha,
                modifier = Modifier.offset(
                    x = playerPlatformX - spriteFrameWidth / 2f,
                    y = platformY - spriteSize * 0.75f
                )
            )

            BattleFighter(
                imageRes = wildImageRes,
                mirrored = false,
                spriteWidth = spriteFrameWidth,
                spriteHeight = spriteSize,
                effectOffsetX = wildFeedbackOffset,
                effectOffsetY = wildDropOffset,
                tintColor = wildTintColor,
                alpha = wildAlpha,
                modifier = Modifier.offset(
                    x = wildPlatformX - spriteFrameWidth / 2f,
                    y = platformY - spriteSize * 0.75f
                )
            )

            activeCaptureAnimation?.let { animation ->
                CaptureBallAnimation(
                    progress = activeCaptureProgress,
                    startX = playerPlatformX,
                    startY = platformY - spriteSize * 0.42f,
                    targetX = captureTargetX,
                    targetY = captureTargetY,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                captureResultAnimation?.let { animation ->
                    CaptureResultBindingStone(
                        caught = animation.captureSucceeded,
                        isResultRevealed = isCaptureResultRevealed,
                        targetX = captureTargetX,
                        targetY = captureTargetY,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            BattlePanel(
                message = currentBattleMessage,
                mode = panelMode,
                moves = playerChimera.moves,
                team = player.team,
                activeChimera = playerChimera,
                inventoryItems = player.inventory.items,
                onFight = { panelMode = BattlePanelMode.Moves },
                onBag = { panelMode = BattlePanelMode.Bag },
                onTeam = { panelMode = BattlePanelMode.Team },
                onMoveSelected = { move ->
                    val result = battleManager.performTurnWithAnimations(BattleAction.UseMove(move))
                    showBattleResult(result.log, result.animations)
                },
                onSwitchSelected = { chimera ->
                    val result = battleManager.performTurnWithAnimations(BattleAction.SwitchChimera(chimera))
                    showBattleResult(result.log, result.animations)
                },
                onItemSelected = { item ->
                    val result = battleManager.performTurnWithAnimations(BattleAction.UseItem(item))
                    showBattleResult(result.log, result.animations)
                },
                onRun = {
                    val result = battleManager.performTurnWithAnimations(BattleAction.Run)
                    showBattleResult(result.log, result.animations)
                },
                onBackToActions = { panelMode = BattlePanelMode.Actions },
                colors = colors,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun BattleFighter(
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
private fun CaptureBallAnimation(
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
private fun CaptureResultBindingStone(
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
private fun BindingStoneCaptureSprite(
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
private fun CaptureAbsorbFlash(
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

private enum class CaptureBallPhase {
    Throwing,
    Absorbing,
    Shaking,
    Open,
    Locked
}

private fun captureThrowPoint(
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

private fun trajectoryRotationDegrees(
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

private fun easeInOutCubic(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return if (p < 0.5f) {
        4f * p * p * p
    } else {
        val t = -2f * p + 2f
        1f - (t * t * t) / 2f
    }
}

private fun easeInCubic(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return p * p * p
}

@Composable
private fun StatusPlate(
    name: String,
    level: Int,
    currentHp: Int,
    maxHp: Int,
    currentExp: Int?,
    expToNextLevel: Int?,
    attackStage: Int,
    defenceStage: Int,
    speedStage: Int,
    refreshKey: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val hpRatio = ((currentHp + refreshKey * 0).toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.82f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$name  Lv.$level",
                color = colors.primary,
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = "$currentHp/$maxHp",
                color = colors.primary,
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF2B190E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(hpRatio)
                    .fillMaxHeight()
                    .background(Color(0xFF66C96A))
            )
        }

        if (currentExp != null && expToNextLevel != null) {
            Spacer(modifier = Modifier.height(5.dp))

            ExpBar(
                currentExp = currentExp,
                expToNextLevel = expToNextLevel
            )
        }

        if (attackStage != 0 || defenceStage != 0 || speedStage != 0) {
            Spacer(modifier = Modifier.height(7.dp))

            StatStagesRow(
                attackStage = attackStage,
                defenceStage = defenceStage,
                speedStage = speedStage
            )
        }
    }
}

@Composable
private fun ExpBar(
    currentExp: Int,
    expToNextLevel: Int
) {
    val colors = MaterialTheme.colorScheme
    val expRatio = (currentExp.toFloat() / expToNextLevel.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EXP",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF2B190E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(expRatio)
                    .fillMaxHeight()
                    .background(Color(0xFF5CCBEA))
            )
        }

        Text(
            text = "$currentExp/$expToNextLevel",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatStagesRow(
    attackStage: Int,
    defenceStage: Int,
    speedStage: Int
) {
    val stages = listOf(
        "ATK" to attackStage,
        "DEF" to defenceStage,
        "SPD" to speedStage
    ).filter { (_, value) -> value != 0 }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        stages.forEach { (label, value) ->
            StatStageChip(
                label = label,
                value = value,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatStageChip(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val valueText = when {
        value > 0 -> "+$value"
        value < 0 -> value.toString()
        else -> "0"
    }
    val chipColor = when {
        value > 0 -> Color(0xFF2E6B3E)
        value < 0 -> Color(0xFF7A2D2D)
        else -> Color(0xFF2B190E)
    }

    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(chipColor.copy(alpha = if (value == 0) 0.46f else 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label $valueText",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun BattlePanel(
    message: String,
    mode: BattlePanelMode,
    moves: List<com.example.chimeralis.logic.chimeras.moves.Move>,
    team: List<Chimera>,
    activeChimera: Chimera,
    inventoryItems: Map<Item, Int>,
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onMoveSelected: (com.example.chimeralis.logic.chimeras.moves.Move) -> Unit,
    onSwitchSelected: (Chimera) -> Unit,
    onItemSelected: (Item) -> Unit,
    onRun: () -> Unit,
    onBackToActions: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val showBackArrow = mode == BattlePanelMode.Moves ||
            mode == BattlePanelMode.Bag ||
            mode == BattlePanelMode.Team

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(colors.surface.copy(alpha = 0.9f))
    ) {
        if (showBackArrow) {
            BattleBackArrowButton(
                onClick = onBackToActions,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = BattlePanelHorizontalPadding)
            )
        }

        if (mode == BattlePanelMode.Log) {
            BattleMessage(
                text = message,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = BattlePanelHorizontalPadding, vertical = 16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (showBackArrow) {
                            BattlePanelHorizontalPadding + BattleBackButtonSize + BattleBackButtonGap
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        top = 7.dp,
                        end = BattlePanelHorizontalPadding,
                        bottom = 7.dp
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                when (mode) {
                    BattlePanelMode.Actions -> BattleActionButtons(
                        onFight = onFight,
                        onBag = onBag,
                        onTeam = onTeam,
                        onRun = onRun
                    )
                    BattlePanelMode.Moves -> MoveButtons(
                        moves = moves,
                        onMoveSelected = onMoveSelected
                    )
                    BattlePanelMode.Bag -> BattleInventoryButtons(
                        inventoryItems = inventoryItems,
                        activeChimera = activeChimera,
                        onItemSelected = onItemSelected
                    )
                    BattlePanelMode.Team -> BattleTeamButtons(
                        team = team,
                        activeChimera = activeChimera,
                        onSwitchSelected = onSwitchSelected
                    )
                    BattlePanelMode.Log -> Unit
                }
            }
        }
    }
}

@Composable
private fun BattleBackArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(BattleBackButtonSize)
            .clip(RoundedCornerShape(5.dp))
            .background(colors.background.copy(alpha = 0.42f))
            .border(1.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        GameSoundPlayer.play(context, R.raw.button_click)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "<",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun BattleMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Text(
        text = text,
        color = colors.onSurface,
        fontFamily = CinzelFamily,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 4.sp,
        modifier = modifier
    )
}

@Composable
private fun BattleActionButtons(
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onRun: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Fight", onClick = onFight)
            MenuButton(text = "Bag", onClick = onBag)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Team", onClick = onTeam)
            MenuButton(text = "Run", onClick = onRun)
        }
    }
}

@Composable
private fun BattleTeamButtons(
    team: List<Chimera>,
    activeChimera: Chimera,
    onSwitchSelected: (Chimera) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val slots = List(MaxBattleTeamSize) { index -> team.getOrNull(index) }
            slots.chunked(3).forEach { rowTeam ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowTeam.forEach { chimera ->
                        if (chimera == null) {
                            EmptyBattleTeamSlot()
                        } else {
                            BattleTeamSlot(
                                chimera = chimera,
                                isActive = chimera === activeChimera,
                                onSwitchSelected = onSwitchSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBattleTeamSlot() {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF3E443E).copy(alpha = 0.26f))
            .border(1.dp, colors.primary.copy(alpha = 0.18f), RoundedCornerShape(3.dp))
    )
}

@Composable
private fun BattleTeamSlot(
    chimera: Chimera,
    isActive: Boolean,
    onSwitchSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canSwitch = !isActive && chimera.stats.isAlive()
    val hpRatio = (chimera.stats.currentHp.toFloat() / chimera.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    val hpPercent = (hpRatio * 100).roundToInt()
    val alpha = if (canSwitch || isActive) 1f else 0.42f

    Row(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                if (isActive) Color(0xFF5B5F55).copy(alpha = 0.92f)
                else Color(0xFF3E443E).copy(alpha = 0.88f)
            )
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = colors.primary.copy(alpha = if (isActive) 0.88f else 0.44f),
                shape = RoundedCornerShape(3.dp)
            )
            .pointerInput(canSwitch, chimera) {
                detectTapGestures(
                    onTap = {
                        if (canSwitch) {
                            onSwitchSelected(chimera)
                        }
                    }
                )
            }
            .padding(3.dp)
            .graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD0C5).copy(alpha = 0.32f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = chimera.species.battleImageRes()),
                contentDescription = chimera.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f }
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chimera.name,
                    color = Color(0xFFE8E8D8),
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    maxLines = 1
                )
                Text(
                    text = "Lv.${chimera.level}",
                    color = Color(0xFFE8E8D8),
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF252818))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpRatio)
                        .fillMaxHeight()
                        .background(
                            when {
                                hpRatio > 0.5f -> Color(0xFF80D35D)
                                hpRatio > 0.2f -> Color(0xFFE0B84B)
                                else -> Color(0xFFD85A4A)
                            }
                        )
                )
            }

            Text(
                text = "HP: ${chimera.stats.currentHp}/${chimera.stats.maxHp} - $hpPercent%",
                color = Color(0xFFE8E8D8),
                fontFamily = CinzelFamily,
                fontSize = 6.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BattleInventoryButtons(
    inventoryItems: Map<Item, Int>,
    activeChimera: Chimera,
    onItemSelected: (Item) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (inventoryItems.isEmpty()) {
            Text(
                text = "Bag is empty",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = CinzelFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        } else {
            inventoryItems.entries.sortedBy { it.key.name }.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { (item, amount) ->
                        val canUseItem = item.isCaptureItem || item.canUseOn(activeChimera)
                        MenuButton(
                            text = "${item.name} x$amount",
                            enabled = canUseItem,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveButtons(
    moves: List<com.example.chimeralis.logic.chimeras.moves.Move>,
    onMoveSelected: (com.example.chimeralis.logic.chimeras.moves.Move) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        moves.chunked(2).forEach { rowMoves ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMoves.forEach { move ->
                    MenuButton(
                        text = "${move.name} ${move.pp}/${move.maxPp}",
                        onClick = {
                            if (move.pp > 0) {
                                onMoveSelected(move)
                            }
                        }
                    )
                }
            }
        }
    }
}

private data class BattleFeedback(
    val side: BattleSide,
    val type: BattleFeedbackType
)

private enum class BattleFeedbackType {
    Damage,
    Faint,
    StatChange
}

private enum class BattlePanelMode {
    Actions,
    Moves,
    Bag,
    Team,
    Log
}

private fun List<BattleMoveFeedback>.toBattleFeedbacks(): List<BattleFeedback> {
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

private fun BattleFeedback?.shakeOffset(frameIndex: Int): Dp {
    if (this?.type != BattleFeedbackType.Damage && this?.type != BattleFeedbackType.Faint) return 0.dp

    return when (frameIndex % 4) {
        0 -> (-7).dp
        1 -> 7.dp
        2 -> (-4).dp
        else -> 4.dp
    }
}

private fun BattleFeedback?.faintDropOffset(frameIndex: Int): Dp {
    if (this?.type != BattleFeedbackType.Faint) return 0.dp

    return (34f * faintProgress(frameIndex)).dp
}

private fun BattleFeedback?.tintColor(): Color? {
    return when (this?.type) {
        BattleFeedbackType.Damage -> Color(0xFFFF3535).copy(alpha = 0.42f)
        BattleFeedbackType.Faint -> Color(0xFFFF3535).copy(alpha = 0.48f)
        BattleFeedbackType.StatChange -> Color(0xFF49A7FF).copy(alpha = 0.42f)
        null -> null
    }
}

private fun fighterAlpha(
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

private fun faintProgress(frameIndex: Int): Float {
    return (frameIndex / 6f).coerceIn(0f, 1f)
}

private fun BattleMoveAnimation?.hasFaintFeedback(side: BattleSide): Boolean {
    return this?.feedbacks?.any {
        it.side == side && it.type == BattleMoveFeedbackType.Faint
    } == true
}

private fun captureTargetAlpha(animation: BattleMoveAnimation?, progress: Float): Float {
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

private fun mapAnimationsToLogMessages(
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

private fun BattleMoveAnimation.message(): String {
    if (kind == BattleAnimationKind.Capture) {
        return "You threw a $moveName!"
    }

    val owner = when (side) {
        BattleSide.Player -> "Your"
        BattleSide.Enemy -> "Enemy"
    }

    return "$owner $chimeraName used $moveName!"
}

private data class BattleAnimationFrame(
    val imageRes: Int,
    val durationMillis: Long,
    val feedbacks: List<BattleMoveFeedback> = emptyList()
)

private fun BattleMoveAnimation.animationFrames(): List<BattleAnimationFrame> {
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
    val moveKey = moveName.lowercase().replace(" ", "")

    val actionFrames = when (species) {
        ChimeraSpecies.Sunflare,
        ChimeraSpecies.Solflare,
        ChimeraSpecies.Solignis -> when (moveKey) {
            "ember" -> listOf(
                R.drawable.starter_fire_ember_1,
                R.drawable.starter_fire_ember_2
            )
            "growl" -> listOf(
                R.drawable.starter_fire_growl
            )
            "tackle" -> listOf(
                R.drawable.starter_fire_tackle_1,
                R.drawable.starter_fire_tackle_2
            )
            else -> emptyList()
        }
        ChimeraSpecies.Sylvhorn -> when (moveKey) {
            "growl" -> listOf(
                R.drawable.starter_grass_growl
            )
            "tackle" -> listOf(
                R.drawable.starter_grass_tackle_1,
                R.drawable.starter_grass_tackle_2
            )
            else -> emptyList()
        }
        ChimeraSpecies.Aquantis -> when (moveKey) {
            "tailwhip" -> listOf(
                R.drawable.starter_water_tailwhip_1,
                R.drawable.starter_water_tailwhip_2
            )
            "tackle" -> listOf(
                R.drawable.starter_water_tackle_1,
                R.drawable.starter_water_tackle_2
            )
            else -> emptyList()
        }
    }

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

private fun createBattleManager(
    player: Player,
    wildSpecies: ChimeraSpecies
): BattleManager {
    val wildChimera = ChimeraFactory.createChimera(wildSpecies, level = 3)
    val enemy = NPC(
        name = "Wild",
        team = mutableListOf(wildChimera)
    )

    return BattleManager(player = player, enemy = enemy)
}

private fun ChimeraSpecies.battleName(): String = when (this) {
    ChimeraSpecies.Sunflare -> "Sunflare"
    ChimeraSpecies.Solflare -> "Solflare"
    ChimeraSpecies.Solignis -> "Solignis"
    ChimeraSpecies.Sylvhorn -> "Sylvhorn"
    ChimeraSpecies.Aquantis -> "Aquantis"
}

private fun com.example.chimeralis.logic.chimeras.Chimera.expToNextLevel(): Int {
    return (level * level * level).coerceAtLeast(1)
}

private fun ChimeraSpecies.battleImageRes(): Int = when (this) {
    ChimeraSpecies.Sunflare,
    ChimeraSpecies.Solflare,
    ChimeraSpecies.Solignis -> R.drawable.starter_fire
    ChimeraSpecies.Sylvhorn -> R.drawable.starter_grass
    ChimeraSpecies.Aquantis -> R.drawable.starter_water
}
