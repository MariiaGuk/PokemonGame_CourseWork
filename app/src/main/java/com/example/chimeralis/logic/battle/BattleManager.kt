package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.trainers.NPC
import com.example.chimeralis.logic.trainers.Player

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
    private var escapeAttempts = 0

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

        when (playerAction) {
            is BattleAction.UseMove -> {

                val playerGoesFirst = playerChimera.stats.speed > enemyChimera.stats.speed ||
                        (playerChimera.stats.speed == enemyChimera.stats.speed && Math.random() < 0.5)

                if (playerGoesFirst) {
                    animations.add(playerTurn(playerAction.move, log))
                    if (isBattleActive) {
                        animations.add(enemyTurn(log))
                    }
                }
                else {
                    animations.add(enemyTurn(log))
                    if (isBattleActive) {
                        animations.add(playerTurn(playerAction.move, log))
                    }
                }
            }
            is BattleAction.UseItem -> {
                useItem(playerAction.item, log)
                animations.add(enemyTurn(log))
            }
            is BattleAction.SwitchChimera -> {
                switchChimera(playerAction.chimera, log)
                animations.add(enemyTurn(log))
            }
            is BattleAction.Run -> {
                tryRun(log, animations)
            }
        }

        return BattleTurnResult(
            log = log,
            animations = animations
        )
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

        if (!playerChimera.stats.isAlive()) {
            if (player.isDefeated()) {
                isBattleActive = false
                log.add("You lost!")
            }
        }

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
            )
        )
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>): BattleMoveAnimation {
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

        if (!enemyChimera.stats.isAlive()) {
            if (enemy.isDefeated()) {
                isBattleActive = false
                log.add("You won!")
                awardExperience(log, enemyChimera)
            }
        }

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
            )
        )
    }

    private fun useItem(item: Item, log: MutableList<String>){
        player.inventory.useItem(item, playerChimera)
        log.add("Used ${item.name} on ${playerChimera.name}!")
    }

    private fun switchChimera(chimera: Chimera, log: MutableList<String>) {
        player.switchChimera(chimera)
        log.add("Go, ${chimera.name}!")
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
        val previousLevel = playerChimera.level

        playerChimera.gainExp(experienceReward)

        log.add("${playerChimera.name} gained $experienceReward EXP.")

        if (playerChimera.level > previousLevel) {
            log.add("${playerChimera.name} grew to Lv.${playerChimera.level}!")
        }
    }

    private fun calculateExperienceReward(defeatedChimera: Chimera): Int {
        return (defeatedChimera.level * 12).coerceAtLeast(1)
    }

    private fun appendBattleChanges(
        log: MutableList<String>,
        targetLabel: String,
        targetBefore: StatsSnapshot,
        targetAfter: StatsSnapshot,
        userLabel: String,
        userBefore: StatsSnapshot,
        userAfter: StatsSnapshot
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
        targetBefore: StatsSnapshot,
        targetAfter: StatsSnapshot,
        userSide: BattleSide,
        userBefore: StatsSnapshot,
        userAfter: StatsSnapshot
    ): List<BattleMoveFeedback> {
        return buildList {
            addFeedbacksForStatSnapshot(targetSide, targetBefore, targetAfter)
            addFeedbacksForStatSnapshot(userSide, userBefore, userAfter)
        }.distinct()
    }

    private fun MutableList<BattleMoveFeedback>.addFeedbacksForStatSnapshot(
        side: BattleSide,
        before: StatsSnapshot,
        after: StatsSnapshot
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
        before: StatsSnapshot,
        after: StatsSnapshot
    ) {
        if (before.currentHp != after.currentHp) {
            log.add("$label has ${after.currentHp}/${after.maxHp} HP.")
        }
    }

    private fun appendStatChanges(
        log: MutableList<String>,
        label: String,
        before: StatsSnapshot,
        after: StatsSnapshot
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

    private fun com.example.chimeralis.logic.chimeras.Stats.snapshot(): StatsSnapshot {
        return StatsSnapshot(
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

    private data class StatsSnapshot(
        val currentHp: Int,
        val maxHp: Int,
        val attack: Int,
        val defence: Int,
        val speed: Int,
        val attackStage: Int,
        val defenceStage: Int,
        val speedStage: Int
    )
}
