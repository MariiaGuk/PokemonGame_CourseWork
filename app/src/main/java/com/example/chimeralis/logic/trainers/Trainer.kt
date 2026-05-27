package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

/** Base trainer model that owns a team and tracks the active chimera. */
abstract class Trainer(
    val name: String,
    val team: MutableList<Chimera> = mutableListOf()
) {
    init {
        require(team.isNotEmpty()) { "Trainer must have at least one chimera" }
    }
    var activeChimera: Chimera = team.first()
        protected set

    /** Resets the active chimera to the first team member. */
    fun resetActiveChimeraToTeamLead() {
        activeChimera = team.first()
    }

    /** Switches to a living chimera that belongs to this trainer. */
    fun switchChimera(chimera: Chimera) {
        require(chimera in team) { "Chimera is not in team" }
        require(chimera.stats.isAlive()) { "Cannot swap to fainted chimera" }
        activeChimera = chimera
    }

    /** Returns true when every team member has fainted. */
    fun isDefeated() = team.none { it.stats.isAlive() }
}
