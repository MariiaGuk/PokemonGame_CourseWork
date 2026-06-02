package com.example.chimeralis.ui.screens.battle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chimeralis.ui.screens.battle.BattleBackButtonGap
import com.example.chimeralis.ui.screens.battle.BattleBackButtonSize
import com.example.chimeralis.ui.screens.battle.BattlePanelHorizontalPadding
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleChimeraSlotPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleItemOptionPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleMoveOptionPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattlePanelMode
import com.example.chimeralis.ui.screens.battle.presentation.model.BattlePanelPresentation

/** Renders the battle panel UI. */
@Composable
internal fun BattlePanel(
    message: String,
    mode: BattlePanelMode,
    isTeamSelectionForced: Boolean,
    presentation: BattlePanelPresentation,
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onMoveSelected: (BattleMoveOptionPresentation) -> Unit,
    onMoveReplacementSelected: (Int?) -> Unit,
    onSwitchSelected: (BattleChimeraSlotPresentation) -> Unit,
    onItemSelected: (BattleItemOptionPresentation) -> Unit,
    onItemTargetSelected: (BattleChimeraSlotPresentation) -> Unit,
    onRun: () -> Unit,
    onBackToActions: () -> Unit,
    colors: ColorScheme,
    modifier: Modifier = Modifier
) {
    val showBackArrow = mode == BattlePanelMode.Moves ||
            mode == BattlePanelMode.Bag ||
            mode == BattlePanelMode.ItemTarget ||
            mode == BattlePanelMode.MoveLearning ||
            (mode == BattlePanelMode.Team && !isTeamSelectionForced)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(colors.surface.copy(alpha = 0.9f))
    ) {
        if (showBackArrow) {
            BattleBackArrowButton(
                onClick = {
                    if (mode == BattlePanelMode.MoveLearning) {
                        onMoveReplacementSelected(null)
                    } else {
                        onBackToActions()
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = BattlePanelHorizontalPadding)
            )
        }

        if (mode == BattlePanelMode.Log || mode == BattlePanelMode.MoveLearning) {
            BattleMessage(
                text = if (mode == BattlePanelMode.MoveLearning) {
                    presentation.moveLearning?.message ?: message
                } else {
                    message
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (mode == BattlePanelMode.MoveLearning) {
                            BattlePanelHorizontalPadding + BattleBackButtonSize + BattleBackButtonGap
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        top = 16.dp,
                        end = if (mode == BattlePanelMode.MoveLearning) {
                            400.dp
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        bottom = 16.dp
                    )
            )
        }

        if (mode != BattlePanelMode.Log) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (showBackArrow) {
                            BattlePanelHorizontalPadding + BattleBackButtonSize + BattleBackButtonGap
                        } else {
                            BattlePanelHorizontalPadding
                        },
                        top = 7.dp,
                        end = BattlePanelHorizontalPadding,
                        bottom = 7.dp
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                when (mode) {
                    BattlePanelMode.Actions -> BattleActionButtons(
                        onFight = onFight,
                        onBag = onBag,
                        onTeam = onTeam,
                        onRun = onRun
                    )
                    BattlePanelMode.Moves -> MoveButtons(
                        moves = presentation.moves,
                        onMoveSelected = onMoveSelected
                    )
                    BattlePanelMode.Bag -> BattleInventoryButtons(
                        inventoryItems = presentation.inventoryItems,
                        onItemSelected = onItemSelected
                    )
                    BattlePanelMode.ItemTarget -> BattleItemTargetButtons(
                        team = presentation.itemTargetSelection,
                        onItemTargetSelected = onItemTargetSelected
                    )
                    BattlePanelMode.Team -> BattleTeamButtons(
                        team = presentation.teamSelection,
                        onSwitchSelected = onSwitchSelected
                    )
                    BattlePanelMode.MoveLearning -> MoveLearningButtons(
                        request = presentation.moveLearning,
                        onReplacementSelected = onMoveReplacementSelected
                    )
                    BattlePanelMode.Log -> Unit
                }
            }
        }
    }
}
