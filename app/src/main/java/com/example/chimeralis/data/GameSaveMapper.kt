package com.example.chimeralis.data

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName

class GameSaveMapper {
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

    fun toInventory(savedItems: List<SavedItem>): Inventory {
        return Inventory().also { inventory ->
            savedItems.forEach { savedItem ->
                inventory.addItem(ItemFactory.createItem(savedItem.itemName), savedItem.amount)
            }
        }
    }

    fun toSavedItem(item: Item, amount: Int): SavedItem {
        return SavedItem(item.itemName, amount)
    }

    fun speciesSaveName(species: ChimeraSpecies): String = when (species) {
        ChimeraSpecies.Sunflare -> "Sunflare"
        ChimeraSpecies.Solflare -> "Solflare"
        ChimeraSpecies.Solignis -> "Solignis"
        ChimeraSpecies.Sylvhorn -> "Sylvhorn"
        ChimeraSpecies.Aquantis -> "Aquantis"
    }

    fun itemSaveName(itemName: ItemName): String = itemName.displayName

    fun toChimeraSpecies(value: String): ChimeraSpecies? = when (value) {
        "Sunflare" -> ChimeraSpecies.Sunflare
        "Solflare" -> ChimeraSpecies.Solflare
        "Solignis" -> ChimeraSpecies.Solignis
        "Sylvhorn" -> ChimeraSpecies.Sylvhorn
        "Aquantis" -> ChimeraSpecies.Aquantis
        else -> null
    }

    fun toItemName(value: String): ItemName? = ItemName.values().firstOrNull {
        it.displayName == value
    } ?: when (value) {
        "Poke Ball" -> ItemName.BINDING_STONE
        else -> null
    }

    fun battleName(species: ChimeraSpecies): String = speciesSaveName(species)
}
