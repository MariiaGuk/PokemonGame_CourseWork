package com.example.chimeralis.ui.navigation

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player

internal fun Player.teamSignature(): String {
    val teamState = team.joinToString(separator = "|") { chimera -> chimera.signature() }
    val storageState = storage.joinToString(separator = "|") { chimera -> chimera.signature() }
    val inventoryState = inventory.items.entries
        .sortedBy { it.key.name }
        .joinToString(separator = "|") { (item, amount) -> "${item.name}:$amount" }

    return "$teamState#$storageState#$inventoryState#$money"
}

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

private fun ChimeraSpecies.battleName(): String = when (this) {
    ChimeraSpecies.Sunflare -> "Sunflare"
    ChimeraSpecies.Solflare -> "Solflare"
    ChimeraSpecies.Solignis -> "Solignis"
    ChimeraSpecies.Sylvhorn -> "Sylvhorn"
    ChimeraSpecies.Aquantis -> "Aquantis"
}
