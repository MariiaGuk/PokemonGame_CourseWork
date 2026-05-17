package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.chimeras.Chimera

class Player(
    name: String,
    team: MutableList<Chimera> = mutableListOf(),
    val inventory: Inventory
) : Trainer(name, team)