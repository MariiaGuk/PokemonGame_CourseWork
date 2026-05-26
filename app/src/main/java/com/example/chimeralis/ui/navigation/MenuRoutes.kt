package com.example.chimeralis.ui.navigation

import androidx.compose.runtime.Composable
import com.example.chimeralis.ui.screens.menu.ContinueScreen
import com.example.chimeralis.ui.screens.menu.MainMenuScreen
import com.example.chimeralis.ui.screens.onboarding.StarterSelectionScreen
import com.example.chimeralis.ui.screens.onboarding.TrainerNameScreen

@Composable
internal fun GameSessionState.MainMenuRoute(onExitGame: () -> Unit) {
    MainMenuScreen(
        musicEnabled = musicEnabled,
        musicVolume = musicVolume,
        soundEnabled = soundEnabled,
        soundVolume = soundVolume,
        encounterChance = encounterChance,
        onMusicEnabledChanged = { musicEnabled = it },
        onMusicVolumeChanged = { musicVolume = it },
        onSoundEnabledChanged = { soundEnabled = it },
        onSoundVolumeChanged = { soundVolume = it },
        onEncounterChanceChanged = { encounterChance = it },
        onNewGame = {
            resetForNewGame()
            currentScreen = GameScreen.TrainerName
        },
        onContinue = {
            refreshSaves()
            currentScreen = GameScreen.Continue
        },
        onExitGame = onExitGame
    )
}

@Composable
internal fun GameSessionState.ContinueRoute() {
    ContinueScreen(
        saves = saves,
        onLoad = { save ->
            loadSave(save)
            currentScreen = GameScreen.LavaField
        },
        onDelete = { save ->
            saveStore.delete(save.trainerName)
            refreshSaves()
        },
        onBack = { currentScreen = GameScreen.MainMenu }
    )
}

@Composable
internal fun GameSessionState.TrainerNameRoute() {
    TrainerNameScreen(
        onNameConfirmed = { name ->
            if (saveStore.hasSaveForTrainer(name)) {
                trainerNameError = "A trainer with this name already exists."
            } else {
                trainerName = name
                trainerNameError = null
                currentScreen = GameScreen.StarterSelection
            }
        },
        onBack = {
            trainerNameError = null
            currentScreen = GameScreen.MainMenu
        },
        errorMessage = trainerNameError,
        onNameEdited = { trainerNameError = null }
    )
}

@Composable
internal fun GameSessionState.StarterSelectionRoute() {
    StarterSelectionScreen(
        onStarterSelected = { starter, nickname ->
            startNewGame(starter, nickname)
            saveCurrentGame(column = 1, row = 1)
            currentScreen = GameScreen.LavaField
        },
        onBack = { currentScreen = GameScreen.TrainerName }
    )
}
