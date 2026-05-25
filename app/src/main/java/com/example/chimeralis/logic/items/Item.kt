package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.IItemEffect
import com.example.chimeralis.logic.chimeras.Chimera

class Item(
    val name: String,
    val effects: List<IItemEffect>
) {
    fun canUseOn(target: Chimera): Boolean {
        return when (name) {
            "Potion", "Super Potion" -> target.stats.isAlive() &&
                    target.stats.currentHp < target.stats.maxHp
            "Revive" -> !target.stats.isAlive()
            else -> true
        }
    }

    fun use(target: Chimera) {
        if (!canUseOn(target)) return
        effects.forEach { it.apply(target) }
    }
}
