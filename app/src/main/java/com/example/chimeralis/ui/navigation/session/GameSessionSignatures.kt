package com.example.chimeralis.ui.navigation.session

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player

/** Handles team signature behavior. */
internal fun Player.teamSignature(): String {
    val teamState = team.joinToString(separator = "|") { chimera -> chimera.signature() }
    val storageState = storage.joinToString(separator = "|") { chimera -> chimera.signature() }
    val inventoryState = inventory.items.entries
        .sortedBy { it.key.name }
        .joinToString(separator = "|") { (item, amount) -> "${item.name}:$amount" }

    return "$teamState#$storageState#$inventoryState#$money"
}

/** Handles signature behavior. */
private fun Chimera.signature(): String {
    return listOf(
        species.battleName(),
        name,
        level,
        exp,
        stats.currentHp,
        ivStats.maxHp,
        ivStats.attack,
        ivStats.defence,
        ivStats.speed,
        moves.joinToString(separator = ",") { move -> "${move.name}.${move.pp}" }
    ).joinToString(separator = ":")
}

/** Handles battle name behavior. */
private fun ChimeraSpecies.battleName(): String = ChimeraFactory.speciesName(this)
