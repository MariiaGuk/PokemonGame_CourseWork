package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player

/** Describes battle-state changes caused by the player's active chimera fainting. */
data class PlayerFaintResolution(
    val isBattleActive: Boolean,
    val isWaitingForPlayerSwitch: Boolean,
    val message: String
)

/** Describes battle-state changes caused by an enemy chimera fainting. */
data class EnemyFaintResolution(
    val isBattleActive: Boolean,
    val nextChimera: Chimera?,
    val shouldAwardMoney: Boolean,
    val message: String
)

/** Resolves faint and forced-switch outcomes without applying them to the battle manager. */
class BattleFaintResolver {

    /** Resolves the outcome after the player's active chimera may have fainted. */
    fun resolvePlayerFaint(player: Player): PlayerFaintResolution? {
        if (player.activeChimera.stats.isAlive()) return null

        return promptForcedSwitch(player)
    }

    /** Resolves the outcome after an enemy chimera may have fainted. */
    fun resolveEnemyFaint(enemy: NPC, defeatedChimera: Chimera): EnemyFaintResolution? {
        if (defeatedChimera.stats.isAlive()) return null

        return if (enemy.isDefeated()) {
            EnemyFaintResolution(
                isBattleActive = false,
                nextChimera = null,
                shouldAwardMoney = true,
                message = "You won!"
            )
        } else {
            val nextChimera = enemy.firstLivingChimera() ?: return null
            EnemyFaintResolution(
                isBattleActive = true,
                nextChimera = nextChimera,
                shouldAwardMoney = false,
                message = "${enemy.name} sent out ${nextChimera.name}!"
            )
        }
    }

    /** Resolves a forced switch prompt or defeat state. */
    fun promptForcedSwitch(player: Player): PlayerFaintResolution {
        val shouldSwitch = !player.isDefeated()
        return if (shouldSwitch) {
            PlayerFaintResolution(
                isBattleActive = true,
                isWaitingForPlayerSwitch = true,
                message = "Choose your next chimera!"
            )
        } else {
            PlayerFaintResolution(
                isBattleActive = false,
                isWaitingForPlayerSwitch = false,
                message = "You lost!"
            )
        }
    }
}
