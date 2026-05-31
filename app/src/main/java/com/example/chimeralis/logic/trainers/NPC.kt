package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

/** Non-player trainer with a team. */
class NPC(
    name: String,
    team: List<Chimera> = emptyList()
) : Trainer(name, team)
