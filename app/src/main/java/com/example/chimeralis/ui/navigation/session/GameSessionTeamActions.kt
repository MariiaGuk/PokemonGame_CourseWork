package com.example.chimeralis.ui.navigation.session

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.items.price

/** Uses one inventory item on the selected chimera and marks the team as changed. */
fun GameSessionState.useInventoryItem(item: Item, chimera: Chimera): Boolean {
    val didUse = player?.useInventoryItem(item, chimera) == true
    if (didUse) {
        teamVersion++
    }
    return didUse
}

/** Restores HP and PP for every chimera in the active team. */
fun GameSessionState.healTeam() {
    player?.healTeam()
    teamVersion++
}

/** Reorders two members inside the active team. */
fun GameSessionState.swapTeamMembers(fromIndex: Int, toIndex: Int): Boolean {
    val didSwap = player?.swapTeamMembers(fromIndex, toIndex) == true
    if (!didSwap) return false

    teamVersion++
    return true
}

/** Moves a team member into storage while keeping at least one active chimera. */
fun GameSessionState.depositTeamMember(teamIndex: Int): Boolean {
    val didDeposit = player?.depositTeamMember(teamIndex) == true
    if (!didDeposit) return false

    teamVersion++
    return true
}

/** Moves a stored chimera into the team when a free team slot exists. */
fun GameSessionState.withdrawStoredChimera(storageIndex: Int): Boolean {
    val didWithdraw = player?.withdrawStoredChimera(storageIndex) == true
    if (!didWithdraw) return false

    teamVersion++
    return true
}

/** Swaps one active team member with one stored chimera. */
fun GameSessionState.swapTeamWithStorage(teamIndex: Int, storageIndex: Int): Boolean {
    val didSwap = player?.swapTeamWithStorage(teamIndex, storageIndex) == true
    if (!didSwap) return false

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
