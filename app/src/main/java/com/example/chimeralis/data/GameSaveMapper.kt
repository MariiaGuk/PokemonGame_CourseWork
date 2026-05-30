package com.example.chimeralis.data

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName

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
            chimera.rename(savedChimera.nickname)
            if (savedChimera.exp > 0) {
                chimera.gainExp(savedChimera.exp)
            }
            chimera.stats.restoreHp(savedChimera.currentHp)
            val savedPpsByMoveName = savedChimera.moves.associateBy { it.moveName }
            chimera.moves.forEach { move ->
                savedPpsByMoveName[move.name]?.let { savedMove ->
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
    fun toItemName(value: String): ItemName? = ItemName.values().firstOrNull {
        it.displayName == value
    }

    /** Handles battle name behavior. */
    fun battleName(species: ChimeraSpecies): String = ChimeraFactory.speciesName(species)
}
