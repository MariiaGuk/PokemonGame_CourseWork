package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.chimeras.Chimera

class Player(
    name: String,
    team: MutableList<Chimera> = mutableListOf(),
    val inventory: Inventory,
    money: Int = 0
) : Trainer(name, team) {
    var money: Int = money
        private set(value) {
            field = value.coerceAtLeast(0)
        }

    fun earnMoney(amount: Int) {
        money += amount
    }

    fun spendMoney(amount: Int): Boolean {
        if (amount < 0 || money < amount) return false
        money -= amount
        return true
    }
}
