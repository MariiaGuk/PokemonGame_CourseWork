package com.example.chimeralis.data.save

import android.content.Context
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.trainers.Player

/** Handles reading and writing game saves from Android SharedPreferences. */
class GameSaveStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
    private val mapper = GameSaveMapper()

    /**
     * Checks whether a trainer already has a saved game.
     *
     * @param trainerName The trainer name value used by this operation.
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
    fun hasSaveForTrainer(trainerName: String): Boolean {
        return trainerId(trainerName) in trainerIds()
    }

    /**
     * Loads all valid saves sorted from newest to oldest.
     *
     * @return The collection produced by this operation.
     */
    fun loadAll(): List<GameSave> {
        return trainerIds()
            .mapNotNull { id -> runCatching { load(id) }.getOrNull() }
            .sortedByDescending { it.updatedAt }
    }

    /**
     * Persists a complete game save snapshot.
     *
     * @param gameSave The game save value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Deletes one trainer save and its top-level metadata.
     *
     * @param trainerName The trainer name value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Loads one save by its normalized trainer id.
     *
     * @param id The id value used by this operation.
     * @return The resolved game save value, or null when it is unavailable.
     */
    private fun load(id: String): GameSave? {
        val trainerName = stringPref("$id.$TrainerNameKey") ?: return null
        val team = loadTeam(id).ifEmpty { return null }
        val storage = loadStorage(id)
        val inventoryItems = loadInventoryItems(id)
        val money = intPref("$id.$MoneyKey", StartingMoney).coerceAtLeast(0)
        val playerColumn = intPref("$id.$PlayerColumnKey", DefaultPlayerColumn).coerceAtLeast(0)
        val playerRow = intPref("$id.$PlayerRowKey", DefaultPlayerRow).coerceAtLeast(0)
        val location = stringPref("$id.$LocationKey")
            ?.let { savedName ->
                SavedGameLocation.values().firstOrNull { it.name == savedName }
            }
            ?: SavedGameLocation.LavaField
        val updatedAt = longPref("$id.$UpdatedAtKey", 0L).coerceAtLeast(0L)

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

    /**
     * Builds and persists a save snapshot from the current player state.
     *
     * @param trainerName The trainer name value used by this operation.
     * @param player Domain object used by this operation: player.
     * @param playerColumn The player column value used by this operation.
     * @param playerRow The player row value used by this operation.
     * @param location The location value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Recreates a runtime player model from a persisted save snapshot.
     *
     * @param gameSave The game save value used by this operation.
     * @return The resulting Player value.
     */
    fun createPlayer(gameSave: GameSave): Player {
        val team = gameSave.team.mapNotNull { savedChimera ->
            runCatching { mapper.toChimera(savedChimera) }.getOrNull()
        }
        require(team.isNotEmpty()) { "Saved player must have at least one valid chimera" }

        return Player(
            name = gameSave.trainerName,
            team = team,
            inventory = mapper.toInventory(gameSave.inventoryItems),
            storage = gameSave.storage.mapNotNull { savedChimera ->
                runCatching { mapper.toChimera(savedChimera) }.getOrNull()
            },
            money = gameSave.money
        )
    }

    /**
     * Saves one active team chimera.
     *
     * @param id The id value used by this operation.
     * @param index Numeric value used by this operation: index.
     * @param chimera Domain object used by this operation: chimera.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun saveChimera(id: String, index: Int, chimera: SavedChimera) {
        val prefix = "$id.$TeamKey.$index"
        saveChimera(prefix, chimera)
    }

    /**
     * Saves one stored chimera.
     *
     * @param id The id value used by this operation.
     * @param index Numeric value used by this operation: index.
     * @param chimera Domain object used by this operation: chimera.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun saveStorageChimera(id: String, index: Int, chimera: SavedChimera) {
        val prefix = "$id.$StorageKey.$index"
        saveChimera(prefix, chimera)
    }

    /**
     * Writes one chimera snapshot using the provided preference prefix.
     *
     * @param prefix The prefix value used by this operation.
     * @param chimera Domain object used by this operation: chimera.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
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

    /**
     * Saves one inventory item entry.
     *
     * @param id The id value used by this operation.
     * @param index Numeric value used by this operation: index.
     * @param item Domain object used by this operation: item.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun saveItem(id: String, index: Int, item: SavedItem) {
        val prefix = "$id.$InventoryKey.$index"

        prefs.edit()
            .putString("$prefix.$ItemNameKey", mapper.itemSaveName(item.itemName))
            .putInt("$prefix.$ItemAmountKey", item.amount)
            .apply()
    }

    /**
     * Loads the active team from the current save format.
     *
     * @param id The id value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun loadTeam(id: String): List<SavedChimera> {
        val teamSize = intPref("$id.$TeamSizeKey", 0).coerceAtLeast(0)
        if (teamSize > 0) {
            return (0 until teamSize).mapNotNull { index ->
                loadChimera("$id.$TeamKey.$index")
            }
        }

        return emptyList()
    }

    /**
     * Loads the stored chimeras from the current save format.
     *
     * @param id The id value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun loadStorage(id: String): List<SavedChimera> {
        val storageSize = intPref("$id.$StorageSizeKey", 0).coerceAtLeast(0)
        return (0 until storageSize).mapNotNull { index ->
            loadChimera("$id.$StorageKey.$index")
        }
    }

    /**
     * Loads every valid inventory entry for one save.
     *
     * @param id The id value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun loadInventoryItems(id: String): List<SavedItem> {
        val inventorySize = intPref("$id.$InventorySizeKey", 0).coerceAtLeast(0)
        return (0 until inventorySize).mapNotNull { loadItem(id, it) }
    }

    /**
     * Loads one chimera snapshot from a preference prefix.
     *
     * @param prefix The prefix value used by this operation.
     * @return The resolved saved chimera value, or null when it is unavailable.
     */
    private fun loadChimera(prefix: String): SavedChimera? {
        val species = stringPref("$prefix.$SpeciesKey")?.let(mapper::toChimeraSpecies) ?: return null
        val nickname = stringPref("$prefix.$NicknameKey") ?: mapper.battleName(species)

        return SavedChimera(
            species = species,
            nickname = nickname,
            level = intPref("$prefix.$LevelKey", DefaultChimeraLevel).coerceAtLeast(1),
            exp = intPref("$prefix.$ExpKey", 0).coerceAtLeast(0),
            currentHp = intPref("$prefix.$CurrentHpKey", NoSavedHp),
            ivStats = Stats(
                maxHp = intPref("$prefix.$IvHpKey", 0),
                attack = intPref("$prefix.$IvAttackKey", 0),
                defence = intPref("$prefix.$IvDefenceKey", 0),
                speed = intPref("$prefix.$IvSpeedKey", 0)
            ),
            moves = loadMovePps(prefix)
        )
    }

    /**
     * Saves the remaining PP values for one move.
     *
     * @param chimeraPrefix The chimera prefix value used by this operation.
     * @param index Numeric value used by this operation: index.
     * @param move Domain object used by this operation: move.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun saveMovePp(chimeraPrefix: String, index: Int, move: SavedMovePp) {
        val prefix = "$chimeraPrefix.$MovePpKey.$index"

        prefs.edit()
            .putString("$prefix.$MoveNameKey", move.moveName)
            .putInt("$prefix.$MovePpValueKey", move.pp)
            .apply()
    }

    /**
     * Loads all saved move PP values for one chimera.
     *
     * @param chimeraPrefix The chimera prefix value used by this operation.
     * @return The collection produced by this operation.
     */
    private fun loadMovePps(chimeraPrefix: String): List<SavedMovePp> {
        val movePpSize = intPref("$chimeraPrefix.$MovePpSizeKey", 0).coerceAtLeast(0)
        return (0 until movePpSize).mapNotNull { index ->
            val prefix = "$chimeraPrefix.$MovePpKey.$index"
            val moveName = stringPref("$prefix.$MoveNameKey") ?: return@mapNotNull null

            SavedMovePp(
                moveName = moveName,
                pp = intPref("$prefix.$MovePpValueKey", 0)
            )
        }
    }

    /**
     * Loads one inventory item entry.
     *
     * @param id The id value used by this operation.
     * @param index Numeric value used by this operation: index.
     * @return The resolved saved item value, or null when it is unavailable.
     */
    private fun loadItem(id: String, index: Int): SavedItem? {
        val prefix = "$id.$InventoryKey.$index"
        val itemName = stringPref("$prefix.$ItemNameKey")?.let(mapper::toItemName) ?: return null
        val amount = intPref("$prefix.$ItemAmountKey", 0)

        if (amount <= 0) return null

        return SavedItem(itemName, amount)
    }

    /**
     * Returns all trainer ids currently known to the save store.
     *
     * @return The collection produced by this operation.
     */
    private fun trainerIds(): Set<String> {
        return runCatching { prefs.getStringSet(TrainerIdsKey, emptySet()).orEmpty() }.getOrDefault(emptySet())
    }

    /**
     * Normalizes a trainer name into the preference id format.
     *
     * @param trainerName The trainer name value used by this operation.
     * @return The text value produced by this operation.
     */
    private fun trainerId(trainerName: String): String {
        return trainerName.trim().lowercase()
    }

    /**
     * Safely reads a string preference and ignores values with an unexpected type.
     *
     * @param key The key value used by this operation.
     * @return The resolved string value, or null when it is unavailable.
     */
    private fun stringPref(key: String): String? {
        return runCatching { prefs.getString(key, null) }.getOrNull()
    }

    /**
     * Safely reads an integer preference and ignores values with an unexpected type.
     *
     * @param key The key value used by this operation.
     * @param defaultValue The default value value used by this operation.
     * @return The calculated numeric value.
     */
    private fun intPref(key: String, defaultValue: Int): Int {
        return runCatching { prefs.getInt(key, defaultValue) }.getOrDefault(defaultValue)
    }

    /**
     * Safely reads a long preference and ignores values with an unexpected type.
     *
     * @param key The key value used by this operation.
     * @param defaultValue The default value value used by this operation.
     * @return The calculated numeric value.
     */
    private fun longPref(key: String, defaultValue: Long): Long {
        return runCatching { prefs.getLong(key, defaultValue) }.getOrDefault(defaultValue)
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
        const val DefaultPlayerColumn = 1
        const val DefaultPlayerRow = 1
        const val DefaultChimeraLevel = 5
    }
}
