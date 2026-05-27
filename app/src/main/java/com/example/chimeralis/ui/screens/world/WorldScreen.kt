package com.example.chimeralis.ui.screens.world

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.overlays.WorldControlsOverlay
import com.example.chimeralis.ui.screens.world.locations.TownLocationBuildings
import com.example.chimeralis.ui.screens.world.locations.TownLocationSigns
import com.example.chimeralis.ui.screens.world.locations.TownLocationTiles
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.TownSign
import com.example.chimeralis.ui.screens.world.locations.WildFieldLocationTiles
import com.example.chimeralis.ui.screens.world.locations.grassTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownBuildingTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownPathTiles
import com.example.chimeralis.ui.screens.world.locations.grassTownSigns
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.math.roundToInt

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
    val colors = MaterialTheme.colorScheme
    var playerColumn by remember { mutableIntStateOf(initialPlayerColumn) }
    var playerRow by remember { mutableIntStateOf(initialPlayerRow) }
    var targetColumn by remember { mutableIntStateOf(initialPlayerColumn) }
    var targetRow by remember { mutableIntStateOf(initialPlayerRow) }
    var direction by remember { mutableStateOf(initialPlayerDirection) }
    var requestedDirection by remember { mutableStateOf<Direction?>(null) }
    var isMoving by remember { mutableStateOf(false) }
    var animationFrame by remember { mutableIntStateOf(0) }
    var isGameMenuOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isInventoryOpen by remember { mutableStateOf(false) }
    var selectedInventoryItem by remember { mutableStateOf<Item?>(null) }
    var pendingExitAction by remember { mutableStateOf<ExitAction?>(null) }
    var pendingExitRequiresSave by remember { mutableStateOf(false) }
    var showSaveMessage by remember { mutableStateOf(false) }
    var isWildEncounterStarting by remember { mutableStateOf(false) }
    var isWorldInputLocked by remember { mutableStateOf(false) }
    var itemTargetSelection by remember { mutableStateOf<Item?>(null) }
    var pendingItemUseConfirmation by remember { mutableStateOf<Pair<Item, Chimera>?>(null) }
    var shiftNpcIdleFrame by remember { mutableIntStateOf(0) }
    var shiftNpcDialogStep by remember { mutableStateOf<Int?>(null) }
    var trainerNpcIdleFrame by remember { mutableIntStateOf(0) }
    var trainerNpcDialogStep by remember { mutableStateOf<Int?>(null) }
    var activeTownSign by remember { mutableStateOf<TownSign?>(null) }
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
    val isShiftNpcDialogOpen = shiftNpcDialogStep != null
    val isTrainerNpcDialogOpen = trainerNpcDialogStep != null
    val trainerNpcTile = GrassTrainerNpcColumn to GrassTrainerNpcRow
    val canInteractWithShiftNpc = showShiftNpc &&
            !isShiftNpcDialogOpen &&
            !isTrainerNpcDialogOpen &&
            abs(playerColumn - shiftNpcTile.first) + abs(playerRow - shiftNpcTile.second) == 1
    val canInteractWithTrainerNpc = field == WorldField.Grass &&
            !isShiftNpcDialogOpen &&
            !isTrainerNpcDialogOpen &&
            abs(playerColumn - trainerNpcTile.first) + abs(playerRow - trainerNpcTile.second) == 1
    val townInteriorAtDoor = when {
        field == WorldField.Grass && playerRow == 4 && playerColumn in 7..8 -> TownInterior.ChimeraCenter
        field == WorldField.Grass && playerRow == 4 && playerColumn in 12..13 -> TownInterior.ChimeraStore
        else -> null
    }
    val canEnterTownInterior = townInteriorAtDoor != null && !isShiftNpcDialogOpen && !isTrainerNpcDialogOpen
    val readableTownSign = if (field == WorldField.Grass && !isShiftNpcDialogOpen && !isTrainerNpcDialogOpen) {
        grassTownSigns.firstOrNull { sign ->
            abs(playerColumn - sign.column) + abs(playerRow - sign.row) <= 1
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
        targetValue = targetColumn.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerColumn"
    )
    val animatedRow by animateFloatAsState(
        targetValue = targetRow.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerRow"
    )

    LaunchedEffect(isMoving, direction) {
        while (true) {
            animationFrame++
            delay(if (isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(showSaveMessage) {
        if (showSaveMessage) {
            delay(1600L)
            showSaveMessage = false
        }
    }

    LaunchedEffect(inputLockKey) {
        if (inputLockKey == 0) return@LaunchedEffect

        requestedDirection = null
        isMoving = false
        isWorldInputLocked = true
        delay(WorldReturnInputLockMs)
        isWorldInputLocked = false
    }

    LaunchedEffect(showShiftNpc) {
        if (!showShiftNpc) return@LaunchedEffect

        while (true) {
            shiftNpcIdleFrame++
            delay(ShiftNpcIdleFrameDelayMs)
        }
    }

    LaunchedEffect(field) {
        if (field != WorldField.Grass) return@LaunchedEffect

        while (true) {
            trainerNpcIdleFrame++
            delay(ShiftNpcIdleFrameDelayMs)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (isGameMenuOpen ||
                isInventoryOpen ||
                isWildEncounterStarting ||
                isWorldInputLocked ||
                shiftNpcDialogStep != null ||
                trainerNpcDialogStep != null ||
                activeTownSign != null ||
                itemTargetSelection != null ||
                pendingItemUseConfirmation != null
            ) {
                requestedDirection = null
                isMoving = false
                delay(16L)
                continue
            }

            val nextDirection = requestedDirection
            if (nextDirection == null) {
                delay(16L)
                continue
            }

            val currentColumn = playerColumn
            val currentRow = playerRow
            val nextTile = nextTile(currentColumn, currentRow, nextDirection)
            direction = nextDirection
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

            targetColumn = nextTile.first
            targetRow = nextTile.second
            isMoving = true
            delay(StepDurationMs.toLong())

            playerColumn = nextTile.first
            playerRow = nextTile.second
            isMoving = false
            onPlayerPositionChanged(playerColumn, playerRow)

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
                requestedDirection = null
                isMoving = false
                isWildEncounterStarting = true
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
                    frameIndex = shiftNpcIdleFrame,
                    modifier = npcModifier
                )
            }

            if (shouldDrawTrainerNpcBeforePlayer) {
                TrainerNpcWorldSprite(
                    frameIndex = trainerNpcIdleFrame,
                    modifier = trainerNpcModifier
                )
            }

            Image(
                painter = painterResource(id = playerFrame(direction, isMoving, animationFrame)),
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
                        scaleX = if (direction == Direction.Left) -1f else 1f
                    }
            )

            if (showShiftNpc && !shouldDrawShiftNpcBeforePlayer) {
                ShiftNpcWorldSprite(
                    frameIndex = shiftNpcIdleFrame,
                    modifier = npcModifier
                )
            }

            if (field == WorldField.Grass && !shouldDrawTrainerNpcBeforePlayer) {
                TrainerNpcWorldSprite(
                    frameIndex = trainerNpcIdleFrame,
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

        val worldControlsEnabled = !isGameMenuOpen &&
                !isInventoryOpen &&
                !isWildEncounterStarting &&
                !isWorldInputLocked &&
                !isShiftNpcDialogOpen &&
                !isTrainerNpcDialogOpen &&
                activeTownSign == null &&
                itemTargetSelection == null &&
                pendingItemUseConfirmation == null
        val worldActionLabel = if (!isGameMenuOpen && !isInventoryOpen && activeTownSign == null) {
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
            joystickResetKey = "$inputLockKey:${activeTownSign?.title.orEmpty()}",
            actionLabel = worldActionLabel,
            onDirectionChanged = { x, y ->
                if (worldControlsEnabled) {
                    requestedDirection = joystickDirection(x, y)
                }
            },
            onMenu = {
                if (isWorldInputLocked || isShiftNpcDialogOpen || isTrainerNpcDialogOpen || activeTownSign != null) return@WorldControlsOverlay

                requestedDirection = null
                isMoving = false
                isSettingsOpen = false
                isInventoryOpen = false
                isGameMenuOpen = true
            },
            onBag = {
                if (isWorldInputLocked || isShiftNpcDialogOpen || isTrainerNpcDialogOpen || activeTownSign != null) return@WorldControlsOverlay

                requestedDirection = null
                isMoving = false
                isSettingsOpen = false
                isGameMenuOpen = false
                selectedInventoryItem = null
                isInventoryOpen = true
            },
            onAction = {
                requestedDirection = null
                isMoving = false
                if (canEnterTownInterior) {
                    townInteriorAtDoor?.let(onEnterTownInterior)
                } else if (canInteractWithShiftNpc) {
                    shiftNpcDialogStep = 0
                } else if (canInteractWithTrainerNpc) {
                    trainerNpcDialogStep = 0
                } else {
                    activeTownSign = readableTownSign
                }
            }
        )

        if (isInventoryOpen && !isGameMenuOpen) {
            WorldInventoryPanel(
                inventoryItems = inventoryItems,
                selectedItem = selectedInventoryItem,
                money = money,
                onSelectedItemChanged = { item ->
                    selectedInventoryItem = item
                },
                onClose = {
                    selectedInventoryItem = null
                    isInventoryOpen = false
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 14.dp)
            )
        }

        if (isInventoryOpen && !isGameMenuOpen) {
            selectedInventoryItem?.let { item ->
                val amount = inventoryItems[item]
                if (amount != null) {
                    InventoryItemDetailsPlate(
                        item = item,
                        amount = amount,
                        canUse = !item.isCaptureItem,
                        onUse = {
                            requestedDirection = null
                            isMoving = false
                            selectedInventoryItem = null
                            isInventoryOpen = false
                            itemTargetSelection = item
                            pendingItemUseConfirmation = null
                        },
                        onCancel = {
                            selectedInventoryItem = null
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (isGameMenuOpen) {
            InGameMenuOverlay(
                showSettings = isSettingsOpen,
                pendingExitAction = pendingExitAction,
                pendingExitRequiresSave = pendingExitRequiresSave,
                showSaveMessage = showSaveMessage,
                musicEnabled = musicEnabled,
                musicVolume = musicVolume,
                soundEnabled = soundEnabled,
                soundVolume = soundVolume,
                encounterChance = encounterChance,
                onResume = {
                    isSettingsOpen = false
                    isInventoryOpen = false
                    pendingExitAction = null
                    pendingExitRequiresSave = false
                    isGameMenuOpen = false
                },
                onSettings = {
                    isSettingsOpen = true
                    isInventoryOpen = false
                },
                onMusicEnabledChanged = onMusicEnabledChanged,
                onMusicVolumeChanged = onMusicVolumeChanged,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onSoundVolumeChanged = onSoundVolumeChanged,
                onEncounterChanceChanged = onEncounterChanceChanged,
                onBackFromSubmenu = {
                    isSettingsOpen = false
                },
                onSaveGame = {
                    onSaveGame(playerColumn, playerRow)
                    showSaveMessage = true
                },
                onMainMenu = {
                    pendingExitAction = ExitAction.MainMenu
                    pendingExitRequiresSave = hasUnsavedChanges
                },
                onExitGame = {
                    pendingExitAction = ExitAction.ExitGame
                    pendingExitRequiresSave = hasUnsavedChanges
                },
                onCancelExit = {
                    pendingExitAction = null
                    pendingExitRequiresSave = false
                },
                onExitWithSave = {
                    val exitAction = pendingExitAction
                    pendingExitAction = null
                    pendingExitRequiresSave = false
                    onSaveGame(playerColumn, playerRow)
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                },
                onExitWithoutSave = {
                    val exitAction = pendingExitAction
                    pendingExitAction = null
                    pendingExitRequiresSave = false
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                }
            )
        }

        itemTargetSelection?.let { item ->
            ItemTargetSelectionOverlay(
                item = item,
                team = team,
                teamStateKey = teamStateKey,
                onChimeraSelected = { chimera ->
                    pendingItemUseConfirmation = item to chimera
                },
                onCancel = {
                    itemTargetSelection = null
                    pendingItemUseConfirmation = null
                }
            )
        }

        pendingItemUseConfirmation?.let { (item, chimera) ->
            ConfirmItemUseDialog(
                item = item,
                chimera = chimera,
                onConfirm = {
                    onUseInventoryItem(item, chimera)
                    itemTargetSelection = null
                    pendingItemUseConfirmation = null
                },
                onCancel = {
                    pendingItemUseConfirmation = null
                }
            )
        }

        activeTownSign?.let { sign ->
            TownSignDialogOverlay(
                sign = sign,
                onClose = {
                    activeTownSign = null
                }
            )
        }

        shiftNpcDialogStep?.let { step ->
            val isReturnDialog = field == WorldField.Grass
            val isShortTravelDialog = field == WorldField.Lava && shiftNpcIntroSeen

            ShiftNpcDialogOverlay(
                step = step,
                isReturnDialog = isReturnDialog,
                isShortTravelDialog = isShortTravelDialog,
                onNext = {
                    val nextStep = ((shiftNpcDialogStep ?: step) + 1).coerceAtMost(3)
                    shiftNpcDialogStep = nextStep
                },
                onStay = {
                    if (field == WorldField.Lava && !shiftNpcIntroSeen && step >= 3) {
                        onShiftNpcIntroSeen()
                    }
                    shiftNpcDialogStep = null
                },
                onTravel = {
                    shiftNpcDialogStep = null
                    requestedDirection = null
                    isMoving = false
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

        trainerNpcDialogStep?.let { step ->
            TrainerNpcChallengeOverlay(
                step = step,
                onNext = {
                    trainerNpcDialogStep = ((trainerNpcDialogStep ?: step) + 1).coerceAtMost(1)
                },
                onDecline = {
                    trainerNpcDialogStep = null
                },
                onChallenge = {
                    trainerNpcDialogStep = null
                    requestedDirection = null
                    isMoving = false
                    onTrainerChallenge()
                }
            )
        }
    }
}

