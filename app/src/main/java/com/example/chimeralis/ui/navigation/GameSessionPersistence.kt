package com.example.chimeralis.ui.navigation

import com.example.chimeralis.data.GameSave
import com.example.chimeralis.ui.screens.world.Direction

fun GameSessionState.refreshSaves() {
    saves = saveStore.loadAll()
}

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
    returnWorldScreen = GameScreen.LavaField
    lastSavedColumn = save.playerColumn
    lastSavedRow = save.playerRow
    lastSavedTeamSignature = loadedPlayer.teamSignature() + "|0"
    wildEncounter = null
}

fun GameSessionState.markSaved(column: Int, row: Int) {
    val currentPlayer = player ?: return
    lastSavedColumn = column
    lastSavedRow = row
    lastSavedTeamSignature = currentPlayer.teamSignature() + "|$teamVersion"
    refreshSaves()
}

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
        playerRow = row
    )
    markSaved(column, row)
    return true
}
