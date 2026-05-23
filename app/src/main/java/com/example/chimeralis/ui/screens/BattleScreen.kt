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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlin.math.roundToInt

private const val MaxBattleTeamSize = 6
private val BattlePanelHorizontalPadding = 30.dp
private val BattleBackButtonSize = 38.dp
private val BattleBackButtonGap = 14.dp

@Composable
fun BattleScreen(
    player: Player,
    battleKey: Any? = null,
    wildSpecies: ChimeraSpecies,
    onBattleFinished: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
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
    var uiVersion by remember(battleManager) { mutableIntStateOf(0) }
    val refreshKey = uiVersion
    val playerChimera = battleManager.playerChimera
    val wildChimera = battleManager.enemyChimera
    val currentBattleMessage = battleLogMessages.getOrElse(battleLogIndex) { "" }

    fun showBattleLog(messages: List<String>) {
        battleLogMessages = messages.ifEmpty { listOf("Nothing happened.") }
        battleLogIndex = 0
        panelMode = BattlePanelMode.Log
    }

    fun advanceBattleLog() {
        if (panelMode != BattlePanelMode.Log) return

        if (battleLogIndex < battleLogMessages.lastIndex) {
            battleLogIndex++
        } else if (battleManager.isBattleActive) {
            panelMode = BattlePanelMode.Actions
        } else {
            onBattleFinished()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (panelMode == BattlePanelMode.Log) {
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
        val wildPlatformX = maxWidth * 0.70f
        val spriteSize = minOf(maxWidth * 0.25f, maxHeight * 0.45f)

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
                imageRes = playerChimera.species.battleImageRes(),
                mirrored = true,
                spriteSize = spriteSize,
                modifier = Modifier.offset(
                    x = playerPlatformX - spriteSize / 1.5f,
                    y = platformY - spriteSize * 0.75f
                )
            )

            BattleFighter(
                imageRes = wildChimera.species.battleImageRes(),
                mirrored = false,
                spriteSize = spriteSize,
                modifier = Modifier.offset(
                    x = wildPlatformX - spriteSize / 1.5f,
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
                    val log = battleManager.performTurn(BattleAction.UseMove(move))
                    showBattleLog(log)
                    uiVersion++
                },
                onSwitchSelected = { chimera ->
                    val log = battleManager.performTurn(BattleAction.SwitchChimera(chimera))
                    showBattleLog(log)
                    uiVersion++
                },
                onItemSelected = { item ->
                    val log = battleManager.performTurn(BattleAction.UseItem(item))
                    showBattleLog(log)
                    uiVersion++
                },
                onRun = {
                    val log = battleManager.performTurn(BattleAction.Run)
                    showBattleLog(log)
                    uiVersion++
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
    spriteSize: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(spriteSize)
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

    Box(
        modifier = modifier
            .size(BattleBackButtonSize)
            .clip(RoundedCornerShape(5.dp))
            .background(colors.background.copy(alpha = 0.42f))
            .border(1.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
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

private enum class BattlePanelMode {
    Actions,
    Moves,
    Bag,
    Team,
    Log
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
