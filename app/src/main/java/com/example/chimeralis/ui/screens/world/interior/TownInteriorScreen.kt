package com.example.chimeralis.ui.screens.world.interior

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.ui.overlays.WorldControlsOverlay
import com.example.chimeralis.ui.screens.world.ChimeraStorageOverlay
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
import kotlinx.coroutines.delay
import kotlin.math.abs

/** Renders the town interior screen UI. */
@Composable
fun TownInteriorScreen(
    interior: TownInterior,
    team: List<Chimera> = emptyList(),
    storage: List<Chimera> = emptyList(),
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
    onSwapTeamMembers: (Int, Int) -> Unit = { _, _ -> },
    onDepositTeamMember: (Int) -> Unit = {},
    onWithdrawStoredChimera: (Int) -> Unit = {},
    onSwapTeamWithStorage: (Int, Int) -> Unit = { _, _ -> },
    onSaveGame: (Int, Int) -> Unit = { _, _ -> },
    onBackToMainMenu: () -> Unit = {},
    onExitGame: () -> Unit = {},
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val movementState = rememberTownInteriorMovementState(
        interior = interior,
        initialPlayerColumn = initialPlayerColumn,
        initialPlayerRow = initialPlayerRow,
        initialPlayerDirection = initialPlayerDirection
    )
    val interactionState = rememberTownInteriorInteractionState(interior)
    val interiorData = interior.data
    val walkableTiles = interiorData.walkableTiles
    val canExit = movementState.playerRow == 14 &&
            movementState.playerColumn in 7..8 &&
            !movementState.isMoving
    val npcColumn = interiorData.npcColumn
    val npcRow = interiorData.npcRow
    val isServiceUiOpen = interactionState.isServiceUiOpen
    val isInteriorUiOpen = interactionState.isInteriorUiOpen
    val canTalkToServiceNpc = !movementState.isMoving &&
            !isInteriorUiOpen &&
            (abs(movementState.playerColumn - npcColumn) + abs(movementState.playerRow - npcRow)) <= 3
    val canOpenStorage = !movementState.isMoving &&
            !isInteriorUiOpen &&
            interiorData.storageColumn != null &&
            interiorData.storageRow != null &&
            (abs(movementState.playerColumn - interiorData.storageColumn) +
                    abs(movementState.playerRow - interiorData.storageRow)) <= 1

    LaunchedEffect(movementState.isMoving) {
        while (true) {
            movementState.advanceAnimationFrame()
            delay(if (movementState.isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(interior) {
        while (true) {
            movementState.advanceServiceNpcIdleFrame()
            delay(ServiceNpcIdleFrameDelayMs)
        }
    }

    LaunchedEffect(interactionState.showSaveMessage) {
        if (interactionState.showSaveMessage) {
            delay(1600L)
            interactionState.hideSaveConfirmation()
        }
    }

    LaunchedEffect(inputLockKey) {
        if (inputLockKey == 0) return@LaunchedEffect

        movementState.stopMovement()
        interactionState.beginInputLock()
        delay(WorldReturnInputLockMs)
        interactionState.endInputLock()
    }

    LaunchedEffect(interactionState.isHealingInProgress) {
        if (!interactionState.isHealingInProgress) return@LaunchedEffect

        GameSoundPlayer.play(context, R.raw.healing_chimeras)
        delay(3400L)
        onHealTeam()
        interactionState.finishHealing("All your chimeras are healthy again.")
    }

    LaunchedEffect(interior) {
        while (true) {
            val nextDirection = movementState.requestedDirection
            if (nextDirection == null || interactionState.isInteriorUiOpen) {
                delay(16L)
                continue
            }

            val currentColumn = movementState.playerColumn
            val currentRow = movementState.playerRow
            val nextTile = nextInteriorTile(currentColumn, currentRow, nextDirection)
            movementState.face(nextDirection)
            onPlayerDirectionChanged(nextDirection)

            if (nextTile !in walkableTiles ||
                nextTile == npcColumn to npcRow ||
                nextTile == currentColumn to currentRow
            ) {
                delay(HeldStepDelayMs)
                continue
            }

            movementState.beginStepTo(nextTile.first, nextTile.second)
            delay(InteriorStepDurationMs.toLong())
            movementState.finishStepAt(nextTile.first, nextTile.second)
            onPlayerPositionChanged(movementState.playerColumn, movementState.playerRow)
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
            targetValue = if (movementState.isMoving) {
                movementState.targetColumn.toFloat()
            } else {
                movementState.playerColumn.toFloat()
            },
            animationSpec = tween(durationMillis = InteriorStepDurationMs, easing = LinearEasing),
            label = "interiorPlayerColumn"
        )
        val animatedRow by animateFloatAsState(
            targetValue = if (movementState.isMoving) {
                movementState.targetRow.toFloat()
            } else {
                movementState.playerRow.toFloat()
            },
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
            direction = movementState.direction,
            isMoving = movementState.isMoving,
            animationFrame = movementState.animationFrame,
            serviceNpcIdleFrame = movementState.serviceNpcIdleFrame
        )

        val interiorActionLabel = when {
            canOpenStorage -> "Storage"
            canTalkToServiceNpc -> "Talk"
            canExit && !isServiceUiOpen -> "Exit"
            else -> null
        }

        WorldControlsOverlay(
            team = team,
            teamStateKey = teamStateKey,
            joystickEnabled = !isInteriorUiOpen,
            joystickResetKey = "$inputLockKey:${interactionState.interiorJoystickResetKey}",
            actionLabel = interiorActionLabel,
            onDirectionChanged = { x, y ->
                if (!isInteriorUiOpen) {
                    movementState.requestDirection(joystickDirection(x, y))
                }
            },
            onMenu = {
                if (interactionState.openGameMenu()) {
                    movementState.stopMovement()
                }
            },
            onBag = {
                if (interactionState.openInventory()) {
                    movementState.stopMovement()
                }
            },
            onAction = {
                movementState.requestDirection(null)
                if (canOpenStorage) {
                    interactionState.openStorage()
                } else if (canTalkToServiceNpc) {
                    interactionState.openServiceDialog()
                } else if (canExit && !isServiceUiOpen) {
                    movementState.stopMovement()
                    interactionState.beginInputLock()
                    onExit()
                }
            }
        )

        if (interactionState.isInventoryOpen && !interactionState.isGameMenuOpen) {
            WorldInventoryPanel(
                inventoryItems = inventoryItems,
                selectedItem = interactionState.selectedInventoryItem,
                money = money,
                onSelectedItemChanged = interactionState::selectInventoryItem,
                onClose = interactionState::closeInventory,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 14.dp)
            )
        }

        if (interactionState.isInventoryOpen && !interactionState.isGameMenuOpen) {
            interactionState.selectedInventoryItem?.let { item ->
                val amount = inventoryItems[item]
                if (amount != null) {
                    InventoryItemDetailsPlate(
                        item = item,
                        amount = amount,
                        canUse = !item.isCaptureItem,
                        onUse = {
                            movementState.stopMovement()
                            interactionState.startItemTargetSelection(item)
                        },
                        onCancel = {
                            interactionState.selectInventoryItem(null)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (interactionState.isGameMenuOpen) {
            InGameMenuOverlay(
                showSettings = interactionState.isSettingsOpen,
                pendingExitAction = interactionState.pendingExitAction,
                pendingExitRequiresSave = interactionState.pendingExitRequiresSave,
                showSaveMessage = interactionState.showSaveMessage,
                musicEnabled = musicEnabled,
                musicVolume = musicVolume,
                soundEnabled = soundEnabled,
                soundVolume = soundVolume,
                encounterChance = encounterChance,
                onResume = interactionState::resumeGame,
                onSettings = interactionState::openSettings,
                onMusicEnabledChanged = onMusicEnabledChanged,
                onMusicVolumeChanged = onMusicVolumeChanged,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onSoundVolumeChanged = onSoundVolumeChanged,
                onEncounterChanceChanged = onEncounterChanceChanged,
                onBackFromSubmenu = interactionState::closeSettings,
                onSaveGame = {
                    onSaveGame(movementState.playerColumn, movementState.playerRow)
                    interactionState.showSaveConfirmation()
                },
                onMainMenu = {
                    interactionState.requestExit(ExitAction.MainMenu, hasUnsavedChanges)
                },
                onExitGame = {
                    interactionState.requestExit(ExitAction.ExitGame, hasUnsavedChanges)
                },
                onCancelExit = interactionState::clearPendingExit,
                onExitWithSave = {
                    val exitAction = interactionState.consumePendingExitAction()
                    onSaveGame(movementState.playerColumn, movementState.playerRow)
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                },
                onExitWithoutSave = {
                    val exitAction = interactionState.consumePendingExitAction()
                    when (exitAction) {
                        ExitAction.MainMenu -> onBackToMainMenu()
                        ExitAction.ExitGame -> onExitGame()
                        null -> Unit
                    }
                }
            )
        }

        interactionState.itemTargetSelection?.let { item ->
            ItemTargetSelectionOverlay(
                item = item,
                team = team,
                teamStateKey = teamStateKey,
                onChimeraSelected = { chimera ->
                    interactionState.requestItemUseConfirmation(item, chimera)
                },
                onCancel = interactionState::cancelItemTargetSelection
            )
        }

        interactionState.pendingItemUseConfirmation?.let { (item, chimera) ->
            ConfirmItemUseDialog(
                item = item,
                chimera = chimera,
                onConfirm = {
                    onUseInventoryItem(item, chimera)
                    interactionState.completeItemUse()
                },
                onCancel = interactionState::cancelItemUseConfirmation
            )
        }

        interactionState.dialogStep?.let { step ->
            ServiceNpcDialogOverlay(
                interior = interior,
                step = step,
                message = interactionState.serviceMessage,
                onNext = interactionState::advanceServiceDialog,
                onHeal = interactionState::startHealing,
                onOpenShop = interactionState::openShop,
                onClose = interactionState::closeServiceDialog
            )
        }

        if (interactionState.isShopOpen) {
            ShopOverlay(
                money = money,
                inventoryItems = inventoryItems,
                message = interactionState.serviceMessage,
                onBuyItem = { itemName, amount ->
                    val bought = onBuyItem(itemName, amount)
                    val message = if (bought) {
                        "Bought ${itemName.displayName} x$amount."
                    } else {
                        "Not enough coins."
                    }
                    interactionState.showServiceMessage(message)
                },
                onClose = interactionState::closeShop
            )
        }

        if (interactionState.isStorageOpen) {
            ChimeraStorageOverlay(
                team = team,
                storage = storage,
                teamStateKey = teamStateKey,
                onSwapTeamMembers = onSwapTeamMembers,
                onDepositTeamMember = onDepositTeamMember,
                onWithdrawStoredChimera = onWithdrawStoredChimera,
                onSwapTeamWithStorage = onSwapTeamWithStorage,
                onClose = interactionState::closeStorage
            )
        }

        if (interactionState.isHealingInProgress) {
            HealingOverlay()
        }
    }
}

