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
            BattleScreen(
                player = currentPlayer,
                battleKey = wildEncounter,
                wildSpecies = wildEncounter ?: ChimeraSpecies.Sylvhorn,
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
