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
internal fun BattlePanel(
    message: String,
    mode: BattlePanelMode,
    isTeamSelectionForced: Boolean,
    moves: List<Move>,
    pendingMoveLearning: MoveLearnRequest?,
    team: List<Chimera>,
    activeChimera: Chimera,
    inventoryItems: Map<Item, Int>,
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onMoveSelected: (Move) -> Unit,
    onMoveReplacementSelected: (Int?) -> Unit,
    onSwitchSelected: (Chimera) -> Unit,
    onItemSelected: (Item) -> Unit,
    selectedItem: Item?,
    onItemTargetSelected: (Chimera) -> Unit,
    onRun: () -> Unit,
    onBackToActions: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val showBackArrow = mode == BattlePanelMode.Moves ||
            mode == BattlePanelMode.Bag ||
            mode == BattlePanelMode.ItemTarget ||
            mode == BattlePanelMode.MoveLearning ||
            (mode == BattlePanelMode.Team && !isTeamSelectionForced)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(colors.surface.copy(alpha = 0.9f))
    ) {
        if (showBackArrow) {
            BattleBackArrowButton(
                onClick = {
                    if (mode == BattlePanelMode.MoveLearning) {
                        onMoveReplacementSelected(null)
                    } else {
                        onBackToActions()
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = BattlePanelHorizontalPadding)
            )
        }

        if (mode == BattlePanelMode.Log || mode == BattlePanelMode.MoveLearning) {
            BattleMessage(
                text = if (mode == BattlePanelMode.MoveLearning) {
                    pendingMoveLearning?.let { request ->
                        "${request.chimera.name} wants to learn ${request.move.name}.\nForget which move? Back keeps old moves."
                    } ?: message
                } else {
                    message
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (mode == BattlePanelMode.MoveLearning) {
                            BattlePanelHorizontalPadding + BattleBackButtonSize + BattleBackButtonGap
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        top = 16.dp,
                        end = if (mode == BattlePanelMode.MoveLearning) {
                            400.dp
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        bottom = 16.dp
                    )
            )
        }

        if (mode != BattlePanelMode.Log) {
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
                        team = team,
                        onItemSelected = onItemSelected
                    )
                    BattlePanelMode.ItemTarget -> BattleItemTargetButtons(
                        item = selectedItem,
                        team = team,
                        onItemTargetSelected = onItemTargetSelected
                    )
                    BattlePanelMode.Team -> BattleTeamButtons(
                        team = team,
                        activeChimera = activeChimera,
                        onSwitchSelected = onSwitchSelected
                    )
                    BattlePanelMode.MoveLearning -> MoveLearningButtons(
                        request = pendingMoveLearning,
                        onReplacementSelected = onMoveReplacementSelected
                    )
                    BattlePanelMode.Log -> Unit
                }
            }
        }
    }
}

@Composable
internal fun BattleBackArrowButton(
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
internal fun BattleMessage(
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
internal fun BattleActionButtons(
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
internal fun BattleTeamButtons(
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
internal fun EmptyBattleTeamSlot() {
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
internal fun BattleTeamSlot(
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
internal fun BattleInventoryButtons(
    inventoryItems: Map<Item, Int>,
    team: List<Chimera>,
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
                        val canUseItem = item.isCaptureItem || team.any { item.canUseOn(it) }
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
internal fun BattleItemTargetButtons(
    item: Item?,
    team: List<Chimera>,
    onItemTargetSelected: (Chimera) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.End
    ) {
        val slots = List(MaxBattleTeamSize) { index -> team.getOrNull(index) }
        slots.chunked(3).forEach { rowTeam ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTeam.forEach { chimera ->
                    if (chimera == null) {
                        EmptyBattleTeamSlot()
                    } else {
                        BattleItemTargetSlot(
                            chimera = chimera,
                            canUseItem = item?.canUseOn(chimera) == true,
                            onItemTargetSelected = onItemTargetSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BattleItemTargetSlot(
    chimera: Chimera,
    canUseItem: Boolean,
    onItemTargetSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val hpRatio = (chimera.stats.currentHp.toFloat() / chimera.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    val hpPercent = (hpRatio * 100).roundToInt()

    Row(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF3E443E).copy(alpha = 0.88f))
            .border(
                width = if (canUseItem) 2.dp else 1.dp,
                color = colors.primary.copy(alpha = if (canUseItem) 0.78f else 0.22f),
                shape = RoundedCornerShape(3.dp)
            )
            .pointerInput(canUseItem, chimera) {
                detectTapGestures(
                    onTap = {
                        if (canUseItem) {
                            onItemTargetSelected(chimera)
                        }
                    }
                )
            }
            .padding(3.dp)
            .graphicsLayer { alpha = if (canUseItem) 1f else 0.42f },
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
internal fun MoveLearningButtons(
    request: MoveLearnRequest?,
    onReplacementSelected: (Int?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (request == null) {
            MenuButton(text = "Continue", onClick = { onReplacementSelected(null) })
            return
        }

        val moveSlots = List(4) { index -> request.chimera.moves.getOrNull(index) }
        moveSlots.chunked(2).forEachIndexed { rowIndex, rowMoves ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMoves.forEachIndexed { columnIndex, move ->
                    if (move == null) {
                        Spacer(modifier = Modifier.width(BattleMenuButtonWidth))
                    } else {
                        val moveIndex = rowIndex * 2 + columnIndex
                        MenuButton(
                            text = move.name,
                            onClick = { onReplacementSelected(moveIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun MoveButtons(
    moves: List<Move>,
    onMoveSelected: (Move) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        val moveSlots = List(4) { index -> moves.getOrNull(index) }
        moveSlots.chunked(2).forEach { rowMoves ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMoves.forEach { move ->
                    if (move == null) {
                        Spacer(modifier = Modifier.width(BattleMenuButtonWidth))
                    } else {
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
}
