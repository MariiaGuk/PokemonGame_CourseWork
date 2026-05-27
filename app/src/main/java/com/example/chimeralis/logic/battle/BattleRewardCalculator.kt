package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.trainers.Player

/** Calculates and applies battle rewards after victory or capture. */
class BattleRewardCalculator {

    /** Awards experience to all living participants and logs level-up events. */
    fun awardExperience(
        playerBattleParticipants: Collection<Chimera>,
        defeatedChimera: Chimera,
        log: MutableList<String>
    ) {
        val experienceReward = experienceReward(defeatedChimera)
        val eligibleParticipants = playerBattleParticipants
            .filter { it.stats.isAlive() }

        eligibleParticipants.forEach { chimera ->
            val previousLevel = chimera.level
            val previousMoves = chimera.moves.map { it.name }.toSet()

            chimera.gainExp(experienceReward)

            log.add("${chimera.name} gained $experienceReward EXP.")

            if (chimera.level > previousLevel) {
                log.add("${chimera.name} grew to Lv.${chimera.level}!")
            }

            val learnedMoveNames = chimera.moves
                .filter { it.name !in previousMoves }
                .map { it.name }
            learnedMoveNames.forEach { moveName ->
                log.add("${chimera.name} learned $moveName!")
            }

            chimera.pendingMoveToLearn?.let { move ->
                log.add("${chimera.name} wants to learn ${move.name}.")
                log.add("Choose a move to forget, or keep the old moves.")
            }
        }
    }

    /** Awards coins to the player based on the defeated chimera. */
    fun awardMoney(
        player: Player,
        defeatedChimera: Chimera,
        log: MutableList<String>
    ) {
        val moneyReward = moneyReward(defeatedChimera)
        player.earnMoney(moneyReward)
        log.add("You earned $moneyReward coins.")
    }

    /** Calculates experience from defeated chimera level. */
    private fun experienceReward(defeatedChimera: Chimera): Int {
        return (defeatedChimera.level * ExpPerLevel).coerceAtLeast(1)
    }

    /** Calculates coin reward from defeated chimera level. */
    private fun moneyReward(defeatedChimera: Chimera): Int {
        return (defeatedChimera.level * CoinsPerLevel).coerceAtLeast(MinCoins)
    }

    private companion object {
        const val ExpPerLevel = 12
        const val CoinsPerLevel = 18
        const val MinCoins = 10
    }
}
