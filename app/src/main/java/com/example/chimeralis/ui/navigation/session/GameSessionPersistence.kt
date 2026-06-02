package com.example.chimeralis.ui.navigation.session

import com.example.chimeralis.data.save.GameSave
import com.example.chimeralis.data.save.SavedGameLocation
import com.example.chimeralis.ui.navigation.GameScreen
import com.example.chimeralis.ui.screens.world.model.Direction

/**
 * Handles refresh saves behavior.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
fun GameSessionState.refreshSaves() {
    saves = saveStore.loadAll()
}

/**
 * Loads the load save.
 *
 * @receiver The game session state receiver used by this operation.
 * @param save The save value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Handles mark saved behavior.
 *
 * @receiver The game session state receiver used by this operation.
 * @param column Numeric value used by this operation: column.
 * @param row Numeric value used by this operation: row.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
fun GameSessionState.markSaved(column: Int, row: Int) {
    val currentPlayer = player ?: return
    lastSavedColumn = column
    lastSavedRow = row
    lastSavedLocation = currentSaveLocation()
    lastSavedTeamSignature = currentPlayer.teamSignature() + "|$teamVersion"
    refreshSaves()
}

/**
 * Saves the save current game.
 *
 * @receiver The game session state receiver used by this operation.
 * @param column Numeric value used by this operation: column.
 * @param row Numeric value used by this operation: row.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
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

/**
 * Handles current save location behavior.
 *
 * @receiver The game session state receiver used by this operation.
 * @return The resulting SavedGameLocation value.
 */
fun GameSessionState.currentSaveLocation(): SavedGameLocation {
    return currentScreen.toSavedGameLocation() ?: returnWorldScreen.toSavedGameLocation()
    ?: SavedGameLocation.LavaField
}

/**
 * Converts data into game screen.
 *
 * @receiver The saved game location receiver used by this operation.
 * @return The resulting GameScreen value.
 */
fun SavedGameLocation.toGameScreen(): GameScreen {
    return when (this) {
        SavedGameLocation.LavaField -> GameScreen.LavaField
        SavedGameLocation.GrassField -> GameScreen.GrassField
        SavedGameLocation.ChimeraCenterInterior -> GameScreen.ChimeraCenterInterior
        SavedGameLocation.ChimeraStoreInterior -> GameScreen.ChimeraStoreInterior
    }
}

/**
 * Handles return world screen behavior.
 *
 * @receiver The saved game location receiver used by this operation.
 * @return The resulting GameScreen value.
 */
private fun SavedGameLocation.returnWorldScreen(): GameScreen {
    return when (this) {
        SavedGameLocation.LavaField -> GameScreen.LavaField
        SavedGameLocation.GrassField,
        SavedGameLocation.ChimeraCenterInterior,
        SavedGameLocation.ChimeraStoreInterior -> GameScreen.GrassField
    }
}

/**
 * Converts data into saved game location.
 *
 * @receiver The game screen receiver used by this operation.
 * @return The resolved saved game location value, or null when it is unavailable.
 */
private fun GameScreen.toSavedGameLocation(): SavedGameLocation? {
    return when (this) {
        GameScreen.LavaField -> SavedGameLocation.LavaField
        GameScreen.GrassField -> SavedGameLocation.GrassField
        GameScreen.ChimeraCenterInterior -> SavedGameLocation.ChimeraCenterInterior
        GameScreen.ChimeraStoreInterior -> SavedGameLocation.ChimeraStoreInterior
        else -> null
    }
}
