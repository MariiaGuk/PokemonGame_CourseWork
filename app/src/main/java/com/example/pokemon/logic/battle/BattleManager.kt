package com.example.pokemon.logic.battle

import com.example.pokemon.logic.items.Item
import com.example.pokemon.logic.pokemons.Pokemon
import com.example.pokemon.logic.pokemons.moves.Move
import com.example.pokemon.logic.trainers.NPC
import com.example.pokemon.logic.trainers.Player

/**
 * Manager to guide the course of the battle
 */
class BattleManager(
    val player: Player,
    val enemy: NPC
) {
    val playerPokemon get() = player.activePokemon
    val enemyPokemon get() = enemy.activePokemon
    var isBattleActive: Boolean = true
        private set
    private var escapeAttempts = 0

    fun performTurn(playerAction: BattleAction): List<String> {
        val log = mutableListOf<String>()

        if (!isBattleActive) return listOf("The fight is over!")

        when (playerAction) {
            is BattleAction.UseMove -> {

                val playerGoesFirst = playerPokemon.stats.speed > enemyPokemon.stats.speed ||
                        (playerPokemon.stats.speed == enemyPokemon.stats.speed && Math.random() < 0.5)

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
            is BattleAction.SwapPokemon -> {
                switchPokemon(playerAction.pokemon, log)
                enemyTurn(log)
            }
            is BattleAction.Run -> {
                tryRun(log)
            }
        }

        return log
    }

    private fun enemyTurn(log: MutableList<String>) {
        val enemyMove = enemyPokemon.moves.random() //Maybe implement ai logic in the future
        enemyMove.execute(enemyPokemon, playerPokemon)
        log.add("Enemy ${enemyPokemon.name} used ${enemyMove.name}!")
        log.add("Your ${playerPokemon.name} has ${playerPokemon.stats.currentHp} HP.")

        if (!playerPokemon.stats.isAlive()) {
            if (player.isDefeated()) {
                isBattleActive = false
                log.add("You lost!")
            }
        }
    }

    private fun playerTurn(playerMove: Move, log: MutableList<String>) {
        playerMove.execute(playerPokemon, enemyPokemon)
        log.add("Your ${playerPokemon.name} used ${playerMove.name}!")
        log.add("Enemy ${enemyPokemon.name} has ${enemyPokemon.stats.currentHp} HP.")

        if (!enemyPokemon.stats.isAlive()) {
            if (enemy.isDefeated()) {
                isBattleActive = false
                log.add("You won!")
            }
        }
    }

    private fun useItem(item: Item, log: MutableList<String>){
        player.inventory.useItem(item, playerPokemon)
        log.add("Used ${item.name} on ${playerPokemon.name}!")
    }

    private fun switchPokemon(pokemon: Pokemon, log: MutableList<String>) {
        player.switchPokemon(pokemon)
        log.add("Go, ${pokemon.name}!")
    }

    private fun tryRun(log: MutableList<String>) {
        val playerSpeed = playerPokemon.stats.speed
        val enemySpeed = enemyPokemon.stats.speed

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