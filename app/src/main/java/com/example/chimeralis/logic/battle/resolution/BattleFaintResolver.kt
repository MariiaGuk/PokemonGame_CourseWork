package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player

/** Resolves faint and forced-switch outcomes without applying them to the battle manager. */
class BattleFaintResolver {

    /**
     * Resolves the outcome after the player's active chimera may have fainted.
     *
     * @param player Domain object used by this operation: player.
     * @return The resolved player faint resolution value, or null when it is unavailable.
     */
    fun resolvePlayerFaint(player: Player): PlayerFaintResolution? {
        if (player.activeChimera.stats.isAlive()) return null

        return promptForcedSwitch(player)
    }

    /**
     * Resolves the outcome after an enemy chimera may have fainted.
     *
     * @param enemy Domain object used by this operation: enemy.
     * @param defeatedChimera The defeated chimera value used by this operation.
     * @return The resolved enemy faint resolution value, or null when it is unavailable.
     */
    fun resolveEnemyFaint(enemy: NPC, defeatedChimera: Chimera): EnemyFaintResolution? {
        if (defeatedChimera.stats.isAlive()) return null

        return if (enemy.isDefeated()) {
            EnemyFaintResolution(
                isBattleActive = false,
                nextChimera = null,
                shouldAwardMoney = true,
                message = "You won!",
                extraMessages = listOfNotNull(enemy.dialogue.defeat(enemy.name))
            )
        } else {
            val nextChimera = enemy.firstLivingChimera() ?: return null
            EnemyFaintResolution(
                isBattleActive = true,
                nextChimera = nextChimera,
                shouldAwardMoney = false,
                message = enemy.dialogue.nextChimera(enemy.name, nextChimera.name)
                    ?: "${enemy.name} sent out ${nextChimera.name}!"
            )
        }
    }

    /**
     * Resolves a forced switch prompt or defeat state.
     *
     * @param player Domain object used by this operation: player.
     * @return The resulting PlayerFaintResolution value.
     */
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
