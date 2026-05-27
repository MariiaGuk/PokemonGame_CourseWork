package com.example.chimeralis.ui.navigation.routes

import androidx.compose.runtime.Composable
import com.example.chimeralis.ui.navigation.GameScreen
import com.example.chimeralis.ui.navigation.session.GameSessionState
import com.example.chimeralis.ui.navigation.session.loadSave
import com.example.chimeralis.ui.navigation.session.refreshSaves
import com.example.chimeralis.ui.navigation.session.resetForNewGame
import com.example.chimeralis.ui.navigation.session.saveCurrentGame
import com.example.chimeralis.ui.navigation.session.startNewGame
import com.example.chimeralis.ui.navigation.session.toGameScreen
import com.example.chimeralis.ui.screens.menu.ContinueScreen
import com.example.chimeralis.ui.screens.menu.MainMenuScreen
import com.example.chimeralis.ui.screens.onboarding.StarterSelectionScreen
import com.example.chimeralis.ui.screens.onboarding.TrainerNameScreen

/** Renders the main menu route UI. */
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

/** Renders the continue route UI. */
@Composable
internal fun GameSessionState.ContinueRoute() {
    ContinueScreen(
        saves = saves,
        onLoad = { save ->
            loadSave(save)
            currentScreen = save.location.toGameScreen()
        },
        onDelete = { save ->
            saveStore.delete(save.trainerName)
            refreshSaves()
        },
        onBack = { currentScreen = GameScreen.MainMenu }
    )
}

/** Renders the trainer name route UI. */
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

/** Renders the starter selection route UI. */
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
