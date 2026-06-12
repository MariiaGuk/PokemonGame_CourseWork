package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

/** Restores a fixed amount of HP to a living chimera. */
class HealItemEffect(private val amount: Int) : ItemEffect {

    /**
     * Applies the healing item effect to the target.
     *
     * @param target The target value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun apply(target: Chimera) {
        target.stats.heal(amount)
    }
}
