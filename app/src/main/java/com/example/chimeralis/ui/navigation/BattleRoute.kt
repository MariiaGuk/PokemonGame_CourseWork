package com.example.chimeralis.ui.navigation

import androidx.compose.runtime.Composable
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.battle.BattleScreen

@Composable
internal fun GameSessionState.BattleRoute(transition: GameTransitionState) {
    player?.let { currentPlayer ->
        if (currentPlayer.isDefeated()) {
            currentScreen = returnWorldScreen
        } else {
            val isTrainerBattle = trainerBattleKey != null
            BattleScreen(
                player = currentPlayer,
                battleKey = trainerBattleKey ?: wildEncounter,
                wildSpecies = wildEncounter ?: ChimeraSpecies.Sylvhorn,
                isTrainerBattle = isTrainerBattle,
                onBattleResultSoundStarted = {
                    isBattleResultMusicSuppressed = true
                },
                onBattleFinished = {
                    teamVersion++
                    selectedStarter = currentPlayer.activeChimera.species
                    starterNickname = currentPlayer.activeChimera.name
                    worldInputLockKey++
                    transition.transitionTo(returnWorldScreen, this)
                }
            )
        }
    } ?: run {
        currentScreen = returnWorldScreen
    }
}
