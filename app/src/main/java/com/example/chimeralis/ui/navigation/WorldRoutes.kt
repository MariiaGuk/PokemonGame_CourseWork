package com.example.chimeralis.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.ui.screens.world.Direction
import com.example.chimeralis.ui.screens.world.WorldField
import com.example.chimeralis.ui.screens.world.WorldScreen
import com.example.chimeralis.ui.screens.world.locations.TownInterior

@Composable
internal fun GameSessionState.WorldRoute(
    screen: GameScreen,
    field: WorldField,
    transition: GameTransitionState,
    context: Context,
    onExitGame: () -> Unit
) {
    WorldScreen(
        starter = selectedStarter,
        team = player?.team?.toList().orEmpty(),
        inventoryItems = player?.inventory?.items.orEmpty(),
        money = player?.money ?: 0,
        teamStateKey = teamVersion,
        canStartBattles = canStartBattles,
        field = field,
        showShiftNpc = true,
        shiftNpcIntroSeen = shiftNpcIntroSeen,
        worldTransitionScale = transition.battleZoomScale.value,
        inputLockKey = worldInputLockKey,
        initialPlayerColumn = playerColumn,
        initialPlayerRow = playerRow,
        initialPlayerDirection = playerDirection,
        hasUnsavedChanges = hasUnsavedChanges,
        musicEnabled = musicEnabled,
        musicVolume = musicVolume,
        soundEnabled = soundEnabled,
        soundVolume = soundVolume,
        encounterChance = encounterChance,
        onMusicEnabledChanged = { musicEnabled = it },
        onMusicVolumeChanged = { musicVolume = it },
        onSoundEnabledChanged = { soundEnabled = it },
        onSoundVolumeChanged = { soundVolume = it },
        onEncounterChanceChanged = { encounterChance = it },
        onPlayerPositionChanged = { column, row ->
            playerColumn = column
            playerRow = row
        },
        onPlayerDirectionChanged = { playerDirection = it },
        onSaveGame = { column, row ->
            playerColumn = column
            playerRow = row
            if (saveCurrentGame(column, row)) {
                GameSoundPlayer.play(context, R.raw.save_game)
            }
        },
        onUseInventoryItem = { item, chimera ->
            useInventoryItem(item, chimera)
        },
        onTravelToGrassField = {
            playerColumn = 1
            playerRow = 4
            playerDirection = Direction.Right
            returnWorldScreen = GameScreen.GrassField
            transition.transitionTo(GameScreen.GrassField, this)
        },
        onReturnToLavaField = {
            playerColumn = 19
            playerRow = 4
            playerDirection = Direction.Left
            returnWorldScreen = GameScreen.LavaField
            transition.transitionTo(GameScreen.LavaField, this)
        },
        onEnterTownInterior = { interior ->
            playerColumn = 7
            playerRow = 14
            playerDirection = Direction.Up
            transition.transitionTo(interior.screen, this)
        },
        onShiftNpcIntroSeen = {
            shiftNpcIntroSeen = true
        },
        onBackToMainMenu = {
            currentScreen = GameScreen.MainMenu
        },
        onExitGame = onExitGame,
        onWildEncounter = { wildSpecies ->
            if (player?.isDefeated() != false) return@WorldScreen

            wildEncounter = wildSpecies
            returnWorldScreen = screen
            transition.transitionTo(GameScreen.Battle, this)
        }
    )
}

private val TownInterior.screen: GameScreen
    get() = when (this) {
        TownInterior.ChimeraCenter -> GameScreen.ChimeraCenterInterior
        TownInterior.ChimeraStore -> GameScreen.ChimeraStoreInterior
    }
