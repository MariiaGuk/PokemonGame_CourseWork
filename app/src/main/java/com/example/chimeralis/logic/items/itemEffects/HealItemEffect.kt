package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

class HealItemEffect(private val amount: Int) : IItemEffect {
    override fun apply(target: Chimera) {
        target.stats.heal(amount)
    }
}