package com.example.chimeralis.ui.screens.battle

import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.DefaultRandomProvider
import com.example.chimeralis.logic.battle.RandomProvider
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes

/** Creates the create battle manager. */
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

/** Creates the create trainer battle manager. */
internal fun createTrainerBattleManager(
    player: Player,
    randomProvider: RandomProvider = DefaultRandomProvider
): BattleManager {
    player.resetActiveChimeraToTeamLead()

    val playerTeam = player.team.ifEmpty {
        listOf(ChimeraFactory.createChimera(ChimeraFactory.trainerBattleSpecies().first(), level = 5))
    }
    val trainerTeam = List(playerTeam.size.coerceIn(1, MaxBattleTeamSize)) { index ->
        ChimeraFactory.createChimera(
            species = trainerBattleSpecies(randomProvider),
            level = playerTeam[randomProvider.nextInt(playerTeam.indices)].level
        )
    }
    val enemy = NPC(
        name = "Rival Trainer",
        team = trainerTeam.toMutableList(),
        dialogue = "Want to test your chimeras against mine?"
    )

    return BattleManager(
        player = player,
        enemy = enemy,
        canCaptureEnemy = false,
        randomProvider = randomProvider
    )
}

/** Handles trainer battle species behavior. */
private fun trainerBattleSpecies(randomProvider: RandomProvider): ChimeraSpecies {
    val pool = ChimeraFactory.trainerBattleSpecies()
    return pool[randomProvider.nextInt(pool.indices)]
}

/** Handles battle name behavior. */
internal fun ChimeraSpecies.battleName(): String = ChimeraFactory.speciesName(this)

/** Handles scaled wild chimera level behavior. */
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

/** Handles exp to next level behavior. */
internal fun Int.expToNextLevel(): Int {
    return (this * this * this).coerceAtLeast(1)
}

/** Handles battle image res behavior. */
internal fun ChimeraSpecies.battleImageRes(): Int = chimeraImageRes()
