package com.example.chimeralis.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
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
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.math.roundToInt

private const val MapColumns = 21
private const val MapRows = 10
private const val StepDurationMs = 280
private const val InteriorStepDurationMs = 150
private const val HeldStepDelayMs = 65L
private const val JoystickDeadZone = 0.35f
private const val MovingFrameDelayMs = 160L
private const val IdleFrameDelayMs = 320L
private const val WorldReturnInputLockMs = 1400L
private const val WorldZoom = 1.28f
private const val MaxTeamSize = 6
private const val WorldInventoryColumns = 3
private const val WorldInventorySlotCount = 9
private const val LavaShiftNpcColumn = 19
private const val LavaShiftNpcRow = 5
private const val GrassShiftNpcColumn = 1
private const val GrassShiftNpcRow = 5
private const val ShiftNpcIdleFrameDelayMs = 720L
private const val InteriorColumns = 16
private const val InteriorRows = 16

enum class WorldField { Lava, Grass }

private data class TownBuilding(
    val imageRes: Int,
    val column: Int,
    val row: Int,
    val columns: Int = 4,
    val rows: Int = 4
)

private val grassTiles = setOf(
    3 to 2, 4 to 2, 5 to 2,
    3 to 3, 4 to 3, 5 to 3,
    12 to 2, 13 to 2, 14 to 2, 15 to 2, 16 to 2, 17 to 2, 18 to 2, 19 to 2,
    12 to 3, 13 to 3, 14 to 3, 15 to 3, 16 to 3, 17 to 3, 18 to 3, 19 to 3,
    7 to 5, 8 to 5, 9 to 5,
    7 to 6, 8 to 6, 9 to 6,
    11 to 7, 12 to 7, 13 to 7, 14 to 7,
    11 to 8, 12 to 8, 13 to 8, 14 to 8,
    2 to 7, 3 to 7, 18 to 7, 19 to 7,
    2 to 8, 3 to 8, 18 to 8, 19 to 8
)

private val grassTownBuildings = listOf(
    TownBuilding(imageRes = R.drawable.town_building_library, column = 1, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.pokecenter, column = 6, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.pokestore, column = 11, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_shop, column = 16, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_hotel, column = 1, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_bank, column = 6, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_cafe, column = 11, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_hair_salon, column = 16, row = 6, columns = 4, rows = 3)
)

private val grassTownBuildingTiles = grassTownBuildings
    .flatMap { building ->
        (building.column until building.column + building.columns).flatMap { column ->
            (building.row until building.row + building.rows).map { row -> column to row }
        }
    }
    .toSet()

private val grassTownPathTiles = buildSet {
    for (column in 0 until MapColumns) {
        add(column to 0)
        add(column to 4)
        add(column to 5)
        add(column to 9)
    }

    for (row in 0 until MapRows) {
        add(0 to row)
        add(5 to row)
        add(10 to row)
        add(15 to row)
        add(20 to row)
    }
}

private val pokeCenterWalkableTiles = buildSet {
    for (row in 5..14) {
        for (column in 1..14) {
            add(column to row)
        }
    }

    removeAll(
        buildSet {
            for (row in 5..11) {
                add(1 to row)
                add(2 to row)
                add(13 to row)
                add(14 to row)
            }
            for (column in 1..14) {
                if (column !in 7..8) {
                    add(column to 14)
                }
            }
        }
    )
}

private val pokeStoreWalkableTiles = buildSet {
    for (row in 4..14) {
        for (column in 1..14) {
            add(column to row)
        }
    }

    removeAll(
        buildSet {
            for (column in 1..14) {
                if (column !in 7..8) {
                    add(column to 14)
                }
            }
            for (row in 4..10) {
                add(1 to row)
                add(2 to row)
                add(13 to row)
                add(14 to row)
            }
            for (row in 6..10) {
                add(4 to row)
                add(5 to row)
                add(10 to row)
                add(11 to row)
            }
            for (column in 5..12) {
                add(column to 4)
            }
        }
    )
}

enum class Direction { Down, Up, Left, Right }
private enum class ExitAction { MainMenu, ExitGame }
enum class TownInterior { PokeCenter, PokeStore }

@Composable
fun WorldScreen(
    starter: ChimeraSpecies?,
    team: List<Chimera> = emptyList(),
    inventoryItems: Map<Item, Int> = emptyMap(),
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
    val canInteractWithShiftNpc = showShiftNpc &&
            !isShiftNpcDialogOpen &&
            abs(playerColumn - shiftNpcTile.first) + abs(playerRow - shiftNpcTile.second) == 1
    val townInteriorAtDoor = when {
        field == WorldField.Grass && playerRow == 4 && playerColumn in 7..8 -> TownInterior.PokeCenter
        field == WorldField.Grass && playerRow == 4 && playerColumn in 12..13 -> TownInterior.PokeStore
        else -> null
    }
    val canEnterTownInterior = townInteriorAtDoor != null && !isShiftNpcDialogOpen
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

    LaunchedEffect(Unit) {
        while (true) {
            if (isGameMenuOpen ||
                isInventoryOpen ||
                isWildEncounterStarting ||
                isWorldInputLocked ||
                shiftNpcDialogStep != null ||
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
        @Composable
        fun TownBuildings(drawOverPlayer: Boolean) {
            if (field != WorldField.Grass) return

            grassTownBuildings.forEach { building ->
                val shouldDrawOverPlayer = animatedRow < building.row
                if (shouldDrawOverPlayer != drawOverPlayer) return@forEach

                val painter = painterResource(id = building.imageRes)
                val buildingWidth = tileWidth * building.columns
                val fallbackHeight = tileHeight * building.rows
                val intrinsicSize = painter.intrinsicSize
                val buildingHeight = if (intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
                    buildingWidth * intrinsicSize.height / intrinsicSize.width
                } else {
                    fallbackHeight
                }
                val left = mapLeft + building.column * tileWidth
                val bottom = mapTop + (building.row + building.rows) * tileHeight
                val top = bottom - buildingHeight
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = left.roundToInt(),
                                y = top.roundToInt()
                            )
                        }
                        .size(
                            width = with(density) { buildingWidth.toDp() },
                            height = with(density) { buildingHeight.toDp() }
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = worldTransitionScale
                    scaleY = worldTransitionScale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until MapRows) {
                    for (column in 0 until MapColumns) {
                        val left = mapLeft + column * tileWidth
                        val top = mapTop + row * tileHeight
                        val tile = column to row
                        val isPath = field == WorldField.Grass && tile in grassTownPathTiles
                        val isGrass = field != WorldField.Grass &&
                                tile in grassTiles &&
                                !(field == WorldField.Grass && tile in grassTownBuildingTiles) &&
                                !isPath
                        val tileDstSize = IntSize(
                            width = (tileWidth + 1f).roundToInt(),
                            height = (tileHeight + 1f).roundToInt()
                        )

                        drawImage(
                            image = groundTexture,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(groundTexture.width, groundTexture.height),
                            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                            dstSize = tileDstSize
                        )

                        if (isPath) {
                            drawImage(
                                image = pathTexture,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(pathTexture.width, pathTexture.height),
                                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                                dstSize = tileDstSize
                            )
                        } else if (isGrass) {
                            drawImage(
                                image = grassTexture,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(grassTexture.width, grassTexture.height),
                                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                                dstSize = tileDstSize
                            )
                        }

                    }
                }
            }

            TownBuildings(drawOverPlayer = false)

            if (shouldDrawShiftNpcBeforePlayer) {
                ShiftNpcWorldSprite(
                    frameIndex = shiftNpcIdleFrame,
                    modifier = npcModifier
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

            TownBuildings(drawOverPlayer = true)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .width(92.dp)
                .height(34.dp)
        ) {
            SmallWorldMenuButton(
                text = "Menu",
                onClick = {
                    if (isWorldInputLocked || isShiftNpcDialogOpen) return@SmallWorldMenuButton

                    requestedDirection = null
                    isMoving = false
                    isSettingsOpen = false
                    isInventoryOpen = false
                    isGameMenuOpen = true
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .width(76.dp)
                .height(34.dp)
        ) {
            SmallWorldMenuButton(
                text = "Bag",
                onClick = {
                    if (isWorldInputLocked || isShiftNpcDialogOpen) return@SmallWorldMenuButton

                    requestedDirection = null
                    isMoving = false
                    isSettingsOpen = false
                    isGameMenuOpen = false
                    selectedInventoryItem = null
                    isInventoryOpen = true
                }
            )
        }

        TeamSlots(
            team = team,
            stateKey = teamStateKey,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 39.dp, bottom = 20.dp)
        )

        Joystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 93.dp, bottom = 43.dp),
            onDirectionChanged = { x, y ->
                if (!isGameMenuOpen &&
                    !isInventoryOpen &&
                    !isWildEncounterStarting &&
                    !isWorldInputLocked &&
                    !isShiftNpcDialogOpen &&
                    itemTargetSelection == null &&
                    pendingItemUseConfirmation == null
                ) {
                    requestedDirection = joystickDirection(x, y)
                }
            }
        )

        if ((canInteractWithShiftNpc || canEnterTownInterior) && !isGameMenuOpen && !isInventoryOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .width(128.dp)
                    .height(38.dp)
            ) {
                SmallWorldMenuButton(
                    text = if (canEnterTownInterior) "Enter" else "Talk",
                    onClick = {
                        requestedDirection = null
                        isMoving = false
                        if (canEnterTownInterior) {
                            townInteriorAtDoor?.let(onEnterTownInterior)
                        } else {
                            shiftNpcDialogStep = 0
                        }
                    }
                )
            }
        }

        if (isInventoryOpen && !isGameMenuOpen) {
            WorldInventoryPanel(
                inventoryItems = inventoryItems,
                selectedItem = selectedInventoryItem,
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
    }
}

@Composable
fun TownInteriorScreen(
    interior: TownInterior,
    initialPlayerColumn: Int = 7,
    initialPlayerRow: Int = 14,
    initialPlayerDirection: Direction = Direction.Up,
    onExit: () -> Unit
) {
    var playerColumn by remember(interior) { mutableIntStateOf(initialPlayerColumn) }
    var playerRow by remember(interior) { mutableIntStateOf(initialPlayerRow) }
    var targetColumn by remember(interior) { mutableIntStateOf(initialPlayerColumn) }
    var targetRow by remember(interior) { mutableIntStateOf(initialPlayerRow) }
    var direction by remember(interior) { mutableStateOf(initialPlayerDirection) }
    var requestedDirection by remember(interior) { mutableStateOf<Direction?>(null) }
    var isMoving by remember(interior) { mutableStateOf(false) }
    var animationFrame by remember(interior) { mutableIntStateOf(0) }

    val backgroundRes = when (interior) {
        TownInterior.PokeCenter -> R.drawable.pokecenter_interior
        TownInterior.PokeStore -> R.drawable.pokestore_interior
    }
    val walkableTiles = when (interior) {
        TownInterior.PokeCenter -> pokeCenterWalkableTiles
        TownInterior.PokeStore -> pokeStoreWalkableTiles
    }
    val canExit = playerRow == 14 && playerColumn in 7..8 && !isMoving

    LaunchedEffect(isMoving) {
        while (true) {
            animationFrame++
            delay(if (isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val nextDirection = requestedDirection
            if (nextDirection == null) {
                delay(16L)
                continue
            }

            val nextTile = nextInteriorTile(playerColumn, playerRow, nextDirection)
            direction = nextDirection

            if (nextTile !in walkableTiles || nextTile == playerColumn to playerRow) {
                delay(HeldStepDelayMs)
                continue
            }

            targetColumn = nextTile.first
            targetRow = nextTile.second
            isMoving = true
            delay(InteriorStepDurationMs.toLong())
            playerColumn = nextTile.first
            playerRow = nextTile.second
            isMoving = false
            delay(1L)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val imageSizePx = minOf(widthPx, heightPx)
        val imageLeft = (widthPx - imageSizePx) / 2f
        val imageTop = (heightPx - imageSizePx) / 2f
        val tileSize = imageSizePx / InteriorColumns

        val animatedColumn by animateFloatAsState(
            targetValue = if (isMoving) targetColumn.toFloat() else playerColumn.toFloat(),
            animationSpec = tween(durationMillis = InteriorStepDurationMs, easing = LinearEasing),
            label = "interiorPlayerColumn"
        )
        val animatedRow by animateFloatAsState(
            targetValue = if (isMoving) targetRow.toFloat() else playerRow.toFloat(),
            animationSpec = tween(durationMillis = InteriorStepDurationMs, easing = LinearEasing),
            label = "interiorPlayerRow"
        )

        val playerCenterX = imageLeft + (animatedColumn + 0.5f) * tileSize
        val playerCenterY = imageTop + (animatedRow + 0.5f) * tileSize
        val worldLikeTileSize = minOf(widthPx / MapColumns * WorldZoom, heightPx / MapRows * WorldZoom)
        val spriteWidth = worldLikeTileSize * 1.06f
        val spriteHeight = worldLikeTileSize * 1.62f

        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = when (interior) {
                TownInterior.PokeCenter -> "Poke Center"
                TownInterior.PokeStore -> "Poke Store"
            },
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(with(density) { imageSizePx.toDp() })
        )

        Image(
            painter = painterResource(id = playerFrame(direction, isMoving, animationFrame)),
            contentDescription = "Player",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (playerCenterX - spriteWidth / 2f).roundToInt(),
                        y = (playerCenterY - spriteHeight * 0.82f).roundToInt()
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

        Joystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 30.dp, bottom = 30.dp),
            onDirectionChanged = { x, y ->
                requestedDirection = joystickDirection(x, y)
            }
        )

        if (canExit) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .width(128.dp)
                    .height(38.dp)
            ) {
                SmallWorldMenuButton(
                    text = "Exit",
                    onClick = {
                        requestedDirection = null
                        isMoving = false
                        onExit()
                    }
                )
            }
        }
    }
}

@Composable
private fun TeamSlots(
    team: List<Chimera>,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    stateKey: Int = 0,
    targetItem: Item? = null,
    onChimeraSelected: (Chimera) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(MaxTeamSize) { index ->
            val chimera = team.getOrNull(index)
            TeamSlot(
                chimera = chimera,
                isActive = index == 0 && team.isNotEmpty(),
                selectionMode = selectionMode,
                stateKey = stateKey,
                isSelectable = chimera?.let { targetItem?.canUseOn(it) ?: true } ?: false,
                onChimeraSelected = onChimeraSelected
            )
        }
    }
}

@Composable
private fun TeamSlot(
    chimera: Chimera?,
    isActive: Boolean,
    selectionMode: Boolean,
    stateKey: Int,
    isSelectable: Boolean,
    onChimeraSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val hpRatio = chimera?.let {
        (it.stats.currentHp.toFloat() / it.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    } ?: 0f
    val frameAlpha = if (chimera == null) 0.22f else 0.58f
    val contentAlpha = when {
        chimera == null -> 0.18f
        selectionMode && !isSelectable -> 0.28f
        chimera.stats.isAlive() -> 1f
        else -> 0.45f
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surface.copy(alpha = frameAlpha))
            .border(
                width = if (isActive || selectionMode) 2.dp else 1.dp,
                color = colors.primary.copy(
                    alpha = when {
                        selectionMode && chimera != null && isSelectable -> 0.92f
                        selectionMode && chimera != null -> 0.24f
                        isActive -> 0.8f
                        else -> 0.38f
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(chimera, selectionMode, isSelectable, stateKey) {
                detectTapGestures(
                    onTap = {
                        if (selectionMode && chimera != null && isSelectable) {
                            onChimeraSelected(chimera)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (chimera != null) {
            Image(
                painter = painterResource(id = chimera.species.teamImageRes()),
                contentDescription = chimera.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-2).dp, y = -2.dp)
                    .size(38.dp)
                    .graphicsLayer { alpha = contentAlpha }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 5.dp, vertical = 3.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF2B190E).copy(alpha = 0.9f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpRatio)
                        .height(3.dp)
                        .background(
                            when {
                                hpRatio > 0.5f -> Color(0xFF66C96A)
                                hpRatio > 0.2f -> Color(0xFFE0B84B)
                                else -> Color(0xFFD85A4A)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun ShiftNpcWorldSprite(
    frameIndex: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = shiftNpcIdleFrame(frameIndex)),
        contentDescription = "Shift NPC",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun SmallWorldMenuButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface.copy(alpha = 0.42f))
            .border(1.dp, colors.primary.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        GameSoundPlayer.play(context, R.raw.button_click)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = colors.primary.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily
            )
        )
    }
}

@Composable
private fun ItemTargetSelectionOverlay(
    item: Item,
    team: List<Chimera>,
    teamStateKey: Int,
    onChimeraSelected: (Chimera) -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.78f))
                .border(1.dp, colors.primary.copy(alpha = 0.46f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.name,
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Choose a chimera",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily
            )

            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(28.dp)
            ) {
                SmallWorldMenuButton(text = "Cancel", onClick = onCancel)
            }
        }

        TeamSlots(
            team = team,
            selectionMode = true,
            stateKey = teamStateKey,
            targetItem = item,
            onChimeraSelected = onChimeraSelected,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 39.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun ConfirmItemUseDialog(
    item: Item,
    chimera: Chimera,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canUseItem = item.canUseOn(chimera)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.9f))
                .border(1.dp, colors.primary.copy(alpha = 0.54f), RoundedCornerShape(8.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Use ${item.name}?",
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = if (canUseItem) "Use on ${chimera.name}?"
                else "${item.name} cannot be used on ${chimera.name}.",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily
            )

            MenuButton(text = "Use", enabled = canUseItem, onClick = onConfirm)
            MenuButton(text = "Cancel", onClick = onCancel)
        }
    }
}

@Composable
private fun ShiftNpcDialogOverlay(
    step: Int,
    isReturnDialog: Boolean,
    isShortTravelDialog: Boolean,
    onNext: () -> Unit,
    onStay: () -> Unit,
    onTravel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var portraitFrame by remember(step) { mutableIntStateOf(0) }

    LaunchedEffect(step) {
        while (true) {
            portraitFrame++
            delay(520L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
    ) {
        Image(
            painter = painterResource(
                id = shiftNpcDialogFrame(
                    step = if (isReturnDialog || isShortTravelDialog) 2 else step,
                    frameIndex = portraitFrame
                )
            ),
            contentDescription = "Shift NPC dialog",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 130.dp)
                .height(620.dp)
                .graphicsLayer(
                    scaleX = 1.6f,
                    scaleY = 1.6f,
                    transformOrigin = TransformOrigin(0.2f, 0.2f)
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(152.dp)
                .background(colors.surface.copy(alpha = 0.78f))
                .border(1.dp, colors.primary.copy(alpha = 0.42f))
                .padding(horizontal = 90.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isReturnDialog -> "Ready to go back?"
                    isShortTravelDialog -> "Ready to head to trainer town?"
                    else -> shiftNpcDialogText(step)
                },
                color = colors.primary,
                fontSize = if (step >= 2 || isReturnDialog || isShortTravelDialog) 15.sp else 18.sp,
                lineHeight = if (step >= 2 || isReturnDialog || isShortTravelDialog) 21.sp else 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier.weight(1f)
            )

            if (step < 3 && !isReturnDialog && !isShortTravelDialog) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(42.dp)
                ) {
                    MenuButton(text = "Next", onClick = onNext)
                }
            } else {
                Column(
                    modifier = Modifier.width(174.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isReturnDialog) "Return?" else "Go now?",
                        color = colors.onSurface.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MenuButton(text = "Yes", onClick = onTravel)
                    MenuButton(text = "No", onClick = onStay)
                }
            }
        }
    }
}

@Composable
private fun InGameMenuOverlay(
    showSettings: Boolean,
    pendingExitAction: ExitAction?,
    pendingExitRequiresSave: Boolean,
    showSaveMessage: Boolean,
    musicEnabled: Boolean,
    musicVolume: Float,
    soundEnabled: Boolean,
    soundVolume: Float,
    encounterChance: Float,
    onResume: () -> Unit,
    onSettings: () -> Unit,
    onMusicEnabledChanged: (Boolean) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onSoundVolumeChanged: (Float) -> Unit,
    onEncounterChanceChanged: (Float) -> Unit,
    onBackFromSubmenu: () -> Unit,
    onSaveGame: () -> Unit,
    onMainMenu: () -> Unit,
    onExitGame: () -> Unit,
    onCancelExit: () -> Unit,
    onExitWithSave: () -> Unit,
    onExitWithoutSave: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = when {
                    pendingExitAction != null && pendingExitRequiresSave -> "Save Progress?"
                    pendingExitAction != null -> "Are You Sure?"
                    showSettings -> "Settings"
                    else -> "Game Menu"
                },
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            if (pendingExitAction != null && pendingExitRequiresSave) {
                Text(
                    text = "Do you want to save before leaving?",
                    color = colors.onSurface.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontFamily = CinzelFamily
                )
                MenuButton(text = "Save", onClick = onExitWithSave)
                MenuButton(text = "Don't Save", onClick = onExitWithoutSave)
                MenuButton(text = "Cancel", onClick = onCancelExit)
            } else if (pendingExitAction != null) {
                Text(
                    text = when (pendingExitAction) {
                        ExitAction.MainMenu -> "Return to the main menu?"
                        ExitAction.ExitGame -> "Exit the game?"
                    },
                    color = colors.onSurface.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontFamily = CinzelFamily
                )
                MenuButton(text = "Yes", onClick = onExitWithoutSave)
                MenuButton(text = "Cancel", onClick = onCancelExit)
            } else if (showSettings) {
                GameSettingsPanel(
                    musicEnabled = musicEnabled,
                    musicVolume = musicVolume,
                    soundEnabled = soundEnabled,
                    soundVolume = soundVolume,
                    encounterChance = encounterChance,
                    onMusicEnabledChanged = onMusicEnabledChanged,
                    onMusicVolumeChanged = onMusicVolumeChanged,
                    onSoundEnabledChanged = onSoundEnabledChanged,
                    onSoundVolumeChanged = onSoundVolumeChanged,
                    onEncounterChanceChanged = onEncounterChanceChanged
                )
                MenuButton(text = "Back", onClick = onBackFromSubmenu)
            } else {
                MenuButton(text = "Resume", onClick = onResume)
                MenuButton(text = "Save", onClick = onSaveGame)
                MenuButton(text = "Settings", onClick = onSettings)
                MenuButton(text = "Main Menu", onClick = onMainMenu)
                MenuButton(text = "Exit Game", onClick = onExitGame)
            }
        }
        if (showSaveMessage) {
            SaveMessagePlate(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
            )
        }
    }
}

@Composable
private fun WorldInventoryPanel(
    inventoryItems: Map<Item, Int>,
    selectedItem: Item?,
    onSelectedItemChanged: (Item?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val filledSlots = inventoryItems.entries
        .sortedBy { it.key.name }
        .map { it.key to it.value }
    val slots = buildList {
        addAll(filledSlots)
        repeat((WorldInventorySlotCount - filledSlots.size).coerceAtLeast(0)) {
            add(null)
        }
    }.take(WorldInventorySlotCount)

    LaunchedEffect(inventoryItems, selectedItem) {
        val item = selectedItem
        if (item != null && item !in inventoryItems.keys) {
            onSelectedItemChanged(null)
        }
    }

    Box(
        modifier = modifier
            .width(198.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.58f))
            .border(1.dp, colors.primary.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CinzelFamily
                )

                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(24.dp)
                ) {
                    SmallWorldMenuButton(text = "X", onClick = onClose)
                }
            }

            slots.chunked(WorldInventoryColumns).forEach { rowSlots ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    rowSlots.forEach { slot ->
                        WorldInventorySlot(
                            slot = slot,
                            isSelected = slot?.first == selectedItem,
                            onSelected = { item ->
                                onSelectedItemChanged(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemDetailsPlate(
    item: Item,
    amount: Int,
    onUse: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.72f))
            .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${item.name} x$amount",
            color = colors.primary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = item.description(),
            color = colors.onSurface.copy(alpha = 0.82f),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuButton(
                text = "Use",
                onClick = onUse
            )
            MenuButton(
                text = "Cancel",
                onClick = onCancel
            )
        }
    }
}

@Composable
private fun WorldInventorySlot(
    slot: Pair<Item, Int>?,
    isSelected: Boolean,
    onSelected: (Item) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val item = slot?.first
    val amount = slot?.second

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.background.copy(alpha = if (item == null) 0.22f else 0.48f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = colors.primary.copy(
                    alpha = when {
                        isSelected -> 0.86f
                        item == null -> 0.22f
                        else -> 0.48f
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(item) {
                detectTapGestures(
                    onTap = {
                        if (item != null) {
                            onSelected(item)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (item != null && amount != null) {
            ItemIcon(item = item)

            Text(
                text = "x$amount",
                color = colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun ItemIcon(item: Item) {
    val iconColor = when (item.name) {
        "Potion" -> Color(0xFFE05A6F)
        "Super Potion" -> Color(0xFF5CCBEA)
        "Revive" -> Color(0xFFE0B84B)
        else -> Color(0xFFB689FF)
    }

    Canvas(modifier = Modifier.size(34.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)

        when (item.name) {
            "Revive" -> {
                val path = Path().apply {
                    moveTo(center.x, size.height * 0.1f)
                    lineTo(size.width * 0.84f, center.y)
                    lineTo(center.x, size.height * 0.9f)
                    lineTo(size.width * 0.16f, center.y)
                    close()
                }
                drawPath(path = path, color = iconColor)
                drawPath(path = path, color = Color.White.copy(alpha = 0.42f), style = Stroke(width = 2f))
            }
            else -> {
                drawCircle(
                    color = iconColor.copy(alpha = 0.95f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(center.x, size.height * 0.58f)
                )
                drawRect(
                    color = iconColor.copy(alpha = 0.9f),
                    topLeft = Offset(size.width * 0.38f, size.height * 0.18f),
                    size = Size(size.width * 0.24f, size.height * 0.34f)
                )
                drawRect(
                    color = Color.White.copy(alpha = 0.75f),
                    topLeft = Offset(size.width * 0.34f, size.height * 0.11f),
                    size = Size(size.width * 0.32f, size.height * 0.1f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = size.minDimension * 0.1f,
                    center = Offset(size.width * 0.42f, size.height * 0.52f)
                )
            }
        }
    }
}

private fun Item.description(): String {
    return when (name) {
        "Potion" -> "Restores 20 HP to one chimera."
        "Super Potion" -> "Restores 60 HP to one chimera."
        "Revive" -> "Revives a fainted chimera."
        else -> "Can be used on a chimera."
    }
}

@Composable
private fun SaveMessagePlate(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            colors.secondary.copy(alpha = 0.6f),
            colors.primary.copy(alpha = 0.9f),
            colors.secondary.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier
            .width(184.dp)
            .height(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cut = size.height * 0.25f
            val path = Path().apply {
                moveTo(cut, 0f)
                lineTo(size.width - cut, 0f)
                lineTo(size.width, cut)
                lineTo(size.width, size.height - cut)
                lineTo(size.width - cut, size.height)
                lineTo(cut, size.height)
                lineTo(0f, size.height - cut)
                lineTo(0f, cut)
                close()
            }

            drawPath(
                path = path,
                color = colors.background.copy(alpha = 0.75f)
            )
            drawPath(
                path = path,
                brush = gradientBrush,
                style = Stroke(width = 2f)
            )
        }

        Text(
            text = "Progress Saved",
            color = colors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = CinzelFamily
        )
    }
}

@Composable
private fun Joystick(
    modifier: Modifier = Modifier,
    onDirectionChanged: (Float, Float) -> Unit
) {
    var knobX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var knobY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val radiusPx = with(density) { 42.dp.toPx() }

    Box(
        modifier = modifier
            .size(112.dp)
            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        knobX = 0f
                        knobY = 0f
                    },
                    onDragEnd = {
                        knobX = 0f
                        knobY = 0f
                        onDirectionChanged(0f, 0f)
                    },
                    onDragCancel = {
                        knobX = 0f
                        knobY = 0f
                        onDirectionChanged(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val nextX = knobX + dragAmount.x
                        val nextY = knobY + dragAmount.y
                        val distance = hypot(nextX, nextY)
                        val scale = if (distance > radiusPx) radiusPx / distance else 1f

                        knobX = nextX * scale
                        knobY = nextY * scale
                        onDirectionChanged(knobX / radiusPx, knobY / radiusPx)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                .size(42.dp)
                .background(Color(0xFFFFD700).copy(alpha = 0.9f), CircleShape)
                .border(2.dp, Color(0xFF3D1500), CircleShape)
        )
    }
}

private fun joystickDirection(x: Float, y: Float): Direction? {
    if (abs(x) < JoystickDeadZone && abs(y) < JoystickDeadZone) return null
    return if (abs(x) > abs(y)) {
        if (x < 0f) Direction.Left else Direction.Right
    } else {
        if (y < 0f) Direction.Up else Direction.Down
    }
}

private fun nextTile(column: Int, row: Int, direction: Direction): Pair<Int, Int> = when (direction) {
    Direction.Down -> column to (row + 1).coerceAtMost(MapRows - 1)
    Direction.Up -> column to (row - 1).coerceAtLeast(0)
    Direction.Left -> (column - 1).coerceAtLeast(0) to row
    Direction.Right -> (column + 1).coerceAtMost(MapColumns - 1) to row
}

private fun nextInteriorTile(column: Int, row: Int, direction: Direction): Pair<Int, Int> = when (direction) {
    Direction.Down -> column to (row + 1).coerceAtMost(InteriorRows - 1)
    Direction.Up -> column to (row - 1).coerceAtLeast(0)
    Direction.Left -> (column - 1).coerceAtLeast(0) to row
    Direction.Right -> (column + 1).coerceAtMost(InteriorColumns - 1) to row
}

private fun playerFrame(direction: Direction, isMoving: Boolean, frameIndex: Int): Int {
    val frames = when {
        isMoving && direction == Direction.Down -> frontRunFrames
        isMoving && direction == Direction.Up -> backRunFrames
        isMoving && (direction == Direction.Left || direction == Direction.Right) -> sideRunFrames
        !isMoving && direction == Direction.Down -> frontIdleFrames
        !isMoving && direction == Direction.Up -> backIdleFrames
        else -> sideIdleFrames
    }

    return frames[frameIndex % frames.size]
}

private fun shiftNpcIdleFrame(frameIndex: Int): Int {
    return shiftNpcIdleFrames[frameIndex % shiftNpcIdleFrames.size]
}

private fun shiftNpcDialogFrame(step: Int, frameIndex: Int): Int {
    val frames = when (step) {
        0 -> shiftNpcSeriousDialogFrames
        1 -> shiftNpcSurprisedDialogFrames
        else -> shiftNpcCalmDialogFrames
    }

    return frames[frameIndex % frames.size]
}

private fun shiftNpcDialogText(step: Int): String {
    return when (step) {
        0 -> "Hey, who are you and what do you want?"
        1 -> "Wait, you are a trainer too?"
        2 -> "Sorry, I did not expect to see any new faces here. You are just starting your journey, right?"
        else -> "In that case, let me show you our trainer town. You can heal your chimeras there and stock up on new gear."
    }
}

private val frontIdleFrames = listOf(
    R.drawable.player_front_idle_1,
    R.drawable.player_front_idle_2,
    R.drawable.player_front_idle_3,
    R.drawable.player_front_idle_4
)
private val backIdleFrames = listOf(
    R.drawable.player_back_idle_1,
    R.drawable.player_back_idle_2,
    R.drawable.player_back_idle_3,
    R.drawable.player_back_idle_4
)
private val sideIdleFrames = listOf(
    R.drawable.player_side_idle_1,
    R.drawable.player_side_idle_2,
    R.drawable.player_side_idle_3
)
private val frontRunFrames = listOf(
    R.drawable.player_front_run_1,
    R.drawable.player_front_run_2,
    R.drawable.player_front_run_3,
    R.drawable.player_front_run_4,
    R.drawable.player_front_run_5,
    R.drawable.player_front_run_6,
    R.drawable.player_front_run_7,
    R.drawable.player_front_run_8
)
private val backRunFrames = listOf(
    R.drawable.player_back_run_1,
    R.drawable.player_back_run_2,
    R.drawable.player_back_run_3,
    R.drawable.player_back_run_4,
    R.drawable.player_back_run_5,
    R.drawable.player_back_run_6,
    R.drawable.player_back_run_7,
    R.drawable.player_back_run_8
)
private val sideRunFrames = listOf(
    R.drawable.player_side_run_1,
    R.drawable.player_side_run_2,
    R.drawable.player_side_run_3,
    R.drawable.player_side_run_4,
    R.drawable.player_side_run_5,
    R.drawable.player_side_run_6,
    R.drawable.player_side_run_7,
    R.drawable.player_side_run_8
)

private val shiftNpcIdleFrames = listOf(
    R.drawable.shift_npc_1,
    R.drawable.shift_npc_2,
    R.drawable.shift_npc_3
)

private val shiftNpcSeriousDialogFrames = listOf(
    R.drawable.dialog_shift_npc_serious_1,
    R.drawable.dialog_shift_npc_serious_2
)

private val shiftNpcSurprisedDialogFrames = listOf(
    R.drawable.dialog_shift_npc_surprised_1,
    R.drawable.dialog_shift_npc_surprised_2
)

private val shiftNpcCalmDialogFrames = listOf(
    R.drawable.dialog_shift_npc_calm_1,
    R.drawable.dialog_shift_npc_calm_2
)

private fun randomWildChimera(starter: ChimeraSpecies?): ChimeraSpecies {
    val pool = listOf(
        ChimeraSpecies.Sunflare,
        ChimeraSpecies.Sylvhorn,
        ChimeraSpecies.Aquantis
    )

    return pool.random()
}

private fun ChimeraSpecies.teamImageRes(): Int = when (this) {
    ChimeraSpecies.Sunflare,
    ChimeraSpecies.Solflare,
    ChimeraSpecies.Solignis -> R.drawable.starter_fire
    ChimeraSpecies.Sylvhorn -> R.drawable.starter_grass
    ChimeraSpecies.Aquantis -> R.drawable.starter_water
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBushTile(
    left: Float,
    top: Float,
    tileSize: Float
) {
    val dark = Color(0xFF1F5E28)
    val light = Color(0xFF55A84B)

    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.28f, top + tileSize * 0.55f)
    )
    drawCircle(
        color = light,
        radius = tileSize * 0.25f,
        center = Offset(left + tileSize * 0.5f, top + tileSize * 0.42f)
    )
    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.72f, top + tileSize * 0.55f)
    )
}
