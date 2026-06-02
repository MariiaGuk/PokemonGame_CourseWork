package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

/** Describes a pending move-learning choice for one chimera. */
data class MoveLearnRequest(
    val chimera: Chimera,
    val move: Move
)
