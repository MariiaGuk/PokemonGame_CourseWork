package com.example.chimeralis.ui.screens.battle

import com.example.chimeralis.R
import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.DefaultRandomProvider
import com.example.chimeralis.logic.battle.RandomProvider
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player


internal fun createBattleManager(
    player: Player,
    wildSpecies: ChimeraSpecies,
    randomProvider: RandomProvider = DefaultRandomProvider
): BattleManager {
    player.resetActiveChimeraToTeamLead()

    val wildChimera = ChimeraFactory.createChimera(
        species = wildSpecies,
        level = player.scaledWildChimeraLevel(randomProvider)
    )
    val enemy = NPC(
        name = "Wild",
        team = mutableListOf(wildChimera)
    )

    return BattleManager(player = player, enemy = enemy, randomProvider = randomProvider)
}

internal fun ChimeraSpecies.battleName(): String = when (this) {
    ChimeraSpecies.Sunflare -> "Sunflare"
    ChimeraSpecies.Solflare -> "Solflare"
    ChimeraSpecies.Solignis -> "Solignis"
    ChimeraSpecies.Sylvhorn -> "Sylvhorn"
    ChimeraSpecies.Aquantis -> "Aquantis"
}

internal fun Player.scaledWildChimeraLevel(randomProvider: RandomProvider = DefaultRandomProvider): Int {
    val strongestLevel = team
        .filter { it.stats.isAlive() }
        .ifEmpty { team }
        .maxOfOrNull { it.level }
        ?: 3
    val minLevel = (strongestLevel - 2).coerceAtLeast(3)
    val levelRange = strongestLevel - minLevel + 1

    return minLevel + randomProvider.nextInt(0 until levelRange)
}

internal fun Int.expToNextLevel(): Int {
    return (this * this * this).coerceAtLeast(1)
}

internal fun ChimeraSpecies.battleImageRes(): Int = when (this) {
    ChimeraSpecies.Sunflare,
    ChimeraSpecies.Solflare,
    ChimeraSpecies.Solignis -> R.drawable.starter_fire
    ChimeraSpecies.Sylvhorn -> R.drawable.starter_grass
    ChimeraSpecies.Aquantis -> R.drawable.starter_water
}
