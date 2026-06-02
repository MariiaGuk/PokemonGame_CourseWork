package com.example.chimeralis.logic.trainers

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item

/** Player-controlled trainer with inventory, storage, and money. */
class Player(
    name: String,
    team: List<Chimera> = emptyList(),
    val inventory: Inventory,
    storage: List<Chimera> = emptyList(),
    money: Int = 0
) : Trainer(name, team) {
    private val storedChimeras = storage.toMutableList()

    val storage: List<Chimera>
        get() = storedChimeras.toList()

    var money: Int = money
        private set(value) {
            field = value.coerceAtLeast(0)
        }

    /** Adds money to the player's wallet. */
    fun earnMoney(amount: Int) {
        money += amount
    }

    /** Spends money when the player has enough funds. */
    fun spendMoney(amount: Int): Boolean {
        if (amount < 0 || money < amount) return false
        money -= amount
        return true
    }

    /** Uses an inventory item on a chimera from the active team. */
    fun useInventoryItem(item: Item, chimera: Chimera): Boolean {
        if (!hasTeamMember(chimera)) return false
        return inventory.useItem(item, chimera)
    }

    /** Restores HP and PP for every active team member. */
    fun healTeam() {
        team.forEach { chimera ->
            chimera.stats.restoreHp(chimera.stats.maxHp)
            chimera.moves.forEach { move -> move.restorePp(move.maxPp) }
        }
    }

    /** Reorders two members inside the active team. */
    fun swapTeamMembers(fromIndex: Int, toIndex: Int): Boolean {
        return moveTeamMember(fromIndex, toIndex)
    }

    /** Moves a team member into storage while keeping at least one active chimera. */
    fun depositTeamMember(teamIndex: Int): Boolean {
        if (storedChimeras.size >= PlayerCollectionLimits.MaxStorageSize) return false

        val chimera = removeTeamMemberAt(teamIndex) ?: return false
        storedChimeras.add(chimera)
        resetActiveChimeraToTeamLead()
        return true
    }

    /** Moves a stored chimera into the active team when a free slot exists. */
    fun withdrawStoredChimera(storageIndex: Int): Boolean {
        if (storageIndex !in storedChimeras.indices || team.size >= PlayerCollectionLimits.MaxTeamSize) {
            return false
        }

        addTeamMember(storedChimeras.removeAt(storageIndex))
        resetActiveChimeraToTeamLead()
        return true
    }

    /** Swaps one active team member with one stored chimera. */
    fun swapTeamWithStorage(teamIndex: Int, storageIndex: Int): Boolean {
        if (storageIndex !in storedChimeras.indices) return false

        val storedChimera = storedChimeras[storageIndex]
        val teamChimera = replaceTeamMemberAt(teamIndex, storedChimera) ?: return false
        storedChimeras[storageIndex] = teamChimera
        resetActiveChimeraToTeamLead()
        return true
    }

    /** Returns true when a newly caught chimera can be stored by this player. */
    fun canStoreChimera(): Boolean {
        return team.size < PlayerCollectionLimits.MaxTeamSize ||
                storedChimeras.size < PlayerCollectionLimits.MaxStorageSize
    }

    /** Adds a newly caught chimera to the team or storage. */
    fun addCaughtChimera(chimera: Chimera): PlayerChimeraPlacement? {
        return when {
            team.size < PlayerCollectionLimits.MaxTeamSize -> {
                addTeamMember(chimera)
                PlayerChimeraPlacement.Team
            }
            storedChimeras.size < PlayerCollectionLimits.MaxStorageSize -> {
                storedChimeras.add(chimera)
                PlayerChimeraPlacement.Storage
            }
            else -> null
        }
    }
}
