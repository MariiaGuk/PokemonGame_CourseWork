package com.example.chimeralis.ui.screens

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
    var activeBattleFeedbacks by remember(battleManager) { mutableStateOf<List<BattleFeedback>>(emptyList()) }
    var battleFeedbackFrameIndex by remember(battleManager) { mutableIntStateOf(0) }
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
        activeBattleFeedbacks = emptyList()
        battleFeedbackFrameIndex = 0
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

        if (battleLogIndex < battleLogMessages.lastIndex) {
            battleLogIndex++
        } else if (battleManager.isBattleActive) {
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
            return@LaunchedEffect
        }

        activeMoveAnimation = animation
        val frames = animation.animationFrames()
        frames.forEachIndexed { frameIndex, frame ->
            activeMoveFrameIndex = frameIndex
            val feedbacks = frame.feedbacks.toBattleFeedbacks()

            if (feedbacks.isEmpty()) {
                delay(frame.durationMillis)
            } else {
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
                activeBattleFeedbacks = emptyList()
                battleFeedbackFrameIndex = 0
            }
        }

        delay(90L)

        activeMoveAnimation = null
        activeMoveFrameIndex = 0
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
        val spriteSize = minOf(maxWidth * 0.25f, maxHeight * 0.45f)
        val playerAnimation = activeMoveAnimation?.takeIf { it.side == BattleSide.Player }
        val wildAnimation = activeMoveAnimation?.takeIf { it.side == BattleSide.Enemy }
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
        val playerTintColor = playerFeedback.tintColor()
        val wildTintColor = wildFeedback.tintColor()

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
                tintColor = playerTintColor,
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
                tintColor = wildTintColor,
                modifier = Modifier.offset(
                    x = wildPlatformX - spriteFrameWidth / 2f,
                    y = platformY - spriteSize * 0.75f
                )
            )

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
    tintColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = tintColor?.let { ColorFilter.tint(it, BlendMode.SrcAtop) },
        modifier = modifier
            .offset(x = effectOffsetX)
            .width(spriteWidth)
            .height(spriteHeight)
            .graphicsLayer {
                scaleX = if (mirrored) -1f else 1f
            }
    )
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
                        MenuButton(
                            text = "${item.name} x$amount",
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
                BattleMoveFeedbackType.StatChange -> BattleFeedbackType.StatChange
            }
        )
    }
}

private fun BattleFeedback?.shakeOffset(frameIndex: Int): Dp {
    if (this?.type != BattleFeedbackType.Damage) return 0.dp

    return when (frameIndex % 4) {
        0 -> (-7).dp
        1 -> 7.dp
        2 -> (-4).dp
        else -> 4.dp
    }
}

private fun BattleFeedback?.tintColor(): Color? {
    return when (this?.type) {
        BattleFeedbackType.Damage -> Color(0xFFFF3535).copy(alpha = 0.42f)
        BattleFeedbackType.StatChange -> Color(0xFF49A7FF).copy(alpha = 0.42f)
        null -> null
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
