package com.example.chimeralis.ui.navigation

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.items.price

fun GameSessionState.useInventoryItem(item: Item, chimera: Chimera): Boolean {
    val didUse = player?.inventory?.useItem(item, chimera) == true
    if (didUse) {
        teamVersion++
    }
    return didUse
}

fun GameSessionState.healTeam() {
    player?.team?.forEach { chimera ->
        chimera.stats.restoreHp(chimera.stats.maxHp)
        chimera.moves.forEach { move -> move.restorePp(move.maxPp) }
    }
    teamVersion++
}

fun GameSessionState.buyItem(itemName: ItemName, amount: Int): Boolean {
    val currentPlayer = player ?: return false
    val price = itemName.price() * amount
    if (!currentPlayer.spendMoney(price)) return false

    currentPlayer.inventory.addItem(ItemFactory.createItem(itemName), amount)
    teamVersion++
    return true
}
