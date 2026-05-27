package com.example.chimeralis.ui.navigation.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.data.GameSaveStore
import com.example.chimeralis.data.SavedGameLocation
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.navigation.GameScreen
import com.example.chimeralis.ui.screens.world.Direction

/** Represents the game session state. */
class GameSessionState(
    internal val saveStore: GameSaveStore
) {
    var currentScreen by mutableStateOf(GameScreen.Splash)
    var returnWorldScreen by mutableStateOf(GameScreen.LavaField)
    var musicScreen by mutableStateOf(GameScreen.Splash)
    var selectedStarter by mutableStateOf<ChimeraSpecies?>(null)
    var starterNickname by mutableStateOf("")
    var player by mutableStateOf<Player?>(null)
    var teamVersion by mutableIntStateOf(0)
    var trainerName by mutableStateOf("")
    var playerColumn by mutableIntStateOf(1)
    var playerRow by mutableIntStateOf(1)
    var playerDirection by mutableStateOf(Direction.Down)
    var worldInputLockKey by mutableIntStateOf(0)
    var shiftNpcIntroSeen by mutableStateOf(false)
    var lastSavedColumn by mutableIntStateOf(1)
    var lastSavedRow by mutableIntStateOf(1)
    var lastSavedLocation by mutableStateOf(SavedGameLocation.LavaField)
    var lastSavedTeamSignature by mutableStateOf("")
    var trainerNameError by mutableStateOf<String?>(null)
    var wildEncounter by mutableStateOf<ChimeraSpecies?>(null)
    var trainerBattleKey by mutableStateOf<Int?>(null)
    var saves by mutableStateOf(saveStore.loadAll())
    var isScreenTransitionRunning by mutableStateOf(false)
    var musicEnabled by mutableStateOf(true)
    var isBattleResultMusicSuppressed by mutableStateOf(false)
    var musicVolume by mutableFloatStateOf(0.62f)
    var soundEnabled by mutableStateOf(true)
    var soundVolume by mutableFloatStateOf(1f)
    var encounterChance by mutableFloatStateOf(0.22f)

    val teamSignature: String
        get() = player?.teamSignature().orEmpty() + "|$teamVersion"

    val canStartBattles: Boolean
        get() = player?.isDefeated() == false

    val hasUnsavedChanges: Boolean
        get() = playerColumn != lastSavedColumn ||
                playerRow != lastSavedRow ||
                currentSaveLocation() != lastSavedLocation ||
                teamSignature != lastSavedTeamSignature
}

/** Remembers the remember game session state state. */
@Composable
fun rememberGameSessionState(saveStore: GameSaveStore): GameSessionState {
    return remember(saveStore) { GameSessionState(saveStore) }
}
