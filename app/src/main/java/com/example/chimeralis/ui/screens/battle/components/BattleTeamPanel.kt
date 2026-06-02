package com.example.chimeralis.ui.screens.battle.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleChimeraSlotPresentation
import com.example.chimeralis.ui.screens.battle.presentation.model.BattleTeamPresentation

/**
 * Renders the battle team buttons UI.
 *
 * @param team The team value used by this operation.
 * @param onSwitchSelected Callback invoked when switch selected occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun BattleTeamButtons(
    team: BattleTeamPresentation,
    onSwitchSelected: (BattleChimeraSlotPresentation) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            team.slots.chunked(3).forEach { rowTeam ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowTeam.forEach { slot ->
                        if (slot == null) {
                            EmptyBattleTeamSlot()
                        } else {
                            BattleTeamSlot(
                                slot = slot,
                                onSwitchSelected = onSwitchSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the battle item target buttons UI.
 *
 * @param team The team value used by this operation.
 * @param onItemTargetSelected Callback invoked when item target selected occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun BattleItemTargetButtons(
    team: BattleTeamPresentation,
    onItemTargetSelected: (BattleChimeraSlotPresentation) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.End
    ) {
        team.slots.chunked(3).forEach { rowTeam ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTeam.forEach { slot ->
                    if (slot == null) {
                        EmptyBattleTeamSlot()
                    } else {
                        BattleItemTargetSlot(
                            slot = slot,
                            onItemTargetSelected = onItemTargetSelected
                        )
                    }
                }
            }
        }
    }
}
