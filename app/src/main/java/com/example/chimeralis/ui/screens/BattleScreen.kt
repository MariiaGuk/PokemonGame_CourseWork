package com.example.chimeralis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.logic.battle.BattleAction
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily

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
    var panelMode by remember(battleManager) { mutableStateOf(BattlePanelMode.Actions) }
    var battleMessage by remember(battleManager) {
        mutableStateOf("A wild ${battleManager.enemyChimera.name} appeared!")
    }
    var uiVersion by remember(battleManager) { mutableIntStateOf(0) }
    val refreshKey = uiVersion
    val playerChimera = battleManager.playerChimera
    val wildChimera = battleManager.enemyChimera

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                message = battleMessage,
                mode = panelMode,
                moves = playerChimera.moves,
                isBattleActive = battleManager.isBattleActive,
                onFight = { panelMode = BattlePanelMode.Moves },
                onMoveSelected = { move ->
                    val log = battleManager.performTurn(BattleAction.UseMove(move))
                    battleMessage = log.joinToString("\n")
                    panelMode = BattlePanelMode.Log
                    uiVersion++
                },
                onRun = {
                    val log = battleManager.performTurn(BattleAction.Run)
                    battleMessage = log.joinToString("\n")
                    panelMode = BattlePanelMode.Log
                    uiVersion++
                },
                onBackToActions = { panelMode = BattlePanelMode.Actions },
                onContinue = {
                    if (battleManager.isBattleActive) {
                        panelMode = BattlePanelMode.Actions
                    } else {
                        onBattleFinished()
                    }
                },
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
    isBattleActive: Boolean,
    onFight: () -> Unit,
    onMoveSelected: (com.example.chimeralis.logic.chimeras.moves.Move) -> Unit,
    onRun: () -> Unit,
    onBackToActions: () -> Unit,
    onContinue: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(colors.surface.copy(alpha = 0.9f))
            .padding(25.dp,7.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BattleMessage(text = message, modifier = Modifier.weight(1f))

        when (mode) {
            BattlePanelMode.Actions -> BattleActionButtons(
                onFight = onFight,
                onRun = onRun
            )
            BattlePanelMode.Moves -> MoveButtons(
                moves = moves,
                onMoveSelected = onMoveSelected,
                onBack = onBackToActions
            )
            BattlePanelMode.Log -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton(
                        text = if (isBattleActive) "Continue" else "Finish",
                        onClick = onContinue
                    )
                }
            }
        }
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
    onRun: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Fight", onClick = onFight)
            MenuButton(text = "Bag", onClick = {})
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Team", onClick = {})
            MenuButton(text = "Run", onClick = onRun)
        }
    }
}

@Composable
private fun MoveButtons(
    moves: List<com.example.chimeralis.logic.chimeras.moves.Move>,
    onMoveSelected: (com.example.chimeralis.logic.chimeras.moves.Move) -> Unit,
    onBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
        MenuButton(text = "Back", onClick = onBack)
    }
}

private enum class BattlePanelMode {
    Actions,
    Moves,
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
