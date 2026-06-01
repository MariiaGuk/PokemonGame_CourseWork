package com.example.chimeralis.logic.battle.progression

import com.example.chimeralis.logic.battle.ChimeraEvolutionEvent
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraEvolutionService
import com.example.chimeralis.logic.trainers.Player

/** Queues and applies post-battle chimera evolutions. */
class BattleEvolutionQueue(
    private val evolutionService: ChimeraEvolutionService = ChimeraEvolutionService()
) {
    private val pendingEvents = mutableListOf<ChimeraEvolutionEvent>()
    private val queuedSources = mutableSetOf<Chimera>()

    /** Returns evolution events that should currently be shown by the UI. */
    val events: List<ChimeraEvolutionEvent>
        get() = pendingEvents.toList()

    /** Clears events already returned with the latest battle result. */
    fun clearPendingEvents() {
        pendingEvents.clear()
    }

    /** Queues all participant evolutions that became available after battle rewards. */
    fun queueReadyEvolutions(participants: Collection<Chimera>) {
        participants.forEach { chimera ->
            if (!evolutionService.canEvolve(chimera) || chimera in queuedSources) return@forEach

            val evolvedChimera = evolutionService.evolve(chimera) ?: return@forEach
            queuedSources.add(chimera)
            pendingEvents.add(
                ChimeraEvolutionEvent(
                    oldChimera = chimera,
                    newChimera = evolvedChimera,
                    oldSpecies = chimera.species,
                    newSpecies = evolvedChimera.species,
                    oldName = chimera.name,
                    newName = evolvedChimera.name
                )
            )
        }
    }

    /** Applies one selected evolution to the player's team and battle participants. */
    fun apply(
        event: ChimeraEvolutionEvent,
        player: Player,
        participants: MutableSet<Chimera>
    ) {
        if (!player.replaceTeamMember(event.oldChimera, event.newChimera)) return

        participants.remove(event.oldChimera)
        participants.add(event.newChimera)
    }
}
