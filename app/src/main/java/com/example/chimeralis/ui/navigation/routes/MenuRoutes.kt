package com.example.chimeralis.ui.navigation.routes

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
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
import com.example.chimeralis.ui.screens.onboarding.TutorialScreen
import com.example.chimeralis.ui.screens.onboarding.TrainerNameScreen

/**
 * Renders the main menu route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @param onExitGame Callback invoked when exit game occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the continue route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun GameSessionState.ContinueRoute() {
    val context = LocalContext.current

    ContinueScreen(
        saves = saves,
        onLoad = { save ->
            loadSave(save)
            GameSoundPlayer.play(context, R.raw.load_game)
            currentScreen = save.location.toGameScreen()
        },
        onDelete = { save ->
            saveStore.delete(save.trainerName)
            refreshSaves()
        },
        onBack = { currentScreen = GameScreen.MainMenu }
    )
}

/**
 * Renders the trainer name route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the starter selection route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun GameSessionState.StarterSelectionRoute() {
    StarterSelectionScreen(
        onStarterSelected = { starter, nickname ->
            startNewGame(starter, nickname)
            currentScreen = GameScreen.Tutorial
        },
        onBack = { currentScreen = GameScreen.TrainerName }
    )
}

/**
 * Renders the tutorial route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun GameSessionState.TutorialRoute() {
    TutorialScreen(
        trainerName = trainerName,
        starter = selectedStarter,
        starterNickname = starterNickname,
        onBegin = {
            saveCurrentGame(column = 1, row = 1)
            currentScreen = GameScreen.LavaField
        }
    )
}
