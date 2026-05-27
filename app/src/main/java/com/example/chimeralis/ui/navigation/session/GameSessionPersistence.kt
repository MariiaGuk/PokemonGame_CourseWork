package com.example.chimeralis.ui.navigation.session

import com.example.chimeralis.data.GameSave
import com.example.chimeralis.data.SavedGameLocation
import com.example.chimeralis.ui.navigation.GameScreen
import com.example.chimeralis.ui.screens.world.Direction

/** Handles refresh saves behavior. */
fun GameSessionState.refreshSaves() {
    saves = saveStore.loadAll()
}

/** Loads the load save. */
fun GameSessionState.loadSave(save: GameSave) {
    val loadedPlayer = saveStore.createPlayer(save)
    trainerName = save.trainerName
    player = loadedPlayer
    teamVersion = 0
    selectedStarter = loadedPlayer.activeChimera.species
    starterNickname = loadedPlayer.activeChimera.name
    playerColumn = save.playerColumn
    playerRow = save.playerRow
    playerDirection = Direction.Down
    worldInputLockKey = 0
    shiftNpcIntroSeen = false
    returnWorldScreen = save.location.returnWorldScreen()
    lastSavedColumn = save.playerColumn
    lastSavedRow = save.playerRow
    lastSavedLocation = save.location
    lastSavedTeamSignature = loadedPlayer.teamSignature() + "|0"
    wildEncounter = null
}

/** Handles mark saved behavior. */
fun GameSessionState.markSaved(column: Int, row: Int) {
    val currentPlayer = player ?: return
    lastSavedColumn = column
    lastSavedRow = row
    lastSavedLocation = currentSaveLocation()
    lastSavedTeamSignature = currentPlayer.teamSignature() + "|$teamVersion"
    refreshSaves()
}

/** Saves the save current game. */
fun GameSessionState.saveCurrentGame(
    column: Int = playerColumn,
    row: Int = playerRow
): Boolean {
    val currentPlayer = player ?: return false
    if (trainerName.isBlank()) return false

    saveStore.saveFromPlayer(
        trainerName = trainerName,
        player = currentPlayer,
        playerColumn = column,
        playerRow = row,
        location = currentSaveLocation()
    )
    markSaved(column, row)
    return true
}

/** Handles current save location behavior. */
fun GameSessionState.currentSaveLocation(): SavedGameLocation {
    return currentScreen.toSavedGameLocation() ?: returnWorldScreen.toSavedGameLocation()
    ?: SavedGameLocation.LavaField
}

/** Converts data into game screen. */
fun SavedGameLocation.toGameScreen(): GameScreen {
    return when (this) {
        SavedGameLocation.LavaField -> GameScreen.LavaField
        SavedGameLocation.GrassField -> GameScreen.GrassField
        SavedGameLocation.ChimeraCenterInterior -> GameScreen.ChimeraCenterInterior
        SavedGameLocation.ChimeraStoreInterior -> GameScreen.ChimeraStoreInterior
    }
}

/** Handles return world screen behavior. */
private fun SavedGameLocation.returnWorldScreen(): GameScreen {
    return when (this) {
        SavedGameLocation.LavaField -> GameScreen.LavaField
        SavedGameLocation.GrassField,
        SavedGameLocation.ChimeraCenterInterior,
        SavedGameLocation.ChimeraStoreInterior -> GameScreen.GrassField
    }
}

/** Converts data into saved game location. */
private fun GameScreen.toSavedGameLocation(): SavedGameLocation? {
    return when (this) {
        GameScreen.LavaField -> SavedGameLocation.LavaField
        GameScreen.GrassField -> SavedGameLocation.GrassField
        GameScreen.ChimeraCenterInterior -> SavedGameLocation.ChimeraCenterInterior
        GameScreen.ChimeraStoreInterior -> SavedGameLocation.ChimeraStoreInterior
        else -> null
    }
}
