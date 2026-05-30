package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player
import com.example.chimeralis.logic.trainers.PlayerCollectionLimits

/** Describes a pending move-learning choice for one chimera. */
data class MoveLearnRequest(
    val chimera: Chimera,
    val move: Move
)

/**
 * Coordinates battle turns, captures, switching, rewards, and move learning.
 */
class BattleManager(
    val player: Player,
    val enemy: NPC,
    private val canCaptureEnemy: Boolean = true,
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
    private var pendingEnemySwitch: Chimera? = null
    private val pendingEvolutionEvents = mutableListOf<ChimeraEvolutionEvent>()
    private val queuedEvolutionSources = mutableSetOf<Chimera>()

    init {
        markPlayerParticipant(playerChimera)
    }

    /** Executes one player action and returns UI log messages with animations. */
    fun performTurnWithAnimations(playerAction: BattleAction): BattleTurnResult {
        val log = mutableListOf<String>()
        val animations = mutableListOf<BattleMoveAnimation>()
        pendingEvolutionEvents.clear()

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
            animations = animations,
            evolutions = pendingEvolutionEvents.toList()
        )
    }

    /** Applies the player's decision for a pending move-learning request. */
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

    /** Sends out the next enemy chimera after the faint log has been shown. */
    fun resolvePendingEnemySwitch() {
        val nextChimera = pendingEnemySwitch ?: return
        if (nextChimera.stats.isAlive()) {
            enemy.switchChimera(nextChimera)
        }
        pendingEnemySwitch = null
    }

    /** Applies a queued evolution when the post-battle animation starts. */
    fun applyEvolution(event: ChimeraEvolutionEvent) {
        val teamIndex = player.team.indexOf(event.oldChimera)
        if (teamIndex == -1) return

        player.team[teamIndex] = event.newChimera
        if (player.activeChimera === event.oldChimera && event.newChimera.stats.isAlive()) {
            player.switchChimera(event.newChimera)
        }
        playerBattleParticipants.remove(event.oldChimera)
        playerBattleParticipants.add(event.newChimera)
    }

    /** Executes an enemy move and resolves resulting faint states. */
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

    /** Executes a player move and resolves resulting faint states. */
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

    /** Applies a battle item or redirects capture items into catch logic. */
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

    /** Resolves a capture attempt and stores the caught chimera when possible. */
    private fun tryCatchChimera(
        item: Item,
        log: MutableList<String>,
        animations: MutableList<BattleMoveAnimation>
    ): Boolean {
        if (!canCaptureEnemy) {
            log.add("You cannot catch another trainer's chimera.")
            return false
        }

        val canStoreCaughtChimera = player.team.size < PlayerCollectionLimits.MaxTeamSize ||
                player.storage.size < PlayerCollectionLimits.MaxStorageSize
        if (!canStoreCaughtChimera) {
            log.add("Storage is full. You cannot catch more chimeras.")
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
            if (player.team.size < PlayerCollectionLimits.MaxTeamSize) {
                player.team.add(enemyChimera)
            } else {
                player.storage.add(enemyChimera)
            }
            isBattleActive = false
            log.add("Gotcha! ${enemyChimera.name} was caught!")
            if (enemyChimera in player.storage) {
                log.add("${enemyChimera.name} was sent to storage.")
            }
            awardExperience(log, enemyChimera)
        } else {
            log.add("${enemyChimera.name} broke free!")
        }

        return isBattleActive
    }

    /** Switches the active chimera and marks it as a battle participant. */
    private fun switchChimera(chimera: Chimera, log: MutableList<String>) {
        player.switchChimera(chimera)
        markPlayerParticipant(chimera)
        log.add("Go, ${chimera.name}!")
    }

    /** Resolves the player's active chimera fainting. */
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

    /** Resolves the enemy chimera fainting and battle victory rewards. */
    private fun resolveEnemyFaint(log: MutableList<String>, defeatedChimera: Chimera) {
        if (defeatedChimera.stats.isAlive()) return

        awardExperience(log, defeatedChimera)

        if (enemy.isDefeated()) {
            isBattleActive = false
            log.add("You won!")
            awardMoney(log, defeatedChimera)
        } else {
            val nextChimera = enemy.team.firstOrNull { it.stats.isAlive() } ?: return
            pendingEnemySwitch = nextChimera
            log.add("${enemy.name} sent out ${nextChimera.name}!")
        }
    }

    /** Prompts a forced switch or ends the battle when the player is defeated. */
    private fun promptForcedSwitch(log: MutableList<String>) {
        isWaitingForPlayerSwitch = !player.isDefeated()
        if (isWaitingForPlayerSwitch) {
            log.add("Choose your next chimera!")
        } else {
            isBattleActive = false
            log.add("You lost!")
        }
    }

    /** Attempts to escape from the battle and lets the enemy act on failure. */
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

    /** Awards experience to all participating player chimeras. */
    private fun awardExperience(log: MutableList<String>, defeatedChimera: Chimera) {
        rewardCalculator.awardExperience(playerBattleParticipants, defeatedChimera, log)
        queueReadyEvolutions()
    }

    /** Queues evolution events without changing battle sprites or team members yet. */
    private fun queueReadyEvolutions() {
        playerBattleParticipants.forEach { chimera ->
            if (!chimera.canEvolve() || chimera in queuedEvolutionSources) return@forEach

            val evolvedChimera = chimera.evolution() ?: return@forEach
            queuedEvolutionSources.add(chimera)
            pendingEvolutionEvents.add(
                ChimeraEvolutionEvent(
                    oldChimera = chimera,
                    newChimera = evolvedChimera,
                    oldSpecies = chimera.species,
                    newSpecies = evolvedChimera.species,
                    oldName = chimera.name,
                    newName = evolvedChimera.name
                )
            )
        }
    }

    /** Records a living player chimera as eligible for experience. */
    private fun markPlayerParticipant(chimera: Chimera) {
        if (chimera.stats.isAlive()) {
            playerBattleParticipants.add(chimera)
        }
    }

    /** Awards money after defeating an enemy trainer or wild chimera. */
    private fun awardMoney(log: MutableList<String>, defeatedChimera: Chimera) {
        rewardCalculator.awardMoney(player, defeatedChimera, log)
    }

}
