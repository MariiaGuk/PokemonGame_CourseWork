package com.example.chimeralis.logic.battle.scenario

import com.example.chimeralis.logic.battle.BattleManager
import com.example.chimeralis.logic.battle.random.DefaultRandomProvider
import com.example.chimeralis.logic.battle.random.RandomProvider
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.NpcDialogues
import com.example.chimeralis.logic.trainers.Player

/** Creates configured battle scenarios for wild and trainer encounters. */
object BattleScenarioFactory {

    /**
     * Creates a battle against one wild chimera.
     *
     * @param player Domain object used by this operation: player.
     * @param wildSpecies The wild species value used by this operation.
     * @param randomProvider The random provider value used by this operation.
     * @return The resulting BattleManager value.
     */
    fun createWildBattle(
        player: Player,
        wildSpecies: ChimeraSpecies,
        randomProvider: RandomProvider = DefaultRandomProvider
    ): BattleManager {
        require(player.selectFirstLivingChimera()) {
            "Player must have at least one living chimera to start battle"
        }

        val wildChimera = ChimeraFactory.createChimera(
            species = wildSpecies,
            level = scaledWildChimeraLevel(player, randomProvider)
        )
        val enemy = NPC(
            name = WildTrainerName,
            team = listOf(wildChimera),
            dialogue = NpcDialogues.WildEncounter
        )

        return BattleManager(player = player, enemy = enemy, randomProvider = randomProvider)
    }

    /**
     * Creates a battle against a rival trainer team.
     *
     * @param player Domain object used by this operation: player.
     * @param randomProvider The random provider value used by this operation.
     * @return The resulting BattleManager value.
     */
    fun createTrainerBattle(
        player: Player,
        randomProvider: RandomProvider = DefaultRandomProvider
    ): BattleManager {
        require(player.selectFirstLivingChimera()) {
            "Player must have at least one living chimera to start battle"
        }

        val playerTeam = player.team.ifEmpty {
            listOf(
                ChimeraFactory.createChimera(
                    species = ChimeraFactory.trainerBattleSpecies().first(),
                    level = FallbackTrainerBattleLevel
                )
            )
        }
        val trainerTeam = List(playerTeam.size.coerceIn(1, MaxTrainerBattleTeamSize)) {
            ChimeraFactory.createChimera(
                species = trainerBattleSpecies(randomProvider),
                level = playerTeam[randomProvider.nextInt(playerTeam.indices)].level
            )
        }
        val enemy = NPC(
            name = RivalTrainerName,
            team = trainerTeam,
            dialogue = NpcDialogues.RivalTrainer
        )

        return BattleManager(
            player = player,
            enemy = enemy,
            canCaptureEnemy = false,
            randomProvider = randomProvider
        )
    }

    /**
     * Selects a wild chimera level close to the player's current team strength.
     *
     * @param player Domain object used by this operation: player.
     * @param randomProvider The random provider value used by this operation.
     * @return The calculated numeric value.
     */
    private fun scaledWildChimeraLevel(
        player: Player,
        randomProvider: RandomProvider
    ): Int {
        val strongestLevel = player.team
            .filter { chimera -> chimera.stats.isAlive() }
            .ifEmpty { player.team }
            .maxOfOrNull { chimera -> chimera.level }
            ?: DefaultWildChimeraLevel
        val minLevel = (strongestLevel - WildLevelSpread).coerceAtLeast(DefaultWildChimeraLevel)
        val levelRange = strongestLevel - minLevel + 1

        return minLevel + randomProvider.nextInt(0 until levelRange)
    }

    /**
     * Selects one species from the configured trainer battle pool.
     *
     * @param randomProvider The random provider value used by this operation.
     * @return The resulting ChimeraSpecies value.
     */
    private fun trainerBattleSpecies(randomProvider: RandomProvider): ChimeraSpecies {
        val pool = ChimeraFactory.trainerBattleSpecies()
        return pool[randomProvider.nextInt(pool.indices)]
    }

    private const val MaxTrainerBattleTeamSize = 6
    private const val DefaultWildChimeraLevel = 3
    private const val WildLevelSpread = 2
    private const val FallbackTrainerBattleLevel = 5
    private const val WildTrainerName = "Wild"
    private const val RivalTrainerName = "Rival Trainer"
}
