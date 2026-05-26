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
    val enemy: NPC,
    private val randomProvider: RandomProvider = DefaultRandomProvider,
    private val rewardCalculator: BattleRewardCalculator = BattleRewardCalculator(),
    private val captureResolver: BattleCaptureResolver = BattleCaptureResolver(randomProvider),
    private val enemyMoveSelector: EnemyMoveSelector = EnemyMoveSelector(randomProvider),
    private val escapeResolver: BattleEscapeResolver = BattleEscapeResolver(randomProvider),
    private val moveReporter: BattleMoveReporter = BattleMoveReporter()
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
                        (playerChimera.stats.speed == enemyChimera.stats.speed &&
                                randomProvider.nextDouble() < 0.5)

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
        val enemyMove = enemyMoveSelector.selectMove(enemyChimera)
        val beforeTargetStats = playerChimera.stats.toBattleStatsSnapshot()
        val beforeUserStats = enemyChimera.stats.toBattleStatsSnapshot()
        enemyMove.execute(enemyChimera, playerChimera)
        val animation = moveReporter.reportMove(
            log = log,
            side = BattleSide.Enemy,
            user = enemyChimera,
            target = playerChimera,
            move = enemyMove,
            userBefore = beforeUserStats,
            targetBefore = beforeTargetStats
        )

        resolvePlayerFaint(log)
        resolveEnemyFaint(log, enemyChimera)

        return animation
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>): BattleMoveAnimation {
        markPlayerParticipant(playerChimera)
        val beforeTargetStats = enemyChimera.stats.toBattleStatsSnapshot()
        val beforeUserStats = playerChimera.stats.toBattleStatsSnapshot()
        playerMove.execute(playerChimera, enemyChimera)
        val animation = moveReporter.reportMove(
            log = log,
            side = BattleSide.Player,
            user = playerChimera,
            target = enemyChimera,
            move = playerMove,
            userBefore = beforeUserStats,
            targetBefore = beforeTargetStats
        )

        resolveEnemyFaint(log, enemyChimera)
        resolvePlayerFaint(log)

        return animation
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
        val targetBefore = itemTarget.stats.toBattleStatsSnapshot()
        if (itemTarget !in player.team || !player.inventory.useItem(item, itemTarget)) {
            log.add("${item.name} cannot be used on ${itemTarget.name}.")
            return false
        }
        val targetAfter = itemTarget.stats.toBattleStatsSnapshot()

        log.add("Used ${item.name} on ${itemTarget.name}!")
        if (itemTarget === playerChimera) {
            animations.add(moveReporter.reportItem(item, itemTarget, targetBefore, targetAfter))
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

        val captureResult = captureResolver.resolve(enemyChimera)

        animations.add(moveReporter.reportCapture(item, enemyChimera, captureResult))

        if (captureResult.caught) {
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

        if (escapeResolver.canEscape(playerSpeed, enemySpeed, escapeAttempts)) {
            isBattleActive = false
            log.add("Got away safely!")
        }
        else {
            log.add("Can't escape!")
            animations.add(enemyTurn(log))
        }
    }

    private fun awardExperience(log: MutableList<String>, defeatedChimera: Chimera) {
        rewardCalculator.awardExperience(playerBattleParticipants, defeatedChimera, log)
    }

    private fun markPlayerParticipant(chimera: Chimera) {
        if (chimera.stats.isAlive()) {
            playerBattleParticipants.add(chimera)
        }
    }

    private fun awardMoney(log: MutableList<String>, defeatedChimera: Chimera) {
        rewardCalculator.awardMoney(player, defeatedChimera, log)
    }

    private companion object {
        const val MaxTeamSize = 6
    }
}
