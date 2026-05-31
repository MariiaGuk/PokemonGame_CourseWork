package com.example.chimeralis.ui.screens.battle

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
import com.example.chimeralis.logic.battle.MoveLearnRequest
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.items.Item

/** Renders the battle panel UI. */
@Composable
internal fun BattlePanel(
    message: String,
    mode: BattlePanelMode,
    isTeamSelectionForced: Boolean,
    moves: List<Move>,
    pendingMoveLearning: MoveLearnRequest?,
    team: List<Chimera>,
    activeChimera: Chimera,
    inventoryItems: Map<Item, Int>,
    canUseCaptureItems: Boolean = true,
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onMoveSelected: (Move) -> Unit,
    onMoveReplacementSelected: (Int?) -> Unit,
    onSwitchSelected: (Chimera) -> Unit,
    onItemSelected: (Item) -> Unit,
    selectedItem: Item?,
    onItemTargetSelected: (Chimera) -> Unit,
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
                    pendingMoveLearning?.let { request ->
                        "${request.chimera.name} wants to learn ${request.move.name}.\nForget which move? Back keeps old moves."
                    } ?: message
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
                        moves = moves,
                        onMoveSelected = onMoveSelected
                    )
                    BattlePanelMode.Bag -> BattleInventoryButtons(
                        inventoryItems = inventoryItems,
                        team = team,
                        canUseCaptureItems = canUseCaptureItems,
                        onItemSelected = onItemSelected
                    )
                    BattlePanelMode.ItemTarget -> BattleItemTargetButtons(
                        item = selectedItem,
                        team = team,
                        onItemTargetSelected = onItemTargetSelected
                    )
                    BattlePanelMode.Team -> BattleTeamButtons(
                        team = team,
                        activeChimera = activeChimera,
                        onSwitchSelected = onSwitchSelected
                    )
                    BattlePanelMode.MoveLearning -> MoveLearningButtons(
                        request = pendingMoveLearning,
                        onReplacementSelected = onMoveReplacementSelected
                    )
                    BattlePanelMode.Log -> Unit
                }
            }
        }
    }
}
