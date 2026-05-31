package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item

/** Renders the battle team buttons UI. */
@Composable
internal fun BattleTeamButtons(
    team: List<Chimera>,
    activeChimera: Chimera,
    onSwitchSelected: (Chimera) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val slots = List(MaxBattleTeamSize) { index -> team.getOrNull(index) }
            slots.chunked(3).forEach { rowTeam ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowTeam.forEach { chimera ->
                        if (chimera == null) {
                            EmptyBattleTeamSlot()
                        } else {
                            BattleTeamSlot(
                                chimera = chimera,
                                isActive = chimera === activeChimera,
                                onSwitchSelected = onSwitchSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Renders the battle item target buttons UI. */
@Composable
internal fun BattleItemTargetButtons(
    item: Item?,
    team: List<Chimera>,
    onItemTargetSelected: (Chimera) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.End
    ) {
        val slots = List(MaxBattleTeamSize) { index -> team.getOrNull(index) }
        slots.chunked(3).forEach { rowTeam ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTeam.forEach { chimera ->
                    if (chimera == null) {
                        EmptyBattleTeamSlot()
                    } else {
                        BattleItemTargetSlot(
                            chimera = chimera,
                            canUseItem = item?.canUseOn(chimera) == true,
                            onItemTargetSelected = onItemTargetSelected
                        )
                    }
                }
            }
        }
    }
}
