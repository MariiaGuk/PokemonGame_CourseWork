package com.example.chimeralis.ui.navigation

import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.screens.world.Direction

fun GameSessionState.resetForNewGame() {
    trainerName = ""
    trainerNameError = null
    selectedStarter = null
    starterNickname = ""
    player = null
    teamVersion = 0
    playerColumn = 1
    playerRow = 1
    playerDirection = Direction.Down
    worldInputLockKey = 0
    shiftNpcIntroSeen = false
    returnWorldScreen = GameScreen.LavaField
    lastSavedColumn = 1
    lastSavedRow = 1
    lastSavedTeamSignature = ""
    wildEncounter = null
}

fun GameSessionState.startNewGame(starter: ChimeraSpecies, nickname: String) {
    val starterChimera = ChimeraFactory.createChimera(starter, level = 5)
    if (nickname.isNotBlank()) {
        starterChimera.rename(nickname)
    }

    selectedStarter = starter
    starterNickname = starterChimera.name
    player = Player(
        name = trainerName,
        team = mutableListOf(starterChimera),
        inventory = createStartingInventory(),
        money = StartingMoney
    )
    teamVersion = 0
    playerColumn = 1
    playerRow = 1
    playerDirection = Direction.Down
    worldInputLockKey = 0
    shiftNpcIntroSeen = false
    returnWorldScreen = GameScreen.LavaField
    lastSavedColumn = 1
    lastSavedRow = 1
}

private fun createStartingInventory(): Inventory {
    return Inventory().also { inventory ->
        inventory.addItem(ItemFactory.createItem(ItemName.POTION), 3)
        inventory.addItem(ItemFactory.createItem(ItemName.SUPER_POTION), 1)
        inventory.addItem(ItemFactory.createItem(ItemName.REVIVE), 1)
        inventory.addItem(ItemFactory.createItem(ItemName.BINDING_STONE), 5)
    }
}

private const val StartingMoney = 200
