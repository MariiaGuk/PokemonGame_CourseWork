package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

/** Non-player trainer with a team and optional dialogue. */
class NPC(
    name: String,
    team: MutableList<Chimera> = mutableListOf(),
    val dialogue: String = ""
) : Trainer(name, team)
