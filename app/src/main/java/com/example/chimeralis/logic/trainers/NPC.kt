package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

/** Non-player trainer with a team and optional dialogue. */
class NPC(
    name: String,
    team: List<Chimera> = emptyList(),
    val dialogue: String = ""
) : Trainer(name, team)
