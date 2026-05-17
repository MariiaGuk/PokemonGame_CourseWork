package com.example.chimeralis.logic.items

import com.example.chimeralis.logic.items.itemEffects.IItemEffect
import com.example.chimeralis.logic.chimeras.Chimera

class Item(
    val name: String,
    val effects: List<IItemEffect>
) {
    fun use(target: Chimera) {
        effects.forEach { it.apply(target) }
    }
}