package com.example.chimeralis.logic.battle.resolution

import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.logic.battle.reporting.BattleMoveReporter
import com.example.chimeralis.logic.battle.reporting.toBattleStatsSnapshot
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.logic.trainers.PlayerChimeraPlacement

/** Resolves battle item usage, including capture-item flow. */
class BattleItemResolver(
    private val canCaptureEnemy: Boolean = true,
    private val captureResolver: BattleCaptureResolver = BattleCaptureResolver(),
    private val moveReporter: BattleMoveReporter = BattleMoveReporter()
) {

    /**
     * Resolves an item action against the current battle state.
     *
     * @param item Domain object used by this operation: item.
     * @param target The target value used by this operation.
     * @param player Domain object used by this operation: player.
     * @param playerChimera The player chimera value used by this operation.
     * @param enemyChimera The enemy chimera value used by this operation.
     * @return The resulting BattleItemResolution value.
     */
    fun resolve(
        item: Item,
        target: Chimera?,
        player: Player,
        playerChimera: Chimera,
        enemyChimera: Chimera
    ): BattleItemResolution {
        return if (item.isCaptureItem) {
            resolveCaptureItem(item, player, enemyChimera)
        } else {
            resolveBattleItem(item, target ?: playerChimera, player, playerChimera)
        }
    }

    /**
     * Resolves a regular battle item used on a player's chimera.
     *
     * @param item Domain object used by this operation: item.
     * @param target The target value used by this operation.
     * @param player Domain object used by this operation: player.
     * @param playerChimera The player chimera value used by this operation.
     * @return The resulting BattleItemResolution value.
     */
    private fun resolveBattleItem(
        item: Item,
        target: Chimera,
        player: Player,
        playerChimera: Chimera
    ): BattleItemResolution {
        val targetBefore = target.stats.toBattleStatsSnapshot()
        if (!player.useInventoryItem(item, target)) {
            return BattleItemResolution(
                log = listOf("${item.name} cannot be used on ${target.name}.")
            )
        }
        val targetAfter = target.stats.toBattleStatsSnapshot()

        return BattleItemResolution(
            log = listOf("Used ${item.name} on ${target.name}!"),
            animation = if (target === playerChimera) {
                moveReporter.reportItem(item, target, targetBefore, targetAfter)
            } else {
                null
            },
            shouldEnemyAct = true
        )
    }

    /**
     * Resolves a capture item used against the enemy chimera.
     *
     * @param item Domain object used by this operation: item.
     * @param player Domain object used by this operation: player.
     * @param enemyChimera The enemy chimera value used by this operation.
     * @return The resulting BattleItemResolution value.
     */
    private fun resolveCaptureItem(
        item: Item,
        player: Player,
        enemyChimera: Chimera
    ): BattleItemResolution {
        if (!canCaptureEnemy) {
            return BattleItemResolution(
                log = listOf("You cannot catch another trainer's chimera.")
            )
        }

        if (!player.canStoreChimera()) {
            return BattleItemResolution(
                log = listOf("Storage is full. You cannot catch more chimeras.")
            )
        }

        if (!player.inventory.consumeItem(item)) {
            return BattleItemResolution(
                log = listOf("You do not have any ${item.name}s.")
            )
        }

        val log = mutableListOf("You threw a ${item.name}!")
        val captureResult = captureResolver.resolve(enemyChimera)
        val animation = moveReporter.reportCapture(item, enemyChimera, captureResult)

        if (!captureResult.caught) {
            log.add("${enemyChimera.name} broke free!")
            return BattleItemResolution(
                log = log,
                animation = animation,
                shouldEnemyAct = true
            )
        }

        enemyChimera.stats.resetBattleStages()
        val placement = player.addCaughtChimera(enemyChimera)
        log.add("Gotcha! ${enemyChimera.name} was caught!")
        if (placement == PlayerChimeraPlacement.Storage) {
            log.add("${enemyChimera.name} was sent to storage.")
        }

        return BattleItemResolution(
            log = log,
            animation = animation,
            isBattleActive = false,
            caughtChimera = enemyChimera
        )
    }
}
