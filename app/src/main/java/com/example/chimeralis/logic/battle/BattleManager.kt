package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player

data class MoveLearnRequest(
    val chimera: Chimera,
    val move: Move
)

/**
 * Manager to guide the course of the battle
 */
class BattleManager(
    val player: Player,
    val enemy: NPC
) {
    val playerChimera get() = player.activeChimera
    val enemyChimera get() = enemy.activeChimera
    var isBattleActive: Boolean = true
        private set
    var isWaitingForPlayerSwitch: Boolean = false
        private set
    val pendingMoveLearning: MoveLearnRequest?
        get() = player.team
            .firstOrNull { it.pendingMoveToLearn != null }
            ?.let { chimera ->
                MoveLearnRequest(
                    chimera = chimera,
                    move = chimera.pendingMoveToLearn!!
                )
            }
    val isWaitingForMoveLearning: Boolean
        get() = pendingMoveLearning != null
    private var escapeAttempts = 0
    private val playerBattleParticipants = linkedSetOf<Chimera>()

    init {
        markPlayerParticipant(playerChimera)
    }

    fun performTurn(playerAction: BattleAction): List<String> {
        return performTurnWithAnimations(playerAction).log
    }

    fun performTurnWithAnimations(playerAction: BattleAction): BattleTurnResult {
        val log = mutableListOf<String>()
        val animations = mutableListOf<BattleMoveAnimation>()

        if (!isBattleActive) {
            return BattleTurnResult(
                log = listOf("The fight is over!"),
                animations = emptyList()
            )
        }

        if (isWaitingForMoveLearning) {
            return BattleTurnResult(
                log = listOf("Choose a move for ${pendingMoveLearning?.chimera?.name} to forget."),
                animations = emptyList()
            )
        }

        when (playerAction) {
            is BattleAction.UseMove -> {
                if (isWaitingForPlayerSwitch || !playerChimera.stats.isAlive()) {
                    promptForcedSwitch(log)
                    return BattleTurnResult(log = log, animations = animations)
                }

                val playerGoesFirst = playerChimera.stats.speed > enemyChimera.stats.speed ||
                        (playerChimera.stats.speed == enemyChimera.stats.speed && Math.random() < 0.5)

                if (playerGoesFirst) {
                    animations.add(playerTurn(playerAction.move, log))
                    if (isBattleActive && playerChimera.stats.isAlive() && enemyChimera.stats.isAlive()) {
                        animations.add(enemyTurn(log))
                    }
                }
                else {
                    animations.add(enemyTurn(log))
                    if (isBattleActive && playerChimera.stats.isAlive() && enemyChimera.stats.isAlive()) {
                        animations.add(playerTurn(playerAction.move, log))
                    }
                }
            }
            is BattleAction.UseItem -> {
                if (isWaitingForPlayerSwitch || !playerChimera.stats.isAlive()) {
                    promptForcedSwitch(log)
                    return BattleTurnResult(log = log, animations = animations)
                }

                if (useItem(playerAction.item, playerAction.target, log, animations)) {
                    animations.add(enemyTurn(log))
                }
            }
            is BattleAction.SwitchChimera -> {
                val wasForcedSwitch = isWaitingForPlayerSwitch
                switchChimera(playerAction.chimera, log)
                isWaitingForPlayerSwitch = false
                if (!wasForcedSwitch) {
                    animations.add(enemyTurn(log))
                }
            }
            is BattleAction.Run -> {
                if (isWaitingForPlayerSwitch || !playerChimera.stats.isAlive()) {
                    promptForcedSwitch(log)
                    return BattleTurnResult(log = log, animations = animations)
                }

                tryRun(log, animations)
            }
        }

        return BattleTurnResult(
            log = log,
            animations = animations
        )
    }

    fun resolvePendingMoveLearning(replaceIndex: Int?): List<String> {
        val request = pendingMoveLearning ?: return emptyList()
        val log = mutableListOf<String>()

        if (replaceIndex == null) {
            val skippedMove = request.chimera.skipPendingMove()
            if (skippedMove != null) {
                log.add("${request.chimera.name} did not learn ${skippedMove.name}.")
            }
        } else {
            val learnedMoves = request.chimera.replaceMoveWithPending(replaceIndex)
            if (learnedMoves != null) {
                val (forgottenMove, learnedMove) = learnedMoves
                log.add("${request.chimera.name} forgot ${forgottenMove.name}.")
                log.add("${request.chimera.name} learned ${learnedMove.name}!")
            }
        }

        pendingMoveLearning?.let { nextRequest ->
            log.add("${nextRequest.chimera.name} wants to learn ${nextRequest.move.name}.")
            log.add("Choose a move to forget, or keep the old moves.")
        }

        return log.ifEmpty { listOf("Nothing happened.") }
    }

    private fun enemyTurn(log: MutableList<String>): BattleMoveAnimation {
        val enemyMove = enemyChimera.moves.random() //Maybe implement ai logic in the future
        val beforeTargetStats = playerChimera.stats.snapshot()
        val beforeUserStats = enemyChimera.stats.snapshot()
        enemyMove.execute(enemyChimera, playerChimera)
        val afterTargetStats = playerChimera.stats.snapshot()
        val afterUserStats = enemyChimera.stats.snapshot()
        log.add("Enemy ${enemyChimera.name} used ${enemyMove.name}!")
        appendBattleChanges(
            log = log,
            targetLabel = "Your ${playerChimera.name}",
            targetBefore = beforeTargetStats,
            targetAfter = afterTargetStats,
            userLabel = "Enemy ${enemyChimera.name}",
            userBefore = beforeUserStats,
            userAfter = afterUserStats
        )

        resolvePlayerFaint(log)
        resolveEnemyFaint(log, enemyChimera)

        return BattleMoveAnimation(
            side = BattleSide.Enemy,
            species = enemyChimera.species,
            chimeraName = enemyChimera.name,
            moveName = enemyMove.name,
            feedbacks = collectMoveFeedbacks(
                targetSide = BattleSide.Player,
                targetBefore = beforeTargetStats,
                targetAfter = afterTargetStats,
                userSide = BattleSide.Enemy,
                userBefore = beforeUserStats,
                userAfter = afterUserStats
            ),
            userBefore = beforeUserStats,
            userAfter = afterUserStats,
            targetBefore = beforeTargetStats,
            targetAfter = afterTargetStats
        )
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>): BattleMoveAnimation {
        markPlayerParticipant(playerChimera)
        val beforeTargetStats = enemyChimera.stats.snapshot()
        val beforeUserStats = playerChimera.stats.snapshot()
        playerMove.execute(playerChimera, enemyChimera)
        val afterTargetStats = enemyChimera.stats.snapshot()
        val afterUserStats = playerChimera.stats.snapshot()
        log.add("Your ${playerChimera.name} used ${playerMove.name}!")
        appendBattleChanges(
            log = log,
            targetLabel = "Enemy ${enemyChimera.name}",
            targetBefore = beforeTargetStats,
            targetAfter = afterTargetStats,
            userLabel = "Your ${playerChimera.name}",
            userBefore = beforeUserStats,
            userAfter = afterUserStats
        )

        resolveEnemyFaint(log, enemyChimera)
        resolvePlayerFaint(log)

        return BattleMoveAnimation(
            side = BattleSide.Player,
            species = playerChimera.species,
            chimeraName = playerChimera.name,
            moveName = playerMove.name,
            feedbacks = collectMoveFeedbacks(
                targetSide = BattleSide.Enemy,
                targetBefore = beforeTargetStats,
                targetAfter = afterTargetStats,
                userSide = BattleSide.Player,
                userBefore = beforeUserStats,
                userAfter = afterUserStats
            ),
            userBefore = beforeUserStats,
            userAfter = afterUserStats,
            targetBefore = beforeTargetStats,
            targetAfter = afterTargetStats
        )
    }

    private fun useItem(
        item: Item,
        target: Chimera?,
        log: MutableList<String>,
        animations: MutableList<BattleMoveAnimation>
    ): Boolean {
        if (item.isCaptureItem) {
            return tryCatchChimera(item, log, animations)
        }

        val itemTarget = target ?: playerChimera
        val targetBefore = itemTarget.stats.snapshot()
        if (itemTarget !in player.team || !player.inventory.useItem(item, itemTarget)) {
            log.add("${item.name} cannot be used on ${itemTarget.name}.")
            return false
        }
        val targetAfter = itemTarget.stats.snapshot()

        log.add("Used ${item.name} on ${itemTarget.name}!")
        if (itemTarget === playerChimera) {
            animations.add(
                BattleMoveAnimation(
                    side = BattleSide.Player,
                    species = itemTarget.species,
                    chimeraName = itemTarget.name,
                    moveName = item.name,
                    kind = BattleAnimationKind.Item,
                    userBefore = targetBefore,
                    userAfter = targetAfter
                )
            )
        }
        return true
    }

    private fun tryCatchChimera(
        item: Item,
        log: MutableList<String>,
        animations: MutableList<BattleMoveAnimation>
    ): Boolean {
        if (player.team.size >= MaxTeamSize) {
            log.add("Your team is full!")
            return false
        }

        if (!player.inventory.consumeItem(item)) {
            log.add("You do not have any ${item.name}s.")
            return false
        }

        log.add("You threw a ${item.name}!")

        val hpRatio = enemyChimera.stats.currentHp.toFloat() / enemyChimera.stats.maxHp.toFloat()
        val catchChance = (0.28f + (1f - hpRatio) * 0.55f).coerceIn(0.25f, 0.9f)
        val caught = Math.random() < catchChance

        animations.add(
            BattleMoveAnimation(
                side = BattleSide.Player,
                species = enemyChimera.species,
                chimeraName = enemyChimera.name,
                moveName = item.name,
                kind = BattleAnimationKind.Capture,
                captureSucceeded = caught
            )
        )

        if (caught) {
            enemyChimera.stats.resetBattleStages()
            player.team.add(enemyChimera)
            isBattleActive = false
            log.add("Gotcha! ${enemyChimera.name} was caught!")
            awardExperience(log, enemyChimera)
        } else {
            log.add("${enemyChimera.name} broke free!")
        }

        return isBattleActive
    }

    private fun switchChimera(chimera: Chimera, log: MutableList<String>) {
        player.switchChimera(chimera)
        markPlayerParticipant(chimera)
        log.add("Go, ${chimera.name}!")
    }

    private fun resolvePlayerFaint(log: MutableList<String>) {
        if (playerChimera.stats.isAlive()) return

        if (player.isDefeated()) {
            isBattleActive = false
            isWaitingForPlayerSwitch = false
            log.add("You lost!")
        } else {
            isWaitingForPlayerSwitch = true
            log.add("Choose your next chimera!")
        }
    }

    private fun resolveEnemyFaint(log: MutableList<String>, defeatedChimera: Chimera) {
        if (defeatedChimera.stats.isAlive()) return

        if (enemy.isDefeated()) {
            isBattleActive = false
            log.add("You won!")
            awardExperience(log, defeatedChimera)
            awardMoney(log, defeatedChimera)
        }
    }

    private fun promptForcedSwitch(log: MutableList<String>) {
        isWaitingForPlayerSwitch = !player.isDefeated()
        if (isWaitingForPlayerSwitch) {
            log.add("Choose your next chimera!")
        } else {
            isBattleActive = false
            log.add("You lost!")
        }
    }

    private fun tryRun(
        log: MutableList<String>,
        animations: MutableList<BattleMoveAnimation>
    ) {
        val playerSpeed = playerChimera.stats.speed
        val enemySpeed = enemyChimera.stats.speed

        escapeAttempts++

        val odds = ((playerSpeed * 32) / (enemySpeed / 4).coerceAtLeast(1) % 256) + 30 * escapeAttempts

        if (odds >= 255 || Math.random() < odds / 255.0) {
            isBattleActive = false
            log.add("Got away safely!")
        }
        else {
            log.add("Can't escape!")
            animations.add(enemyTurn(log))
        }
    }

    private fun awardExperience(log: MutableList<String>, defeatedChimera: Chimera) {
        val experienceReward = calculateExperienceReward(defeatedChimera)
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

    private fun markPlayerParticipant(chimera: Chimera) {
        if (chimera.stats.isAlive()) {
            playerBattleParticipants.add(chimera)
        }
    }

    private fun calculateExperienceReward(defeatedChimera: Chimera): Int {
        return (defeatedChimera.level * 12).coerceAtLeast(1)
    }

    private fun awardMoney(log: MutableList<String>, defeatedChimera: Chimera) {
        val moneyReward = (defeatedChimera.level * 18).coerceAtLeast(10)
        player.earnMoney(moneyReward)
        log.add("You earned $moneyReward coins.")
    }

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

    private fun com.example.chimeralis.logic.chimeras.Stats.snapshot(): BattleStatsSnapshot {
        return BattleStatsSnapshot(
            currentHp = currentHp,
            maxHp = maxHp,
            attack = attack,
            defence = defence,
            speed = speed,
            attackStage = attackStage,
            defenceStage = defenceStage,
            speedStage = speedStage
        )
    }

    private companion object {
        const val MaxTeamSize = 6
    }
}
