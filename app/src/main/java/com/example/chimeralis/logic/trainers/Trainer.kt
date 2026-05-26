package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

abstract class Trainer(
    val name: String,
    val team: MutableList<Chimera> = mutableListOf()
) {
    init {
        require(team.isNotEmpty()) { "Trainer must have at least one chimera" }
    }
    var activeChimera: Chimera = team.first()
        protected set

    fun resetActiveChimeraToTeamLead() {
        activeChimera = team.first()
    }

    fun switchChimera(chimera: Chimera) {
        require(chimera in team) { "Chimera is not in team" }
        require(chimera.stats.isAlive()) { "Cannot swap to fainted chimera" }
        activeChimera = chimera
    }

    fun isDefeated() = team.none { it.stats.isAlive() }
}
