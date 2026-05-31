package com.example.chimeralis.ui.screens.battle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.ChimeraEvolutionEvent

/** Stores queued post-battle evolution presentation state. */
internal class BattleEvolutionUiState {
    var activeEvolutionEvent by mutableStateOf<ChimeraEvolutionEvent?>(null)
        private set
    var pendingEvolutionEvents by mutableStateOf<List<ChimeraEvolutionEvent>>(emptyList())
        private set

    /** Adds evolution events that should be presented after the battle ends. */
    fun addPending(events: List<ChimeraEvolutionEvent>) {
        pendingEvolutionEvents = pendingEvolutionEvents + events
    }

    /** Shows one pending evolution event. */
    fun showEvolution(event: ChimeraEvolutionEvent) {
        activeEvolutionEvent = event
    }

    /** Clears post-battle evolution overlay state. */
    fun clearEvolutionState() {
        activeEvolutionEvent = null
        pendingEvolutionEvents = emptyList()
    }
}
