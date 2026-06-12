package com.example.chimeralis.ui.navigation.routes

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.R
import com.example.chimeralis.ui.navigation.GameScreen
import com.example.chimeralis.ui.navigation.GameTransitionState
import com.example.chimeralis.ui.navigation.session.buyItem
import com.example.chimeralis.ui.navigation.session.depositTeamMember
import com.example.chimeralis.ui.navigation.session.GameSessionState
import com.example.chimeralis.ui.navigation.session.healTeam
import com.example.chimeralis.ui.navigation.session.renameStoredChimera
import com.example.chimeralis.ui.navigation.session.renameTeamChimera
import com.example.chimeralis.ui.navigation.session.saveCurrentGame
import com.example.chimeralis.ui.navigation.session.swapTeamMembers
import com.example.chimeralis.ui.navigation.session.swapTeamWithStorage
import com.example.chimeralis.ui.navigation.session.useInventoryItem
import com.example.chimeralis.ui.navigation.session.withdrawStoredChimera
import com.example.chimeralis.ui.screens.world.model.Direction
import com.example.chimeralis.ui.screens.world.interior.TownInteriorScreen
import com.example.chimeralis.ui.screens.world.locations.TownInterior

/**
 * Renders the town interior route UI.
 *
 * @receiver The game session state receiver used by this operation.
 * @param interior The interior value used by this operation.
 * @param transition The transition value used by this operation.
 * @param context Android context used to access application resources and services.
 * @param onExitGame Callback invoked when exit game occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun GameSessionState.TownInteriorRoute(
    interior: TownInterior,
    transition: GameTransitionState,
    context: Context,
    onExitGame: () -> Unit
) {
    TownInteriorScreen(
        interior = interior,
        team = player?.team.orEmpty(),
        storage = player?.storage.orEmpty(),
        inventoryItems = player?.inventory?.items.orEmpty(),
        teamStateKey = teamVersion,
        money = player?.money ?: 0,
        musicEnabled = musicEnabled,
        musicVolume = musicVolume,
        soundEnabled = soundEnabled,
        soundVolume = soundVolume,
        encounterChance = encounterChance,
        hasUnsavedChanges = hasUnsavedChanges,
        onMusicEnabledChanged = { musicEnabled = it },
        onMusicVolumeChanged = { musicVolume = it },
        onSoundEnabledChanged = { soundEnabled = it },
        onSoundVolumeChanged = { soundVolume = it },
        onEncounterChanceChanged = { encounterChance = it },
        initialPlayerColumn = playerColumn,
        initialPlayerRow = playerRow,
        initialPlayerDirection = playerDirection,
        inputLockKey = worldInputLockKey,
        onHealTeam = {
            if (interior != TownInterior.ChimeraCenter) return@TownInteriorScreen

            healTeam()
        },
        onBuyItem = { itemName, amount ->
            if (interior != TownInterior.ChimeraStore) return@TownInteriorScreen false

            buyItem(itemName, amount)
        },
        onUseInventoryItem = { item, chimera ->
            useInventoryItem(item, chimera)
        },
        onPlayerPositionChanged = { column, row ->
            playerColumn = column
            playerRow = row
        },
        onPlayerDirectionChanged = { playerDirection = it },
        onSwapTeamMembers = { fromIndex, toIndex ->
            swapTeamMembers(fromIndex, toIndex)
        },
        onDepositTeamMember = { teamIndex ->
            depositTeamMember(teamIndex)
        },
        onWithdrawStoredChimera = { storageIndex ->
            withdrawStoredChimera(storageIndex)
        },
        onSwapTeamWithStorage = { teamIndex, storageIndex ->
            swapTeamWithStorage(teamIndex, storageIndex)
        },
        onRenameTeamChimera = { teamIndex, newName ->
            renameTeamChimera(teamIndex, newName)
        },
        onRenameStoredChimera = { storageIndex, newName ->
            renameStoredChimera(storageIndex, newName)
        },
        onSaveGame = { column, row ->
            playerColumn = column
            playerRow = row
            if (saveCurrentGame(column, row)) {
                GameSoundPlayer.play(context, R.raw.save_game)
            }
        },
        onBackToMainMenu = {
            currentScreen = GameScreen.MainMenu
        },
        onExitGame = onExitGame,
        onExit = {
            when (interior) {
                TownInterior.ChimeraCenter -> {
                    playerColumn = 7
                    playerRow = 4
                }
                TownInterior.ChimeraStore -> {
                    playerColumn = 12
                    playerRow = 4
                }
            }
            playerDirection = Direction.Down
            transition.transitionTo(GameScreen.GrassField, this)
        }
    )
}
