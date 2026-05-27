package com.example.chimeralis.ui.navigation

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.items.price
import com.example.chimeralis.logic.trainers.PlayerCollectionLimits

/** Uses one inventory item on the selected chimera and marks the team as changed. */
fun GameSessionState.useInventoryItem(item: Item, chimera: Chimera): Boolean {
    val didUse = player?.inventory?.useItem(item, chimera) == true
    if (didUse) {
        teamVersion++
    }
    return didUse
}

/** Restores HP and PP for every chimera in the active team. */
fun GameSessionState.healTeam() {
    player?.team?.forEach { chimera ->
        chimera.stats.restoreHp(chimera.stats.maxHp)
        chimera.moves.forEach { move -> move.restorePp(move.maxPp) }
    }
    teamVersion++
}

/** Reorders two members inside the active team. */
fun GameSessionState.swapTeamMembers(fromIndex: Int, toIndex: Int): Boolean {
    val team = player?.team ?: return false
    if (fromIndex !in team.indices || toIndex !in team.indices || fromIndex == toIndex) return false

    val chimera = team.removeAt(fromIndex)
    team.add(toIndex, chimera)
    player?.resetActiveChimeraToTeamLead()
    teamVersion++
    return true
}

/** Moves a team member into storage while keeping at least one active chimera. */
fun GameSessionState.depositTeamMember(teamIndex: Int): Boolean {
    val currentPlayer = player ?: return false
    if (
        teamIndex !in currentPlayer.team.indices ||
        currentPlayer.team.size <= 1 ||
        currentPlayer.storage.size >= PlayerCollectionLimits.MaxStorageSize
    ) return false

    val chimera = currentPlayer.team.removeAt(teamIndex)
    currentPlayer.storage.add(chimera)
    currentPlayer.resetActiveChimeraToTeamLead()
    teamVersion++
    return true
}

/** Moves a stored chimera into the team when a free team slot exists. */
fun GameSessionState.withdrawStoredChimera(storageIndex: Int): Boolean {
    val currentPlayer = player ?: return false
    if (
        storageIndex !in currentPlayer.storage.indices ||
        currentPlayer.team.size >= PlayerCollectionLimits.MaxTeamSize
    ) return false

    val chimera = currentPlayer.storage.removeAt(storageIndex)
    currentPlayer.team.add(chimera)
    currentPlayer.resetActiveChimeraToTeamLead()
    teamVersion++
    return true
}

/** Swaps one active team member with one stored chimera. */
fun GameSessionState.swapTeamWithStorage(teamIndex: Int, storageIndex: Int): Boolean {
    val currentPlayer = player ?: return false
    if (teamIndex !in currentPlayer.team.indices || storageIndex !in currentPlayer.storage.indices) return false

    val teamChimera = currentPlayer.team[teamIndex]
    currentPlayer.team[teamIndex] = currentPlayer.storage[storageIndex]
    currentPlayer.storage[storageIndex] = teamChimera
    currentPlayer.resetActiveChimeraToTeamLead()
    teamVersion++
    return true
}

/** Purchases items from the shop and updates player money and inventory. */
fun GameSessionState.buyItem(itemName: ItemName, amount: Int): Boolean {
    val currentPlayer = player ?: return false
    val price = itemName.price() * amount
    if (!currentPlayer.spendMoney(price)) return false

    currentPlayer.inventory.addItem(ItemFactory.createItem(itemName), amount)
    teamVersion++
    return true
}
