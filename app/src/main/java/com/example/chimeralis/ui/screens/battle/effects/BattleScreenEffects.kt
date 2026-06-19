package com.example.chimeralis.ui.screens.battle.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.screens.battle.BattleEndInputLockMillis
import com.example.chimeralis.ui.screens.battle.BattleIntroInputLockMillis
import com.example.chimeralis.ui.screens.battle.components.EvolutionOverlayDurationMillis
import com.example.chimeralis.ui.screens.battle.components.EvolutionRevealMillis
import com.example.chimeralis.ui.screens.battle.presentation.model.BattlePanelMode
import com.example.chimeralis.ui.screens.battle.state.BattleUiState
import kotlinx.coroutines.delay

/**
 * Runs battle-screen side effects that react to UI state changes.
 *
 * @param player Domain object used by this operation: player.
 * @param battleManager The battle manager value used by this operation.
 * @param uiState The ui state value used by this operation.
 * @param playerChimera The player chimera value used by this operation.
 * @param wildChimera The wild chimera value used by this operation.
 * @param isTrainerBattle Flag that controls or describes is trainer battle.
 * @param battleSoundEvents The battle sound events value used by this operation.
 * @param onBattleFinished Callback invoked when battle finished occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun BattleScreenEffects(
    player: Player,
    battleManager: BattleManager,
    uiState: BattleUiState,
    playerChimera: Chimera,
    wildChimera: Chimera,
    isTrainerBattle: Boolean,
    battleSoundEvents: BattleSoundEventHandler,
    onBattleFinished: () -> Unit
) {
    LaunchedEffect(battleManager) {
        delay(BattleIntroInputLockMillis)
        uiState.unlockBattleIntro()
    }

    LaunchedEffect(uiState.isBattleExitPending) {
        if (!uiState.isBattleExitPending) return@LaunchedEffect

        delay(BattleEndInputLockMillis)
        uiState.pendingEvolutionEvents.forEach { event ->
            uiState.showEvolution(event)
            battleSoundEvents.playEvolutionRevealSounds()
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

        uiState.playLogAnimation(animation, battleSoundEvents::playAnimationSound)
    }

    LaunchedEffect(uiState.panelMode, uiState.battleLogIndex, uiState.currentBattleMessage) {
        if (uiState.panelMode != BattlePanelMode.Log) return@LaunchedEffect

        if (uiState.shouldRevealEnemyDefeatForCurrentMessage(isTrainerBattle)) {
            uiState.revealEnemyDefeatForCurrentMessage()
        }
        uiState.syncPlayerProgressForCurrentMessage(playerChimera)
        battleSoundEvents.playLogMessageSound(
            message = uiState.currentBattleMessage,
            isLevelUpMessage = uiState.isCurrentMessageLevelUp()
        )
    }

    LaunchedEffect(playerChimera, playerChimera.stats.currentHp, wildChimera, wildChimera.stats.currentHp) {
        uiState.showRecoveredFighters(playerChimera, wildChimera)
    }
}
