package com.example.chimeralis.ui.navigation

import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.Player

internal fun Player.teamSignature(): String {
    val teamState = team.joinToString(separator = "|") { chimera ->
        listOf(
            chimera.species.battleName(),
            chimera.name,
            chimera.level,
            chimera.exp,
            chimera.stats.currentHp,
            chimera.ivStats.maxHp,
            chimera.ivStats.attack,
            chimera.ivStats.defence,
            chimera.ivStats.speed,
            chimera.moves.joinToString(separator = ",") { move -> "${move.name}.${move.pp}" }
        ).joinToString(separator = ":")
    }
    val inventoryState = inventory.items.entries
        .sortedBy { it.key.name }
        .joinToString(separator = "|") { (item, amount) -> "${item.name}:$amount" }

    return "$teamState#$inventoryState#$money"
}

private fun ChimeraSpecies.battleName(): String = when (this) {
    ChimeraSpecies.Sunflare -> "Sunflare"
    ChimeraSpecies.Solflare -> "Solflare"
    ChimeraSpecies.Solignis -> "Solignis"
    ChimeraSpecies.Sylvhorn -> "Sylvhorn"
    ChimeraSpecies.Aquantis -> "Aquantis"
}
