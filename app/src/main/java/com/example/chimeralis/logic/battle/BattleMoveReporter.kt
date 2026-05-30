package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.items.Item

/** Represents the battle move reporter. */
class BattleMoveReporter {

    /** Handles report move behavior. */
    fun reportMove(
        log: MutableList<String>,
        side: BattleSide,
        user: Chimera,
        target: Chimera,
        move: Move,
        userBefore: BattleStatsSnapshot,
        targetBefore: BattleStatsSnapshot
    ): BattleMoveAnimation {
        val userAfter = user.stats.toBattleStatsSnapshot()
        val targetAfter = target.stats.toBattleStatsSnapshot()
        val userLabel = if (side == BattleSide.Player) "Your ${user.name}" else "Enemy ${user.name}"
        val targetLabel = if (side == BattleSide.Player) "Enemy ${target.name}" else "Your ${target.name}"

        log.add("$userLabel used ${move.name}!")
        appendBattleChanges(
            log = log,
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

    /** Handles report item behavior. */
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

    /** Handles report capture behavior. */
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

    /** Handles append battle changes behavior. */
    private fun appendBattleChanges(
        log: MutableList<String>,
        targetLabel: String,
        targetBefore: BattleStatsSnapshot,
        targetAfter: BattleStatsSnapshot,
        userLabel: String,
        userBefore: BattleStatsSnapshot,
        userAfter: BattleStatsSnapshot
    ) {
        val oldSize = log.size

        appendHpChange(log, targetLabel, targetBefore, targetAfter)
        appendStatChanges(log, targetLabel, targetBefore, targetAfter)
        appendHpChange(log, userLabel, userBefore, userAfter)
        appendStatChanges(log, userLabel, userBefore, userAfter)

        if (log.size == oldSize) {
            log.add("But it had no effect!")
        }
    }

    /** Handles collect move feedbacks behavior. */
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

    /** Handles add feedbacks for stat snapshot behavior. */
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

    /** Handles append hp change behavior. */
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

    /** Handles append stat changes behavior. */
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

    /** Handles append stat change behavior. */
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

    /** Handles opponent behavior. */
    private fun BattleSide.opponent(): BattleSide {
        return when (this) {
            BattleSide.Player -> BattleSide.Enemy
            BattleSide.Enemy -> BattleSide.Player
        }
    }
}
