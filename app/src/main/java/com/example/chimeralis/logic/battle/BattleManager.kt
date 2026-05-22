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
        val log = mutableListOf<String>()

        if (!isBattleActive) return listOf("The fight is over!")

        when (playerAction) {
            is BattleAction.UseMove -> {

                val playerGoesFirst = playerChimera.stats.speed > enemyChimera.stats.speed ||
                        (playerChimera.stats.speed == enemyChimera.stats.speed && Math.random() < 0.5)

                if (playerGoesFirst) {
                    playerTurn(playerAction.move, log)
                    if (isBattleActive) enemyTurn(log)
                }
                else {
                    enemyTurn(log)
                    if (isBattleActive) playerTurn(playerAction.move, log)
                }
            }
            is BattleAction.UseItem -> {
                useItem(playerAction.item, log)
                enemyTurn(log)
            }
            is BattleAction.SwitchChimera -> {
                switchChimera(playerAction.chimera, log)
                enemyTurn(log)
            }
            is BattleAction.Run -> {
                tryRun(log)
            }
        }

        return log
    }

    private fun enemyTurn(log: MutableList<String>) {
        val enemyMove = enemyChimera.moves.random() //Maybe implement ai logic in the future
        val beforeTargetStats = playerChimera.stats.snapshot()
        val beforeUserStats = enemyChimera.stats.snapshot()
        enemyMove.execute(enemyChimera, playerChimera)
        log.add("Enemy ${enemyChimera.name} used ${enemyMove.name}!")
        appendBattleChanges(
            log = log,
            targetLabel = "Your ${playerChimera.name}",
            targetBefore = beforeTargetStats,
            targetAfter = playerChimera.stats.snapshot(),
            userLabel = "Enemy ${enemyChimera.name}",
            userBefore = beforeUserStats,
            userAfter = enemyChimera.stats.snapshot()
        )

        if (!playerChimera.stats.isAlive()) {
            if (player.isDefeated()) {
                isBattleActive = false
                log.add("You lost!")
            }
        }
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>) {
        val beforeTargetStats = enemyChimera.stats.snapshot()
        val beforeUserStats = playerChimera.stats.snapshot()
        playerMove.execute(playerChimera, enemyChimera)
        log.add("Your ${playerChimera.name} used ${playerMove.name}!")
        appendBattleChanges(
            log = log,
            targetLabel = "Enemy ${enemyChimera.name}",
            targetBefore = beforeTargetStats,
            targetAfter = enemyChimera.stats.snapshot(),
            userLabel = "Your ${playerChimera.name}",
            userBefore = beforeUserStats,
            userAfter = playerChimera.stats.snapshot()
        )

        if (!enemyChimera.stats.isAlive()) {
            if (enemy.isDefeated()) {
                isBattleActive = false
                log.add("You won!")
            }
        }
    }

    private fun useItem(item: Item, log: MutableList<String>){
        player.inventory.useItem(item, playerChimera)
        log.add("Used ${item.name} on ${playerChimera.name}!")
    }

    private fun switchChimera(chimera: Chimera, log: MutableList<String>) {
        player.switchChimera(chimera)
        log.add("Go, ${chimera.name}!")
    }

    private fun tryRun(log: MutableList<String>) {
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
            enemyTurn(log)
        }
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
