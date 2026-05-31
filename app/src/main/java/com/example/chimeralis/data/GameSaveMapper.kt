package com.example.chimeralis.data

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.chimeras.moves.Move

/** Represents the game save mapper. */
class GameSaveMapper {

    /** Converts data into saved chimera. */
    fun toSavedChimera(chimera: Chimera): SavedChimera {
        return SavedChimera(
            species = chimera.species,
            nickname = chimera.name,
            level = chimera.level,
            exp = chimera.exp,
            currentHp = chimera.stats.currentHp,
            ivStats = chimera.ivStats,
            moves = chimera.moves.map { move ->
                SavedMovePp(
                    moveName = move.name,
                    pp = move.pp
                )
            }
        )
    }

    /** Converts data into chimera. */
    fun toChimera(savedChimera: SavedChimera): Chimera {
        return ChimeraFactory.createChimera(
            species = savedChimera.species,
            level = savedChimera.level,
            ivStats = savedChimera.ivStats
        ).also { chimera ->
            restoreNickname(chimera, savedChimera.nickname)
            if (savedChimera.exp > 0) {
                chimera.gainExp(savedChimera.exp)
            }
            if (savedChimera.currentHp >= 0) {
                chimera.stats.restoreHp(savedChimera.currentHp)
            }
            chimera.moves.forEach { move ->
                savedMovePp(move, savedChimera.moves)?.let { savedMove ->
                    move.restorePp(savedMove.pp)
                }
            }
        }
    }

    /** Converts data into inventory. */
    fun toInventory(savedItems: List<SavedItem>): Inventory {
        return Inventory().also { inventory ->
            savedItems.forEach { savedItem ->
                inventory.addItem(ItemFactory.createItem(savedItem.itemName), savedItem.amount)
            }
        }
    }

    /** Converts data into saved item. */
    fun toSavedItem(item: Item, amount: Int): SavedItem {
        return SavedItem(item.itemName, amount)
    }

    /** Handles species save name behavior. */
    fun speciesSaveName(species: ChimeraSpecies): String = battleName(species)

    /** Handles item save name behavior. */
    fun itemSaveName(itemName: ItemName): String = itemName.displayName

    /** Converts data into chimera species. */
    fun toChimeraSpecies(value: String): ChimeraSpecies? = ChimeraFactory.speciesByName(value)

    /** Converts data into item name. */
    fun toItemName(value: String): ItemName? {
        val lookupKey = value.toLookupKey()
        return ItemName.values().firstOrNull { itemName ->
            itemName.displayName.toLookupKey() == lookupKey ||
                    itemName.name.toLookupKey() == lookupKey ||
                    itemName.legacySaveNames.any { legacyName -> legacyName.toLookupKey() == lookupKey }
        }
    }

    /** Restores a nickname only when it still passes current validation rules. */
    private fun restoreNickname(chimera: Chimera, nickname: String) {
        if (nickname.isBlank()) return

        runCatching {
            chimera.rename(nickname)
        }
    }

    /** Finds a saved PP row for a move using both enum and display-name formats. */
    private fun savedMovePp(move: Move, savedMoves: List<SavedMovePp>): SavedMovePp? {
        return savedMoves.firstOrNull { savedMove ->
            savedMove.moveName.toLookupKey() == move.name.toLookupKey() ||
                    savedMove.moveName.toLookupKey() == move.id.name.toLookupKey()
        }
    }

    /** Lists legacy persisted item names accepted during loading. */
    private val ItemName.legacySaveNames: List<String>
        get() = when (this) {
            ItemName.BINDING_STONE -> listOf("Binding Stone", "Binding Stone", "BINDING_STONE")
            else -> emptyList()
        }

    /** Normalizes persisted identifiers so old formatting does not break loading. */
    private fun String.toLookupKey(): String {
        return filter(Char::isLetterOrDigit).lowercase()
    }

    /** Handles battle name behavior. */
    fun battleName(species: ChimeraSpecies): String = ChimeraFactory.speciesName(species)
}
