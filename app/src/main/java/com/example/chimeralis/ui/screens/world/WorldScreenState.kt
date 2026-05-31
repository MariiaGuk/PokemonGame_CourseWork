package com.example.chimeralis.ui.screens.world

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.screens.world.locations.TownSign

/** Controls transient UI state for the main world screen. */
internal class WorldScreenState(
    initialPlayerColumn: Int,
    initialPlayerRow: Int,
    initialPlayerDirection: Direction
) {
    var playerColumn by mutableIntStateOf(initialPlayerColumn)
        private set
    var playerRow by mutableIntStateOf(initialPlayerRow)
        private set
    var targetColumn by mutableIntStateOf(initialPlayerColumn)
        private set
    var targetRow by mutableIntStateOf(initialPlayerRow)
        private set
    var direction by mutableStateOf(initialPlayerDirection)
        private set
    var requestedDirection by mutableStateOf<Direction?>(null)
        private set
    var isMoving by mutableStateOf(false)
        private set
    var animationFrame by mutableIntStateOf(0)
        private set
    var isGameMenuOpen by mutableStateOf(false)
        private set
    var isSettingsOpen by mutableStateOf(false)
        private set
    var isInventoryOpen by mutableStateOf(false)
        private set
    var selectedInventoryItem by mutableStateOf<Item?>(null)
        private set
    var pendingExitAction by mutableStateOf<ExitAction?>(null)
        private set
    var pendingExitRequiresSave by mutableStateOf(false)
        private set
    var showSaveMessage by mutableStateOf(false)
        private set
    var isWildEncounterStarting by mutableStateOf(false)
        private set
    var isWorldInputLocked by mutableStateOf(false)
        private set
    var itemTargetSelection by mutableStateOf<Item?>(null)
        private set
    var pendingItemUseConfirmation by mutableStateOf<Pair<Item, Chimera>?>(null)
        private set
    var shiftNpcIdleFrame by mutableIntStateOf(0)
        private set
    var shiftNpcDialogStep by mutableStateOf<Int?>(null)
        private set
    var trainerNpcIdleFrame by mutableIntStateOf(0)
        private set
    var trainerNpcDialogStep by mutableStateOf<Int?>(null)
        private set
    var activeTownSign by mutableStateOf<TownSign?>(null)
        private set

    val isShiftNpcDialogOpen: Boolean
        get() = shiftNpcDialogStep != null

    val isTrainerNpcDialogOpen: Boolean
        get() = trainerNpcDialogStep != null

    val areWorldControlsEnabled: Boolean
        get() = !isGameMenuOpen &&
                !isInventoryOpen &&
                !isWildEncounterStarting &&
                !isWorldInputLocked &&
                !isShiftNpcDialogOpen &&
                !isTrainerNpcDialogOpen &&
                activeTownSign == null &&
                itemTargetSelection == null &&
                pendingItemUseConfirmation == null

    /** Updates the direction requested by the virtual joystick. */
    fun requestDirection(direction: Direction?) {
        requestedDirection = direction
    }

    /** Stops active movement and clears held movement input. */
    fun stopMovement() {
        requestedDirection = null
        isMoving = false
    }

    /** Changes the direction the player sprite is facing. */
    fun face(direction: Direction) {
        this.direction = direction
    }

    /** Starts moving the player sprite toward the target map tile. */
    fun beginStepTo(column: Int, row: Int) {
        targetColumn = column
        targetRow = row
        isMoving = true
    }

    /** Commits the player position after a step animation finishes. */
    fun finishStepAt(column: Int, row: Int) {
        playerColumn = column
        playerRow = row
        isMoving = false
    }

    /** Advances the player sprite animation frame. */
    fun advanceAnimationFrame() {
        animationFrame++
    }

    /** Advances the shift NPC idle animation frame. */
    fun advanceShiftNpcIdleFrame() {
        shiftNpcIdleFrame++
    }

    /** Advances the trainer NPC idle animation frame. */
    fun advanceTrainerNpcIdleFrame() {
        trainerNpcIdleFrame++
    }

    /** Locks world input while the screen returns from another flow. */
    fun beginWorldInputLock() {
        stopMovement()
        isWorldInputLocked = true
    }

    /** Unlocks world input after a temporary return lock. */
    fun endWorldInputLock() {
        isWorldInputLocked = false
    }

    /** Marks the world as transitioning into a wild encounter. */
    fun startWildEncounter() {
        stopMovement()
        isWildEncounterStarting = true
    }

    /** Opens the in-game menu when no blocking overlay is active. */
    fun openGameMenu() {
        if (!canOpenQuickOverlay()) return

        stopMovement()
        isSettingsOpen = false
        isInventoryOpen = false
        isGameMenuOpen = true
    }

    /** Opens the inventory panel when no blocking overlay is active. */
    fun openInventory() {
        if (!canOpenQuickOverlay()) return

        stopMovement()
        isSettingsOpen = false
        isGameMenuOpen = false
        selectedInventoryItem = null
        isInventoryOpen = true
    }

    /** Closes the inventory panel and clears its selected item. */
    fun closeInventory() {
        selectedInventoryItem = null
        isInventoryOpen = false
    }

    /** Selects an item in the inventory panel. */
    fun selectInventoryItem(item: Item?) {
        selectedInventoryItem = item
    }

    /** Starts selecting a team target for an inventory item. */
    fun startItemTargetSelection(item: Item) {
        stopMovement()
        selectedInventoryItem = null
        isInventoryOpen = false
        itemTargetSelection = item
        pendingItemUseConfirmation = null
    }

    /** Opens confirmation for using an item on a selected chimera. */
    fun requestItemUseConfirmation(item: Item, chimera: Chimera) {
        pendingItemUseConfirmation = item to chimera
    }

    /** Cancels item target selection and any pending confirmation. */
    fun cancelItemTargetSelection() {
        itemTargetSelection = null
        pendingItemUseConfirmation = null
    }

    /** Clears item usage state after the selected item is applied. */
    fun completeItemUse() {
        itemTargetSelection = null
        pendingItemUseConfirmation = null
    }

    /** Closes only the item use confirmation dialog. */
    fun cancelItemUseConfirmation() {
        pendingItemUseConfirmation = null
    }

    /** Returns from the in-game menu to the world. */
    fun resumeGame() {
        isSettingsOpen = false
        isInventoryOpen = false
        clearPendingExit()
        isGameMenuOpen = false
    }

    /** Opens the settings submenu inside the in-game menu. */
    fun openSettings() {
        isSettingsOpen = true
        isInventoryOpen = false
    }

    /** Closes the settings submenu inside the in-game menu. */
    fun closeSettings() {
        isSettingsOpen = false
    }

    /** Stores the selected exit action before asking about saving. */
    fun requestExit(action: ExitAction, requiresSave: Boolean) {
        pendingExitAction = action
        pendingExitRequiresSave = requiresSave
    }

    /** Clears the pending exit action. */
    fun clearPendingExit() {
        pendingExitAction = null
        pendingExitRequiresSave = false
    }

    /** Returns and clears the pending exit action. */
    fun consumePendingExitAction(): ExitAction? {
        val action = pendingExitAction
        clearPendingExit()
        return action
    }

    /** Shows the temporary save confirmation message. */
    fun showSaveConfirmation() {
        showSaveMessage = true
    }

    /** Hides the temporary save confirmation message. */
    fun hideSaveConfirmation() {
        showSaveMessage = false
    }

    /** Opens a readable town sign dialog. */
    fun openTownSign(sign: TownSign?) {
        activeTownSign = sign
    }

    /** Closes the active town sign dialog. */
    fun closeTownSign() {
        activeTownSign = null
    }

    /** Starts the shift NPC dialog. */
    fun openShiftNpcDialog() {
        shiftNpcDialogStep = 0
    }

    /** Moves the shift NPC dialog to its next step. */
    fun advanceShiftNpcDialog(maxStep: Int) {
        shiftNpcDialogStep = ((shiftNpcDialogStep ?: 0) + 1).coerceAtMost(maxStep)
    }

    /** Closes the shift NPC dialog. */
    fun closeShiftNpcDialog() {
        shiftNpcDialogStep = null
    }

    /** Clears dialog and movement state before a shift NPC transition. */
    fun prepareShiftNpcTravel() {
        closeShiftNpcDialog()
        stopMovement()
    }

    /** Starts the trainer NPC challenge dialog. */
    fun openTrainerNpcDialog() {
        trainerNpcDialogStep = 0
    }

    /** Moves the trainer NPC dialog to its next step. */
    fun advanceTrainerNpcDialog(maxStep: Int) {
        trainerNpcDialogStep = ((trainerNpcDialogStep ?: 0) + 1).coerceAtMost(maxStep)
    }

    /** Closes the trainer NPC challenge dialog. */
    fun closeTrainerNpcDialog() {
        trainerNpcDialogStep = null
    }

    /** Clears dialog and movement state before starting a trainer battle. */
    fun prepareTrainerChallenge() {
        closeTrainerNpcDialog()
        stopMovement()
    }

    /** Checks whether the menu or inventory can be opened over the world. */
    private fun canOpenQuickOverlay(): Boolean {
        return areWorldControlsEnabled
    }
}

/** Remembers the state controller for the main world screen. */
@Composable
internal fun rememberWorldScreenState(
    initialPlayerColumn: Int,
    initialPlayerRow: Int,
    initialPlayerDirection: Direction
): WorldScreenState {
    return remember {
        WorldScreenState(
            initialPlayerColumn = initialPlayerColumn,
            initialPlayerRow = initialPlayerRow,
            initialPlayerDirection = initialPlayerDirection
        )
    }
}
