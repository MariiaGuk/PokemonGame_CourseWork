package com.example.chimeralis.logic.battle.model

/** Stores battle turn result data. */
data class BattleTurnResult(
    val log: List<String>,
    val animations: List<BattleMoveAnimation>,
    val evolutions: List<ChimeraEvolutionEvent> = emptyList()
)
