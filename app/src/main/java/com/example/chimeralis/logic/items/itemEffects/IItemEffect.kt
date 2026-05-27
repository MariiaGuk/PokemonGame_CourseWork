package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

/** Defines polymorphic behavior for item effects. */
interface IItemEffect {
    /** Applies the item effect to the selected target. */
    fun apply(target: Chimera)
}
