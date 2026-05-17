package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

class ReviveItemEffect : IItemEffect {
    override fun apply(target: Chimera) {
        if (!target.stats.isAlive()) {
            target.stats.heal(target.stats.maxHp / 2)
        }
    }
}