package com.example.chimeralis.data

import android.content.Context
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.trainers.Player

data class SavedChimera(
    val species: ChimeraSpecies,
    val nickname: String,
    val level: Int,
    val exp: Int,
    val currentHp: Int,
    val ivStats: Stats,
    val moves: List<SavedMovePp> = emptyList()
)

data class SavedMovePp(
    val moveName: String,
    val pp: Int
)

data class SavedItem(
    val itemName: ItemName,
    val amount: Int
)

data class GameSave(
    val trainerName: String,
    val team: List<SavedChimera>,
    val inventoryItems: List<SavedItem> = emptyList(),
    val playerColumn: Int,
    val playerRow: Int,
    val updatedAt: Long
) {
    val starterSpecies: ChimeraSpecies get() = team.first().species
    val starterNickname: String get() = team.first().nickname
}

class GameSaveStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    fun hasSaveForTrainer(trainerName: String): Boolean {
        return trainerId(trainerName) in trainerIds()
    }

    fun loadAll(): List<GameSave> {
        return trainerIds()
            .mapNotNull(::load)
            .sortedByDescending { it.updatedAt }
    }

    fun save(gameSave: GameSave) {
        val id = trainerId(gameSave.trainerName)
        val ids = trainerIds() + id

        prefs.edit()
            .putStringSet(TrainerIdsKey, ids)
            .putString("$id.$TrainerNameKey", gameSave.trainerName)
            .putInt("$id.$TeamSizeKey", gameSave.team.size)
            .putInt("$id.$InventorySizeKey", gameSave.inventoryItems.size)
            .putInt("$id.$PlayerColumnKey", gameSave.playerColumn)
            .putInt("$id.$PlayerRowKey", gameSave.playerRow)
            .putLong("$id.$UpdatedAtKey", gameSave.updatedAt)
            .apply()

        gameSave.team.forEachIndexed { index, chimera ->
            saveChimera(id, index, chimera)
        }
        gameSave.inventoryItems.forEachIndexed { index, item ->
            saveItem(id, index, item)
        }
    }

    fun delete(trainerName: String) {
        val id = trainerId(trainerName)
        val ids = trainerIds() - id

        prefs.edit()
            .putStringSet(TrainerIdsKey, ids)
            .remove("$id.$TrainerNameKey")
            .remove("$id.$TeamSizeKey")
            .remove("$id.$InventorySizeKey")
            .remove("$id.$StarterSpeciesKey")
            .remove("$id.$StarterNicknameKey")
            .remove("$id.$StarterLevelKey")
            .remove("$id.$StarterExpKey")
            .remove("$id.$StarterCurrentHpKey")
            .remove("$id.$PlayerColumnKey")
            .remove("$id.$PlayerRowKey")
            .remove("$id.$UpdatedAtKey")
            .apply()
    }

    private fun load(id: String): GameSave? {
        val trainerName = prefs.getString("$id.$TrainerNameKey", null) ?: return null
        val team = loadTeam(id).ifEmpty { return null }
        val inventoryItems = loadInventoryItems(id)
        val playerColumn = prefs.getInt("$id.$PlayerColumnKey", 1)
        val playerRow = prefs.getInt("$id.$PlayerRowKey", 1)
        val updatedAt = prefs.getLong("$id.$UpdatedAtKey", 0L)

        return GameSave(
            trainerName = trainerName,
            team = team,
            inventoryItems = inventoryItems,
            playerColumn = playerColumn,
            playerRow = playerRow,
            updatedAt = updatedAt
        )
    }

    fun saveFromPlayer(
        trainerName: String,
        player: Player,
        playerColumn: Int,
        playerRow: Int
    ) {
        save(
            GameSave(
                trainerName = trainerName,
                team = player.team.map { it.toSavedChimera() },
                inventoryItems = player.inventory.items.mapNotNull { (item, amount) ->
                    item.toItemName()?.let { SavedItem(it, amount) }
                },
                playerColumn = playerColumn,
                playerRow = playerRow,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun createPlayer(gameSave: GameSave): Player {
        return Player(
            name = gameSave.trainerName,
            team = gameSave.team.map { it.toChimera() }.toMutableList(),
            inventory = gameSave.inventoryItems.toInventory()
        )
    }

    private fun saveChimera(id: String, index: Int, chimera: SavedChimera) {
        val prefix = "$id.$TeamKey.$index"

        prefs.edit()
            .putString("$prefix.$SpeciesKey", chimera.species.saveName())
            .putString("$prefix.$NicknameKey", chimera.nickname)
            .putInt("$prefix.$LevelKey", chimera.level)
            .putInt("$prefix.$ExpKey", chimera.exp)
            .putInt("$prefix.$CurrentHpKey", chimera.currentHp)
            .putInt("$prefix.$IvHpKey", chimera.ivStats.maxHp)
            .putInt("$prefix.$IvAttackKey", chimera.ivStats.attack)
            .putInt("$prefix.$IvDefenceKey", chimera.ivStats.defence)
            .putInt("$prefix.$IvSpeedKey", chimera.ivStats.speed)
            .putInt("$prefix.$MovePpSizeKey", chimera.moves.size)
            .apply()

        chimera.moves.forEachIndexed { moveIndex, move ->
            saveMovePp(prefix, moveIndex, move)
        }
    }

    private fun saveItem(id: String, index: Int, item: SavedItem) {
        val prefix = "$id.$InventoryKey.$index"

        prefs.edit()
            .putString("$prefix.$ItemNameKey", item.itemName.saveName())
            .putInt("$prefix.$ItemAmountKey", item.amount)
            .apply()
    }

    private fun loadTeam(id: String): List<SavedChimera> {
        val teamSize = prefs.getInt("$id.$TeamSizeKey", 0)
        if (teamSize > 0) {
            return (0 until teamSize).mapNotNull { loadChimera(id, it) }
        }

        return loadLegacyStarter(id)?.let(::listOf).orEmpty()
    }

    private fun loadInventoryItems(id: String): List<SavedItem> {
        val inventorySize = prefs.getInt("$id.$InventorySizeKey", 0)
        return (0 until inventorySize).mapNotNull { loadItem(id, it) }
    }

    private fun loadChimera(id: String, index: Int): SavedChimera? {
        val prefix = "$id.$TeamKey.$index"
        val species = prefs.getString("$prefix.$SpeciesKey", null)?.toChimeraSpecies() ?: return null
        val nickname = prefs.getString("$prefix.$NicknameKey", null) ?: species.battleName()

        return SavedChimera(
            species = species,
            nickname = nickname,
            level = prefs.getInt("$prefix.$LevelKey", 5).coerceAtLeast(1),
            exp = prefs.getInt("$prefix.$ExpKey", 0).coerceAtLeast(0),
            currentHp = prefs.getInt("$prefix.$CurrentHpKey", NoSavedHp).coerceAtLeast(0),
            ivStats = Stats(
                maxHp = prefs.getInt("$prefix.$IvHpKey", 0),
                attack = prefs.getInt("$prefix.$IvAttackKey", 0),
                defence = prefs.getInt("$prefix.$IvDefenceKey", 0),
                speed = prefs.getInt("$prefix.$IvSpeedKey", 0)
            ),
            moves = loadMovePps(prefix)
        )
    }

    private fun saveMovePp(chimeraPrefix: String, index: Int, move: SavedMovePp) {
        val prefix = "$chimeraPrefix.$MovePpKey.$index"

        prefs.edit()
            .putString("$prefix.$MoveNameKey", move.moveName)
            .putInt("$prefix.$MovePpValueKey", move.pp)
            .apply()
    }

    private fun loadMovePps(chimeraPrefix: String): List<SavedMovePp> {
        val movePpSize = prefs.getInt("$chimeraPrefix.$MovePpSizeKey", 0)
        return (0 until movePpSize).mapNotNull { index ->
            val prefix = "$chimeraPrefix.$MovePpKey.$index"
            val moveName = prefs.getString("$prefix.$MoveNameKey", null) ?: return@mapNotNull null

            SavedMovePp(
                moveName = moveName,
                pp = prefs.getInt("$prefix.$MovePpValueKey", 0)
            )
        }
    }

    private fun loadItem(id: String, index: Int): SavedItem? {
        val prefix = "$id.$InventoryKey.$index"
        val itemName = prefs.getString("$prefix.$ItemNameKey", null)?.toItemName() ?: return null
        val amount = prefs.getInt("$prefix.$ItemAmountKey", 0)

        if (amount <= 0) return null

        return SavedItem(itemName, amount)
    }

    private fun loadLegacyStarter(id: String): SavedChimera? {
        val species = prefs.getString("$id.$StarterSpeciesKey", null)?.toChimeraSpecies() ?: return null
        val savedCurrentHp = prefs.getInt("$id.$StarterCurrentHpKey", NoSavedHp)
        val starter = ChimeraFactory.createChimera(species, prefs.getInt("$id.$StarterLevelKey", 5))

        return SavedChimera(
            species = species,
            nickname = prefs.getString("$id.$StarterNicknameKey", null) ?: species.battleName(),
            level = starter.level,
            exp = prefs.getInt("$id.$StarterExpKey", 0).coerceAtLeast(0),
            currentHp = savedCurrentHp.takeIf { it != NoSavedHp } ?: starter.stats.maxHp,
            ivStats = starter.ivStats,
            moves = emptyList()
        )
    }

    private fun Chimera.toSavedChimera(): SavedChimera {
        return SavedChimera(
            species = species,
            nickname = name,
            level = level,
            exp = exp,
            currentHp = stats.currentHp,
            ivStats = ivStats,
            moves = moves.map { move ->
                SavedMovePp(
                    moveName = move.name,
                    pp = move.pp
                )
            }
        )
    }

    private fun SavedChimera.toChimera(): Chimera {
        return ChimeraFactory.createChimera(
            species = species,
            level = level,
            ivStats = ivStats
        ).also { chimera ->
            chimera.rename(nickname)
            if (exp > 0) {
                chimera.gainExp(exp)
            }
            chimera.stats.restoreHp(currentHp)
            val savedPpsByMoveName = moves.associateBy { it.moveName }
            chimera.moves.forEach { move ->
                savedPpsByMoveName[move.name]?.let { savedMove ->
                    move.restorePp(savedMove.pp)
                }
            }
        }
    }

    private fun List<SavedItem>.toInventory(): Inventory {
        return Inventory().also { inventory ->
            forEach { savedItem ->
                inventory.addItem(ItemFactory.createItem(savedItem.itemName), savedItem.amount)
            }
        }
    }

    private fun trainerIds(): Set<String> {
        return prefs.getStringSet(TrainerIdsKey, emptySet()).orEmpty()
    }

    private fun trainerId(trainerName: String): String {
        return trainerName.trim().lowercase()
    }

    private fun ChimeraSpecies.saveName(): String = when (this) {
        ChimeraSpecies.Sunflare -> "Sunflare"
        ChimeraSpecies.Solflare -> "Solflare"
        ChimeraSpecies.Solignis -> "Solignis"
        ChimeraSpecies.Sylvhorn -> "Sylvhorn"
        ChimeraSpecies.Aquantis -> "Aquantis"
    }

    private fun String.toChimeraSpecies(): ChimeraSpecies? = when (this) {
        "Sunflare" -> ChimeraSpecies.Sunflare
        "Solflare" -> ChimeraSpecies.Solflare
        "Solignis" -> ChimeraSpecies.Solignis
        "Sylvhorn" -> ChimeraSpecies.Sylvhorn
        "Aquantis" -> ChimeraSpecies.Aquantis
        else -> null
    }

    private fun Item.toItemName(): ItemName? = when (name) {
        "Potion" -> ItemName.POTION
        "Super Potion" -> ItemName.SUPER_POTION
        "Revive" -> ItemName.REVIVE
        "Poke Ball", "Binding Stone" -> ItemName.BINDING_STONE
        else -> null
    }

    private fun ItemName.saveName(): String = when (this) {
        ItemName.POTION -> "Potion"
        ItemName.SUPER_POTION -> "Super Potion"
        ItemName.REVIVE -> "Revive"
        ItemName.BINDING_STONE -> "Binding Stone"
    }

    private fun String.toItemName(): ItemName? = when (this) {
        "Potion" -> ItemName.POTION
        "Super Potion" -> ItemName.SUPER_POTION
        "Revive" -> ItemName.REVIVE
        "Poke Ball", "Binding Stone" -> ItemName.BINDING_STONE
        else -> null
    }

    private fun ChimeraSpecies.battleName(): String = when (this) {
        ChimeraSpecies.Sunflare -> "Sunflare"
        ChimeraSpecies.Solflare -> "Solflare"
        ChimeraSpecies.Solignis -> "Solignis"
        ChimeraSpecies.Sylvhorn -> "Sylvhorn"
        ChimeraSpecies.Aquantis -> "Aquantis"
    }

    private companion object {
        const val PrefsName = "chimeralis_saves"
        const val TrainerIdsKey = "trainer_ids"
        const val TrainerNameKey = "trainer_name"
        const val TeamSizeKey = "team_size"
        const val TeamKey = "team"
        const val InventorySizeKey = "inventory_size"
        const val InventoryKey = "inventory"
        const val ItemNameKey = "item_name"
        const val ItemAmountKey = "item_amount"
        const val SpeciesKey = "species"
        const val NicknameKey = "nickname"
        const val LevelKey = "level"
        const val ExpKey = "exp"
        const val CurrentHpKey = "current_hp"
        const val IvHpKey = "iv_hp"
        const val IvAttackKey = "iv_attack"
        const val IvDefenceKey = "iv_defence"
        const val IvSpeedKey = "iv_speed"
        const val MovePpSizeKey = "move_pp_size"
        const val MovePpKey = "move_pp"
        const val MoveNameKey = "move_name"
        const val MovePpValueKey = "move_pp_value"
        const val StarterSpeciesKey = "starter_species"
        const val StarterNicknameKey = "starter_nickname"
        const val StarterLevelKey = "starter_level"
        const val StarterExpKey = "starter_exp"
        const val StarterCurrentHpKey = "starter_current_hp"
        const val PlayerColumnKey = "player_column"
        const val PlayerRowKey = "player_row"
        const val UpdatedAtKey = "updated_at"
        const val NoSavedHp = -1
    }
}
