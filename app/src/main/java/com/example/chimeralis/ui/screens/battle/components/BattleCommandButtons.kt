package com.example.chimeralis.ui.screens.battle.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.battle.BattleMenuButtonWidth
import com.example.chimeralis.ui.screens.battle.presentation.BattleMoveLearningPresentation
import com.example.chimeralis.ui.screens.battle.presentation.BattleMoveOptionPresentation

/** Renders the battle action buttons UI. */
@Composable
internal fun BattleActionButtons(
    onFight: () -> Unit,
    onBag: () -> Unit,
    onTeam: () -> Unit,
    onRun: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Fight", onClick = onFight)
            MenuButton(text = "Bag", onClick = onBag)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuButton(text = "Team", onClick = onTeam)
            MenuButton(text = "Run", onClick = onRun)
        }
    }
}

/** Renders the move learning buttons UI. */
@Composable
internal fun MoveLearningButtons(
    request: BattleMoveLearningPresentation?,
    onReplacementSelected: (Int?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (request == null) {
            MenuButton(text = "Continue", onClick = { onReplacementSelected(null) })
            return
        }

        request.replacementMoves.chunked(2).forEach { rowMoves ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMoves.forEach { option ->
                    if (option == null) {
                        Spacer(modifier = Modifier.width(BattleMenuButtonWidth))
                    } else {
                        MenuButton(
                            text = option.label,
                            onClick = { onReplacementSelected(option.index) }
                        )
                    }
                }
            }
        }
    }
}

/** Renders the move buttons UI. */
@Composable
internal fun MoveButtons(
    moves: List<BattleMoveOptionPresentation>,
    onMoveSelected: (BattleMoveOptionPresentation) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        val moveSlots = List(4) { index -> moves.getOrNull(index) }
        moveSlots.chunked(2).forEach { rowMoves ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMoves.forEach { move ->
                    if (move == null) {
                        Spacer(modifier = Modifier.width(BattleMenuButtonWidth))
                    } else {
                        MenuButton(
                            text = move.label,
                            enabled = move.enabled,
                            onClick = { onMoveSelected(move) }
                        )
                    }
                }
            }
        }
    }
}
