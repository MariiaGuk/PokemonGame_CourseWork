package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.chimeras.Chimera

/** Player-controlled trainer with inventory, storage, and money. */
class Player(
    name: String,
    team: MutableList<Chimera> = mutableListOf(),
    val inventory: Inventory,
    val storage: MutableList<Chimera> = mutableListOf(),
    money: Int = 0
) : Trainer(name, team) {
    var money: Int = money
        private set(value) {
            field = value.coerceAtLeast(0)
        }

    /** Adds money to the player's wallet. */
    fun earnMoney(amount: Int) {
        money += amount
    }

    /** Spends money when the player has enough funds. */
    fun spendMoney(amount: Int): Boolean {
        if (amount < 0 || money < amount) return false
        money -= amount
        return true
    }
}
