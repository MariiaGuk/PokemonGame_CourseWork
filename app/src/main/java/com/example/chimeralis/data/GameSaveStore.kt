package com.example.chimeralis.data

import android.content.Context
import com.example.chimeralis.logic.chimeras.ChimeraSpecies

data class GameSave(
    val trainerName: String,
    val starterSpecies: ChimeraSpecies,
    val starterNickname: String,
    val playerColumn: Int,
    val playerRow: Int,
    val updatedAt: Long
)

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
            .putString("$id.$StarterSpeciesKey", gameSave.starterSpecies.saveName())
            .putString("$id.$StarterNicknameKey", gameSave.starterNickname)
            .putInt("$id.$PlayerColumnKey", gameSave.playerColumn)
            .putInt("$id.$PlayerRowKey", gameSave.playerRow)
            .putLong("$id.$UpdatedAtKey", gameSave.updatedAt)
            .apply()
    }

    fun delete(trainerName: String) {
        val id = trainerId(trainerName)
        val ids = trainerIds() - id

        prefs.edit()
            .putStringSet(TrainerIdsKey, ids)
            .remove("$id.$TrainerNameKey")
            .remove("$id.$StarterSpeciesKey")
            .remove("$id.$StarterNicknameKey")
            .remove("$id.$PlayerColumnKey")
            .remove("$id.$PlayerRowKey")
            .remove("$id.$UpdatedAtKey")
            .apply()
    }

    private fun load(id: String): GameSave? {
        val trainerName = prefs.getString("$id.$TrainerNameKey", null) ?: return null
        val speciesName = prefs.getString("$id.$StarterSpeciesKey", null) ?: return null
        val species = speciesName.toChimeraSpecies() ?: return null
        val nickname = prefs.getString("$id.$StarterNicknameKey", null) ?: species.battleName()
        val playerColumn = prefs.getInt("$id.$PlayerColumnKey", 1)
        val playerRow = prefs.getInt("$id.$PlayerRowKey", 1)
        val updatedAt = prefs.getLong("$id.$UpdatedAtKey", 0L)

        return GameSave(
            trainerName = trainerName,
            starterSpecies = species,
            starterNickname = nickname,
            playerColumn = playerColumn,
            playerRow = playerRow,
            updatedAt = updatedAt
        )
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
        const val StarterSpeciesKey = "starter_species"
        const val StarterNicknameKey = "starter_nickname"
        const val PlayerColumnKey = "player_column"
        const val PlayerRowKey = "player_row"
        const val UpdatedAtKey = "updated_at"
    }
}
