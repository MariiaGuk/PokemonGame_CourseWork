package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.IItemEffect
import com.example.chimeralis.logic.chimeras.Chimera

class Item(
    val itemName: ItemName,
    val effects: List<IItemEffect>
) {
    val name: String get() = itemName.displayName

    val isCaptureItem: Boolean
        get() = itemName.kind == ItemKind.Capture

    fun canUseOn(target: Chimera): Boolean {
        return when (itemName.kind) {
            ItemKind.Healing -> target.stats.isAlive() &&
                    target.stats.currentHp < target.stats.maxHp
            ItemKind.Revival -> !target.stats.isAlive()
            ItemKind.Capture -> false
        }
    }

    fun use(target: Chimera) {
        if (!canUseOn(target)) return
        effects.forEach { it.apply(target) }
    }
}
