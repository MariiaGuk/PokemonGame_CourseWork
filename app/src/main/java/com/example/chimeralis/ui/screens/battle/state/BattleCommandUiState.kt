package com.example.chimeralis.ui.screens.battle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.screens.battle.presentation.model.BattlePanelMode

/** Stores command-panel state for the battle UI. */
internal class BattleCommandUiState {
    var panelMode by mutableStateOf(BattlePanelMode.Log)
    var selectedBattleItem by mutableStateOf<Item?>(null)
    var isBattleIntroLocked by mutableStateOf(true)
    var isBattleExitPending by mutableStateOf(false)

    val isBattleInputLocked: Boolean get() = isBattleIntroLocked || isBattleExitPending

    /**
     * Unlocks player input after the opening battle intro delay.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun unlockBattleIntro() {
        isBattleIntroLocked = false
    }

    /**
     * Changes the active command panel.
     *
     * @param mode The mode value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun openPanel(mode: BattlePanelMode) {
        panelMode = mode
    }

    /**
     * Selects a regular item and asks the player for a target chimera.
     *
     * @param item Domain object used by this operation: item.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun selectBattleItem(item: Item) {
        selectedBattleItem = item
        panelMode = BattlePanelMode.ItemTarget
    }

    /**
     * Returns from nested battle panels to the correct parent panel.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun backToActionSelection() {
        if (panelMode == BattlePanelMode.ItemTarget) {
            selectedBattleItem = null
            panelMode = BattlePanelMode.Bag
        } else {
            panelMode = BattlePanelMode.Actions
        }
    }

    /**
     * Resets command state while showing a battle log sequence.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun showLog() {
        selectedBattleItem = null
        panelMode = BattlePanelMode.Log
    }

    /**
     * Marks the battle as ready to leave after its closing delay.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun requestBattleExit() {
        isBattleExitPending = true
    }
}
