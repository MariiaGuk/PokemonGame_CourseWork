package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.itemEffects.ItemEffect

/** Represents an inventory item and its domain effects. */
class Item(
    val itemName: ItemName,
    val effects: List<ItemEffect>
) {
    val name: String get() = itemName.displayName

    val isCaptureItem: Boolean
        get() = itemName.kind == ItemKind.Capture

    /**
     * Checks whether this item can currently be used on the target chimera.
     *
     * @param target The target value used by this operation.
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
    fun canUseOn(target: Chimera): Boolean {
        return when (itemName.kind) {
            ItemKind.Healing -> target.stats.isAlive() &&
                    target.stats.currentHp < target.stats.maxHp
            ItemKind.Revival -> !target.stats.isAlive()
            ItemKind.Capture -> false
        }
    }

    /**
     * Applies all item effects to a valid target.
     *
     * @param target The target value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun use(target: Chimera) {
        if (!canUseOn(target)) return
        effects.forEach { it.apply(target) }
    }
}
