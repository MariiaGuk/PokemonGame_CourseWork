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
        enemyMove.execute(enemyChimera, playerChimera)
        log.add("Enemy ${enemyChimera.name} used ${enemyMove.name}!")
        log.add("Your ${playerChimera.name} has ${playerChimera.stats.currentHp} HP.")

        if (!playerChimera.stats.isAlive()) {
            if (player.isDefeated()) {
                isBattleActive = false
                log.add("You lost!")
            }
        }
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>) {
        playerMove.execute(playerChimera, enemyChimera)
        log.add("Your ${playerChimera.name} used ${playerMove.name}!")
        log.add("Enemy ${enemyChimera.name} has ${enemyChimera.stats.currentHp} HP.")

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
}