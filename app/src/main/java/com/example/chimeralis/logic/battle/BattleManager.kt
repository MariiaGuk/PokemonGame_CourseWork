package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.battle.ai.EnemyMoveSelector
import com.example.chimeralis.logic.battle.progression.BattleEvolutionQueue
import com.example.chimeralis.logic.battle.progression.BattleRewardCalculator
import com.example.chimeralis.logic.battle.reporting.BattleMoveReporter
import com.example.chimeralis.logic.battle.reporting.toBattleStatsSnapshot
import com.example.chimeralis.logic.battle.resolution.BattleCaptureResolver
import com.example.chimeralis.logic.battle.resolution.BattleEscapeResolver
import com.example.chimeralis.logic.battle.resolution.BattleFaintResolver
import com.example.chimeralis.logic.battle.resolution.BattleItemResolution
import com.example.chimeralis.logic.battle.resolution.BattleItemResolver
import com.example.chimeralis.logic.battle.resolution.BattleMoveAccuracyResolver
import com.example.chimeralis.logic.battle.resolution.BattleMoveLearningResolver
import com.example.chimeralis.logic.battle.resolution.BattleTurnOrderResolver
import com.example.chimeralis.logic.battle.resolution.MoveLearnRequest
import com.example.chimeralis.logic.battle.resolution.PlayerFaintResolution
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.chimeras.moves.MoveExecutionResult
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player

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
    private val moveReporter: BattleMoveReporter = BattleMoveReporter(),
    private val moveAccuracyResolver: BattleMoveAccuracyResolver = BattleMoveAccuracyResolver(randomProvider),
    private val turnOrderResolver: BattleTurnOrderResolver = BattleTurnOrderResolver(randomProvider),
    private val faintResolver: BattleFaintResolver = BattleFaintResolver(),
    private val itemResolver: BattleItemResolver = BattleItemResolver(
        canCaptureEnemy = canCaptureEnemy,
        captureResolver = captureResolver,
        moveReporter = moveReporter
    )
) {
    val playerChimera get() = player.activeChimera
    val enemyChimera get() = enemy.activeChimera
    var isBattleActive: Boolean = true
        private set
    var isWaitingForPlayerSwitch: Boolean = false
        private set
    val pendingMoveLearning: MoveLearnRequest?
        get() = moveLearningResolver.pendingRequest()
    val isWaitingForMoveLearning: Boolean
        get() = pendingMoveLearning != null
    private var escapeAttempts = 0
    private val playerBattleParticipants = linkedSetOf<Chimera>()
    private var pendingEnemySwitch: Chimera? = null
    private val moveLearningResolver = BattleMoveLearningResolver(player)
    private val evolutionQueue = BattleEvolutionQueue()

    init {
        markPlayerParticipant(playerChimera)
    }

    /** Executes one player action and returns UI log messages with animations. */
    fun performTurnWithAnimations(playerAction: BattleAction): BattleTurnResult {
        val log = mutableListOf<String>()
        val animations = mutableListOf<BattleMoveAnimation>()
        evolutionQueue.clearPendingEvents()

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

                if (turnOrderResolver.playerActsFirst(playerChimera, enemyChimera)) {
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

                val itemResolution = itemResolver.resolve(
                    item = playerAction.item,
                    target = playerAction.target,
                    player = player,
                    playerChimera = playerChimera,
                    enemyChimera = enemyChimera
                )
                applyItemResolution(itemResolution, log, animations)

                if (itemResolution.shouldEnemyAct) {
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
            evolutions = evolutionQueue.events
        )
    }

    /** Applies the player's decision for a pending move-learning request. */
    fun resolvePendingMoveLearning(replaceIndex: Int?): List<String> {
        return moveLearningResolver.resolve(replaceIndex)
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
        evolutionQueue.apply(event, player, playerBattleParticipants)
    }

    /** Executes an enemy move and resolves resulting faint states. */
    private fun enemyTurn(log: MutableList<String>): BattleMoveAnimation {
        val enemyMove = enemyMoveSelector.selectMove(enemyChimera)
        val beforeTargetStats = playerChimera.stats.toBattleStatsSnapshot()
        val beforeUserStats = enemyChimera.stats.toBattleStatsSnapshot()
        val executionResult = executeMove(enemyMove, enemyChimera, playerChimera)
        val animation = moveReporter.reportMove(
            log = log,
            side = BattleSide.Enemy,
            user = enemyChimera,
            target = playerChimera,
            move = enemyMove,
            executionResult = executionResult,
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
        val executionResult = executeMove(playerMove, playerChimera, enemyChimera)
        val animation = moveReporter.reportMove(
            log = log,
            side = BattleSide.Player,
            user = playerChimera,
            target = enemyChimera,
            move = playerMove,
            executionResult = executionResult,
            userBefore = beforeUserStats,
            targetBefore = beforeTargetStats
        )

        resolveEnemyFaint(log, enemyChimera)
        resolvePlayerFaint(log)

        return animation
    }

    /** Applies the result of an item action to the battle manager state. */
    private fun applyItemResolution(
        resolution: BattleItemResolution,
        log: MutableList<String>,
        animations: MutableList<BattleMoveAnimation>
    ) {
        log.addAll(resolution.log)
        resolution.animation?.let(animations::add)
        isBattleActive = resolution.isBattleActive
        resolution.caughtChimera?.let { chimera ->
            awardExperience(log, chimera)
        }
    }

    /** Switches the active chimera and marks it as a battle participant. */
    private fun switchChimera(chimera: Chimera, log: MutableList<String>) {
        player.switchChimera(chimera)
        markPlayerParticipant(chimera)
        log.add("Go, ${chimera.name}!")
    }

    /** Resolves the player's active chimera fainting. */
    private fun resolvePlayerFaint(log: MutableList<String>) {
        val resolution = faintResolver.resolvePlayerFaint(player) ?: return
        applyPlayerFaintResolution(resolution, log)
    }

    /** Applies a player faint or forced-switch result to the battle state. */
    private fun applyPlayerFaintResolution(
        resolution: PlayerFaintResolution,
        log: MutableList<String>
    ) {
        isBattleActive = resolution.isBattleActive
        isWaitingForPlayerSwitch = resolution.isWaitingForPlayerSwitch
        log.add(resolution.message)
    }

    /** Resolves the enemy chimera fainting and battle victory rewards. */
    private fun resolveEnemyFaint(log: MutableList<String>, defeatedChimera: Chimera) {
        val resolution = faintResolver.resolveEnemyFaint(enemy, defeatedChimera) ?: return

        awardExperience(log, defeatedChimera)
        isBattleActive = resolution.isBattleActive
        pendingEnemySwitch = resolution.nextChimera
        log.add(resolution.message)
        if (resolution.shouldAwardMoney) {
            awardMoney(log, defeatedChimera)
        }
    }

    /** Prompts a forced switch or ends the battle when the player is defeated. */
    private fun promptForcedSwitch(log: MutableList<String>) {
        applyPlayerFaintResolution(faintResolver.promptForcedSwitch(player), log)
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
        evolutionQueue.queueReadyEvolutions(playerBattleParticipants)
    }

    /** Records a living player chimera as eligible for experience. */
    private fun markPlayerParticipant(chimera: Chimera) {
        if (chimera.stats.isAlive()) {
            playerBattleParticipants.add(chimera)
        }
    }

    /** Executes one move using the battle's injected accuracy resolver. */
    private fun executeMove(
        move: Move,
        attacker: Chimera,
        target: Chimera
    ): MoveExecutionResult {
        return move.execute(
            attacker = attacker,
            target = target,
            hits = move.pp > 0 && moveAccuracyResolver.moveHits(move)
        )
    }

    /** Awards money after defeating an enemy trainer or wild chimera. */
    private fun awardMoney(log: MutableList<String>, defeatedChimera: Chimera) {
        rewardCalculator.awardMoney(player, defeatedChimera, log)
    }

}
