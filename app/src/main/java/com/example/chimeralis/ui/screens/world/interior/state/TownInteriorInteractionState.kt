package com.example.chimeralis.ui.screens.world.interior.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.screens.world.model.ExitAction
import com.example.chimeralis.ui.screens.world.locations.TownInterior

/** Controls transient interaction state for a town interior screen. */
internal class TownInteriorInteractionState {
    var dialogStep by mutableStateOf<Int?>(null)
        private set
    var isShopOpen by mutableStateOf(false)
        private set
    var isStorageOpen by mutableStateOf(false)
        private set
    var serviceMessage by mutableStateOf<String?>(null)
        private set
    var isGameMenuOpen by mutableStateOf(false)
        private set
    var isSettingsOpen by mutableStateOf(false)
        private set
    var isInventoryOpen by mutableStateOf(false)
        private set
    var selectedInventoryItem by mutableStateOf<Item?>(null)
        private set
    var itemTargetSelection by mutableStateOf<Item?>(null)
        private set
    var pendingItemUseConfirmation by mutableStateOf<Pair<Item, Chimera>?>(null)
        private set
    var pendingExitAction by mutableStateOf<ExitAction?>(null)
        private set
    var pendingExitRequiresSave by mutableStateOf(false)
        private set
    var showSaveMessage by mutableStateOf(false)
        private set
    var isHealingInProgress by mutableStateOf(false)
        private set
    var isInteriorInputLocked by mutableStateOf(false)
        private set
    var interiorJoystickResetKey by mutableIntStateOf(0)
        private set

    val isServiceUiOpen: Boolean
        get() = dialogStep != null || isShopOpen

    val isInteriorUiOpen: Boolean
        get() = isServiceUiOpen ||
                isStorageOpen ||
                isHealingInProgress ||
                isInteriorInputLocked ||
                isGameMenuOpen ||
                isInventoryOpen ||
                itemTargetSelection != null ||
                pendingItemUseConfirmation != null

    /**
     * Locks interior input and resets the joystick.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun beginInputLock() {
        isInteriorInputLocked = true
        interiorJoystickResetKey++
    }

    /**
     * Unlocks interior input after a temporary lock.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun endInputLock() {
        isInteriorInputLocked = false
    }

    /**
     * Opens the in-game menu when the service UI is not active.
     *
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
    fun openGameMenu(): Boolean {
        if (isServiceUiOpen) return false

        isSettingsOpen = false
        isInventoryOpen = false
        isGameMenuOpen = true
        return true
    }

    /**
     * Opens the inventory panel when the service UI is not active.
     *
     * @return True when the operation succeeds or the condition is satisfied; otherwise false.
     */
    fun openInventory(): Boolean {
        if (isServiceUiOpen) return false

        isSettingsOpen = false
        isGameMenuOpen = false
        selectedInventoryItem = null
        isInventoryOpen = true
        return true
    }

    /**
     * Opens the storage overlay and clears conflicting panels.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun openStorage() {
        isStorageOpen = true
        isGameMenuOpen = false
        isInventoryOpen = false
        selectedInventoryItem = null
    }

    /**
     * Closes the storage overlay.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun closeStorage() {
        isStorageOpen = false
    }

    /**
     * Closes the inventory panel and clears the selected item.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun closeInventory() {
        selectedInventoryItem = null
        isInventoryOpen = false
    }

    /**
     * Selects an item inside the inventory panel.
     *
     * @param item Domain object used by this operation: item.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun selectInventoryItem(item: Item?) {
        selectedInventoryItem = item
    }

    /**
     * Starts selecting a team target for an inventory item.
     *
     * @param item Domain object used by this operation: item.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun startItemTargetSelection(item: Item) {
        selectedInventoryItem = null
        isInventoryOpen = false
        itemTargetSelection = item
        pendingItemUseConfirmation = null
    }

    /**
     * Opens confirmation for using an item on a selected chimera.
     *
     * @param item Domain object used by this operation: item.
     * @param chimera Domain object used by this operation: chimera.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun requestItemUseConfirmation(item: Item, chimera: Chimera) {
        pendingItemUseConfirmation = item to chimera
    }

    /**
     * Cancels item target selection and any pending confirmation.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun cancelItemTargetSelection() {
        itemTargetSelection = null
        pendingItemUseConfirmation = null
    }

    /**
     * Clears item usage state after the selected item is applied.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun completeItemUse() {
        itemTargetSelection = null
        pendingItemUseConfirmation = null
    }

    /**
     * Closes only the item use confirmation dialog.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun cancelItemUseConfirmation() {
        pendingItemUseConfirmation = null
    }

    /**
     * Returns from the in-game menu to the interior.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun resumeGame() {
        isSettingsOpen = false
        isInventoryOpen = false
        clearPendingExit()
        isGameMenuOpen = false
    }

    /**
     * Opens the settings submenu inside the in-game menu.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun openSettings() {
        isSettingsOpen = true
        isInventoryOpen = false
    }

    /**
     * Closes the settings submenu inside the in-game menu.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun closeSettings() {
        isSettingsOpen = false
    }

    /**
     * Stores the selected exit action before asking about saving.
     *
     * @param action The action value used by this operation.
     * @param requiresSave The requires save value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun requestExit(action: ExitAction, requiresSave: Boolean) {
        pendingExitAction = action
        pendingExitRequiresSave = requiresSave
    }

    /**
     * Clears the pending exit action.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun clearPendingExit() {
        pendingExitAction = null
        pendingExitRequiresSave = false
    }

    /**
     * Returns and clears the pending exit action.
     *
     * @return The resolved exit action value, or null when it is unavailable.
     */
    fun consumePendingExitAction(): ExitAction? {
        val action = pendingExitAction
        clearPendingExit()
        return action
    }

    /**
     * Shows the temporary save confirmation message.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showSaveConfirmation() {
        showSaveMessage = true
    }

    /**
     * Hides the temporary save confirmation message.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun hideSaveConfirmation() {
        showSaveMessage = false
    }

    /**
     * Starts the service NPC dialog.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun openServiceDialog() {
        serviceMessage = null
        dialogStep = 0
    }

    /**
     * Moves the service NPC dialog to its next step.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun advanceServiceDialog() {
        serviceMessage = null
        dialogStep = (dialogStep ?: 0) + 1
    }

    /**
     * Starts the healing flow after service dialog confirmation.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun startHealing() {
        dialogStep = null
        serviceMessage = null
        isHealingInProgress = true
    }

    /**
     * Finishes the healing flow and shows the service result message.
     *
     * @param message The message value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun finishHealing(message: String) {
        serviceMessage = message
        dialogStep = 2
        isHealingInProgress = false
    }

    /**
     * Opens the shop overlay from the service dialog.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun openShop() {
        dialogStep = null
        isShopOpen = true
        serviceMessage = null
    }

    /**
     * Closes the shop overlay and clears its service message.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun closeShop() {
        isShopOpen = false
        serviceMessage = null
    }

    /**
     * Updates the message shown by the service dialog or shop.
     *
     * @param message The message value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showServiceMessage(message: String?) {
        serviceMessage = message
    }

    /**
     * Closes the service NPC dialog and clears its message.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun closeServiceDialog() {
        dialogStep = null
        serviceMessage = null
    }
}

/**
 * Remembers interaction state for one town interior.
 *
 * @param interior The interior value used by this operation.
 * @return The resulting TownInteriorInteractionState value.
 */
@Composable
internal fun rememberTownInteriorInteractionState(interior: TownInterior): TownInteriorInteractionState {
    return remember(interior) { TownInteriorInteractionState() }
}
