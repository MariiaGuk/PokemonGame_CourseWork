package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

/** Restores a fixed amount of HP to a living chimera. */
class HealItemEffect(private val amount: Int) : IItemEffect {

    /** Applies the healing item effect to the target. */
    override fun apply(target: Chimera) {
        target.stats.heal(amount)
    }
}
