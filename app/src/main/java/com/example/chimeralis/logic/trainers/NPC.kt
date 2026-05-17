package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

class NPC(
    name: String,
    team: MutableList<Chimera> = mutableListOf(),
    val dialogue: String = ""
) : Trainer(name, team)