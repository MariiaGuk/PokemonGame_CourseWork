package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

/** Defines polymorphic behavior for item effects. */
interface ItemEffect {

    /**
     * Applies the item effect to the selected target.
     *
     * @param target The target value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun apply(target: Chimera)
}
