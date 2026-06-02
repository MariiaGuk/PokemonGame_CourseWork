package com.example.chimeralis.ui.navigation.session

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.items.price

/**
 * Uses one inventory item on the selected chimera and marks the team as changed.
 *
 * @receiver The game session state receiver used by this operation.
 * @param item Domain object used by this operation: item.
 * @param chimera Domain object used by this operation: chimera.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.useInventoryItem(item: Item, chimera: Chimera): Boolean {
    val didUse = player?.useInventoryItem(item, chimera) == true
    if (didUse) {
        teamVersion++
    }
    return didUse
}

/**
 * Restores HP and PP for every chimera in the active team.
 *
 * @receiver The game session state receiver used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
fun GameSessionState.healTeam() {
    player?.healTeam()
    teamVersion++
}

/**
 * Reorders two members inside the active team.
 *
 * @receiver The game session state receiver used by this operation.
 * @param fromIndex The from index value used by this operation.
 * @param toIndex The to index value used by this operation.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.swapTeamMembers(fromIndex: Int, toIndex: Int): Boolean {
    val didSwap = player?.swapTeamMembers(fromIndex, toIndex) == true
    if (!didSwap) return false

    teamVersion++
    return true
}

/**
 * Moves a team member into storage while keeping at least one active chimera.
 *
 * @receiver The game session state receiver used by this operation.
 * @param teamIndex The team index value used by this operation.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.depositTeamMember(teamIndex: Int): Boolean {
    val didDeposit = player?.depositTeamMember(teamIndex) == true
    if (!didDeposit) return false

    teamVersion++
    return true
}

/**
 * Moves a stored chimera into the team when a free team slot exists.
 *
 * @receiver The game session state receiver used by this operation.
 * @param storageIndex The storage index value used by this operation.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.withdrawStoredChimera(storageIndex: Int): Boolean {
    val didWithdraw = player?.withdrawStoredChimera(storageIndex) == true
    if (!didWithdraw) return false

    teamVersion++
    return true
}

/**
 * Swaps one active team member with one stored chimera.
 *
 * @receiver The game session state receiver used by this operation.
 * @param teamIndex The team index value used by this operation.
 * @param storageIndex The storage index value used by this operation.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.swapTeamWithStorage(teamIndex: Int, storageIndex: Int): Boolean {
    val didSwap = player?.swapTeamWithStorage(teamIndex, storageIndex) == true
    if (!didSwap) return false

    teamVersion++
    return true
}

/**
 * Purchases items from the shop and updates player money and inventory.
 *
 * @receiver The game session state receiver used by this operation.
 * @param itemName The item name value used by this operation.
 * @param amount Numeric value used by this operation: amount.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
fun GameSessionState.buyItem(itemName: ItemName, amount: Int): Boolean {
    val currentPlayer = player ?: return false
    val price = itemName.price() * amount
    if (!currentPlayer.spendMoney(price)) return false

    currentPlayer.inventory.addItem(ItemFactory.createItem(itemName), amount)
    teamVersion++
    return true
}
