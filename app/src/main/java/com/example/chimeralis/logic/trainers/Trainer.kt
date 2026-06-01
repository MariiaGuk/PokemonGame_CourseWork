package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera

/** Base trainer model that owns a team and tracks the active chimera. */
abstract class Trainer(
    val name: String,
    team: List<Chimera> = emptyList()
) {
    private val teamMembers = team.toMutableList()

    init {
        require(teamMembers.isNotEmpty()) { "Trainer must have at least one chimera" }
    }

    val team: List<Chimera>
        get() = teamMembers.toList()

    var activeChimera: Chimera = teamMembers.first()
        private set

    /** Resets the active chimera to the first team member. */
    fun resetActiveChimeraToTeamLead() {
        activeChimera = teamMembers.first()
    }

    /** Selects the first living team member before battle starts. */
    fun selectFirstLivingChimera(): Boolean {
        val livingChimera = firstLivingChimera() ?: return false
        activeChimera = livingChimera
        return true
    }

    /** Switches to a living chimera that belongs to this trainer. */
    fun switchChimera(chimera: Chimera) {
        require(hasTeamMember(chimera)) { "Chimera is not in team" }
        require(chimera.stats.isAlive()) { "Cannot swap to fainted chimera" }
        activeChimera = chimera
    }

    /** Returns true when this trainer owns the given chimera in the active team. */
    fun hasTeamMember(chimera: Chimera): Boolean {
        return chimera in teamMembers
    }

    /** Returns the first living team member, if one is available. */
    fun firstLivingChimera(): Chimera? {
        return teamMembers.firstOrNull { chimera -> chimera.stats.isAlive() }
    }

    /** Replaces one team member while preserving the active chimera invariant. */
    fun replaceTeamMember(oldChimera: Chimera, newChimera: Chimera): Boolean {
        val index = teamMembers.indexOf(oldChimera)
        if (index == -1) return false

        teamMembers[index] = newChimera
        if (activeChimera === oldChimera) {
            activeChimera = when {
                newChimera.stats.isAlive() -> newChimera
                else -> firstLivingChimera() ?: newChimera
            }
        }
        return true
    }

    /** Returns true when every team member has fainted. */
    fun isDefeated() = teamMembers.none { it.stats.isAlive() }

    /** Adds a new chimera to the end of the team. */
    protected fun addTeamMember(chimera: Chimera) {
        teamMembers.add(chimera)
    }

    /** Removes a team member while keeping at least one chimera in the team. */
    protected fun removeTeamMemberAt(index: Int): Chimera? {
        if (index !in teamMembers.indices || teamMembers.size <= 1) return null

        val removed = teamMembers.removeAt(index)
        if (activeChimera === removed) {
            activeChimera = firstLivingChimera() ?: teamMembers.first()
        }
        return removed
    }

    /** Moves one team member to a new position. */
    protected fun moveTeamMember(fromIndex: Int, toIndex: Int): Boolean {
        if (fromIndex !in teamMembers.indices || toIndex !in teamMembers.indices || fromIndex == toIndex) {
            return false
        }

        val chimera = teamMembers.removeAt(fromIndex)
        teamMembers.add(toIndex, chimera)
        resetActiveChimeraToTeamLead()
        return true
    }

    /** Replaces a team member at index and returns the previous chimera. */
    protected fun replaceTeamMemberAt(index: Int, chimera: Chimera): Chimera? {
        if (index !in teamMembers.indices) return null

        val previous = teamMembers[index]
        teamMembers[index] = chimera
        if (activeChimera === previous) {
            activeChimera = if (chimera.stats.isAlive()) chimera else firstLivingChimera() ?: chimera
        }
        return previous
    }
}
