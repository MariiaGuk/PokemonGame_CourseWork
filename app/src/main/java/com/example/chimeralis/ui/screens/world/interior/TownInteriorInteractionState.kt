package com.example.chimeralis.ui.screens.world.interior

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.screens.world.ExitAction
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

    /** Locks interior input and resets the joystick. */
    fun beginInputLock() {
        isInteriorInputLocked = true
        interiorJoystickResetKey++
    }

    /** Unlocks interior input after a temporary lock. */
    fun endInputLock() {
        isInteriorInputLocked = false
    }

    /** Opens the in-game menu when the service UI is not active. */
    fun openGameMenu(): Boolean {
        if (isServiceUiOpen) return false

        isSettingsOpen = false
        isInventoryOpen = false
        isGameMenuOpen = true
        return true
    }

    /** Opens the inventory panel when the service UI is not active. */
    fun openInventory(): Boolean {
        if (isServiceUiOpen) return false

        isSettingsOpen = false
        isGameMenuOpen = false
        selectedInventoryItem = null
        isInventoryOpen = true
        return true
    }

    /** Opens the storage overlay and clears conflicting panels. */
    fun openStorage() {
        isStorageOpen = true
        isGameMenuOpen = false
        isInventoryOpen = false
        selectedInventoryItem = null
    }

    /** Closes the storage overlay. */
    fun closeStorage() {
        isStorageOpen = false
    }

    /** Closes the inventory panel and clears the selected item. */
    fun closeInventory() {
        selectedInventoryItem = null
        isInventoryOpen = false
    }

    /** Selects an item inside the inventory panel. */
    fun selectInventoryItem(item: Item?) {
        selectedInventoryItem = item
    }

    /** Starts selecting a team target for an inventory item. */
    fun startItemTargetSelection(item: Item) {
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

    /** Returns from the in-game menu to the interior. */
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

    /** Starts the service NPC dialog. */
    fun openServiceDialog() {
        serviceMessage = null
        dialogStep = 0
    }

    /** Moves the service NPC dialog to its next step. */
    fun advanceServiceDialog() {
        serviceMessage = null
        dialogStep = (dialogStep ?: 0) + 1
    }

    /** Starts the healing flow after service dialog confirmation. */
    fun startHealing() {
        dialogStep = null
        serviceMessage = null
        isHealingInProgress = true
    }

    /** Finishes the healing flow and shows the service result message. */
    fun finishHealing(message: String) {
        serviceMessage = message
        dialogStep = 2
        isHealingInProgress = false
    }

    /** Opens the shop overlay from the service dialog. */
    fun openShop() {
        dialogStep = null
        isShopOpen = true
        serviceMessage = null
    }

    /** Closes the shop overlay and clears its service message. */
    fun closeShop() {
        isShopOpen = false
        serviceMessage = null
    }

    /** Updates the message shown by the service dialog or shop. */
    fun showServiceMessage(message: String?) {
        serviceMessage = message
    }

    /** Closes the service NPC dialog and clears its message. */
    fun closeServiceDialog() {
        dialogStep = null
        serviceMessage = null
    }
}

/** Remembers interaction state for one town interior. */
@Composable
internal fun rememberTownInteriorInteractionState(interior: TownInterior): TownInteriorInteractionState {
    return remember(interior) { TownInteriorInteractionState() }
}
