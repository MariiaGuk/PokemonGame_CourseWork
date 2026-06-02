package com.example.chimeralis.ui.screens.battle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.battle.model.ChimeraEvolutionEvent

/** Stores queued post-battle evolution presentation state. */
internal class BattleEvolutionUiState {
    var activeEvolutionEvent by mutableStateOf<ChimeraEvolutionEvent?>(null)
        private set
    var pendingEvolutionEvents by mutableStateOf<List<ChimeraEvolutionEvent>>(emptyList())
        private set

    /**
     * Adds evolution events that should be presented after the battle ends.
     *
     * @param events The events value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun addPending(events: List<ChimeraEvolutionEvent>) {
        pendingEvolutionEvents = pendingEvolutionEvents + events
    }

    /**
     * Shows one pending evolution event.
     *
     * @param event The event value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showEvolution(event: ChimeraEvolutionEvent) {
        activeEvolutionEvent = event
    }

    /**
     * Clears post-battle evolution overlay state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearEvolutionState() {
        activeEvolutionEvent = null
        pendingEvolutionEvents = emptyList()
    }
}
