package com.example.chimeralis.ui.screens.battle.presentation

import com.example.chimeralis.logic.battle.model.BattleStatsSnapshot
import com.example.chimeralis.logic.battle.resolution.MoveLearnRequest
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.ui.screens.battle.MaxBattleTeamSize
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleChimeraSlotPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleFighterStatusPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleItemOptionPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleMoveLearningPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleMoveOptionPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleMoveReplacementOptionPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattlePanelPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleTeamPresentation
import kotlin.math.roundToInt

/**
 * Maps battle domain objects into UI presentation models.
 *
 * @receiver The player receiver used by this operation.
 * @param activeChimera The active chimera value used by this operation.
 * @param selectedItem The selected item value used by this operation.
 * @param pendingMoveLearning The pending move learning value used by this operation.
 * @param canUseCaptureItems Flag that controls or describes can use capture items.
 * @return The resulting BattlePanelPresentation value.
 */
internal fun Player.toBattlePanelPresentation(
    activeChimera: Chimera,
    selectedItem: Item?,
    pendingMoveLearning: MoveLearnRequest?,
    canUseCaptureItems: Boolean
): BattlePanelPresentation {
    return BattlePanelPresentation(
        moves = activeChimera.moves.map { move ->
            BattleMoveOptionPresentation(
                move = move,
                label = "${move.name} ${move.pp}/${move.maxPp}",
                enabled = move.pp > 0
            )
        },
        moveLearning = pendingMoveLearning?.toBattleMoveLearningPresentation(),
        inventoryItems = inventory.items.entries
            .sortedBy { (item, _) -> item.name }
            .map { (item, amount) ->
                BattleItemOptionPresentation(
                    item = item,
                    label = "${item.name} x$amount",
                    enabled = item.canUseInBattle(
                        team = team,
                        canUseCaptureItems = canUseCaptureItems
                    ),
                    isCaptureItem = item.isCaptureItem
                )
            },
        teamSelection = team.toBattleTeamPresentation(
            activeChimera = activeChimera,
            selectedItem = null
        ),
        itemTargetSelection = team.toBattleTeamPresentation(
            activeChimera = activeChimera,
            selectedItem = selectedItem
        )
    )
}

/**
 * Maps player status data into a UI presentation model.
 *
 * @receiver The chimera receiver used by this operation.
 * @param visibleStats The visible stats value used by this operation.
 * @param visibleLevel The visible level value used by this operation.
 * @param visibleExp The visible exp value used by this operation.
 * @param refreshKey The refresh key value used by this operation.
 * @return The resulting BattleFighterStatusPresentation value.
 */
internal fun Chimera.toPlayerStatusPresentation(
    visibleStats: BattleStatsSnapshot,
    visibleLevel: Int,
    visibleExp: Int,
    refreshKey: Int
): BattleFighterStatusPresentation {
    return toBattleStatusPresentation(
        visibleStats = visibleStats,
        visibleLevel = visibleLevel,
        currentExp = visibleExp,
        expToNextLevel = visibleLevel.expToNextLevel(),
        refreshKey = refreshKey
    )
}

/**
 * Maps enemy status data into a UI presentation model.
 *
 * @receiver The chimera receiver used by this operation.
 * @param visibleStats The visible stats value used by this operation.
 * @param refreshKey The refresh key value used by this operation.
 * @return The resulting BattleFighterStatusPresentation value.
 */
internal fun Chimera.toEnemyStatusPresentation(
    visibleStats: BattleStatsSnapshot,
    refreshKey: Int
): BattleFighterStatusPresentation {
    return toBattleStatusPresentation(
        visibleStats = visibleStats,
        visibleLevel = level,
        currentExp = null,
        expToNextLevel = null,
        refreshKey = refreshKey
    )
}

/**
 * Maps a move-learning request into a UI presentation model.
 *
 * @receiver The move learn request receiver used by this operation.
 * @return The resulting BattleMoveLearningPresentation value.
 */
private fun MoveLearnRequest.toBattleMoveLearningPresentation(): BattleMoveLearningPresentation {
    return BattleMoveLearningPresentation(
        message = "${chimera.name} wants to learn ${move.name}.\nForget which move? Back keeps old moves.",
        replacementMoves = List(4) { index ->
            chimera.moves.getOrNull(index)?.let { knownMove ->
                BattleMoveReplacementOptionPresentation(
                    index = index,
                    label = knownMove.name
                )
            }
        }
    )
}

/**
 * Maps a team into a fixed battle selection grid.
 *
 * @receiver The list<chimera> receiver used by this operation.
 * @param activeChimera The active chimera value used by this operation.
 * @param selectedItem The selected item value used by this operation.
 * @return The resulting BattleTeamPresentation value.
 */
private fun List<Chimera>.toBattleTeamPresentation(
    activeChimera: Chimera,
    selectedItem: Item?
): BattleTeamPresentation {
    return BattleTeamPresentation(
        slots = List(MaxBattleTeamSize) { index ->
            val chimera = getOrNull(index)
            chimera?.toBattleChimeraSlotPresentation(
                isActive = selectedItem == null && chimera === activeChimera,
                enabled = if (selectedItem == null) {
                    chimera !== activeChimera && chimera.stats.isAlive()
                } else {
                    selectedItem.canUseOn(chimera)
                }
            )
        }
    )
}

/**
 * Maps one chimera into a reusable battle selection slot.
 *
 * @receiver The chimera receiver used by this operation.
 * @param isActive Flag that controls or describes is active.
 * @param enabled Flag that controls or describes enabled.
 * @return The resulting BattleChimeraSlotPresentation value.
 */
private fun Chimera.toBattleChimeraSlotPresentation(
    isActive: Boolean,
    enabled: Boolean
): BattleChimeraSlotPresentation {
    val hpRatio = (stats.currentHp.toFloat() / stats.maxHp.toFloat()).coerceIn(0f, 1f)
    val hpPercent = (hpRatio * 100).roundToInt()

    return BattleChimeraSlotPresentation(
        chimera = this,
        name = name,
        levelLabel = "Lv.$level",
        imageRes = species.battleImageRes(),
        hpRatio = hpRatio,
        hpText = "HP: ${stats.currentHp}/${stats.maxHp} - $hpPercent%",
        isActive = isActive,
        enabled = enabled
    )
}

/**
 * Maps one fighter status into a reusable status plate model.
 *
 * @receiver The chimera receiver used by this operation.
 * @param visibleStats The visible stats value used by this operation.
 * @param visibleLevel The visible level value used by this operation.
 * @param currentExp The current exp value used by this operation.
 * @param expToNextLevel The exp to next level value used by this operation.
 * @param refreshKey The refresh key value used by this operation.
 * @return The resulting BattleFighterStatusPresentation value.
 */
private fun Chimera.toBattleStatusPresentation(
    visibleStats: BattleStatsSnapshot,
    visibleLevel: Int,
    currentExp: Int?,
    expToNextLevel: Int?,
    refreshKey: Int
): BattleFighterStatusPresentation {
    return BattleFighterStatusPresentation(
        name = name,
        level = visibleLevel,
        imageRes = species.battleImageRes(),
        currentHp = visibleStats.currentHp,
        maxHp = visibleStats.maxHp,
        currentExp = currentExp,
        expToNextLevel = expToNextLevel,
        attackStage = visibleStats.attackStage,
        defenceStage = visibleStats.defenceStage,
        speedStage = visibleStats.speedStage,
        refreshKey = refreshKey
    )
}

/**
 * Returns whether one item should be enabled in the current battle inventory.
 *
 * @receiver The item receiver used by this operation.
 * @param team The team value used by this operation.
 * @param canUseCaptureItems Flag that controls or describes can use capture items.
 * @return True when the operation succeeds or the condition is satisfied; otherwise false.
 */
private fun Item.canUseInBattle(
    team: List<Chimera>,
    canUseCaptureItems: Boolean
): Boolean {
    return if (isCaptureItem) {
        canUseCaptureItems
    } else {
        team.any { chimera -> canUseOn(chimera) }
    }
}
