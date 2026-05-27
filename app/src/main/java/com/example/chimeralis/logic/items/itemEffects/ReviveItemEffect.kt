package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

/** Revives a fainted chimera with partial HP. */
class ReviveItemEffect : IItemEffect {

    /** Applies revive only when the target is fainted. */
    override fun apply(target: Chimera) {
        if (!target.stats.isAlive()) {
            target.stats.heal(target.stats.maxHp / 2)
        }
    }
}
