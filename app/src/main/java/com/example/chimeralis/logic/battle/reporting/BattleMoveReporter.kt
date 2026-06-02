package com.example.chimeralis.logic.battle.reporting

import com.example.chimeralis.logic.battle.model.BattleAnimationKind
import com.example.chimeralis.logic.battle.model.BattleMoveAnimation
import com.example.chimeralis.logic.battle.model.BattleMoveFeedback
import com.example.chimeralis.logic.battle.model.BattleMoveFeedbackType
import com.example.chimeralis.logic.battle.model.BattleSide
import com.example.chimeralis.logic.battle.model.BattleStatsSnapshot
import com.example.chimeralis.logic.battle.resolution.BattleCaptureResult
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.chimeras.moves.MoveExecutionResult
import com.example.chimeralis.logic.items.Item

/** Represents the battle move reporter. */
class BattleMoveReporter {

    /**
     * Handles report move behavior.
     *
     * @param log The log value used by this operation.
     * @param side The side value used by this operation.
     * @param user The user value used by this operation.
     * @param target The target value used by this operation.
     * @param move Domain object used by this operation: move.
     * @param executionResult The execution result value used by this operation.
     * @param userBefore The user before value used by this operation.
     * @param targetBefore The target before value used by this operation.
     * @return The resulting BattleMoveAnimation value.
     */
    fun reportMove(
        log: MutableList<String>,
        side: BattleSide,
        user: Chimera,
        target: Chimera,
        move: Move,
        executionResult: MoveExecutionResult,
        userBefore: BattleStatsSnapshot,
        targetBefore: BattleStatsSnapshot
    ): BattleMoveAnimation {
        val userAfter = user.stats.toBattleStatsSnapshot()
        val targetAfter = target.stats.toBattleStatsSnapshot()
        val userLabel = if (side == BattleSide.Player) "Your ${user.name}" else "Enemy ${user.name}"
        val targetLabel = if (side == BattleSide.Player) "Enemy ${target.name}" else "Your ${target.name}"

        log.add("$userLabel used ${move.name}!")
        appendMoveResult(
            log = log,
            executionResult = executionResult,
            targetLabel = targetLabel,
            targetBefore = targetBefore,
            targetAfter = targetAfter,
            userLabel = userLabel,
            userBefore = userBefore,
            userAfter = userAfter
        )

        return BattleMoveAnimation(
            side = side,
            species = user.species,
            chimeraName = user.name,
            moveName = move.name,
            moveId = move.id,
            feedbacks = collectMoveFeedbacks(
                targetSide = side.opponent(),
                targetBefore = targetBefore,
                targetAfter = targetAfter,
                userSide = side,
                userBefore = userBefore,
                userAfter = userAfter
            ),
            userBefore = userBefore,
            userAfter = userAfter,
            targetBefore = targetBefore,
            targetAfter = targetAfter
        )
    }

    /**
     * Handles report item behavior.
     *
     * @param item Domain object used by this operation: item.
     * @param target The target value used by this operation.
     * @param targetBefore The target before value used by this operation.
     * @param targetAfter The target after value used by this operation.
     * @return The resulting BattleMoveAnimation value.
     */
    fun reportItem(
        item: Item,
        target: Chimera,
        targetBefore: BattleStatsSnapshot,
        targetAfter: BattleStatsSnapshot
    ): BattleMoveAnimation {
        return BattleMoveAnimation(
            side = BattleSide.Player,
            species = target.species,
            chimeraName = target.name,
            moveName = item.name,
            moveId = null,
            kind = BattleAnimationKind.Item,
            userBefore = targetBefore,
            userAfter = targetAfter
        )
    }

    /**
     * Handles report capture behavior.
     *
     * @param item Domain object used by this operation: item.
     * @param target The target value used by this operation.
     * @param captureResult The capture result value used by this operation.
     * @return The resulting BattleMoveAnimation value.
     */
    fun reportCapture(item: Item, target: Chimera, captureResult: BattleCaptureResult): BattleMoveAnimation {
        return BattleMoveAnimation(
            side = BattleSide.Player,
            species = target.species,
            chimeraName = target.name,
            moveName = item.name,
            moveId = null,
            kind = BattleAnimationKind.Capture,
            captureSucceeded = captureResult.caught
        )
    }

    /**
     * Adds the move outcome and any resulting battle stat changes to the log.
     *
     * @param log The log value used by this operation.
     * @param executionResult The execution result value used by this operation.
     * @param targetLabel The target label value used by this operation.
     * @param targetBefore The target before value used by this operation.
     * @param targetAfter The target after value used by this operation.
     * @param userLabel The user label value used by this operation.
     * @param userBefore The user before value used by this operation.
     * @param userAfter The user after value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun appendMoveResult(
        log: MutableList<String>,
        executionResult: MoveExecutionResult,
        targetLabel: String,
        targetBefore: BattleStatsSnapshot,
        targetAfter: BattleStatsSnapshot,
        userLabel: String,
        userBefore: BattleStatsSnapshot,
        userAfter: BattleStatsSnapshot
    ) {
        when (executionResult) {
            MoveExecutionResult.NoPowerPoints -> {
                log.add("But there was no PP left!")
                return
            }
            MoveExecutionResult.Missed -> {
                log.add("But it missed!")
                return
            }
            MoveExecutionResult.Hit -> Unit
        }

        val oldSize = log.size

        appendHpChange(log, targetLabel, targetBefore, targetAfter)
        appendStatChanges(log, targetLabel, targetBefore, targetAfter)
        appendHpChange(log, userLabel, userBefore, userAfter)
        appendStatChanges(log, userLabel, userBefore, userAfter)

        if (log.size == oldSize) {
            log.add("But it had no effect!")
        }
    }

    /**
     * Handles collect move feedbacks behavior.
     *
     * @param targetSide The target side value used by this operation.
     * @param targetBefore The target before value used by this operation.
     * @param targetAfter The target after value used by this operation.
     * @param userSide The user side value used by this operation.
     * @param userBefore The user before value used by this operation.
     * @param userAfter The user after value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun collectMoveFeedbacks(
        targetSide: BattleSide,
        targetBefore: BattleStatsSnapshot,
        targetAfter: BattleStatsSnapshot,
        userSide: BattleSide,
        userBefore: BattleStatsSnapshot,
        userAfter: BattleStatsSnapshot
    ): List<BattleMoveFeedback> {
        return buildList {
            addFeedbacksForStatSnapshot(targetSide, targetBefore, targetAfter)
            addFeedbacksForStatSnapshot(userSide, userBefore, userAfter)
        }.distinct()
    }

    /**
     * Handles add feedbacks for stat snapshot behavior.
     *
     * @receiver The mutable list<battle move feedback> receiver used by this operation.
     * @param side The side value used by this operation.
     * @param before The before value used by this operation.
     * @param after The after value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun MutableList<BattleMoveFeedback>.addFeedbacksForStatSnapshot(
        side: BattleSide,
        before: BattleStatsSnapshot,
        after: BattleStatsSnapshot
    ) {
        if (before.currentHp > 0 && after.currentHp <= 0) {
            add(BattleMoveFeedback(side, BattleMoveFeedbackType.Faint))
        } else if (after.currentHp < before.currentHp) {
            add(BattleMoveFeedback(side, BattleMoveFeedbackType.Damage))
        }

        if (after.attack != before.attack ||
            after.defence != before.defence ||
            after.speed != before.speed
        ) {
            add(BattleMoveFeedback(side, BattleMoveFeedbackType.StatChange))
        }
    }

    /**
     * Handles append hp change behavior.
     *
     * @param log The log value used by this operation.
     * @param label The label value used by this operation.
     * @param before The before value used by this operation.
     * @param after The after value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun appendHpChange(
        log: MutableList<String>,
        label: String,
        before: BattleStatsSnapshot,
        after: BattleStatsSnapshot
    ) {
        if (before.currentHp != after.currentHp) {
            log.add("$label has ${after.currentHp}/${after.maxHp} HP.")
        }
    }

    /**
     * Handles append stat changes behavior.
     *
     * @param log The log value used by this operation.
     * @param label The label value used by this operation.
     * @param before The before value used by this operation.
     * @param after The after value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun appendStatChanges(
        log: MutableList<String>,
        label: String,
        before: BattleStatsSnapshot,
        after: BattleStatsSnapshot
    ) {
        appendStatChange(log, label, "attack", before.attack, after.attack)
        appendStatChange(log, label, "defence", before.defence, after.defence)
        appendStatChange(log, label, "speed", before.speed, after.speed)
    }

    /**
     * Handles append stat change behavior.
     *
     * @param log The log value used by this operation.
     * @param label The label value used by this operation.
     * @param statName The stat name value used by this operation.
     * @param before The before value used by this operation.
     * @param after The after value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun appendStatChange(
        log: MutableList<String>,
        label: String,
        statName: String,
        before: Int,
        after: Int
    ) {
        when {
            after < before -> log.add("$label's $statName fell!")
            after > before -> log.add("$label's $statName rose!")
        }
    }

    /**
     * Handles opponent behavior.
     *
     * @receiver The battle side receiver used by this operation.
     * @return The resulting BattleSide value.
     */
    private fun BattleSide.opponent(): BattleSide {
        return when (this) {
            BattleSide.Player -> BattleSide.Enemy
            BattleSide.Enemy -> BattleSide.Player
        }
    }
}
