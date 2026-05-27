package com.example.chimeralis.data

import android.content.Context
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.logic.trainers.Player

/** Serializable chimera snapshot stored in SharedPreferences. */
data class SavedChimera(
    val species: ChimeraSpecies,
    val nickname: String,
    val level: Int,
    val exp: Int,
    val currentHp: Int,
    val ivStats: Stats,
    val moves: List<SavedMovePp> = emptyList()
)

/** Serializable move PP snapshot for a saved chimera. */
data class SavedMovePp(
    val moveName: String,
    val pp: Int
)

/** Serializable inventory entry with item type and amount. */
data class SavedItem(
    val itemName: ItemName,
    val amount: Int
)

/** Identifies the screen or interior where the player saved the game. */
enum class SavedGameLocation {
    LavaField,
    GrassField,
    ChimeraCenterInterior,
    ChimeraStoreInterior
}

/** Full save snapshot used by the continue menu and persistence layer. */
data class GameSave(
    val trainerName: String,
    val team: List<SavedChimera>,
    val storage: List<SavedChimera> = emptyList(),
    val inventoryItems: List<SavedItem> = emptyList(),
    val money: Int = 0,
    val playerColumn: Int,
    val playerRow: Int,
    val location: SavedGameLocation = SavedGameLocation.LavaField,
    val updatedAt: Long
) {
    /** Returns the first team member species for compact save descriptions. */
    val starterSpecies: ChimeraSpecies get() = team.first().species

    /** Returns the first team member nickname for compact save descriptions. */
    val starterNickname: String get() = team.first().nickname
}

/** Handles reading and writing game saves from Android SharedPreferences. */
class GameSaveStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
    private val mapper = GameSaveMapper()

    /** Checks whether a trainer already has a saved game. */
    fun hasSaveForTrainer(trainerName: String): Boolean {
        return trainerId(trainerName) in trainerIds()
    }

    /** Loads all valid saves sorted from newest to oldest. */
    fun loadAll(): List<GameSave> {
        return trainerIds()
            .mapNotNull(::load)
            .sortedByDescending { it.updatedAt }
    }

    /** Persists a complete game save snapshot. */
    fun save(gameSave: GameSave) {
        val id = trainerId(gameSave.trainerName)
        val ids = trainerIds() + id

        prefs.edit()
            .putStringSet(TrainerIdsKey, ids)
            .putString("$id.$TrainerNameKey", gameSave.trainerName)
            .putInt("$id.$TeamSizeKey", gameSave.team.size)
            .putInt("$id.$StorageSizeKey", gameSave.storage.size)
            .putInt("$id.$InventorySizeKey", gameSave.inventoryItems.size)
            .putInt("$id.$MoneyKey", gameSave.money)
            .putInt("$id.$PlayerColumnKey", gameSave.playerColumn)
            .putInt("$id.$PlayerRowKey", gameSave.playerRow)
            .putString("$id.$LocationKey", gameSave.location.name)
            .putLong("$id.$UpdatedAtKey", gameSave.updatedAt)
            .apply()

        gameSave.team.forEachIndexed { index, chimera ->
            saveChimera(id, index, chimera)
        }
        gameSave.storage.forEachIndexed { index, chimera ->
            saveStorageChimera(id, index, chimera)
        }
        gameSave.inventoryItems.forEachIndexed { index, item ->
            saveItem(id, index, item)
        }
    }

    /** Deletes one trainer save and its top-level metadata. */
    fun delete(trainerName: String) {
        val id = trainerId(trainerName)
        val ids = trainerIds() - id

        prefs.edit()
            .putStringSet(TrainerIdsKey, ids)
            .remove("$id.$TrainerNameKey")
            .remove("$id.$TeamSizeKey")
            .remove("$id.$StorageSizeKey")
            .remove("$id.$InventorySizeKey")
            .remove("$id.$MoneyKey")
            .remove("$id.$PlayerColumnKey")
            .remove("$id.$PlayerRowKey")
            .remove("$id.$LocationKey")
            .remove("$id.$UpdatedAtKey")
            .apply()
    }

    /** Loads one save by its normalized trainer id. */
    private fun load(id: String): GameSave? {
        val trainerName = prefs.getString("$id.$TrainerNameKey", null) ?: return null
        val team = loadTeam(id).ifEmpty { return null }
        val storage = loadStorage(id)
        val inventoryItems = loadInventoryItems(id)
        val money = prefs.getInt("$id.$MoneyKey", StartingMoney)
        val playerColumn = prefs.getInt("$id.$PlayerColumnKey", 1)
        val playerRow = prefs.getInt("$id.$PlayerRowKey", 1)
        val location = prefs.getString("$id.$LocationKey", null)
            ?.let { savedName ->
                SavedGameLocation.values().firstOrNull { it.name == savedName }
            }
            ?: SavedGameLocation.LavaField
        val updatedAt = prefs.getLong("$id.$UpdatedAtKey", 0L)

        return GameSave(
            trainerName = trainerName,
            team = team,
            storage = storage,
            inventoryItems = inventoryItems,
            money = money,
            playerColumn = playerColumn,
            playerRow = playerRow,
            location = location,
            updatedAt = updatedAt
        )
    }

    /** Builds and persists a save snapshot from the current player state. */
    fun saveFromPlayer(
        trainerName: String,
        player: Player,
        playerColumn: Int,
        playerRow: Int,
        location: SavedGameLocation
    ) {
        save(
            GameSave(
                trainerName = trainerName,
                team = player.team.map(mapper::toSavedChimera),
                storage = player.storage.map(mapper::toSavedChimera),
                inventoryItems = player.inventory.items.map { (item, amount) ->
                    mapper.toSavedItem(item, amount)
                },
                money = player.money,
                playerColumn = playerColumn,
                playerRow = playerRow,
                location = location,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** Recreates a runtime player model from a persisted save snapshot. */
    fun createPlayer(gameSave: GameSave): Player {
        return Player(
            name = gameSave.trainerName,
            team = gameSave.team.map(mapper::toChimera).toMutableList(),
            inventory = mapper.toInventory(gameSave.inventoryItems),
            storage = gameSave.storage.map(mapper::toChimera).toMutableList(),
            money = gameSave.money
        )
    }

    /** Saves one active team chimera. */
    private fun saveChimera(id: String, index: Int, chimera: SavedChimera) {
        val prefix = "$id.$TeamKey.$index"
        saveChimera(prefix, chimera)
    }

    /** Saves one stored chimera. */
    private fun saveStorageChimera(id: String, index: Int, chimera: SavedChimera) {
        val prefix = "$id.$StorageKey.$index"
        saveChimera(prefix, chimera)
    }

    /** Writes one chimera snapshot using the provided preference prefix. */
    private fun saveChimera(prefix: String, chimera: SavedChimera) {
        prefs.edit()
            .putString("$prefix.$SpeciesKey", mapper.speciesSaveName(chimera.species))
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

    /** Saves one inventory item entry. */
    private fun saveItem(id: String, index: Int, item: SavedItem) {
        val prefix = "$id.$InventoryKey.$index"

        prefs.edit()
            .putString("$prefix.$ItemNameKey", mapper.itemSaveName(item.itemName))
            .putInt("$prefix.$ItemAmountKey", item.amount)
            .apply()
    }

    /** Loads the active team from the current save format. */
    private fun loadTeam(id: String): List<SavedChimera> {
        val teamSize = prefs.getInt("$id.$TeamSizeKey", 0)
        if (teamSize > 0) {
            return (0 until teamSize).mapNotNull { index ->
                loadChimera("$id.$TeamKey.$index")
            }
        }

        return emptyList()
    }

    /** Loads the stored chimeras from the current save format. */
    private fun loadStorage(id: String): List<SavedChimera> {
        val storageSize = prefs.getInt("$id.$StorageSizeKey", 0)
        return (0 until storageSize).mapNotNull { index ->
            loadChimera("$id.$StorageKey.$index")
        }
    }

    /** Loads every valid inventory entry for one save. */
    private fun loadInventoryItems(id: String): List<SavedItem> {
        val inventorySize = prefs.getInt("$id.$InventorySizeKey", 0)
        return (0 until inventorySize).mapNotNull { loadItem(id, it) }
    }

    /** Loads one chimera snapshot from a preference prefix. */
    private fun loadChimera(prefix: String): SavedChimera? {
        val species = prefs.getString("$prefix.$SpeciesKey", null)?.let(mapper::toChimeraSpecies) ?: return null
        val nickname = prefs.getString("$prefix.$NicknameKey", null) ?: mapper.battleName(species)

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

    /** Saves the remaining PP values for one move. */
    private fun saveMovePp(chimeraPrefix: String, index: Int, move: SavedMovePp) {
        val prefix = "$chimeraPrefix.$MovePpKey.$index"

        prefs.edit()
            .putString("$prefix.$MoveNameKey", move.moveName)
            .putInt("$prefix.$MovePpValueKey", move.pp)
            .apply()
    }

    /** Loads all saved move PP values for one chimera. */
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

    /** Loads one inventory item entry. */
    private fun loadItem(id: String, index: Int): SavedItem? {
        val prefix = "$id.$InventoryKey.$index"
        val itemName = prefs.getString("$prefix.$ItemNameKey", null)?.let(mapper::toItemName) ?: return null
        val amount = prefs.getInt("$prefix.$ItemAmountKey", 0)

        if (amount <= 0) return null

        return SavedItem(itemName, amount)
    }

    /** Returns all trainer ids currently known to the save store. */
    private fun trainerIds(): Set<String> {
        return prefs.getStringSet(TrainerIdsKey, emptySet()).orEmpty()
    }

    /** Normalizes a trainer name into the preference id format. */
    private fun trainerId(trainerName: String): String {
        return trainerName.trim().lowercase()
    }

    private companion object {
        const val PrefsName = "chimeralis_saves"
        const val TrainerIdsKey = "trainer_ids"
        const val TrainerNameKey = "trainer_name"
        const val TeamSizeKey = "team_size"
        const val TeamKey = "team"
        const val StorageSizeKey = "storage_size"
        const val StorageKey = "storage"
        const val InventorySizeKey = "inventory_size"
        const val InventoryKey = "inventory"
        const val ItemNameKey = "item_name"
        const val ItemAmountKey = "item_amount"
        const val MoneyKey = "money"
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
        const val PlayerColumnKey = "player_column"
        const val PlayerRowKey = "player_row"
        const val LocationKey = "location"
        const val UpdatedAtKey = "updated_at"
        const val NoSavedHp = -1
        const val StartingMoney = 200
    }
}
