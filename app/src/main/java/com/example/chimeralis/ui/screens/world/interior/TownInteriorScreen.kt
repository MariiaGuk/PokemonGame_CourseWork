package com.example.chimeralis.ui.screens.world.interior

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
import com.example.chimeralis.ui.screens.world.ConfirmItemUseDialog
import com.example.chimeralis.ui.screens.world.Direction
import com.example.chimeralis.ui.screens.world.ExitAction
import com.example.chimeralis.ui.screens.world.HealingOverlay
import com.example.chimeralis.ui.screens.world.HeldStepDelayMs
import com.example.chimeralis.ui.screens.world.IdleFrameDelayMs
import com.example.chimeralis.ui.screens.world.InGameMenuOverlay
import com.example.chimeralis.ui.screens.world.InteriorColumns
import com.example.chimeralis.ui.screens.world.InteriorStepDurationMs
import com.example.chimeralis.ui.screens.world.InventoryItemDetailsPlate
import com.example.chimeralis.ui.screens.world.ItemTargetSelectionOverlay
import com.example.chimeralis.ui.screens.world.MovingFrameDelayMs
import com.example.chimeralis.ui.screens.world.ServiceNpcDialogOverlay
import com.example.chimeralis.ui.screens.world.ServiceNpcIdleFrameDelayMs
import com.example.chimeralis.ui.screens.world.ShopOverlay
import com.example.chimeralis.ui.screens.world.WorldInventoryPanel
import com.example.chimeralis.ui.screens.world.WorldReturnInputLockMs
import com.example.chimeralis.ui.screens.world.joystickDirection
import com.example.chimeralis.ui.screens.world.nextInteriorTile
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.data
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.math.roundToInt

@Composable
fun TownInteriorScreen(
    interior: TownInterior,
    team: List<Chimera> = emptyList(),
    inventoryItems: Map<Item, Int> = emptyMap(),
    teamStateKey: Int = 0,
    money: Int = 0,
    musicEnabled: Boolean = true,
    musicVolume: Float = 1f,
    soundEnabled: Boolean = true,
    soundVolume: Float = 1f,
    encounterChance: Float = 0.22f,
    hasUnsavedChanges: Boolean = false,
    onMusicEnabledChanged: (Boolean) -> Unit = {},
    onMusicVolumeChanged: (Float) -> Unit = {},
    onSoundEnabledChanged: (Boolean) -> Unit = {},
    onSoundVolumeChanged: (Float) -> Unit = {},
    onEncounterChanceChanged: (Float) -> Unit = {},
    initialPlayerColumn: Int = 7,
    initialPlayerRow: Int = 14,
    initialPlayerDirection: Direction = Direction.Up,
    inputLockKey: Int = 0,
    onHealTeam: () -> Unit = {},
    onBuyItem: (ItemName, Int) -> Boolean = { _, _ -> false },
    onUseInventoryItem: (Item, Chimera) -> Unit = { _, _ -> },
    onPlayerPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onPlayerDirectionChanged: (Direction) -> Unit = {},
    onSaveGame: (Int, Int) -> Unit = { _, _ -> },
    onBackToMainMenu: () -> Unit = {},
    onExitGame: () -> Unit = {},
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var playerColumn by remember(interior) { mutableIntStateOf(initialPlayerColumn) }
    var playerRow by remember(interior) { mutableIntStateOf(initialPlayerRow) }
    var targetColumn by remember(interior) { mutableIntStateOf(initialPlayerColumn) }
    var targetRow by remember(interior) { mutableIntStateOf(initialPlayerRow) }
    var direction by remember(interior) { mutableStateOf(initialPlayerDirection) }
    var requestedDirection by remember(interior) { mutableStateOf<Direction?>(null) }
    var isMoving by remember(interior) { mutableStateOf(false) }
    var animationFrame by remember(interior) { mutableIntStateOf(0) }
    var serviceNpcIdleFrame by remember(interior) { mutableIntStateOf(0) }
    var dialogStep by remember(interior) { mutableStateOf<Int?>(null) }
    var isShopOpen by remember(interior) { mutableStateOf(false) }
    var serviceMessage by remember(interior) { mutableStateOf<String?>(null) }
    var isGameMenuOpen by remember(interior) { mutableStateOf(false) }
    var isSettingsOpen by remember(interior) { mutableStateOf(false) }
    var isInventoryOpen by remember(interior) { mutableStateOf(false) }
    var selectedInventoryItem by remember(interior) { mutableStateOf<Item?>(null) }
    var itemTargetSelection by remember(interior) { mutableStateOf<Item?>(null) }
    var pendingItemUseConfirmation by remember(interior) { mutableStateOf<Pair<Item, Chimera>?>(null) }
    var pendingExitAction by remember(interior) { mutableStateOf<ExitAction?>(null) }
    var pendingExitRequiresSave by remember(interior) { mutableStateOf(false) }
    var showSaveMessage by remember(interior) { mutableStateOf(false) }
    var isHealingInProgress by remember(interior) { mutableStateOf(false) }
    var isInteriorInputLocked by remember(interior) { mutableStateOf(false) }
    var interiorJoystickResetKey by remember(interior) { mutableIntStateOf(0) }
    val interiorData = interior.data
    val walkableTiles = interiorData.walkableTiles
    val canExit = playerRow == 14 && playerColumn in 7..8 && !isMoving
    val npcColumn = interiorData.npcColumn
    val npcRow = interiorData.npcRow
    val isServiceUiOpen = dialogStep != null || isShopOpen
    val isInteriorUiOpen = isServiceUiOpen ||
            isHealingInProgress ||
            isInteriorInputLocked ||
            isGameMenuOpen ||
            isInventoryOpen ||
            itemTargetSelection != null ||
            pendingItemUseConfirmation != null
    val canTalkToServiceNpc = !isMoving &&
            !isInteriorUiOpen &&
            (abs(playerColumn - npcColumn) + abs(playerRow - npcRow)) <= 3

    LaunchedEffect(isMoving) {
        while (true) {
            animationFrame++
            delay(if (isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(interior) {
        while (true) {
            serviceNpcIdleFrame++
            delay(ServiceNpcIdleFrameDelayMs)
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
        isInteriorInputLocked = true
        interiorJoystickResetKey++
        delay(WorldReturnInputLockMs)
        isInteriorInputLocked = false
    }

    LaunchedEffect(isHealingInProgress) {
        if (!isHealingInProgress) return@LaunchedEffect

        GameSoundPlayer.play(context, R.raw.healing_chimeras)
        delay(3400L)
        onHealTeam()
        serviceMessage = "All your chimeras are healthy again."
        dialogStep = 2
        isHealingInProgress = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            val nextDirection = requestedDirection
            if (nextDirection == null || isInteriorUiOpen) {
                delay(16L)
                continue
            }

            val nextTile = nextInteriorTile(playerColumn, playerRow, nextDirection)
            direction = nextDirection
            onPlayerDirectionChanged(nextDirection)

            if (nextTile !in walkableTiles ||
                nextTile == npcColumn to npcRow ||
                nextTile == playerColumn to playerRow
            ) {
                delay(HeldStepDelayMs)
                continue
            }

            targetColumn = nextTile.first
            targetRow = nextTile.second
            isMoving = true
            delay(InteriorStepDurationMs.toLong())
            playerColumn = nextTile.first
            playerRow = nextTile.second
            onPlayerPositionChanged(playerColumn, playerRow)
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

        TownInteriorScene(
            interior = interior,
            interiorData = interiorData,
            widthPx = widthPx,
            heightPx = heightPx,
            imageSizePx = imageSizePx,
            imageLeft = imageLeft,
            imageTop = imageTop,
            tileSize = tileSize,
            animatedColumn = animatedColumn,
            animatedRow = animatedRow,
            direction = direction,
            isMoving = isMoving,
            animationFrame = animationFrame,
            serviceNpcIdleFrame = serviceNpcIdleFrame
        )

        val interiorActionLabel = when {
            canTalkToServiceNpc -> "Talk"
            canExit && !isServiceUiOpen -> "Exit"
            else -> null
        }

        WorldControlsOverlay(
            team = team,
            teamStateKey = teamStateKey,
            joystickEnabled = !isInteriorUiOpen,
            joystickResetKey = "$inputLockKey:$interiorJoystickResetKey",
            actionLabel = interiorActionLabel,
            onDirectionChanged = { x, y ->
                if (!isInteriorUiOpen) {
                    requestedDirection = joystickDirection(x, y)
                }
            },
            onMenu = {
                if (isServiceUiOpen) return@WorldControlsOverlay

                requestedDirection = null
                isMoving = false
                isSettingsOpen = false
                isInventoryOpen = false
                isGameMenuOpen = true
            },
            onBag = {
                if (isServiceUiOpen) return@WorldControlsOverlay

                requestedDirection = null
                isMoving = false
                isSettingsOpen = false
                isGameMenuOpen = false
                selectedInventoryItem = null
                isInventoryOpen = true
            },
            onAction = {
                requestedDirection = null
                if (canTalkToServiceNpc) {
                    serviceMessage = null
                    dialogStep = 0
                } else if (canExit && !isServiceUiOpen) {
                    isMoving = false
                    isInteriorInputLocked = true
                    interiorJoystickResetKey++
                    onExit()
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

        dialogStep?.let { step ->
            ServiceNpcDialogOverlay(
                interior = interior,
                step = step,
                message = serviceMessage,
                onNext = {
                    serviceMessage = null
                    dialogStep = (dialogStep ?: 0) + 1
                },
                onHeal = {
                    dialogStep = null
                    serviceMessage = null
                    isHealingInProgress = true
                },
                onOpenShop = {
                    dialogStep = null
                    isShopOpen = true
                    serviceMessage = null
                },
                onClose = {
                    dialogStep = null
                    serviceMessage = null
                }
            )
        }

        if (isShopOpen) {
            ShopOverlay(
                money = money,
                inventoryItems = inventoryItems,
                message = serviceMessage,
                onBuyItem = { itemName, amount ->
                    val bought = onBuyItem(itemName, amount)
                    serviceMessage = if (bought) {
                        "Bought ${itemName.displayName} x$amount."
                    } else {
                        "Not enough coins."
                    }
                },
                onClose = {
                    isShopOpen = false
                    serviceMessage = null
                }
            )
        }

        if (isHealingInProgress) {
            HealingOverlay()
        }
    }
}

