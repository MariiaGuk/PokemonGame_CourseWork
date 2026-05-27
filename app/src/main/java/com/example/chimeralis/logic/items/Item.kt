package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.IItemEffect
import com.example.chimeralis.logic.chimeras.Chimera

/** Represents an inventory item and its domain effects. */
class Item(
    val itemName: ItemName,
    val effects: List<IItemEffect>
) {
    val name: String get() = itemName.displayName

    val isCaptureItem: Boolean
        get() = itemName.kind == ItemKind.Capture

    /** Checks whether this item can currently be used on the target chimera. */
    fun canUseOn(target: Chimera): Boolean {
        return when (itemName.kind) {
            ItemKind.Healing -> target.stats.isAlive() &&
                    target.stats.currentHp < target.stats.maxHp
            ItemKind.Revival -> !target.stats.isAlive()
            ItemKind.Capture -> false
        }
    }

    /** Applies all item effects to a valid target. */
    fun use(target: Chimera) {
        if (!canUseOn(target)) return
        effects.forEach { it.apply(target) }
    }
}
