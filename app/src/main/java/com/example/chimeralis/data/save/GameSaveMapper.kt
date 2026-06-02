package com.example.chimeralis.data.save

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.toSaveLookupKey

/** Represents the game save mapper. */
class GameSaveMapper {

    /**
     * Converts data into saved chimera.
     *
     * @param chimera Domain object used by this operation: chimera.
     * @return The resulting SavedChimera value.
     */
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

    /**
     * Converts data into chimera.
     *
     * @param savedChimera The saved chimera value used by this operation.
     * @return The resulting Chimera value.
     */
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

    /**
     * Converts data into inventory.
     *
     * @param savedItems The saved items value used by this operation.
     * @return The resulting Inventory value.
     */
    fun toInventory(savedItems: List<SavedItem>): Inventory {
        return Inventory().also { inventory ->
            savedItems.forEach { savedItem ->
                inventory.addItem(ItemFactory.createItem(savedItem.itemName), savedItem.amount)
            }
        }
    }

    /**
     * Converts data into saved item.
     *
     * @param item Domain object used by this operation: item.
     * @param amount Numeric value used by this operation: amount.
     * @return The resulting SavedItem value.
     */
    fun toSavedItem(item: Item, amount: Int): SavedItem {
        return SavedItem(item.itemName, amount)
    }

    /**
     * Handles species save name behavior.
     *
     * @param species The species value used by this operation.
     * @return The text value produced by this operation.
     */
    fun speciesSaveName(species: ChimeraSpecies): String = battleName(species)

    /**
     * Handles item save name behavior.
     *
     * @param itemName The item name value used by this operation.
     * @return The text value produced by this operation.
     */
    fun itemSaveName(itemName: ItemName): String = itemName.displayName

    /**
     * Converts data into chimera species.
     *
     * @param value The value value used by this operation.
     * @return The resolved chimera species value, or null when it is unavailable.
     */
    fun toChimeraSpecies(value: String): ChimeraSpecies? = ChimeraFactory.speciesByName(value)

    /**
     * Converts data into item name.
     *
     * @param value The value value used by this operation.
     * @return The resolved item name value, or null when it is unavailable.
     */
    fun toItemName(value: String): ItemName? {
        val lookupKey = value.toSaveLookupKey()
        return ItemName.values().firstOrNull { itemName ->
            itemName.saveLookupNames().any { candidate -> candidate.toSaveLookupKey() == lookupKey }
        }
    }

    /**
     * Restores a nickname only when it still passes current validation rules.
     *
     * @param chimera Domain object used by this operation: chimera.
     * @param nickname The nickname value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun restoreNickname(chimera: Chimera, nickname: String) {
        if (nickname.isBlank()) return

        runCatching {
            chimera.rename(nickname)
        }
    }

    /**
     * Finds a saved PP row for a move using both enum and display-name formats.
     *
     * @param move Domain object used by this operation: move.
     * @param savedMoves The saved moves value used by this operation.
     * @return The resolved saved move pp value, or null when it is unavailable.
     */
    private fun savedMovePp(move: Move, savedMoves: List<SavedMovePp>): SavedMovePp? {
        val moveNameKey = move.name.toSaveLookupKey()
        val moveIdKey = move.id.name.toSaveLookupKey()

        return savedMoves.firstOrNull { savedMove ->
            val savedMoveKey = savedMove.moveName.toSaveLookupKey()
            savedMoveKey == moveNameKey || savedMoveKey == moveIdKey
        }
    }

    /**
     * Returns every persisted-name candidate accepted for one item.
     *
     * @receiver The item name receiver used by this operation.
     * @return The collection produced by this operation.
     */
    private fun ItemName.saveLookupNames(): List<String> {
        return listOf(displayName, name) + legacySaveNames
    }

    /** Lists legacy persisted item names accepted during loading. */
    private val ItemName.legacySaveNames: List<String>
        get() = when (this) {
            ItemName.BINDING_STONE -> listOf("Binding Stone", "Binding Stone", "BINDING_STONE")
            else -> emptyList()
        }

    /**
     * Handles battle name behavior.
     *
     * @param species The species value used by this operation.
     * @return The text value produced by this operation.
     */
    fun battleName(species: ChimeraSpecies): String = ChimeraFactory.speciesName(species)
}
