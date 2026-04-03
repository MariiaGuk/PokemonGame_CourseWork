package com.example.chimeralis.logic.items.itemEffects

import com.example.chimeralis.logic.chimeras.Chimera

interface IItemEffect {
    fun apply(target: Chimera)
}