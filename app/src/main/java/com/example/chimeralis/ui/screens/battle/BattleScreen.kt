package com.example.chimeralis.ui.screens.battle

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.logic.battle.scenario.BattleScenarioFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.screens.battle.components.BattleArenaScene
import com.example.chimeralis.ui.screens.battle.effects.BattleScreenEffects
import com.example.chimeralis.ui.screens.battle.effects.BattleSoundEventHandler
import com.example.chimeralis.ui.screens.battle.presentation.toBattlePanelPresentation
import com.example.chimeralis.ui.screens.battle.presentation.toEnemyStatusPresentation
import com.example.chimeralis.ui.screens.battle.presentation.toPlayerStatusPresentation
import com.example.chimeralis.ui.screens.battle.state.rememberBattleUiState

/**
 * Renders the battle screen UI.
 *
 * @param player Domain object used by this operation: player.
 * @param battleKey The battle key value used by this operation.
 * @param wildSpecies The wild species value used by this operation.
 * @param isTrainerBattle Flag that controls or describes is trainer battle.
 * @param onBattleResultSoundStarted Callback invoked when battle result sound started occurs.
 * @param onBattleFinished Callback invoked when battle finished occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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
    val battleSoundEvents = remember(context, onBattleResultSoundStarted) {
        BattleSoundEventHandler(
            context = context,
            onBattleResultSoundStarted = onBattleResultSoundStarted
        )
    }
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

    BattleScreenEffects(
        player = player,
        battleManager = battleManager,
        uiState = uiState,
        playerChimera = playerChimera,
        wildChimera = wildChimera,
        isTrainerBattle = isTrainerBattle,
        battleSoundEvents = battleSoundEvents,
        onBattleFinished = onBattleFinished
    )

    BattleArenaScene(
        battleManager = battleManager,
        uiState = uiState,
        playerStatus = playerStatus,
        wildStatus = wildStatus,
        panelPresentation = panelPresentation,
        isTrainerBattle = isTrainerBattle,
        colors = colors
    )
}
