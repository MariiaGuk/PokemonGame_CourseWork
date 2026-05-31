package com.example.chimeralis.ui.screens.world

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.chimeralis.R
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.overlays.WorldControlsOverlay
import com.example.chimeralis.ui.screens.world.locations.TownLocationBuildings
import com.example.chimeralis.ui.screens.world.locations.TownLocationSigns
import com.example.chimeralis.ui.screens.world.locations.TownLocationTiles
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.WildFieldLocationTiles
import com.example.chimeralis.ui.screens.world.locations.grassTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownBuildingTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownPathTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownSigns
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/** Renders the world screen UI. */
@Composable
fun WorldScreen(
    starter: ChimeraSpecies?,
    team: List<Chimera> = emptyList(),
    inventoryItems: Map<Item, Int> = emptyMap(),
    money: Int = 0,
    teamStateKey: Int = 0,
    canStartBattles: Boolean = true,
    field: WorldField = WorldField.Lava,
    showShiftNpc: Boolean = false,
    shiftNpcIntroSeen: Boolean = false,
    worldTransitionScale: Float = 1f,
    inputLockKey: Int = 0,
    initialPlayerColumn: Int = 1,
    initialPlayerRow: Int = 1,
    initialPlayerDirection: Direction = Direction.Down,
    hasUnsavedChanges: Boolean = false,
    musicEnabled: Boolean = true,
    musicVolume: Float = 1f,
    soundEnabled: Boolean = true,
    soundVolume: Float = 1f,
    encounterChance: Float = 0.22f,
    onMusicEnabledChanged: (Boolean) -> Unit = {},
    onMusicVolumeChanged: (Float) -> Unit = {},
    onSoundEnabledChanged: (Boolean) -> Unit = {},
    onSoundVolumeChanged: (Float) -> Unit = {},
    onEncounterChanceChanged: (Float) -> Unit = {},
    onPlayerPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onPlayerDirectionChanged: (Direction) -> Unit = {},
    onSaveGame: (Int, Int) -> Unit = { _, _ -> },
    onUseInventoryItem: (Item, Chimera) -> Unit = { _, _ -> },
    onTravelToGrassField: () -> Unit = {},
    onReturnToLavaField: () -> Unit = {},
    onEnterTownInterior: (TownInterior) -> Unit = {},
    onShiftNpcIntroSeen: () -> Unit = {},
    onTrainerChallenge: () -> Unit = {},
    onBackToMainMenu: () -> Unit,
    onExitGame: () -> Unit,
    onWildEncounter: (ChimeraSpecies) -> Unit
) {
    val screenState = rememberWorldScreenState(
        initialPlayerColumn = initialPlayerColumn,
        initialPlayerRow = initialPlayerRow,
        initialPlayerDirection = initialPlayerDirection
    )
    val groundTexture = ImageBitmap.imageResource(
        id = if (field == WorldField.Grass) R.drawable.grass_field_ground else R.drawable.lava_ground
    )
    val grassTexture = ImageBitmap.imageResource(
        id = if (field == WorldField.Grass) R.drawable.bush_field_tile else R.drawable.rock_grass_tile
    )
    val pathTexture = ImageBitmap.imageResource(id = R.drawable.path_field_overlay)
    val shiftNpcTile = if (field == WorldField.Grass) {
        GrassShiftNpcColumn to GrassShiftNpcRow
    } else {
        LavaShiftNpcColumn to LavaShiftNpcRow
    }
    val isShiftNpcDialogOpen = screenState.isShiftNpcDialogOpen
    val isTrainerNpcDialogOpen = screenState.isTrainerNpcDialogOpen
    val trainerNpcTile = GrassTrainerNpcColumn to GrassTrainerNpcRow
    val canInteractWithShiftNpc = showShiftNpc &&
            !isShiftNpcDialogOpen &&
            !isTrainerNpcDialogOpen &&
            abs(screenState.playerColumn - shiftNpcTile.first) + abs(screenState.playerRow - shiftNpcTile.second) == 1
    val canInteractWithTrainerNpc = field == WorldField.Grass &&
            !isShiftNpcDialogOpen &&
            !isTrainerNpcDialogOpen &&
            abs(screenState.playerColumn - trainerNpcTile.first) + abs(screenState.playerRow - trainerNpcTile.second) == 1
    val townInteriorAtDoor = when {
        field == WorldField.Grass && screenState.playerRow == 4 && screenState.playerColumn in 7..8 -> {
            TownInterior.ChimeraCenter
        }
        field == WorldField.Grass && screenState.playerRow == 4 && screenState.playerColumn in 12..13 -> {
            TownInterior.ChimeraStore
        }
        else -> null
    }
    val canEnterTownInterior = townInteriorAtDoor != null && !isShiftNpcDialogOpen && !isTrainerNpcDialogOpen
    val readableTownSign = if (field == WorldField.Grass && !isShiftNpcDialogOpen && !isTrainerNpcDialogOpen) {
        grassTownSigns.firstOrNull { sign ->
            abs(screenState.playerColumn - sign.column) + abs(screenState.playerRow - sign.row) <= 1
        }
    } else {
        null
    }
    val canReadTownSign = readableTownSign != null && !canEnterTownInterior
    val currentEncounterChance by rememberUpdatedState(encounterChance)
    val currentCanStartBattles by rememberUpdatedState(canStartBattles)
    val currentStarter by rememberUpdatedState(starter)
    val currentOnWildEncounter by rememberUpdatedState(onWildEncounter)

    val animatedColumn by animateFloatAsState(
        targetValue = screenState.targetColumn.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerColumn"
    )
    val animatedRow by animateFloatAsState(
        targetValue = screenState.targetRow.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerRow"
    )

    LaunchedEffect(screenState.isMoving, screenState.direction) {
        while (true) {
            screenState.advanceAnimationFrame()
            delay(if (screenState.isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(screenState.showSaveMessage) {
        if (screenState.showSaveMessage) {
            delay(1600L)
            screenState.hideSaveConfirmation()
        }
    }

    LaunchedEffect(inputLockKey) {
        if (inputLockKey == 0) return@LaunchedEffect

        screenState.beginWorldInputLock()
        delay(WorldReturnInputLockMs)
        screenState.endWorldInputLock()
    }

    LaunchedEffect(showShiftNpc) {
        if (!showShiftNpc) return@LaunchedEffect

        while (true) {
            screenState.advanceShiftNpcIdleFrame()
            delay(ShiftNpcIdleFrameDelayMs)
        }
    }

    LaunchedEffect(field) {
        if (field != WorldField.Grass) return@LaunchedEffect

        while (true) {
            screenState.advanceTrainerNpcIdleFrame()
            delay(ShiftNpcIdleFrameDelayMs)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (!screenState.areWorldControlsEnabled) {
                screenState.stopMovement()
                delay(16L)
                continue
            }

            val nextDirection = screenState.requestedDirection
            if (nextDirection == null) {
                delay(16L)
                continue
            }

            val currentColumn = screenState.playerColumn
            val currentRow = screenState.playerRow
            val nextTile = nextTile(currentColumn, currentRow, nextDirection)
            screenState.face(nextDirection)
            onPlayerDirectionChanged(nextDirection)

            if (nextTile.first == currentColumn && nextTile.second == currentRow) {
                delay(HeldStepDelayMs)
                continue
            }

            if (showShiftNpc && nextTile == shiftNpcTile) {
                delay(HeldStepDelayMs)
                continue
            }

            if (field == WorldField.Grass && nextTile == trainerNpcTile) {
                delay(HeldStepDelayMs)
                continue
            }

            if (field == WorldField.Grass && nextTile !in grassTownPathTiles) {
                delay(HeldStepDelayMs)
                continue
            }

            if (field == WorldField.Grass && nextTile in grassTownBuildingTiles) {
                delay(HeldStepDelayMs)
                continue
            }

            screenState.beginStepTo(nextTile.first, nextTile.second)
            delay(StepDurationMs.toLong())

            screenState.finishStepAt(nextTile.first, nextTile.second)
            onPlayerPositionChanged(screenState.playerColumn, screenState.playerRow)

            val chance = currentEncounterChance.coerceIn(0f, 1f)
            val isWildGrassTile = field != WorldField.Grass &&
                    nextTile in grassTiles &&
                    !(field == WorldField.Grass && nextTile in grassTownPathTiles) &&
                    !(field == WorldField.Grass && nextTile in grassTownBuildingTiles)
            val shouldStartEncounter = isWildGrassTile &&
                    currentCanStartBattles &&
                    chance > 0f &&
                    (chance >= 1f || Random.nextFloat() < chance)

            if (shouldStartEncounter) {
                screenState.startWildEncounter()
                currentOnWildEncounter(randomWildChimera(currentStarter))
            }

            delay(1L)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF241829))
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val tileWidth = widthPx / MapColumns * WorldZoom
        val tileHeight = heightPx / MapRows * WorldZoom
        val mapWidth = tileWidth * MapColumns
        val mapHeight = tileHeight * MapRows
        val mapLeft = (widthPx / 2f - (animatedColumn + 0.5f) * tileWidth)
            .coerceIn(widthPx - mapWidth, 0f)
        val mapTop = (heightPx / 2f - (animatedRow + 0.5f) * tileHeight)
            .coerceIn(heightPx - mapHeight, 0f)
        val spriteBaseSize = minOf(tileWidth, tileHeight)
        val spriteWidth = spriteBaseSize * 1.06f
        val spriteHeight = spriteBaseSize * 1.62f
        val playerCenterX = mapLeft + (animatedColumn + 0.5f) * tileWidth
        val playerCenterY = mapTop + (animatedRow + 0.5f) * tileHeight
        val npcHeight = spriteHeight
        val npcWidth = spriteWidth
        val npcCenterX = mapLeft + (shiftNpcTile.first + 0.5f) * tileWidth
        val npcBottomY = mapTop + (shiftNpcTile.second + 0.89f) * tileHeight
        val npcModifier = Modifier
            .offset {
                IntOffset(
                    x = (npcCenterX - npcWidth / 2f).roundToInt(),
                    y = (npcBottomY - npcHeight).roundToInt()
                )
            }
            .size(
                width = with(density) { npcWidth.toDp() },
                height = with(density) { npcHeight.toDp() }
            )
            .graphicsLayer {
                scaleX = if (field == WorldField.Grass) -1f else 1f
            }
        val shouldDrawShiftNpcBeforePlayer = showShiftNpc && animatedRow > shiftNpcTile.second
        val trainerNpcCenterX = mapLeft + (trainerNpcTile.first + 0.5f) * tileWidth
        val trainerNpcBottomY = mapTop + (trainerNpcTile.second + 0.89f) * tileHeight
        val trainerNpcModifier = Modifier
            .offset {
                IntOffset(
                    x = (trainerNpcCenterX - npcWidth / 2f).roundToInt(),
                    y = (trainerNpcBottomY - npcHeight).roundToInt()
                )
            }
            .size(
                width = with(density) { npcWidth.toDp() },
                height = with(density) { npcHeight.toDp() }
            )
        val shouldDrawTrainerNpcBeforePlayer = field == WorldField.Grass && animatedRow > trainerNpcTile.second

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = worldTransitionScale
                    scaleY = worldTransitionScale
                }
        ) {
            if (field == WorldField.Grass) {
                TownLocationTiles(
                    mapLeft = mapLeft,
                    mapTop = mapTop,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight,
                    groundTexture = groundTexture,
                    pathTexture = pathTexture
                )
                TownLocationBuildings(
                    drawOverPlayer = false,
                    animatedRow = animatedRow,
                    mapLeft = mapLeft,
                    mapTop = mapTop,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight
                )
                TownLocationSigns(
                    mapLeft = mapLeft,
                    mapTop = mapTop,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight
                )
            } else {
                WildFieldLocationTiles(
                    mapLeft = mapLeft,
                    mapTop = mapTop,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight,
                    groundTexture = groundTexture,
                    grassTexture = grassTexture
                )
            }

            if (shouldDrawShiftNpcBeforePlayer) {
                ShiftNpcWorldSprite(
                    frameIndex = screenState.shiftNpcIdleFrame,
                    modifier = npcModifier
                )
            }

            if (shouldDrawTrainerNpcBeforePlayer) {
                TrainerNpcWorldSprite(
                    frameIndex = screenState.trainerNpcIdleFrame,
                    modifier = trainerNpcModifier
                )
            }

            Image(
                painter = painterResource(
                    id = playerFrame(
                        screenState.direction,
                        screenState.isMoving,
                        screenState.animationFrame
                    )
                ),
                contentDescription = "Player",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (playerCenterX - spriteWidth / 2f).roundToInt(),
                            y = (playerCenterY - spriteHeight * 0.76f).roundToInt()
                        )
                    }
                    .size(
                        width = with(density) { spriteWidth.toDp() },
                        height = with(density) { spriteHeight.toDp() }
                    )
                    .graphicsLayer {
                        scaleX = if (screenState.direction == Direction.Left) -1f else 1f
                    }
            )

            if (showShiftNpc && !shouldDrawShiftNpcBeforePlayer) {
                ShiftNpcWorldSprite(
                    frameIndex = screenState.shiftNpcIdleFrame,
                    modifier = npcModifier
                )
            }

            if (field == WorldField.Grass && !shouldDrawTrainerNpcBeforePlayer) {
                TrainerNpcWorldSprite(
                    frameIndex = screenState.trainerNpcIdleFrame,
                    modifier = trainerNpcModifier
                )
            }

            if (field == WorldField.Grass) {
                TownLocationBuildings(
                    drawOverPlayer = true,
                    animatedRow = animatedRow,
                    mapLeft = mapLeft,
                    mapTop = mapTop,
                    tileWidth = tileWidth,
                    tileHeight = tileHeight
                )
            }
        }

        val worldControlsEnabled = screenState.areWorldControlsEnabled
        val worldActionLabel = if (
            !screenState.isGameMenuOpen &&
            !screenState.isInventoryOpen &&
            screenState.activeTownSign == null
        ) {
            when {
                canEnterTownInterior -> "Enter"
                canInteractWithShiftNpc -> "Talk"
                canInteractWithTrainerNpc -> "Talk"
                canReadTownSign -> "Read"
                else -> null
            }
        } else {
            null
        }

        WorldControlsOverlay(
            team = team,
            teamStateKey = teamStateKey,
            joystickEnabled = worldControlsEnabled,
            joystickResetKey = "$inputLockKey:${screenState.activeTownSign?.title.orEmpty()}",
            actionLabel = worldActionLabel,
            onDirectionChanged = { x, y ->
                if (worldControlsEnabled) {
                    screenState.requestDirection(joystickDirection(x, y))
                }
            },
            onMenu = {
                screenState.openGameMenu()
            },
            onBag = {
                screenState.openInventory()
            },
            onAction = {
                screenState.stopMovement()
                if (canEnterTownInterior) {
                    townInteriorAtDoor?.let(onEnterTownInterior)
                } else if (canInteractWithShiftNpc) {
                    screenState.openShiftNpcDialog()
                } else if (canInteractWithTrainerNpc) {
                    screenState.openTrainerNpcDialog()
                } else {
                    screenState.openTownSign(readableTownSign)
                }
            }
        )

        if (screenState.isInventoryOpen && !screenState.isGameMenuOpen) {
            WorldInventoryPanel(
                inventoryItems = inventoryItems,
                selectedItem = screenState.selectedInventoryItem,
                money = money,
                onSelectedItemChanged = screenState::selectInventoryItem,
                onClose = screenState::closeInventory,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 14.dp)
            )
        }

        if (screenState.isInventoryOpen && !screenState.isGameMenuOpen) {
            screenState.selectedInventoryItem?.let { item ->
                val amount = inventoryItems[item]
                if (amount != null) {
                    InventoryItemDetailsPlate(
                        item = item,
                        amount = amount,
                        canUse = !item.isCaptureItem,
                        onUse = {
                            screenState.startItemTargetSelection(item)
                        },
                        onCancel = {
                            screenState.selectInventoryItem(null)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (screenState.isGameMenuOpen) {
            InGameMenuOverlay(
                showSettings = screenState.isSettingsOpen,
                pendingExitAction = screenState.pendingExitAction,
                pendingExitRequiresSave = screenState.pendingExitRequiresSave,
                showSaveMessage = screenState.showSaveMessage,
                musicEnabled = musicEnabled,
                musicVolume = musicVolume,
                soundEnabled = soundEnabled,
                soundVolume = soundVolume,
                encounterChance = encounterChance,
                onResume = screenState::resumeGame,
                onSettings = screenState::openSettings,
                onMusicEnabledChanged = onMusicEnabledChanged,
                onMusicVolumeChanged = onMusicVolumeChanged,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onSoundVolumeChanged = onSoundVolumeChanged,
                onEncounterChanceChanged = onEncounterChanceChanged,
                onBackFromSubmenu = screenState::closeSettings,
                onSaveGame = {
                    onSaveGame(screenState.playerColumn, screenState.playerRow)
                    screenState.showSaveConfirmation()
                },
                onMainMenu = {
                    screenState.requestExit(ExitAction.MainMenu, hasUnsavedChanges)
                },
                onExitGame = {
                    screenState.requestExit(ExitAction.ExitGame, hasUnsavedChanges)
                },
                onCancelExit = screenState::clearPendingExit,
                onExitWithSave = {
                    val exitAction = screenState.consumePendingExitAction()
                    onSaveGame(screenState.playerColumn, screenState.playerRow)
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                },
                onExitWithoutSave = {
                    val exitAction = screenState.consumePendingExitAction()
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                }
            )
        }

        screenState.itemTargetSelection?.let { item ->
            ItemTargetSelectionOverlay(
                item = item,
                team = team,
                teamStateKey = teamStateKey,
                onChimeraSelected = { chimera ->
                    screenState.requestItemUseConfirmation(item, chimera)
                },
                onCancel = screenState::cancelItemTargetSelection
            )
        }

        screenState.pendingItemUseConfirmation?.let { (item, chimera) ->
            ConfirmItemUseDialog(
                item = item,
                chimera = chimera,
                onConfirm = {
                    onUseInventoryItem(item, chimera)
                    screenState.completeItemUse()
                },
                onCancel = screenState::cancelItemUseConfirmation
            )
        }

        screenState.activeTownSign?.let { sign ->
            TownSignDialogOverlay(
                sign = sign,
                onClose = screenState::closeTownSign
            )
        }

        screenState.shiftNpcDialogStep?.let { step ->
            val isReturnDialog = field == WorldField.Grass
            val isShortTravelDialog = field == WorldField.Lava && shiftNpcIntroSeen

            ShiftNpcDialogOverlay(
                step = step,
                isReturnDialog = isReturnDialog,
                isShortTravelDialog = isShortTravelDialog,
                onNext = {
                    screenState.advanceShiftNpcDialog(maxStep = 3)
                },
                onStay = {
                    if (field == WorldField.Lava && !shiftNpcIntroSeen && step >= 3) {
                        onShiftNpcIntroSeen()
                    }
                    screenState.closeShiftNpcDialog()
                },
                onTravel = {
                    screenState.prepareShiftNpcTravel()
                    if (field == WorldField.Grass) {
                        onReturnToLavaField()
                    } else {
                        if (!shiftNpcIntroSeen) {
                            onShiftNpcIntroSeen()
                        }
                        onTravelToGrassField()
                    }
                }
            )
        }

        screenState.trainerNpcDialogStep?.let { step ->
            TrainerNpcChallengeOverlay(
                step = step,
                onNext = {
                    screenState.advanceTrainerNpcDialog(maxStep = 1)
                },
                onDecline = screenState::closeTrainerNpcDialog,
                onChallenge = {
                    screenState.prepareTrainerChallenge()
                    onTrainerChallenge()
                }
            )
        }
    }
}

