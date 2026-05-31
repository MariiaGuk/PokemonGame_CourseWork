package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.battle.BattleAnimationKind
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleScenarioFactory
import com.example.chimeralis.logic.battle.BattleSide
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player
import kotlinx.coroutines.delay

/** Renders the battle screen UI. */
@Composable
fun BattleScreen(
    player: Player,
    battleKey: Any? = null,
    wildSpecies: ChimeraSpecies,
    isTrainerBattle: Boolean = false,
    onBattleResultSoundStarted: () -> Unit = {},
    onBattleFinished: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val battleManager = remember(player, battleKey, wildSpecies, isTrainerBattle) {
        if (isTrainerBattle) {
            BattleScenarioFactory.createTrainerBattle(player = player)
        } else {
            BattleScenarioFactory.createWildBattle(
                player = player,
                wildSpecies = wildSpecies
            )
        }
    }
    val openingMessage = if (isTrainerBattle) {
        "${battleManager.enemy.name} challenged you!"
    } else {
        "A wild ${battleManager.enemyChimera.name} appeared!"
    }
    val uiState = rememberBattleUiState(battleManager, openingMessage)
    val playerChimera = battleManager.playerChimera
    val wildChimera = battleManager.enemyChimera
    val playerStatus = playerChimera.toPlayerStatusPresentation(
        visibleStats = uiState.visualPlayerStats,
        visibleLevel = uiState.visualPlayerLevel,
        visibleExp = uiState.visualPlayerExp,
        refreshKey = uiState.refreshKey
    )
    val wildStatus = wildChimera.toEnemyStatusPresentation(
        visibleStats = uiState.visualWildStats,
        refreshKey = uiState.refreshKey
    )
    val panelPresentation = player.toBattlePanelPresentation(
        activeChimera = playerChimera,
        selectedItem = uiState.selectedBattleItem,
        pendingMoveLearning = battleManager.pendingMoveLearning,
        canUseCaptureItems = !isTrainerBattle
    )

    LaunchedEffect(battleManager) {
        delay(BattleIntroInputLockMillis)
        uiState.unlockBattleIntro()
    }

    LaunchedEffect(uiState.isBattleExitPending) {
        if (!uiState.isBattleExitPending) return@LaunchedEffect

        delay(BattleEndInputLockMillis)
        uiState.pendingEvolutionEvents.forEach { event ->
            uiState.showEvolution(event)
            GameSoundPlayer.stopBattleResultSounds()
            GameSoundPlayer.play(context, R.raw.chimera_evolution)
            delay(EvolutionRevealMillis)
            GameSoundPlayer.stop(R.raw.chimera_evolution)
            GameSoundPlayer.play(context, R.raw.chimera_evolved)
            delay(EvolutionRevealMillis)
            battleManager.applyEvolution(event)
            delay(EvolutionOverlayDurationMillis - EvolutionRevealMillis)
        }
        uiState.clearEvolutionState()
        player.team.forEach { chimera ->
            chimera.stats.resetBattleStages()
        }
        battleManager.enemy.team.forEach { chimera ->
            chimera.stats.resetBattleStages()
        }
        onBattleFinished()
    }

    LaunchedEffect(uiState.panelMode, uiState.battleLogIndex, uiState.battleLogAnimations) {
        val animation = uiState.activeLogAnimation

        if (animation == null) {
            uiState.clearActiveAnimation()
            return@LaunchedEffect
        }

        uiState.playLogAnimation(animation) { soundRes ->
            GameSoundPlayer.play(context, soundRes)
        }
    }

    LaunchedEffect(uiState.panelMode, uiState.battleLogIndex, uiState.currentBattleMessage) {
        if (uiState.panelMode != BattlePanelMode.Log) return@LaunchedEffect

        when {
            uiState.currentBattleMessage == "Got away safely!" -> {
                GameSoundPlayer.play(context, R.raw.ran_away)
            }
            uiState.currentBattleMessage == "You won!" -> {
                onBattleResultSoundStarted()
                GameSoundPlayer.play(context, R.raw.battle_victory)
            }
            isTrainerBattle &&
                    uiState.shouldRevealEnemyDefeatForCurrentMessage(isTrainerBattle) -> {
                uiState.revealEnemyDefeatForCurrentMessage()
            }
            uiState.currentBattleMessage == "You lost!" -> {
                onBattleResultSoundStarted()
                GameSoundPlayer.play(context, R.raw.battle_loss)
            }
            uiState.currentBattleMessage.startsWith("Gotcha!") &&
                    uiState.currentBattleMessage.endsWith("was caught!") -> {
                onBattleResultSoundStarted()
                GameSoundPlayer.play(context, R.raw.caught_a_chimera)
            }
            uiState.currentBattleMessage.contains(" gained ") &&
                    uiState.currentBattleMessage.endsWith(" EXP.") -> {
                uiState.syncPlayerProgressForCurrentMessage(playerChimera)
            }
            uiState.isCurrentMessageLevelUp() -> {
                uiState.syncPlayerProgressForCurrentMessage(playerChimera)
                GameSoundPlayer.play(context, R.raw.level_up)
            }
        }
    }

    LaunchedEffect(playerChimera, playerChimera.stats.currentHp, wildChimera, wildChimera.stats.currentHp) {
        uiState.showRecoveredFighters(playerChimera, wildChimera)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (uiState.panelMode == BattlePanelMode.Log &&
                    !uiState.isMoveAnimationPlaying &&
                    !uiState.isBattleFeedbackPlaying &&
                    !uiState.isBattleInputLocked
                ) {
                    Modifier.pointerInput(uiState.battleLogIndex, uiState.battleLogMessages, battleManager.isBattleActive) {
                        detectTapGestures(onTap = { uiState.advanceBattleLog() })
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
        val activeCaptureAnimation = uiState.activeMoveAnimation?.takeIf { it.kind == BattleAnimationKind.Capture }
        val playerAnimation = uiState.activeMoveAnimation?.takeIf {
            it.kind == BattleAnimationKind.Move && it.side == BattleSide.Player
        }
        val wildAnimation = uiState.activeMoveAnimation?.takeIf {
            it.kind == BattleAnimationKind.Move && it.side == BattleSide.Enemy
        }
        val playerFrameResources = playerAnimation?.animationFrames()
        val wildFrameResources = wildAnimation?.animationFrames()
        val playerImageRes = playerFrameResources?.getOrNull(uiState.activeMoveFrameIndex)?.imageRes
            ?: playerStatus.imageRes
        val wildImageRes = wildFrameResources?.getOrNull(uiState.activeMoveFrameIndex)?.imageRes
            ?: wildStatus.imageRes
        val spriteFrameWidth = spriteSize * BattleSpriteFrameAspectRatio
        val playerFeedback = uiState.activeBattleFeedbacks.firstOrNull { it.side == BattleSide.Player }
        val wildFeedback = uiState.activeBattleFeedbacks.firstOrNull { it.side == BattleSide.Enemy }
        val playerFeedbackOffset = playerFeedback.shakeOffset(uiState.battleFeedbackFrameIndex)
        val wildFeedbackOffset = wildFeedback.shakeOffset(uiState.battleFeedbackFrameIndex)
        val playerDropOffset = playerFeedback.faintDropOffset(uiState.battleFeedbackFrameIndex)
        val wildDropOffset = wildFeedback.faintDropOffset(uiState.battleFeedbackFrameIndex)
        val playerTintColor = playerFeedback.tintColor()
        val wildTintColor = wildFeedback.tintColor()
        val playerFaintPending = uiState.activeMoveAnimation.hasFaintFeedback(BattleSide.Player) &&
                BattleSide.Player !in uiState.hiddenFaintedSides
        val wildFaintPending = uiState.activeMoveAnimation.hasFaintFeedback(BattleSide.Enemy) &&
                BattleSide.Enemy !in uiState.hiddenFaintedSides
        val isEnemyHeldInCaptureStone =
            activeCaptureAnimation != null && uiState.activeCaptureProgress >= CaptureAbsorbEndProgress ||
                    activeCaptureAnimation == null &&
                    uiState.captureResultAnimation != null &&
                    !uiState.isCaptureResultRevealed
        val playerAlpha = fighterAlpha(
            currentHp = uiState.visualPlayerStats.currentHp,
            hasPendingFaint = playerFaintPending,
            isHiddenAfterFaint = BattleSide.Player in uiState.hiddenFaintedSides,
            activeFeedback = playerFeedback,
            frameIndex = uiState.battleFeedbackFrameIndex
        )
        val shouldForceHideEnemy = (!battleManager.isBattleActive && uiState.visualWildStats.currentHp <= 0) ||
                (isTrainerBattle &&
                        !battleManager.isBattleActive &&
                        (uiState.currentBattleMessage == "You won!" ||
                                uiState.revealedEnemyDefeatCount >= battleManager.enemy.team.size))
        val wildAlpha = fighterAlpha(
            currentHp = uiState.visualWildStats.currentHp,
            hasPendingFaint = wildFaintPending,
            isHiddenAfterFaint = BattleSide.Enemy in uiState.hiddenFaintedSides,
            activeFeedback = wildFeedback,
            frameIndex = uiState.battleFeedbackFrameIndex
        ) * captureTargetAlpha(activeCaptureAnimation, uiState.activeCaptureProgress) *
                if (uiState.isEnemyCapturedHidden || isEnemyHeldInCaptureStone || shouldForceHideEnemy) 0f else 1f

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
                name = playerStatus.name,
                level = playerStatus.level,
                currentHp = playerStatus.currentHp,
                maxHp = playerStatus.maxHp,
                currentExp = playerStatus.currentExp,
                expToNextLevel = playerStatus.expToNextLevel,
                attackStage = playerStatus.attackStage,
                defenceStage = playerStatus.defenceStage,
                speedStage = playerStatus.speedStage,
                refreshKey = playerStatus.refreshKey,
                modifier = Modifier
                    .width(statusWidth)
                    .offset(
                        x = 22.dp,
                        y = 14.dp
                    )
            )

            StatusPlate(
                name = wildStatus.name,
                level = wildStatus.level,
                currentHp = wildStatus.currentHp,
                maxHp = wildStatus.maxHp,
                currentExp = wildStatus.currentExp,
                expToNextLevel = wildStatus.expToNextLevel,
                attackStage = wildStatus.attackStage,
                defenceStage = wildStatus.defenceStage,
                speedStage = wildStatus.speedStage,
                refreshKey = wildStatus.refreshKey,
                modifier = Modifier
                    .width(statusWidth)
                    .offset(
                        x = screenWidth - statusWidth - 22.dp,
                        y = 14.dp
                    )
            )

            if (isTrainerBattle) {
                TrainerBindingStoneColumn(
                    teamSize = battleManager.enemy.team.size,
                    defeatedCount = uiState.revealedEnemyDefeatCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 60.dp, end = 24.dp)
                )
            }

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
                    progress = uiState.activeCaptureProgress,
                    startX = playerPlatformX,
                    startY = platformY - spriteSize * 0.42f,
                    targetX = captureTargetX,
                    targetY = captureTargetY,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                uiState.captureResultAnimation?.let { animation ->
                    CaptureResultBindingStone(
                        caught = animation.captureSucceeded,
                        isResultRevealed = uiState.isCaptureResultRevealed,
                        targetX = captureTargetX,
                        targetY = captureTargetY,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            BattlePanel(
                message = uiState.currentBattleMessage,
                mode = uiState.panelMode,
                isTeamSelectionForced = battleManager.isWaitingForPlayerSwitch,
                presentation = panelPresentation,
                onFight = { uiState.openPanel(BattlePanelMode.Moves) },
                onBag = { uiState.openPanel(BattlePanelMode.Bag) },
                onTeam = { uiState.openPanel(BattlePanelMode.Team) },
                onMoveSelected = { moveOption ->
                    uiState.performBattleAction(BattleAction.UseMove(moveOption.move))
                },
                onMoveReplacementSelected = { index ->
                    uiState.resolvePendingMoveLearning(index)
                },
                onSwitchSelected = { chimeraSlot ->
                    uiState.performBattleAction(BattleAction.SwitchChimera(chimeraSlot.chimera))
                },
                onItemSelected = { itemOption ->
                    if (itemOption.isCaptureItem && isTrainerBattle) {
                        uiState.showTrainerCaptureBlocked()
                    } else if (itemOption.isCaptureItem) {
                        uiState.performBattleAction(BattleAction.UseItem(itemOption.item))
                    } else {
                        uiState.selectBattleItem(itemOption.item)
                    }
                },
                onItemTargetSelected = { chimeraSlot ->
                    uiState.useSelectedBattleItemOn(chimeraSlot.chimera)
                },
                onRun = {
                    uiState.performBattleAction(BattleAction.Run)
                },
                onBackToActions = {
                    uiState.backToActionSelection()
                },
                colors = colors,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }

        uiState.activeEvolutionEvent?.let { event ->
            EvolutionOverlay(
                event = event,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Renders the trainer binding stone column UI. */
@Composable
private fun TrainerBindingStoneColumn(
    teamSize: Int,
    defeatedCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(MaxBattleTeamSize) { index ->
            val isTrainerSlot = index < teamSize
            val isDefeated = index < defeatedCount
            Image(
                painter = painterResource(
                    id = when {
                        isDefeated -> R.drawable.binding_stone_broken
                        isTrainerSlot -> R.drawable.binding_stone_captured
                        else -> R.drawable.binding_stone_base
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        alpha = if (isTrainerSlot) 1f else 0.7f
                    }
            )
        }
    }
}


